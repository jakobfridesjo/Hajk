package com.example.hajk.ui.notes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.hajk.databinding.ActivityNoteBinding

class NoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteBinding
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Connect to view
        saveButton = binding.saveButton
        cancelButton = binding.cancelButton
        editText = binding.editText
        val text = intent.getStringExtra("text")
        if (text != null) {
            editText.setText(text)
        }

        setButtonListeners()
    }

    /**
     * Sets the listeners on the buttons
     */
    private fun setButtonListeners() {
        // Save note
        saveButton.setOnClickListener {

            // Return result to notes fragment
            println(editText.text.toString())
            val intent = Intent()
            intent.putExtra("result", editText.text.toString())
            setResult(RESULT_OK, intent)
            // End activity
            finish()
        }

        // Cancel editing
        cancelButton.setOnClickListener {
            // Just end activity
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}