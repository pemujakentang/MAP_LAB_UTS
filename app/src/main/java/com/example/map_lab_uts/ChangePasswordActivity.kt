package com.example.map_lab_uts

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        auth = FirebaseAuth.getInstance()

        val newPasswordEditText = findViewById<EditText>(R.id.new_password_edit_text)
        val changePasswordButton = findViewById<Button>(R.id.change_password_button)

        changePasswordButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString()

            if (newPassword.isNotEmpty()) {
                auth.currentUser?.updatePassword(newPassword)
                    ?.addOnSuccessListener {
                        Toast.makeText(this, "Password changed", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    ?.addOnFailureListener { e ->
                        Toast.makeText(this, "Error changing password: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please enter a new password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}