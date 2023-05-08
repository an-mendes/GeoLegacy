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
    override fun onResume() {
        super.onResume()
        if (binding.swLocationsupdates.isChecked) startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            // only requests fine permissions, may need to add coarse locations 1st cause of
            //potential bugs with android 11
            ActivityCompat.requestPermissions(
                this, arrayOf
                    (android.Manifest.permission.ACCESS_FINE_LOCATION), 101
            )
            return
        }
        fusedLocationClient.requestLocationUpdates(
            mLocationRequestHighAccuracy,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    // creating an instance of fusedLocationClient before onCreate
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //initializing binding variable
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.swLocationsupdates.isChecked = false
        mLocationRequestHighAccuracy = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        mLocationRequestBalancedPowerAccuracy = LocationRequest.create().apply {
            interval = 35000
            fastestInterval = 30000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequestHighAccuracy)
            .addLocationRequest(mLocationRequestBalancedPowerAccuracy)
        // ??
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        // ??
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
            .addOnSuccessListener { LocationSettingsResponse ->
                //good i guess?
            }
        // this entire thing is hella sus
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                //TODO implement this
                // location settings are not satisfied, but is fixable
                //TODO by giving the user a dialogue box
                try {
                    exception.startResolutionForResult(this@MainActivity, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    //ignore this error
                }
            }
        }


        //TODO request location updates

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getCurrentlocation()
        //TODO fix this
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    latitude = location.latitude
                    longitude = location.longitude
                    binding.tvLat.text = latitude.toString()
                    binding.tvLon.text = longitude.toString()
                }
            }
        }
        onResume()
    }

    private fun getCurrentlocation() {
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
                // only requests fine permissions, may need to add coarse locations 1st cause of
                //potential bugs with android 11
                ActivityCompat.requestPermissions(
                    this, arrayOf
                        (android.Manifest.permission.ACCESS_FINE_LOCATION), 101
                )
                return
            }
        }
        checkPermissions()
        //^^ checking permissions DON'T TOUCH
        fun lastLocation() {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // since Location is a nullable object, location?.let handles cases
                    //in which location can be null, in which case the code does not get executed
                    location?.let {
                        // storing location in mCurrentLocation
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