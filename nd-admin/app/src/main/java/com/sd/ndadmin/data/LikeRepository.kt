package com.sd.ndadmin.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.google.firebase.Timestamp

// Matches the exact fields stored in your Firestore documents
data class LikeItem(
    val id: String = "",
    val user: String = "",
    val cardType: String = "",
    val contentId: String = "",
    val timestamp: Timestamp? = null)

class LikeRepository {
    private val db = FirebaseFirestore.getInstance()

    val likesFlow: Flow<List<LikeItem>> = callbackFlow {
        val query = db.collection("likes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val likes = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(LikeItem::class.java)?.copy(id = doc.id)
                }
                trySend(likes)
            }
        }

        awaitClose { listener.remove() }
    }
}