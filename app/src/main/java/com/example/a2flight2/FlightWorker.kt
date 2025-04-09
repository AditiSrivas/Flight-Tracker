package com.example.a2flight2

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.a2flight2.data.AppDatabase
import com.example.a2flight2.data.Flight

class FlightWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(applicationContext)
            val flightDao = db.flightDao()

            val flights = parseFlightsFromJson(applicationContext)
            flightDao.insertFlights(flights)

            val count = flightDao.getFlightCount()
            Log.d("FlightWorker", "Inserted ${flights.size} flights. DB now has $count flights.")

            Result.success()
        } catch (e: Exception) {
            Log.e("FlightWorker", "Error in worker", e)
            Result.failure()
        }
    }

    private fun parseFlightsFromJson(context: Context): List<Flight> {
        val jsonString = context.assets.open("flights_data.json").bufferedReader().use { it.readText() }
        val gson = Gson()
        val listType = object : TypeToken<List<Flight>>() {}.type
        return gson.fromJson(jsonString, listType)
    }
}