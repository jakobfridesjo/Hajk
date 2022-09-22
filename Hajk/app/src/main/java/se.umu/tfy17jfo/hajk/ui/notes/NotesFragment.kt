package com.example.hajk.ui.notes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.hajk.InventoryDB
import com.example.hajk.R
import com.example.hajk.databinding.FragmentNoteBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.widget.*
import androidx.core.content.ContextCompat.getSystemService


class NotesFragment : Fragment() {

    private lateinit var notesViewModel: NotesViewModel
    private var _binding: FragmentNoteBinding? = null
    private lateinit var listView: ListView
    private var array = arrayListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private var indexDelete = 0

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var addButton: FloatingActionButton

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        notesViewModel =
                ViewModelProvider(this).get(NotesViewModel::class.java)

        _binding = FragmentNoteBinding.inflate(inflater, container, false)
        val root: View = binding.root

        addButton = binding.addButton
        listView = binding.listView

        setListViewAdapter()
        addListItemsFromDB()

        addButton.setOnClickListener {
            val intent = Intent(activity, NoteActivity::class.java)
            this.startActivityForResult(intent, 1)
        }

        listView.setOnItemClickListener { _, _, i, _ ->
            //Toast.makeText(requireContext(), "Position clicked: $i", Toast.LENGTH_SHORT).show()
            val intent = Intent(activity, NoteActivity::class.java)
            val text = notesViewModel.readDBDataEntry(array[i])
            intent.putExtra("text", text)
            indexDelete = i
            this.startActivityForResult(intent, 2)
        }

        listView.setOnItemLongClickListener { _, _, i, _ ->

            // Inflate a custom view using layout inflater
            val view = inflater.inflate(R.layout.popup_view,null)

            // Popup window
            val popupWindow = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
                LinearLayout.LayoutParams.WRAP_CONTENT // Window height
            )

            // Get buttons
            val buttonDelete = view.findViewById<Button>(R.id.button_delete)
            val buttonCancel = view.findViewById<Button>(R.id.button_cancel)

            // Set listener on buttons
            buttonDelete.setOnClickListener{
                // Delete note from ListView and dismiss the popup window
                notesViewModel.deleteDBEntry(array[i])
                delEntry(i)
                popupWindow.dismiss()
            }
            buttonCancel.setOnClickListener{
                // Dismiss the popup window
                popupWindow.dismiss()
            }

            TransitionManager.beginDelayedTransition(container)
            popupWindow.showAtLocation(
                view, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
            )
            true
        }
        return root
    }

    /**
     * Adds note to database and list, very much wip
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                val string = data?.getStringExtra("result") as String
                addEntry(string, true)
                adapter.notifyDataSetChanged()
            }
        }
        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                val string = data?.getStringExtra("result") as String
                notesViewModel.deleteDBEntry(array[indexDelete])
                array.removeAt(indexDelete)
                addEntry(string, true)
                adapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * Fills the list view with items from database
     */
    private fun addListItemsFromDB() {
        array.clear()
        this.adapter.notifyDataSetChanged()
        val entryList = notesViewModel.getDBData()
        entryList.forEach {
            addEntry(it,false)
        }
        this.adapter.notifyDataSetChanged()
    }

    /**
     * Sets adapter on ListView
     */
    private fun setListViewAdapter() {
        this.adapter = ArrayAdapter(this.requireContext(), R.layout.listview_item, array)
        listView.adapter = adapter
    }

    /**
     * Adds entry to ListView adapter ArrayList
     */
    private fun addEntry(text: String, addToDB: Boolean) {
        val title: String = if (text.length > 20) {
            text.substring(0..20)
        } else {
            text
        }
        array.add(title)
        if (addToDB) {
            notesViewModel.addToDB(text, title)
        }
        this.adapter.notifyDataSetChanged()
    }

    /**
     * Removes entry from ListView adapter dataset
     */
    private fun delEntry(index: Int) {
        this.array.removeAt(index)
        this.adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}