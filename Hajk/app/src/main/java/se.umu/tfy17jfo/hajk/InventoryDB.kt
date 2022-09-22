package com.example.hajk

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper

const val dbName = "INVENTORY"
const val tableName = "Notes"
const val colNote = "Note"
const val colName = "Name"
const val colId = "id"
const val timestamp = "timestamp"

class InventoryDB(context: Context) : SQLiteOpenHelper(context, dbName, null,
    1) {

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = ("CREATE TABLE "
                + tableName + "("
                + colId + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + colName + " TEXT,"
                + colNote + " TEXT,"
                + timestamp + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")")
        db?.execSQL(createTable)
    }

    /**
     * When database version changes
     */
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        onCreate(db)
    }

    /**
     * Insert data into database
     */
    fun insertData(text: String, name: String): Long {
        val database = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(colName, name.hashCode().toString())
        contentValues.put(colNote, text)
        val result = database.insert(tableName, null, contentValues)
        database.close()
        return result
    }

    /**
     * Read data from database
     */
    fun readDataEntry(name: String): String {
        val database = this.readableDatabase

        val cursor = database.query(tableName, arrayOf(colName,colNote),
            "$colName=?",
            arrayOf(name.hashCode().toString()), null, null, null, null)
        cursor?.moveToFirst()
        val text = cursor.getString(1)
        database.close()
        return text
    }

    /**
     * Deletes data from database
     */
    fun deleteData(name: String) {
        val database = this.writableDatabase
        database.delete(tableName, "$colName=?", arrayOf(name.hashCode().toString()))
        database.close()
    }

    /**
     * Gets all strings from database
     * @return list of entries
     */
    fun getDataArrayList(): ArrayList<String> {

        val list = arrayListOf<String>()

        // Query to select all the records from the table.
        val selectQuery = "SELECT  * FROM $tableName"

        val database = this.readableDatabase

        // Cursor is used to read the record one by one. Add them to data model class.
        val cursor: Cursor
        try {
            cursor = database.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            database.execSQL(selectQuery)
            return list
        }

        var text: String
        if (cursor.moveToFirst()) {
            do {
                text = cursor.getString(cursor.getColumnIndex(colNote).coerceAtLeast(1))
                list.add(text)
            } while (cursor.moveToNext())
        }
        database.close()
        return list
    }
}