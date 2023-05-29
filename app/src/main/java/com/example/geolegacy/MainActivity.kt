package com.example.geolegacy

import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.geolegacy.databinding.ActivityMainBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

class MainActivity : AppCompatActivity() {
    private val REQUEST_CHECK_SETTINGS = 123
    lateinit var binding: ActivityMainBinding
    private lateinit var locationCallback: LocationCallback
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var mCurrentLocation: Location? = null
    private lateinit var mLocationRequestBalancedPowerAccuracy: LocationRequest
    private lateinit var mLocationRequestHighAccuracy: LocationRequest

    // Called when the activity is resumed
    override fun onResume() {
        super.onResume()
        if (binding.swLocationsupdates.isChecked) startLocationUpdates()
    }

    // Starts location updates
    private fun startLocationUpdates() {
        // Check if location permissions are granted
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions if not granted
            // Only requests fine permissions, may need to add coarse locations 1st cause of
            // potential bugs with Android 11
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101
            )
            return
        }

        // Request location updates with high accuracy
        fusedLocationClient.requestLocationUpdates(
            mLocationRequestHighAccuracy,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    // Creating an instance of fusedLocationClient before onCreate
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initializing binding variable
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.swLocationsupdates.isChecked = false

        // Creating location request for high accuracy
        mLocationRequestHighAccuracy = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // Creating location request for balanced power accuracy
        mLocationRequestBalancedPowerAccuracy = LocationRequest.create().apply {
            interval = 35000
            fastestInterval = 30000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        // Building location settings request with high accuracy and balanced power accuracy requests
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequestHighAccuracy)
            .addLocationRequest(mLocationRequestBalancedPowerAccuracy)

        // Getting the settings client
        val client: SettingsClient = LocationServices.getSettingsClient(this)

        // Checking location settings
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
            .addOnSuccessListener { LocationSettingsResponse ->
                // Location settings are satisfied
            }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied but fixable
                // Show a dialogue box to the user to enable location settings
                try {
                    exception.startResolutionForResult(this@MainActivity, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore this error
                }
            }
        }

        // Getting the FusedLocationProviderClient instance
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Getting the current location
        getCurrentlocation()

        // Setting up the location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // Handling the location result
                for (location in locationResult.locations) {
                    latitude = location.latitude
                    longitude = location.longitude
                    binding.tvLat.text = latitude.toString()
                    binding.tvLon.text = longitude.toString()
                }
            }
        }

        // Check if location permissions are granted
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            // Request location updates if permissions are granted
            fusedLocationClient.requestLocationUpdates(
                mLocationRequestHighAccuracy,
                Looper.getMainLooper()
            )
        }

        // Set an onCheckedChangeListener for the switch button
        binding.swLocationsupdates.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Start location updates when the switch button is checked
                startLocationUpdates()
            } else {
                // Stop location updates when the switch button is unchecked
                stopLocationUpdates()
                binding.tvLon.text = 0.toString()
                binding.tvLat.text = 0.toString()
            }
        }

        onResume()
    }
    // TODO ^^ THIS IS ON CREATE END

    // Stops location updates
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // Retrieves the current location
    private fun getCurrentlocation() {
        // Checks if location permissions are granted
        fun checkPermissions() {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this, android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                // Only requests fine permissions, may need to add coarse locations 1st cause of
                // potential bugs with Android 11
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101
                )
                return
            }
        }

        checkPermissions()

        // Retrieves the last known location
        fun lastLocation() {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Handles the case when location is not null
                    location?.let {
                        // Storing location in mCurrentLocation
                        mCurrentLocation = it
                        latitude = location.latitude
                        longitude = location.longitude
                        binding.tvLat.text = latitude.toString()
                        binding.tvLon.text = longitude.toString()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
                }
        }

        lastLocation()
    }
}

// Function required for looper to work
private fun FusedLocationProviderClient.requestLocationUpdates(
    mLocationRequestHighAccuracy: LocationRequest,
    mainLooper: Looper?
) {
    // Function to request location updates (implementation is missing)
}

// Insert a new location
val locationId = MapDatabaseManager.insertLocation(latitude, longitude, name)

// Retrieve all locations
val locations = MapDatabaseManager.getAllLocations()
