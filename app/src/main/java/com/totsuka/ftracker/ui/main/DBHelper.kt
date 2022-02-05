package com.totsuka.ftracker.ui.main

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.widget.Toast
import java.util.*

const val TABLE_NAME = "history"
const val COLUMN_NAME_VALUE = "value"
const val COLUMN_NAME_REASON = "reason"
const val COLUMN_NAME_BALANCE = "balance"
const val COLUMN_NAME_DATE = "date"
const val COLUMN_NAME_TIME = "time"

val createTable =
    "CREATE TABLE $TABLE_NAME (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "$COLUMN_NAME_VALUE INT," +
            "$COLUMN_NAME_REASON VARCHAR(256)," +
            "$COLUMN_NAME_BALANCE INT," +
            "$COLUMN_NAME_DATE VARCHAR(20)," +
            "$COLUMN_NAME_TIME VARCHAR(20))"

class User {
    public var id: Int = 0
    public var value: Int = 0
    public var reason: String = ""
    public var balance: Int = 0
    public var date: String = ""
    public var time: String = ""
}

class DbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private var count: Int = 0

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        TODO("lazy, will never need this probably")
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun insertData(value: Int, reason: String, balance: Int, dbDate: String, dbTime: String, id: Int? = null): Int{
        val db = this.writableDatabase

        // Create a new map of values, where column names are the keys
        val values = ContentValues().apply {
            if (id != null) put(BaseColumns._ID, id)
            put(COLUMN_NAME_VALUE, value)
            put(COLUMN_NAME_REASON, reason)
            put(COLUMN_NAME_BALANCE, balance)
            put(COLUMN_NAME_DATE, dbDate)
            put(COLUMN_NAME_TIME, dbTime)
        }

        // Insert the new row, returning the primary key value of the new row
        val newRowId = db?.insert(TABLE_NAME, null, values)
        if (count < newRowId!!) {
            count = newRowId!!.toInt()
        }
        return newRowId!!.toInt()
    }

    fun getData(): MutableList<User>{
        val list: MutableList<User> = ArrayList()
        val db = this.readableDatabase
        var query = "SELECT * FROM $TABLE_NAME "
        val temp = db.rawQuery(query + "ORDER BY ${BaseColumns._ID} DESC LIMIT 1", null)

        var lastId = 0
        if (temp.moveToFirst()) {
            lastId = temp.getString(temp.getColumnIndex(BaseColumns._ID)).toInt()
        }
        if (lastId < count) return list

        val limit = lastId - count
        var order = "DESC"
        if (lastId == (lastId - count)){
            order = "ASC"
        }

        count = lastId
        val result = db.rawQuery(query + "ORDER BY ${BaseColumns._ID} $order LIMIT $limit", null)
        if (result.moveToFirst()) {
            do {
                val user = User()
                user.id = result.getString(result.getColumnIndex(BaseColumns._ID)).toInt()
                user.value = result.getString(result.getColumnIndex(COLUMN_NAME_VALUE)).toInt()
                user.reason = result.getString(result.getColumnIndex(COLUMN_NAME_REASON))
                user.balance = result.getString(result.getColumnIndex(COLUMN_NAME_BALANCE)).toInt()
                user.date = result.getString(result.getColumnIndex(COLUMN_NAME_DATE))
                user.time = result.getString(result.getColumnIndex(COLUMN_NAME_TIME))
                list.add(user)
            }
            while (result.moveToNext())
        }
        return list
    }

    fun deleteRecord(id: Int, context: Context): Int{
        val selection = "${BaseColumns._ID} = '$id'"
        val deletedRows = this.writableDatabase.delete(TABLE_NAME, selection, null)
        if (id == count) {
            count--
        }
        return deletedRows
    }

    fun setCount(c: Int){
        count = c
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "History.db"
    }
}
