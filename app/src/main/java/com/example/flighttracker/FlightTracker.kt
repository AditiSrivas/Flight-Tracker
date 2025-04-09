package com.example.flighttracker

import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.*

class FlightTracker : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlightTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FlightTrackerApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightTrackerApp(viewModel: FlightViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    val sampleFlights = remember {
        listOf(
            "AA1004", "DL2153", "UA862", "WN1356", "B6607",
            "AS402", "LH491", "BA212", "EK203", "QF12", "VA1456",
            "EY6378", "UA7389", "SQ6573", "QR7160", "KE319", "OM308",
            "NH849", "JL35", "HO1386", "EK313", "SQ7443", "UA176",
            "VJ82", "VN778", "5J50"
        )
    }
    var expanded by remember { mutableStateOf(false) }
    var showWebView by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Flight Tracker",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.flightNumber,
                    onValueChange = { viewModel.updateFlightNumber(it) },
                    label = { Text("Select Flight Number") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sampleFlights.forEach { flightNumber ->
                        DropdownMenuItem(
                            text = { Text(flightNumber) },
                            onClick = {
                                viewModel.updateFlightNumber(flightNumber)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    viewModel.trackFlight()
                    focusManager.clearFocus()
                }
            ) {
                Text("Track Flight")
            }
            Button(
                onClick = {
                    showWebView = !showWebView
                }
            ) {
                Text(if (showWebView) "Show Details" else "Show Map")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                ErrorMessage(uiState.error!!)
            }
            uiState.flightData != null -> {
                if (showWebView) {
                    GoogleFlightTracker(uiState.flightNumber)
                } else {
                    LazyColumn {
                        item {
                            FlightDetails(flightData = uiState.flightData!!, isTracking = uiState.isTracking)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleFlightTracker(flightNumber: String) {
    val googleFlightUrl = "https://www.google.com/search?q=flight+$flightNumber"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                            return false
                        }
                    }
                    loadUrl(googleFlightUrl)
                }
            },
            update = { webView ->
                webView.loadUrl(googleFlightUrl)
            }
        )
    }
}

@Composable
fun ErrorMessage(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun FlightDetails(flightData: FlightData, isTracking: Boolean) {
    val flight = flightData.data.firstOrNull()

    if (flight == null) {
        Text("Flight not found. Please check the flight number.")
        return
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Flight Status: ${flight.flight_status.capitalize()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = when (flight.flight_status) {
                        "active" -> MaterialTheme.colorScheme.primary
                        "landed" -> MaterialTheme.colorScheme.tertiary
                        "cancelled" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = flight.departure.iata,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = flight.departure.airport ?: "N/A",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = flight.flight.iata,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = flight.airline.name,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = flight.arrival.iata,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = flight.arrival.airport ?: "N/A",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Flight times
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Departure", style = MaterialTheme.typography.labelMedium)
                        Text(
                            formatDateTime(flight.departure.scheduled),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (flight.departure.delay != null && flight.departure.delay > 0) {
                            Text(
                                "Delayed by ${flight.departure.delay} min",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Arrival", style = MaterialTheme.typography.labelMedium)
                        Text(
                            formatDateTime(flight.arrival.scheduled),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (flight.arrival.delay != null && flight.arrival.delay > 0) {
                            Text(
                                "Delayed by ${flight.arrival.delay} min",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        // Live tracking data
        if (flight.live != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Live Tracking Data",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LiveDataItem("Latitude", "${flight.live.latitude}°")
                        LiveDataItem("Longitude", "${flight.live.longitude}°")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LiveDataItem("Altitude", "${flight.live.altitude} m")
                        LiveDataItem("Speed", "${flight.live.speed_horizontal} km/h")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LiveDataItem("Direction", "${flight.live.direction}°")
                        LiveDataItem("Status", if (flight.live.is_ground) "On Ground" else "In Air")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Last Updated: ${formatDateTime(flight.live.updated)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else if (isTracking) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Live tracking data not available",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (flight.flight_status == "scheduled") {
                        Text(
                            text = "Flight is scheduled but not active yet",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else if (flight.flight_status == "landed") {
                        Text(
                            text = "Flight has already landed",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Aircraft information
        if (flight.aircraft != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Aircraft Information",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Registration", style = MaterialTheme.typography.labelMedium)
                            Text(
                                flight.aircraft.registration ?: "N/A",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Column {
                            Text("Model", style = MaterialTheme.typography.labelMedium)
                            Text(
                                flight.aircraft.iata ?: "N/A",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveDataItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

fun formatDateTime(dateTimeString: String?): String {
    if (dateTimeString == null) return "N/A"

    try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateTimeString)
        return outputFormat.format(date!!)
    } catch (e: Exception) {
        return dateTimeString
    }
}

fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

// API and Data Models
interface AviationStackApi {
    @GET("flights")
    suspend fun getFlights(
        @Query("access_key") accessKey: String,
        @Query("flight_iata") flightIata: String? = null
    ): Response<FlightData>
}

data class FlightData(
    val pagination: Pagination,
    val data: List<Flight>
)

data class Pagination(
    val limit: Int,
    val offset: Int,
    val count: Int,
    val total: Int
)

data class Flight(
    val flight_date: String,
    val flight_status: String,
    val departure: FlightLocation,
    val arrival: FlightLocation,
    val airline: Airline,
    val flight: FlightIdentifier,
    val aircraft: Aircraft?,
    val live: LiveData?
)

data class FlightLocation(
    val airport: String?,
    val timezone: String?,
    val iata: String,
    val icao: String?,
    val terminal: String?,
    val gate: String?,
    val delay: Int?,
    val scheduled: String?,
    val estimated: String?,
    val actual: String?,
    val estimated_runway: String?,
    val actual_runway: String?
)

data class Airline(
    val name: String,
    val iata: String,
    val icao: String
)

data class FlightIdentifier(
    val number: String,
    val iata: String,
    val icao: String
)

data class Aircraft(
    val registration: String?,
    val iata: String?,
    val icao: String?,
    val icao24: String?
)

data class LiveData(
    val updated: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val direction: Double,
    val speed_horizontal: Double,
    val speed_vertical: Double,
    val is_ground: Boolean
)

// ViewModel
class FlightViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FlightUiState())
    val uiState: StateFlow<FlightUiState> = _uiState.asStateFlow()

    private val apiService: AviationStackApi
    private val apiKey = "8d8827ad6d30004c57a31bd5a3b5fbe3"

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.aviationstack.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(AviationStackApi::class.java)
    }

    fun updateFlightNumber(flightNumber: String) {
        _uiState.value = _uiState.value.copy(flightNumber = flightNumber)
    }

    fun trackFlight() {
        val flightNumber = _uiState.value.flightNumber.trim()

        if (flightNumber.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = "Please enter a flight number",
                isLoading = false
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val response = apiService.getFlights(apiKey, flightNumber)
                if (response.isSuccessful) {
                    val flightData = response.body()
                    if (flightData != null && flightData.data.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            flightData = flightData,
                            isLoading = false,
                            error = null,
                            isTracking = true
                        )

                        // If flight is active, start polling for updates
                        val flight = flightData.data.firstOrNull()
                        if (flight?.flight_status == "active") {
                            startLiveTracking()
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = "Flight not found. Please check the flight number.",
                            isLoading = false
                        )
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Invalid API key or access restricted"
                        404 -> "Flight not found"
                        429 -> "API rate limit reached"
                        else -> "Error: ${response.message()}"
                    }
                    _uiState.value = _uiState.value.copy(
                        error = errorMessage,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Network error: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun startLiveTracking() {
        viewModelScope.launch {
            while (_uiState.value.isTracking) {
                delay(60000) // Update every minute
                refreshFlightData()
            }
        }
    }

    private suspend fun refreshFlightData() {
        try {
            val response = apiService.getFlights(apiKey, _uiState.value.flightNumber)
            if (response.isSuccessful) {
                val flightData = response.body()
                if (flightData != null) {
                    _uiState.value = _uiState.value.copy(
                        flightData = flightData
                    )

                    // Stop tracking if flight is no longer active
                    val flight = flightData.data.firstOrNull()
                    if (flight?.flight_status != "active") {
                        _uiState.value = _uiState.value.copy(isTracking = false)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FlightViewModel", "Error refreshing flight data", e)
        }
    }
}

data class FlightUiState(
    val flightNumber: String = "",
    val flightData: FlightData? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTracking: Boolean = false
)

// Theme

@Composable
fun FlightTrackerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
        content = content
    )
}