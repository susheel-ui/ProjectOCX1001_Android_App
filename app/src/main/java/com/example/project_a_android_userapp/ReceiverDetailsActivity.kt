package com.example.project_a_android_userapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ReceiverDetailsActivity : AppCompatActivity() {

    private lateinit var houseEdit: EditText
    private lateinit var nameEdit: EditText
    private lateinit var phoneEdit: EditText
    private lateinit var typeRadioGroup: RadioGroup
    private lateinit var confirmButton: Button

    private var pickupLat: Double = 0.0
    private var pickupLon: Double = 0.0

    private var senderHouse: String? = null
    private var senderName: String? = null
    private var senderPhone: String? = null
    private var senderType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receiver_details)

        // Bind Views
        houseEdit = findViewById(R.id.houseEdit)
        nameEdit = findViewById(R.id.nameEdit)
        phoneEdit = findViewById(R.id.phoneEdit)
        typeRadioGroup = findViewById(R.id.typeRadioGroup)
        confirmButton = findViewById(R.id.confirmButton)

        // Get sender data
        pickupLat = intent.getDoubleExtra("pickupLat", 0.0)
        pickupLon = intent.getDoubleExtra("pickupLon", 0.0)
        senderHouse = intent.getStringExtra("house")
        senderName = intent.getStringExtra("name")
        senderPhone = intent.getStringExtra("phone")
        senderType = intent.getStringExtra("type")

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

            // Go to FareActivity
            val intent = Intent(this, FareActivity::class.java)
            intent.putExtra("pickupLat", pickupLat)
            intent.putExtra("pickupLon", pickupLon)

            // Sender
            intent.putExtra("senderHouse", senderHouse)
            intent.putExtra("senderName", senderName)
            intent.putExtra("senderPhone", senderPhone)
            intent.putExtra("senderType", senderType)

            // Receiver
            intent.putExtra("dropHouse", house)
            intent.putExtra("dropName", name)
            intent.putExtra("dropPhone", phone)
            intent.putExtra("dropType", type)

            startActivity(intent)
        }
    }
}
