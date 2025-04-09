package com.example.a2flight2.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FlightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlights(flights: List<Flight>)

    @Query("SELECT * FROM flights WHERE origin = :origin AND destination = :destination")
    suspend fun getFlights(origin: String, destination: String): List<Flight>

    @Query("SELECT AVG((arrivalTime - departureTime) / 60 + delayMinutes) FROM flights WHERE origin = :origin AND destination = :destination")
    suspend fun getAverageTimeTaken(origin: String, destination: String): Double?

    @Query("SELECT COUNT(*) FROM flights")
    suspend fun getFlightCount(): Int

}