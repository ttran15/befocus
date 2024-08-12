package com.example.befocus

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // hide action bar
        getSupportActionBar()?.hide()

        // declare variables
        val edtTaskName: EditText = findViewById(R.id.edtTaskName)
        val btnSubmit: Button = findViewById(R.id.btnSubmit)

        val spnMin : Spinner = findViewById(R.id.spnMinute)
        var lsMin = Array(60) { (it).toString() }
        spnMin.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,lsMin )

        val spnSec : Spinner = findViewById(R.id.spnSecond)
        var lsSec = Array(60) { (it + 1).toString() }
        spnSec.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,lsSec )

        // actions
        var selectedMinutes = 0
        spnMin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedMinutes = position
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        var selectedSeconds = 0
        spnSec.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedSeconds = position + 1
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        btnSubmit.setOnClickListener {
            val taskName_msg: String = edtTaskName.text.toString()
            if (taskName_msg.isEmpty()){
                var dialog_notification = PopupFragment()
                val args = Bundle().apply {
                    putString("arg_message", "Task Name cannot empty")
                }
                dialog_notification.arguments = args
                dialog_notification.show(supportFragmentManager,"Notification")
            }
            else {
                val intent = Intent(this, TimerActivity::class.java)
                intent.putExtra("taskName", taskName_msg)
                intent.putExtra("taskMinutes", selectedMinutes.toString())
                intent.putExtra("taskSeconds", selectedSeconds.toString())
                startActivity(intent)
            }
        }
    }
}