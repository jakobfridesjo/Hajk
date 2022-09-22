package com.example.hajk.ui.navigation

import android.Manifest
import android.content.Context.LOCATION_SERVICE
import android.content.Context.SENSOR_SERVICE
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_MAGNETIC_FIELD
import android.hardware.SensorManager.SENSOR_DELAY_GAME
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.RotateAnimation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hajk.R
import com.example.hajk.databinding.FragmentNavigationBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.Math.toDegrees
import java.math.RoundingMode
import java.text.DecimalFormat

class NavigationFragment : Fragment(), SensorEventListener {

    private lateinit var navigationViewModel: NavigationViewModel
    private var _binding: FragmentNavigationBinding? = null
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var magnetometer: Sensor
    private var currentDeg = 0f
    private var currentX = 0f
    private var currentY = 0f
    private var mAcc = FloatArray(3)
    private var mMag = FloatArray(3)
    private var locationManager: LocationManager? = null
    private val df = DecimalFormat("#.#####")
    private var setLocation = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var needle: ImageView
    private lateinit var bubbleX: ImageView
    private lateinit var bubbleY: ImageView
    private lateinit var coordButton: FloatingActionButton
    private lateinit var coordView: TextView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Connect to view model
        navigationViewModel =
            ViewModelProvider(this).get(NavigationViewModel::class.java)



        _binding = FragmentNavigationBinding.inflate(inflater, container, false)

        //Connect to View
        needle = binding.compassNeedle
        bubbleX = binding.bubbleX
        bubbleY = binding.bubbleY
        coordButton = binding.coordButton
        coordView = binding.coordView

        // Sensors
        sensorManager = activity?.getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD)

        // Coordinates
        df.roundingMode = RoundingMode.CEILING
        locationManager = activity?.getSystemService(LOCATION_SERVICE) as LocationManager?
        coordView.text = navigationViewModel.getCoords()

        coordButton.setOnClickListener {
            navigationViewModel.setCoords("Getting location...")
            coordView.text = navigationViewModel.getCoords()
            setLocation = true
            getLocation()
        }

        return binding.root
    }

    /**
     * Gets the location
     */
    private fun getLocation() {
        getLocationPermission()
    }

    /**
     * Gets permissions for location
     */
    private fun getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), 1
            )
        }
        if (ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                locationListener
            )
        }
    }

    /**
     * Listener for location
     */
    private val locationListener: LocationListener = object : LocationListener {
        // Get location
        override fun onLocationChanged(location: Location) {
            if (setLocation) {
                navigationViewModel.setCoords(
                    "${df.format(location.latitude)} : ${
                        df.format(
                            location.longitude
                        )
                    }"
                )
                coordView.text = navigationViewModel.getCoords()
                setLocation = false
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    /**
     * Listener for magnetic field sensor and accelerometer
     */
    override fun onSensorChanged(p0: SensorEvent) {
        if ((p0.sensor === accelerometer) || (p0.sensor === magnetometer)) {
            if (p0.sensor === accelerometer) {
                lpFilter(p0.values, mAcc)
                //mAcc = mvFilter0.movingAverageFilter(p0.values)
            } else if (p0.sensor === magnetometer) {
                lpFilter(p0.values, mMag)
                //mMag = mvFilter1.movingAverageFilter(p0.values)
            }
            val rotationMat = FloatArray(9)
            val identityMat = FloatArray(9)
            val result = SensorManager.getRotationMatrix(rotationMat, identityMat, mAcc, mMag)
            if (result) {
                val orientationMat = FloatArray(3)
                SensorManager.getOrientation(rotationMat, orientationMat)
                var deg = (toDegrees(orientationMat[0].toDouble()).toFloat())
                deg = normalizeDeg(deg)
                // Rotate  needle
                compass(deg)
                spiritLevelX(mAcc)
                spiritLevelY(mAcc)
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // Do nothing
    }

    /**
     * Animate compass
     */
    private fun compass(deg: Float) {
        val rotate = RotateAnimation(
            -currentDeg,
            -deg,
            RELATIVE_TO_SELF,
            0.5f,
            RELATIVE_TO_SELF,
            0.5f
        )
        currentDeg = deg
        rotate.duration = 500
        rotate.fillAfter = true
        rotate.repeatCount = 0
        needle.startAnimation(rotate)
    }

    /**
     * Animate y-axis spirit-level
     */
    private fun spiritLevelY(mAcc: FloatArray) {
        // Translate y-axis bubble
        val transY = TranslateAnimation(
            RELATIVE_TO_SELF, 0f,
            RELATIVE_TO_SELF, 0f,
            RELATIVE_TO_SELF, -currentY / 10,
            RELATIVE_TO_SELF, -mAcc[1] / 10
        )
        currentY = mAcc[1]
        transY.duration = 250
        transY.fillAfter = true
        bubbleY.startAnimation(transY)
    }

    /**
     * Animate x-axis spirit-level
     */
    private fun spiritLevelX(mAcc: FloatArray) {
        // Translate x-axis bubble
        val transX = TranslateAnimation(
            RELATIVE_TO_SELF, currentX / 10,
            RELATIVE_TO_SELF, mAcc[0] / 10,
            RELATIVE_TO_SELF, 0f,
            RELATIVE_TO_SELF, 0f
        )
        currentX = mAcc[0]
        transX.duration = 250
        transX.fillAfter = true
        bubbleX.startAnimation(transX)
    }

    /**
     * Normalizes degrees
     * @return normalized degrees
     */
    private fun normalizeDeg(deg: Float): Float {
        return (deg + 360) % 360
    }

    /**
     * Filters data
     * @return filtered data
     */
    private fun lpFilter(values: FloatArray, filtered: FloatArray) {
        val alpha = 0.04f

        for (i in values.indices) {
            filtered[i] = filtered[i] + alpha * (values[i] - filtered[i])
        }
    }

    /**
     * Enable listeners
     */
    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometer, SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, magnetometer, SENSOR_DELAY_GAME)
        if (ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                locationListener
            )
        }
    }

    /**
     * Save some energy, disable sensor listeners
     */
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this, accelerometer)
        sensorManager.unregisterListener(this, magnetometer)
        if (ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager?.removeUpdates(locationListener)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}