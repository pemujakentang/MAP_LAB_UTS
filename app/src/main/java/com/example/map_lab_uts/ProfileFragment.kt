package com.example.map_lab_uts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val user = auth.currentUser
        val emailTextView = view.findViewById<TextView>(R.id.email_text_view)
        val nameTextView = view.findViewById<TextView>(R.id.name_text_view)
        val nimTextView = view.findViewById<TextView>(R.id.nim_text_view)
        val nameEditText = view.findViewById<EditText>(R.id.name_edit_text)
        val nimEditText = view.findViewById<EditText>(R.id.nim_edit_text)
        val saveButton = view.findViewById<Button>(R.id.save_button)
        val updateInfoButton = view.findViewById<Button>(R.id.update_info_button)
        val changePasswordButton = view.findViewById<Button>(R.id.change_password_button)
        val logoutButton = view.findViewById<Button>(R.id.logout_button)

        val constraintLayout = view as androidx.constraintlayout.widget.ConstraintLayout
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        if (user != null) {
            emailTextView.text = user.email

            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name")
                        val nim = document.getString("nim")

                        if (name.isNullOrEmpty() || nim.isNullOrEmpty()) {
                            nameTextView.visibility = View.GONE
                            nimTextView.visibility = View.GONE
                            nameEditText.visibility = View.VISIBLE
                            nimEditText.visibility = View.VISIBLE
                            saveButton.visibility = View.VISIBLE
                            updateInfoButton.visibility = View.GONE
                            changePasswordButton.visibility = View.GONE
                        } else {
                            nameTextView.text = name
                            nimTextView.text = nim
                            nameTextView.visibility = View.VISIBLE
                            nimTextView.visibility = View.VISIBLE
                            nameEditText.visibility = View.GONE
                            nimEditText.visibility = View.GONE
                            saveButton.visibility = View.GONE
                            updateInfoButton.visibility = View.VISIBLE
                            changePasswordButton.visibility = View.VISIBLE
                            logoutButton.visibility = View.VISIBLE
                        }
                    } else {
                        nameTextView.visibility = View.GONE
                        nimTextView.visibility = View.GONE
                        nameEditText.visibility = View.VISIBLE
                        nimEditText.visibility = View.VISIBLE
                        saveButton.visibility = View.VISIBLE
                        updateInfoButton.visibility = View.GONE
                        changePasswordButton.visibility = View.GONE
                    }
                }
        }

        constraintSet.applyTo(constraintLayout)

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
                        Toast.makeText(activity, "Information saved", Toast.LENGTH_SHORT).show()
                        nameTextView.text = name
                        nimTextView.text = nim
                        nameTextView.visibility = View.VISIBLE
                        nimTextView.visibility = View.VISIBLE
                        nameEditText.visibility = View.GONE
                        nimEditText.visibility = View.GONE
                        saveButton.visibility = View.GONE
                        updateInfoButton.visibility = View.VISIBLE
                        changePasswordButton.visibility = View.VISIBLE
                        logoutButton.visibility = View.VISIBLE
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(activity, "Error saving information: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(activity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        updateInfoButton.setOnClickListener {
            startActivity(Intent(activity, UpdateUserInfoActivity::class.java))
        }

        changePasswordButton.setOnClickListener {
            startActivity(Intent(activity, ChangePasswordActivity::class.java))
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            val sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putBoolean("isLoggedIn", false)
                apply()
            }
            startActivity(Intent(activity, LoginActivity::class.java))
            activity?.finish()
        }

        return view
    }
}