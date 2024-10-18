package com.example.map_lab_uts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HistoryFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private val entries = mutableListOf<Entry>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val user = auth.currentUser
        if (user != null) {
            db.collection("entries")
                .whereEqualTo("email", user.email)
                .orderBy("date", Query.Direction.DESCENDING)
                .orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    val entries = documents.map { it.toObject(Entry::class.java) }
                    recyclerView.adapter = HistoryAdapter(entries)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(activity, "Error loading history: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadEntries() {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val user = auth.currentUser
        if (user != null) {
            db.collection("entries")
                .whereEqualTo("email", user.email)
                .orderBy("date", Query.Direction.DESCENDING)
                .orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    entries.clear()
                    for (document in documents) {
                        val entry = document.toObject(Entry::class.java)
                        entries.add(entry)
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(activity, "Error loading entries: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}