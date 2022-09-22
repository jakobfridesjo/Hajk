package com.example.hajk.ui.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.hajk.InventoryDB

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    // Initialize database
    private val db = InventoryDB(application)

    private val _text = MutableLiveData<String>().apply {
        value = "This is inventory fragment"
    }

    val text: LiveData<String> = _text

    /**
     * Gets data from database
     * @return ArrayList of strings in database
     */
    fun getDBData(): ArrayList<String> {
        return db.getDataArrayList()
    }

    /**
     * Gets entry from database
     * @return string entry
     */
    fun readDBDataEntry(name: String): String {
        return db.readDataEntry(name)
    }

    /**
     * Deletes entry from database
     */
    fun deleteDBEntry(name: String) {
        db.deleteData(name)
    }

    /**
     * Adds entry to database
     */
    fun addToDB(text: String, name: String) {
        db.insertData(text, name)
    }
}