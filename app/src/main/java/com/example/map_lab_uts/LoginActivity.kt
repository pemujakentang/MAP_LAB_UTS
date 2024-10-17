package com.example.map_lab_uts

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailField = findViewById<EditText>(R.id.email)
        val passwordField = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login_button)
        val signupRedirectButton = findViewById<Button>(R.id.signup_redirect_button)

        // Initially disable the login button
        loginButton.isEnabled = false

        signupRedirectButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = emailField.text?.toString() ?: ""
                val password = passwordField.text?.toString() ?: ""
                loginButton.isEnabled = email.isNotEmpty() && password.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        emailField.addTextChangedListener(textWatcher)
        passwordField.addTextChangedListener(textWatcher)

        loginButton.setOnClickListener {
            val email = emailField.text?.toString() ?: ""
            val password = passwordField.text?.toString() ?: ""

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}