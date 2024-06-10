package com.example.mnemonic.util

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import com.google.android.gms.location.LocationServices

object GpsUtil {
    private const val TAG = "GpsUtil"
    fun getCurrentUserGpsData(context: Context, callback: (Location?) -> Unit) {
        val permissionsUtil = PermissionsUtil(context)
        if (!permissionsUtil.checkLocationPermission()) {
            permissionsUtil.requestLocationPermissions()
        }
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener (callback)
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get location: ${e.message}")
                callback(null)
            }
    }
}