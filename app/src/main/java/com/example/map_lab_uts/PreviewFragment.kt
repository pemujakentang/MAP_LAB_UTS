package com.example.map_lab_uts

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PreviewFragment : Fragment() {

    companion object {
        private const val ARG_FILE_PATH = "file_path"

        fun newInstance(filePath: String): PreviewFragment {
            val fragment = PreviewFragment()
            val args = Bundle()
            args.putString(ARG_FILE_PATH, filePath)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val imageView = view.findViewById<ImageView>(R.id.preview_image)
        val dateTimeTextView = view.findViewById<TextView>(R.id.date_time_text)
        val entryTypeTextView = view.findViewById<TextView>(R.id.entry_type_text)
        val nextButton = view.findViewById<Button>(R.id.next_button)
        val backButton = view.findViewById<Button>(R.id.back_button)

        val filePath = arguments?.getString(ARG_FILE_PATH)
        var date: Date? = null

        if (filePath != null) {
            val imgFile = File(filePath)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                imageView.setImageBitmap(bitmap)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                date = Date(imgFile.lastModified())
                dateTimeTextView.text = dateFormat.format(date)
            }
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDateString = dateFormat.format(Date())

        db.collection("entries")
            .whereEqualTo("email", auth.currentUser?.email)
            .whereEqualTo("date", todayDateString)
            .get()
            .addOnSuccessListener { documents ->
                var entryType = "IN"
                var canAddEntry = true

                if (!documents.isEmpty) {
                    val entries = documents.map { it.toObject(Entry::class.java) }
                    val hasInEntry = entries.any { it.entryType == "IN" }
                    val hasOutEntry = entries.any { it.entryType == "OUT" }

                    entryType = when {
                        hasInEntry && hasOutEntry -> {
                            canAddEntry = false
                            "You Have Already Completed Today's Checks"
                        }
                        hasInEntry -> "OUT"
                        else -> "IN"
                    }
                }

                entryTypeTextView.text = entryType
                nextButton.isEnabled = canAddEntry
            }
            .addOnFailureListener { e ->
                Toast.makeText(activity, "Error checking today's entries: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        nextButton.setOnClickListener {
            if (filePath != null && date != null) {
                uploadImageAndSaveEntry(filePath, date) { entryType ->
                    entryTypeTextView.text = entryType
                }
            }
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun uploadImageAndSaveEntry(filePath: String, date: Date, callback: (String) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            val email = user.email
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val dateString = dateFormat.format(date)
            val timeString = timeFormat.format(date)

            db.collection("entries")
                .whereEqualTo("email", email)
                .orderBy("date", Query.Direction.DESCENDING)
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    var entryType = "IN"
                    var canAddEntry = true

                    if (!documents.isEmpty) {
                        val lastEntry = documents.documents[0].toObject(Entry::class.java)
                        if (lastEntry != null) {
                            val lastEntryDate = lastEntry.date
                            val lastEntryType = lastEntry.entryType

                            if (lastEntryDate == dateString) {
                                if (lastEntryType == "IN") {
                                    entryType = "OUT"
                                } else if (lastEntryType == "OUT") {
                                    canAddEntry = false
                                    Toast.makeText(activity, "You have already added an IN and OUT entry for today.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    if (canAddEntry) {
                        val storageRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")
                        val file = Uri.fromFile(File(filePath))

                        storageRef.putFile(file)
                            .addOnSuccessListener {
                                storageRef.downloadUrl.addOnSuccessListener { uri ->
                                    val entry = hashMapOf(
                                        "email" to email,
                                        "date" to dateString,
                                        "time" to timeString,
                                        "image" to uri.toString(),
                                        "entryType" to entryType
                                    )

                                    db.collection("entries").add(entry)
                                        .addOnSuccessListener {
                                            Toast.makeText(activity, "Entry saved", Toast.LENGTH_SHORT).show()
                                            callback(entryType)
                                            parentFragmentManager.beginTransaction()
                                                .replace(R.id.fragment_container, HistoryFragment())
                                                .addToBackStack(null)
                                                .commit()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(activity, "Error saving entry: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(activity, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(activity, "Error checking last entry: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}