package com.example.weather_app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.weather_app.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


//b236744261e2f3d23702cd1ce812638a
class MainActivity : AppCompatActivity() {

    private lateinit var editCity: SearchView
    private lateinit var locationBtn: Button
    private lateinit var alertDialog: AlertDialog.Builder
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2
    private lateinit var history: Button

    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var sqliteHelper: SQLiteHelper
    private lateinit var context: Context


    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        alertDialog = AlertDialog.Builder(this)

        sqliteHelper = SQLiteHelper(this)

        history = findViewById(R.id.historyData)

        context = this



        editCity = findViewById(R.id.editTextSearch)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)




        locationBtn = findViewById(R.id.locationSearch)


        history.setOnClickListener{

            val arr = getWeatherData()

            if(arr.size == 0){
                Toast.makeText(applicationContext, "No History Available",Toast.LENGTH_SHORT).show()
            }else{
                val intent = Intent(this,History::class.java)

                startActivity(intent)
            }


        }



        locationBtn.setOnClickListener {

            getLocation()


        }

        if(checkForInternet(this)){
            getLocation()
        }


        searchCity()





    }

    private fun searchCity() {

        val searchView = binding.editTextSearch

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {

                if (query != null && checkForInternet(context) ) {
                    fetchWeatherData(query)
                    hideKeyboard();
                }
                else if(query != null && !checkForInternet(context)){

                    val arr = getWeatherData()

                    val index = checkFromDatabase(query,arr)

                    if(index>=0){




                        val tem = arr[index].currTemp

                        binding.cityDisplay.text = arr[index].city
                        binding.countryDisplay.text = arr[index].country
                        binding.currTempDisplay.text = "Current temperature is \n $tem°C"

                        loadingProgressBar.visibility = View.GONE

                    }else{
                        Toast.makeText(context, "City not present", Toast.LENGTH_SHORT).show()
                    }


                }
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {

               return true
            }


        })
    }

    private fun fetchWeatherData(cityName: String) {


        loadingProgressBar.visibility = View.VISIBLE

        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(cityName,"b236744261e2f3d23702cd1ce812638a","metric")

        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                loadingProgressBar.visibility = View.GONE
                if(response.isSuccessful && responseBody != null){


                    val date = convertUnixTimestampToDate(responseBody.dt)
                    val city = responseBody.name
                    val country = responseBody.sys.country
                    val currTemp = responseBody.main.temp
                    val desc = responseBody.weather[0].description
                    val feelsLike = responseBody.main.feels_like
                    val maxTemp = responseBody.main.temp_max
                    val minTemp = responseBody.main.temp_min
                    val sunrise = convertUnixTimestampToTime(responseBody.sys.sunrise)
                    val sunset = convertUnixTimestampToTime(responseBody.sys.sunset)
                    val windSpeed = responseBody.wind.speed
                    val humidity = responseBody.main.humidity

                    val temperature = responseBody.main.temp.toString()
                    Log.d("temp","temp : $temperature")

                    binding.dateDisplay.text = "$date"
                    binding.cityDisplay.text = "$city"
                    binding.countryDisplay.text = "$country"
                    binding.currTempDisplay.text = "Current temperature is \n $currTemp°C, $desc"
                    binding.maxTempDisplay.text = "Max temperature is \n $maxTemp°C"
                    binding.feelsLikeTempDisplay.text = "Feels like \n$feelsLike°C"
                    binding.minTempDisplay.text = "Min temperature is \n$minTemp°C"
                    binding.sunriseDisplay.text = "Sun woke up at \n $sunrise"
                    binding.SunsetDisplay.text = "Sun sets at \n $sunset"
                    binding.windSpeedDisplay.text = "Wind is blowing at speed of \n $windSpeed m/s"
                    binding.humidityDisplay.text = "Humidity is \n $humidity%"

                    val std = WeatherModel(city=city.toString(), country = country.toString(), currTemp = currTemp.toString())


                    val arr = getWeatherData()

                   if(!checkInDatabase(arr,city)){
                        val status1 = sqliteHelper.insertWeather(std)

                        if(status1 > -1){

                            Toast.makeText(applicationContext, "City Added in History",Toast.LENGTH_SHORT).show()
                        }

                        }
                    else{
                        val status2 = sqliteHelper.updateWeather(std)

                        if(status2 > -1){

                            Toast.makeText(applicationContext, "City Updated in History",Toast.LENGTH_SHORT).show()
                        }
                    }




                }else if(responseBody == null){
                    alertDialog.setTitle("City Not Found")
                    alertDialog.setMessage("The city you entered does not exist.")
                    alertDialog.setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()

                    }

                    alertDialog.show()


                }
                binding.editTextSearch.setQuery("",false)




            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {


                loadingProgressBar.visibility = View.GONE
                Log.d("error","error: $t")

            }


        })

    }

    private fun convertUnixTimestampToDate(unixTimestamp: Long): String {
        // Convert Unix timestamp to milliseconds
        val timestampMillis = unixTimestamp * 1000

        // Create a SimpleDateFormat with the desired date format
        val dateFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())

        // Format the date and return it as a string
        return dateFormat.format(Date(timestampMillis))
    }

    private fun convertUnixTimestampToTime(unixTimestamp: Long): String {
        // Convert Unix timestamp to milliseconds
        val timestampMillis = unixTimestamp * 1000

        // Create a SimpleDateFormat with the desired time format
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        // Format the time and return it as a string
        return timeFormat.format(Date(timestampMillis))
    }

    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                try {
                    mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                        val location: Location? = task.result
                        if (location != null) {
                            val geocoder = Geocoder(this, Locale.getDefault())
                            try {
                                val list: List<Address>? =
                                    geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                if (list != null) {
                                    if ( list.isNotEmpty()) {
                                        val city = list[0].locality



                                        Log.d("list",list[0].locality.toString())

                                        fetchWeatherData("$city")
                                    }else{
                                        Log.d("e","list is empty")
                                    }
                                }
                            } catch (e: IOException) {
                                // Handle Geocoding errors here
                                Log.d("e",e.toString())
                            }
                        }
                    }
                } catch (e: SecurityException) {
                    // Handle SecurityException (e.g., user denied permission) here
                }
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }



    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    private fun checkForInternet(context: Context): Boolean {

        // register activity with the connectivity manager service
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // if the android version is equal to M
        // or greater we need to use the
        // NetworkCapabilities to check what type of
        // network has the internet connection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Returns a Network object corresponding to
            // the currently active default data network.
            val network = connectivityManager.activeNetwork ?: return false

            // Representation of the capabilities of an active network.
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                // Indicates this network uses a Wi-Fi transport,
                // or WiFi has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                // Indicates this network uses a Cellular transport. or
                // Cellular has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                // else return false
                else -> false
            }
        } else {
            // if the android version is below M
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    private fun getWeatherData():ArrayList<WeatherModel>{

        val status = sqliteHelper.getAllWeather()

        return status



    }

    private fun checkFromDatabase(query: String, arr: ArrayList<WeatherModel>): Int {

        for ((index, userData) in arr.withIndex()){

            if(userData.city == query){
                return index
            }
        }

        return -1

    }

    private fun checkInDatabase(arr: ArrayList<WeatherModel>, city: String): Boolean{

        for ((index, userData) in arr.withIndex()){

            if(userData.city == city){
                return true
            }
        }
        return false

    }
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.hideSoftInputFromWindow(editCity.getWindowToken(), 0)
    }

}