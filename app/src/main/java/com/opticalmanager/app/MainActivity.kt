package com.opticalmanager.app

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "OpticalShop.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE inventory (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, category TEXT, price TEXT, quantity TEXT)")
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS inventory")
        onCreate(db)
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit.var dbHelper: DatabaseHelper
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var itemList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)
        listView = findViewById(R.id.listViewInventory)
        itemList = ArrayList()

        loadInventoryData()

        findViewById<Button>(R.id.btnAddLens).setOnClickListener {
            showAddDialog("Lens")
        }

        findViewById<Button>(R.id.btnAddFrame).setOnClickListener {
            showAddDialog("Frame")
        }
    }

    private fun loadInventoryData() {
        itemList.clear()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM inventory", null)
        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(1)
                val cat = cursor.getString(2)
                val price = cursor.getString(3)
                val qty = cursor.getString(4)
                itemList.add("$cat: $name | Rs. $price | Qty: $qty")
            } while (cursor.moveToNext())
        }
        cursor.close()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, itemList)
        listView.adapter = adapter
    }

    private fun showAddDialog(type: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add New $type Stock")

        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(40, 20, 40, 20)

        val inputName = EditText(this)
        inputName.hint = if (type == "Lens") "Lens Power / Type (e.g. -0.50 SPH)" else "Frame Brand / Model"
        layout.addView(inputName)

        val inputPrice = EditText(this)
        inputPrice.hint = "Price in PKR (e.g. 1500)"
        inputPrice.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        layout.addView(inputPrice)

        val inputQty = EditText(this)
        inputQty.hint = "Quantity (e.g. 10)"
        inputQty.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        layout.addView(inputQty)

        builder.setView(layout)

        builder.setPositiveButton("Save") { _, _ ->
            val name = inputName.text.toString()
            val price = inputPrice.text.toString()
            val qty = inputQty.text.toString()

            if (name.isNotEmpty() && price.isNotEmpty() && qty.isNotEmpty()) {
                val db = dbHelper.writableDatabase
                db.execSQL("INSERT INTO inventory (name, category, price, quantity) VALUES ('$name', '$type', '$price', '$qty')")
                Toast.makeText(this, "$type Added Successfully", Toast.LENGTH_SHORT).show()
                loadInventoryData()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}
