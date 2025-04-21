#Flight Tracker
A modern Android application built with Jetpack Compose and Material 3, designed to track and visualize flight information with dynamic theming support for light and dark modes.

##Features
* Light & dark theme with custom Material 3 color schemes.
* Clean architecture following Compose best practices.
* Placeholder for real-time flight data integration.
* Modular structure for easy scaling and feature additions.

##Tech Stack
* Layer -	Tech
* UI	- Jetpack Compose, Material 3
* Theme	- Custom ColorScheme & Typography
* Architecture	- Jetpack Compose Navigation (planned)
* Language	- Kotlin

##Project Structure
com.example.flighttracker
│
├── MainActivity.kt               # Entry point of the app
├── FlightTrackerApp.kt          # Composable root for the app
│
├── ui.theme                     # Theming system
│   ├── Color.kt                 # Light and dark color schemes
│   ├── Theme.kt                 # Theme setup using MaterialTheme
│   └── Type.kt                  # Typography (Material3)

##Theme Customization
This app uses Material3 theming via FlightTrackerTheme, which dynamically switches between light and dark themes based on system settings.

##Getting Started
Prerequisites
* Android Studio Giraffe or higher
* Kotlin 1.9+
* Gradle 8+

##Run Locally
1. Clone the repo:
  '''git clone https://github.com/yourusername/flight-tracker.git
  cd flight-tracker'''
2. Open with Android Studio and sync Gradle.
3. Run the app on an emulator or connected device.

##Planned Features
* Real-time flight data display
* Location-based tracking
* Flight analytics and visualizations
* Notifications for flight changes

