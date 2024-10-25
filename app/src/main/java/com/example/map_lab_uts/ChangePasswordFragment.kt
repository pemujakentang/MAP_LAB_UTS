package com.example.map_lab_uts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_change_password, container, false)

        auth = FirebaseAuth.getInstance()

        val etOldPassword = view.findViewById<EditText>(R.id.etOldPassword)
        val etNewPassword = view.findViewById<EditText>(R.id.etNewPassword)
        val etRepeatNewPassword = view.findViewById<EditText>(R.id.etRepeatNewPassword)
        val btnChangePassword = view.findViewById<Button>(R.id.btnChangePassword)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        btnChangePassword.setOnClickListener {
            val oldPassword = etOldPassword.text.toString()
            val newPassword = etNewPassword.text.toString()
            val repeatNewPassword = etRepeatNewPassword.text.toString()

            if (newPassword == repeatNewPassword) {
                val user = auth.currentUser
                if (user != null && user.email != null) {
                    val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
                    user.reauthenticate(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Toast.makeText(activity, "Password updated", Toast.LENGTH_SHORT).show()
                                    parentFragmentManager.popBackStack()
                                } else {
                                    Toast.makeText(activity, "Failed to update password", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(activity, "Authentication failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(activity, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }
}