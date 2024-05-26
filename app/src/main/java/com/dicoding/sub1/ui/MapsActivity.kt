//package com.dicoding.sub1.ui
//
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import androidx.lifecycle.ViewModelProvider
//import com.dicoding.sub1.helper.LocationConverter
//import com.dicoding.sub1.R
//import com.dicoding.sub1.api.StoryDetail
//import com.dicoding.sub1.dataStore
//
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.OnMapReadyCallback
//import com.google.android.gms.maps.SupportMapFragment
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.MarkerOptions
//import com.dicoding.sub1.databinding.ActivityMapsBinding
//import com.dicoding.sub1.preference.UserPreferences
//import com.dicoding.sub1.viewmodel.HomeViewModel
//import com.dicoding.sub1.viewmodel.LoginViewModel
//import com.dicoding.sub1.viewmodel.ViewModelFactory
//import com.google.android.gms.maps.model.LatLngBounds
//
//class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
//
//    private val boundBuilder = LatLngBounds.Builder()
//    private lateinit var mMap: GoogleMap
//    private lateinit var binding: ActivityMapsBinding
//    private val mapsViewModel: HomeViewModel by lazy {
//        ViewModelProvider(this)[HomeViewModel::class.java]
//    }
//    private val pref by lazy {
//        UserPreferences.getInstance(dataStore)
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityMapsBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        val mapFragment = supportFragmentManager
//            .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//
//        val dataStoreViewModel =
//            ViewModelProvider(this, ViewModelFactory(pref))[LoginViewModel::class.java]
//        dataStoreViewModel.getToken().observe(this) { token ->
//            mapsViewModel.getStories(token)
//        }
//
//        mapsViewModel.stories.observe(this) { stories ->
//            setMarker(stories)
//        }
//    }
//
//    private fun setMarker(data: List<StoryDetail>) {
//        lateinit var locationZoom: LatLng
//        data.forEach {
//            val latLng = LatLng(it.lat, it.lon)
//            val address = LocationConverter.getStringAddress(latLng, this)
//            val marker = mMap.addMarker(
//                MarkerOptions()
//                    .position(latLng)
//                    .title(it.name)
//                    .snippet(address)
//            )
//            boundBuilder.include(latLng)
//            marker?.tag = it
//
//            locationZoom = latLng
//        }
//
//    }
//
//    override fun onMapReady(googleMap: GoogleMap) {
//        mMap = googleMap
//
//        // Enable zoom controls and gestures
//        mMap.uiSettings.isZoomControlsEnabled = true
//        mMap.uiSettings.isZoomGesturesEnabled = true
//
//        val defaultLocation = LatLng(-6.2088, 106.8456)
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 5f))
//
//    }
//}
