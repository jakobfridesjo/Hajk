package com.example.hajk.ui.utilities

import androidx.lifecycle.*

class UtilitiesViewModel(private val savedState: SavedStateHandle) : ViewModel() {

    val torch_state = "State of torch key"
    val alarm_state = "State of alarm key"

    /**
     * Sets the state  of the torch
     */
    public fun setTorchState(toggle: Boolean) {
        savedState[torch_state] = toggle
    }

    /**
     * Returns the state of the torch
     */
    public fun getTorchState(): Boolean {
        return savedState[torch_state] ?: false
    }

    /**
     * Sets the state  of the alarm
     */
    public fun setAlarmState(toggle: Boolean) {
        savedState[alarm_state] = toggle
    }

    /**
     * Returns the state of the alarm
     */
    public fun getAlarmState(): Boolean {
        return savedState[alarm_state] ?: false
    }
}