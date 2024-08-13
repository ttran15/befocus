package com.example.befocus

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class NotificationFragment: DialogFragment(R.layout.notification_fragment) {
    private var content: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            content = it.getString("arg_message")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val txtContent: TextView = view.findViewById(R.id.txtContent)
        txtContent.text = content
        val btnOk : Button = view.findViewById(R.id.btnOK);
        btnOk.setOnClickListener{
            dismiss()
        }
    }
}