package com.example.a2flight2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flights")
data class Flight(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val flightNumber: String,
    val origin: String,
    val destination: String,
    val departureTime: Double,  // Unix timestamp in seconds
    val arrivalTime: Double,    // Unix timestamp in seconds
    val delayMinutes: Double,
    val date: String          // Format: YYYY-MM-DD
)