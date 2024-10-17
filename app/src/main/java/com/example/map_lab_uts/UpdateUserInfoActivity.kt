package com.example.map_lab_uts

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UpdateUserInfoActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_user_info)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val nameEditText = findViewById<EditText>(R.id.name_edit_text)
        val nimEditText = findViewById<EditText>(R.id.nim_edit_text)
        val saveButton = findViewById<Button>(R.id.save_button)

        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name")
                        val nim = document.getString("nim")
                        nameEditText.setText(name)
                        nimEditText.setText(nim)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val nim = nimEditText.text.toString()

            if (name.isNotEmpty() && nim.isNotEmpty()) {
                val userData = hashMapOf(
                    "name" to name,
                    "nim" to nim
                )

                db.collection("users").document(user!!.uid).set(userData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Information updated", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error updating information: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}