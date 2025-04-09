# Flight-Tracker

## Overview
This Android application tracks flights between Delhi and Mumbai, calculating and displaying the average flight time including delays. The app demonstrates the use of Room database for data persistence, WorkManager for periodic background tasks, and dynamic UI construction in Kotlin.

## Features
- Displays the average flight time between Delhi and Mumbai, including delays
- Shows a list of available flights with detailed information
- Uses Room database to store flight information
- Implements a background worker to periodically update flight data
- Handles graceful fallback to sample data if JSON loading fails

## Technical Implementation

### Architecture Components
- **Room Database**: Stores flight data and provides methods to query average times
- **WorkManager**: Schedules periodic updates of flight information
- **Coroutines**: Handles asynchronous operations for database operations
- **Gson**: Parses JSON data from assets

### Key Files

#### Data Classes
- `Flight.kt`: Data entity representing a flight with its attributes
- `FlightDao.kt`: Data Access Object with database operations
- `AppDatabase.kt`: Room database configuration

#### Application Logic
- `MainActivity.kt`: Main UI implementation and data loading logic
- `FlightWorker.kt`: Background worker to periodically update flight data

#### Resources
- `flights_data.json`: Sample flight data in JSON format

## Setup Instructions

1. Clone the repository
2. Open the project in Android Studio
3. Make sure to have the Room and WorkManager dependencies in your app's build.gradle:

```gradle
dependencies {
    implementation "androidx.room:room-runtime:2.5.0"
    implementation "androidx.room:room-ktx:2.5.0"
    kapt "androidx.room:room-compiler:2.5.0"
    
    implementation "androidx.work:work-runtime-ktx:2.8.0"
    
    implementation "com.google.code.gson:gson:2.10.1"
}
```

4. Build and run the application on an emulator or physical device

## Data Structure

Each flight in the database has the following attributes:
- `id`: Unique identifier (auto-generated)
- `flightNumber`: Flight code/number
- `origin`: Departure city
- `destination`: Arrival city
- `departureTime`: Unix timestamp for departure time
- `arrivalTime`: Unix timestamp for arrival time
- `delayMinutes`: Delay duration in minutes
- `date`: Flight date in YYYY-MM-DD format

## Sample Data

The application includes sample flight data between Delhi and Mumbai. If the JSON file cannot be loaded, the app will fall back to hardcoded sample data to ensure functionality.

## Database Migrations

The application uses Room's destructive migration strategy for simplicity. In a production environment, you might want to implement proper migration strategies to preserve user data.

## Future Enhancements
- Add the ability to search for specific flights
- Implement filters for date, airline, or delay status
- Add more cities and routes
- Create a more sophisticated UI with Material Design components
- Add unit and instrumentation tests
