package com.example.a2flight2

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import androidx.work.WorkManager
import com.example.a2flight2.data.AppDatabase
import com.example.a2flight2.data.Flight
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private lateinit var db: AppDatabase
    private lateinit var avgTimeTextView: TextView
    private lateinit var flightsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create main container with padding
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(32, 48, 32, 32)
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
        }

        // Add title
        val titleTextView = TextView(this).apply {
            text = "Flight Information"
            textSize = 24f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }
        mainLayout.addView(titleTextView)

        // Add average time text view
        avgTimeTextView = TextView(this).apply {
            textSize = 18f
            setPadding(0, 0, 0, 32)
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }
        mainLayout.addView(avgTimeTextView)

        // Add subtitle for flights list
        val subtitleTextView = TextView(this).apply {
            text = "Available Flights"
            textSize = 20f
            setPadding(0, 16, 0, 16)
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }
        mainLayout.addView(subtitleTextView)

        // Create scrollable container for flights
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Container for flight items
        flightsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        scrollView.addView(flightsContainer)
        mainLayout.addView(scrollView)

        setContentView(mainLayout)

        // Initialize database and load data
        db = AppDatabase.getInstance(applicationContext)
        scheduleFlightWorker()
        loadFlightData()
    }

    private fun loadFlightData() {
        lifecycleScope.launch {
            try {
                // Try to load from JSON file first
                var flights: List<Flight> = emptyList()
                try {
                    flights = parseFlightsFromJson()
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to load from JSON: ${e.message}")
                    // If JSON loading fails, use sample data
                    flights = loadSampleData()
                }

                // Insert the flights and continue
                db.flightDao().insertFlights(flights)

                // Display average time
                val avgTime = db.flightDao().getAverageTimeTaken("Delhi", "Mumbai")
                val avgText = if (avgTime != null) {
                    "Average Time (incl. delay): ${String.format("%.2f", avgTime)} minutes"
                } else {
                    "No data available yet."
                }
                avgTimeTextView.text = avgText

                // Display list of flights
                displayFlights(flights)

            } catch (e: Exception) {
                avgTimeTextView.text = "Error: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    private fun displayFlights(flights: List<Flight>) {
        // Clear existing views
        flightsContainer.removeAllViews()

        // Format for displaying time
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        // Add each flight to the container
        for (flight in flights) {
            val flightCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
                setPadding(24, 16, 24, 16)
                background = ContextCompat.getDrawable(context, android.R.drawable.dialog_holo_light_frame)
            }

            // Flight number and route
            val flightHeaderText = TextView(this).apply {
                text = "${flight.flightNumber}: ${flight.origin} to ${flight.destination}"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                setPadding(0, 0, 0, 8)
            }
            flightCard.addView(flightHeaderText)

            // Convert Unix timestamps to readable times
            val departureDate = Date(flight.departureTime.toLong() * 1000)
            val arrivalDate = Date(flight.arrivalTime.toLong() * 1000)

            // Time information
            val timeText = TextView(this).apply {
                text = "Departure: ${dateFormat.format(departureDate)} • Arrival: ${dateFormat.format(arrivalDate)}"
                textSize = 14f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            }
            flightCard.addView(timeText)

            // Delay information
            val delayText = TextView(this).apply {
                text = "Delay: ${flight.delayMinutes.toInt()} minutes"
                textSize = 14f
                setTextColor(if (flight.delayMinutes > 0)
                    ContextCompat.getColor(context, android.R.color.holo_red_dark)
                else
                    ContextCompat.getColor(context, android.R.color.holo_green_dark))
            }
            flightCard.addView(delayText)

            // Date
            val dateText = TextView(this).apply {
                text = "Date: ${flight.date}"
                textSize = 14f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            }
            flightCard.addView(dateText)

            // Add this flight card to the main container
            flightsContainer.addView(flightCard)
        }
    }

    private fun loadSampleData(): List<Flight> {
        return listOf(
            Flight(
                flightNumber = "AI101",
                origin = "Delhi",
                destination = "Mumbai",
                departureTime = 1712544000.0,
                arrivalTime = 1712548500.0,
                delayMinutes = 15.0,
                date = "2025-04-08"
            ),
            Flight(
                flightNumber = "6E202",
                origin = "Delhi",
                destination = "Mumbai",
                departureTime = 1712551200.0,
                arrivalTime = 1712556000.0,
                delayMinutes = 5.0,
                date = "2025-04-08"
            ),
            Flight(
                flightNumber = "UK303",
                origin = "Delhi",
                destination = "Mumbai",
                departureTime = 1712562000.0,
                arrivalTime = 1712566800.0,
                delayMinutes = 20.0,
                date = "2025-04-08"
            ),
            Flight(
                flightNumber = "SG404",
                origin = "Delhi",
                destination = "Mumbai",
                departureTime = 1712572800.0,
                arrivalTime = 1712577300.0,
                delayMinutes = 10.0,
                date = "2025-04-08"
            ),
            Flight(
                flightNumber = "AI505",
                origin = "Delhi",
                destination = "Mumbai",
                departureTime = 1712580000.0,
                arrivalTime = 1712584500.0,
                delayMinutes = 0.0,
                date = "2025-04-08"
            )
        )
    }

    private fun parseFlightsFromJson(): List<Flight> {
        try {
            val jsonString = assets.open("flights_data.json").bufferedReader().use { it.readText() }
            val gson = Gson()
            val listType = object : TypeToken<List<Flight>>() {}.type
            return gson.fromJson(jsonString, listType)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error loading JSON: ${e.message}", e)
            throw e
        }
    }

    private fun scheduleFlightWorker() {
        val request = PeriodicWorkRequestBuilder<FlightWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "FlightDataWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}