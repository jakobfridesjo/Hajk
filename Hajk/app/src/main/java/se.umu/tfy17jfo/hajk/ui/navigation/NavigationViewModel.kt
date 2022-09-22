@file:Suppress("PrivatePropertyName")

package com.example.hajk.ui.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class NavigationViewModel(private val savedState: SavedStateHandle) : ViewModel() {

    private val state_coords = "location coordinates"

    /**
     * Sets coordinates
     */
    fun setCoords(coords: String) {
        savedState[state_coords] = coords
    }

    /**
     * Gets coordinates
     */
    fun getCoords(): String {
        return savedState[state_coords] ?: "Location not available"
    }
}