package edu.cs673.yikyak_clone.data

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

// Timeout for getting the current location (milliseconds)
const val CURRENT_LOCATION_REQUEST_TIMEOUT: Long = 5000

// Helper Function to get the current locations' latitude and longitude
fun getCurrentLocation(context: Context, onLocationFetched: (lat: Double, lon: Double) -> Unit) {
    val fusedLPC = LocationServices.getFusedLocationProviderClient(context)

    // Check if Location permissions are not granted in the android manifest and returns if so
    if (ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) return

    // Create a new location request for the current location
    val locRequest = CurrentLocationRequest.Builder()
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY) // High accuracy needed for accurate geotagging
        .setDurationMillis(CURRENT_LOCATION_REQUEST_TIMEOUT)
        .build()

    // Run getCurrentLocation through Fused Location Provider API
    fusedLPC.getCurrentLocation(locRequest, null)
        // If getCurrentLocation runs successfully
        .addOnSuccessListener { location ->
            // If the location is not null declare lat and lon return variables
            if (location != null) {
                onLocationFetched(location.latitude, location.longitude)
            }
        }
}