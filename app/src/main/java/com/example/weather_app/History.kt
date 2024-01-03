package com.example.weather_app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class History : AppCompatActivity() {

    private lateinit var sqliteHelper: SQLiteHelper
    private lateinit var SN: TextView
    private lateinit var city: TextView
    private lateinit var country: TextView
    private lateinit var temp: TextView
    private lateinit var tableLayout: TableLayout
    private lateinit var deleteBtn: Button
    private lateinit var backBtn: Button

    private var i: Int = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        SN = findViewById(R.id.SN)
        city = findViewById(R.id.City)
        country = findViewById(R.id.Country)
        temp = findViewById(R.id.Temp)
        tableLayout = findViewById(R.id.tableLayout)
        deleteBtn = findViewById(R.id.deleteBtn)
        backBtn  = findViewById(R.id.backBtn)


        sqliteHelper = SQLiteHelper(this)

        val arr = getWeatherData()

        deleteBtn.setOnClickListener{
            showDeleteConfirmationDialog()

        }

        backBtn.setOnClickListener{

            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }





        Log.d("val",arr[0].city.toString())

        if(arr.size==0){
            Toast.makeText(applicationContext, "Nothing is present",Toast.LENGTH_SHORT).show()
        }else{

            for ((index, userData) in arr.withIndex()) {
                val city = userData.city
                val country = userData.country
                val temp = userData.currTemp




                val tableRow = TableRow(this)

                i++
                val serialNumber = createTextView(i.toString())
                val nameTextView = createTextView(city)
                val emailTextView = createTextView(country)
                val genderTextView = createTextView(temp)
                val deleteButton = createDeleteButton(userData.city,tableRow)



                tableRow.addView(serialNumber)
                tableRow.addView(nameTextView)
                tableRow.addView(emailTextView)
                tableRow.addView(genderTextView)
                tableRow.addView(deleteButton)



                tableLayout.addView(tableRow)
            }
        }


    }

    private fun createDeleteButton(city: String,tableRow: TableRow): Button {
        val deleteButton = Button(this)
        deleteButton.text = "Delete"
        deleteButton.textAlignment = View.TEXT_ALIGNMENT_CENTER
        deleteButton.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirm Deletion")
            builder.setMessage("Are you sure you want to clear the $city history?")
            builder.setPositiveButton("Yes") { _, _ ->
                // Call the deleteWeather function to delete the database
                val success = sqliteHelper.deleteWeatherByCity(city)

                if(success > -1){
                    Toast.makeText(applicationContext, "$city City history Deleted Successfully",Toast.LENGTH_SHORT).show()
                    tableLayout.removeView(tableRow)
                }
            }
            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()



        }
        return deleteButton
    }

    private fun getWeatherData():ArrayList<WeatherModel>{

        val status = sqliteHelper.getAllWeather()

        return status



    }

    private fun createTextView(text: String?): TextView {
        val textView = TextView(this)
        textView.text = text
        textView.layoutParams = TableRow.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        textView.setPadding(8, 8, 8, 8)
        textView.gravity = android.view.Gravity.CENTER
        return textView
    }
    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Deletion")
        builder.setMessage("Are you sure you want to clear the history?")
        builder.setPositiveButton("Yes") { _, _ ->
            // Call the deleteWeather function to delete the database
            val rowsDeleted = sqliteHelper.deleteWeather()

            if (rowsDeleted > 0) {
                // Database was deleted successfully
                Toast.makeText(applicationContext, "History Deleted Successfully",Toast.LENGTH_SHORT).show()
                tableLayout.removeAllViews()
            } else {
                // An error occurred or the database was empty
            }
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    override fun onStart() {
        super.onStart()
        
        val arr = getWeatherData()

        if(arr.size==0){
            Toast.makeText(applicationContext, "Nothing is present",Toast.LENGTH_SHORT).show()
        }
    }


}