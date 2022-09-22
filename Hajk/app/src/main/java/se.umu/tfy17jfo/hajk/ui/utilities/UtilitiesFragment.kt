package com.example.hajk.ui.utilities

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.hajk.databinding.FragmentUtilitiesBinding


class UtilitiesFragment : Fragment() {

    private lateinit var utilitiesViewModel: UtilitiesViewModel
    private var _binding: FragmentUtilitiesBinding? = null
    private lateinit var torch: ToggleButton
    private lateinit var alarm: ToggleButton
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String
    private val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, Int.MAX_VALUE)

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        utilitiesViewModel =
                ViewModelProvider(this).get(UtilitiesViewModel::class.java)

        _binding = FragmentUtilitiesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        cameraManager = activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList[0]

        torch = binding.torch
        torch.isChecked = utilitiesViewModel.getTorchState()
        torch.setOnCheckedChangeListener {
            _,isChecked -> toggleTorch(isChecked)
        }

        alarm = binding.alarm
        //alarm.isChecked = utilitiesViewModel.getAlarmState()
        alarm.setOnCheckedChangeListener {
            _,isChecked -> toggleAlarm(isChecked)
        }

        return root
    }

    /**
     * Toggles the torch/camera flash of the device
     */
    private fun toggleTorch(toggle: Boolean) {
        println("Toggle: $toggle")
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
            cameraManager.setTorchMode(cameraId, toggle)
            utilitiesViewModel.setTorchState(toggle)
        }
    }

    /**
     * Toggles an alarm on/off
     */
    private fun toggleAlarm(toggle: Boolean) {
        toneGen.stopTone()
        if (toggle) {
            toneGen.startTone(ToneGenerator.TONE_CDMA_HIGH_SS)
        }
        else {
            toneGen.stopTone()
        }
        //utilitiesViewModel.setAlarmState(toggle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        toneGen.stopTone()
        _binding = null
    }
}