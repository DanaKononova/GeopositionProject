package com.example.geopositionproject

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.*
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider


class MainActivity : AppCompatActivity() {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val markerClickCallbacks = mutableListOf<MapObjectTapListener>()

    private val locationCallback: LocationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val point = Point(location.latitude, location.longitude)
                    mapView.map.mapObjects.clear()

                    val markerSize =
                        this@MainActivity.resources.getDimensionPixelSize(R.dimen.map_marker_icon_size)
                    Glide.with(this@MainActivity).asBitmap()
                        .load("https://abrakadabra.fun/uploads/posts/2022-01/1642606164_5-abrakadabra-fun-p-lokatsiya-ikonka-16.png")
                        .into(object : CustomTarget<Bitmap>(markerSize, markerSize) {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                mapView.map.mapObjects.addPlacemark(
                                    point,
                                    ImageProvider.fromBitmap(resource),
                                    IconStyle()
                                )
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                        })

                    mapView.getMap().move(
                        CameraPosition(
                            Point(location.latitude, location.longitude),
                            11.0f,
                            0.0f,
                            0.0f
                        ),
                        Animation(Animation.Type.SMOOTH, 1F),
                        null
                    )
                }
            }
        }
    }

    private val locationRequest = createLocationRequest()

    private val mapView: MapView by lazy {
        findViewById(R.id.mapview)
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.get(
                ACCESS_FINE_LOCATION,
            ) == true -> {
            }
            permissions.get(
                ACCESS_COARSE_LOCATION,
            ) == true -> {
            }
            else -> {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("f96285cf-cf3c-4310-be09-3280cff97d51");
        MapKitFactory.initialize(this)
        setContentView(R.layout.activity_main)

        findViewById<ImageButton>(R.id.officeBt).setOnClickListener {
            getOffice()
        }

        locationPermissionRequest.launch(
            arrayOf(
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION
            )
        )

        findViewById<ImageButton>(R.id.searchBt).setOnClickListener {
            startLocationUpdates()
        }
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    fun getOffice() {
        val officePoint = Point(53.925157, 27.508873)

        val markerSize =
            this@MainActivity.resources.getDimensionPixelSize(R.dimen.map_marker_icon_size)
        Glide.with(this@MainActivity).asBitmap()
            .load("https://abrakadabra.fun/uploads/posts/2022-01/1642606164_5-abrakadabra-fun-p-lokatsiya-ikonka-16.png")
            .into(object : CustomTarget<Bitmap>(markerSize, markerSize) {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    val markerClickCallback =
                        MapObjectTapListener { mapObject, _ ->
                            if (mapObject is PlacemarkMapObject) {
                                val bottomSheetFragment = LocationInfoFragment()
                                supportFragmentManager
                                    .beginTransaction()
                                    .add(bottomSheetFragment, "")
                                    .addToBackStack("")
                                    .commit()
                                true
                            } else false
                        }
                    mapView.map.mapObjects.addPlacemark(
                        officePoint,
                        ImageProvider.fromBitmap(resource),
                        IconStyle()
                    ).addTapListener(markerClickCallback)
                    markerClickCallbacks.add(markerClickCallback)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
        mapView.getMap().move(
            CameraPosition(officePoint, 11.0f, 0.0f, 0.0f),
            Animation(Animation.Type.LINEAR, 1F),
            null
        )
    }

    fun createLocationRequest(): LocationRequest {
        val timeInterval = 10000L
        return LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, timeInterval
        ).build()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }
}