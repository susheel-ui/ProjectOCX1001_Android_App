package com.example.project_a_android_userapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SenderDetailsActivity : AppCompatActivity() {

    private lateinit var houseEdit: EditText
    private lateinit var nameEdit: EditText
    private lateinit var phoneEdit: EditText
    private lateinit var typeRadioGroup: RadioGroup
    private lateinit var confirmButton: Button

    private var pickupLat = 0.0
    private var pickupLon = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sender_details)

        houseEdit = findViewById(R.id.houseEdit)
        nameEdit = findViewById(R.id.nameEdit)
        phoneEdit = findViewById(R.id.phoneEdit)
        typeRadioGroup = findViewById(R.id.typeRadioGroup)
        confirmButton = findViewById(R.id.confirmButton)

        pickupLat = intent.getDoubleExtra("pickupLat", 0.0)
        pickupLon = intent.getDoubleExtra("pickupLon", 0.0)

        confirmButton.setOnClickListener {
            val house = houseEdit.text.toString()
            val name = nameEdit.text.toString()
            val phone = phoneEdit.text.toString()
            val typeId = typeRadioGroup.checkedRadioButtonId

            if (house.isEmpty() || name.isEmpty() || phone.isEmpty() || typeId == -1) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val type = findViewById<RadioButton>(typeId).text.toString()

            val intent = Intent(this, ReceiverDetailsActivity::class.java)
            intent.putExtra("pickupLat", pickupLat)
            intent.putExtra("pickupLon", pickupLon)
            intent.putExtra("house", house)
            intent.putExtra("name", name)
            intent.putExtra("phone", phone)
            intent.putExtra("type", type)
            startActivity(intent)
        }
    }
}
