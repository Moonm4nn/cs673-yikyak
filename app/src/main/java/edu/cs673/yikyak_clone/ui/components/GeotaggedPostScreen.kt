package edu.cs673.yikyak_clone.ui.components

import android.Manifest
import androidx.compose.material3.AlertDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import edu.cs673.yikyak_clone.data.getCurrentLocation

@Composable
fun GeotaggedPostScreen() {
    // Delegate latitude and longitude assignment to a variable, and when updated recomposes any composable
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    // Delegate boolean assignments
    var locationPermissions by remember { mutableStateOf<Boolean?>(null) }
    var showAlert by remember { mutableStateOf<Boolean?>(null) }

    // Assign Context
    val context = LocalContext.current


    // Create list of specific permissions to ask (location permissions)
    val permissionsToRequest = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // Launch external permission request and handle returned permission data
    val permissionLauncher = rememberLauncherForActivityResult(
        // permission contract allows multiple permissions to be requested
        contract = ActivityResultContracts.RequestMultiplePermissions()
        // on permission results
    ) { permissions ->
        // save location permission grant status
        locationPermissions = (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        // if location permissions are not granted, show alert
        showAlert = locationPermissions == false

        // if location permissions are granted, get current location
        if (locationPermissions==true) {
            getCurrentLocation(context) { lat, lon ->
                latitude = lat
                longitude = lon
            }
        }
    }

    // Confirm function, assign location permission to null and rerun permission request
    val onConfirm: () -> Unit = {
        locationPermissions = null
        permissionLauncher.launch(permissionsToRequest)
    }

    // Dismiss function, hide alert
    val onDismiss: () -> Unit = {
        showAlert = false
        // TODO Dismiss function
    }

    // Test Button to trigger permission request
    Column(modifier = Modifier.padding(16.dp)) {
        // Button to trigger permission request
        Button(onClick = {
            permissionLauncher.launch(permissionsToRequest)
        }) {
            Text("Geotag")
        }

        // Text to display coordinates
        latitude?.let {
            Text("Coordinates: $latitude, $longitude")
        }
    }

    // Popup message if location permissions are not granted and prompt user to accept
    if (locationPermissions == false && showAlert == true) {
        // Alert Popup
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "Error: Location Permissions not Granted") },
            text = { Text(text = "To use this feature please grant location permissions.") },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}