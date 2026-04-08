package com.example.edutrackapp.cms.feature.notices

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoticeRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("notices")

    fun getNoticesForRole(role: String): Flow<List<Notice>> = callbackFlow {
        var registration: ListenerRegistration? = null

        registration = collection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }

                val allNotices = snapshot?.documents?.mapNotNull { doc ->
                    Notice(
                        id             = doc.id,
                        title          = doc.getString("title")          ?: "",
                        message        = doc.getString("message")        ?: "",
                        priority       = doc.getString("priority")       ?: NoticePriority.NORMAL.value,
                        audience       = doc.getString("audience")       ?: NoticeAudience.ALL.value,
                        postedBy       = doc.getString("postedBy")       ?: "",
                        postedByRole   = doc.getString("postedByRole")   ?: "",
                        postedByUid    = doc.getString("postedByUid")    ?: "",
                        attachmentUrl  = doc.getString("attachmentUrl")  ?: "",
                        attachmentName = doc.getString("attachmentName") ?: "",
                        attachmentMime = doc.getString("attachmentMime") ?: "",
                        createdAt      = doc.getTimestamp("createdAt")   ?: Timestamp.now()
                    )
                } ?: emptyList()

                val filtered = when (role) {
                    "admin"   -> allNotices
                    "teacher" -> allNotices.filter { it.audience == "all" || it.audience == "teacher" }
                    "student" -> allNotices.filter { it.audience == "all" || it.audience == "student" }
                    else      -> allNotices
                }

                trySend(filtered)
            }

        awaitClose { registration?.remove() }
    }

    suspend fun postNotice(notice: Notice): Result<Unit> {
        return try {
            val doc = mapOf(
                "title"          to notice.title,
                "message"        to notice.message,
                "priority"       to notice.priority,
                "audience"       to notice.audience,
                "postedBy"       to notice.postedBy,
                "postedByRole"   to notice.postedByRole,
                "postedByUid"    to notice.postedByUid,
                "attachmentUrl"  to notice.attachmentUrl,   // Base64 string
                "attachmentName" to notice.attachmentName,
                "attachmentMime" to notice.attachmentMime,
                "createdAt"      to Timestamp.now()
            )
            collection.add(doc).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNotice(noticeId: String): Result<Unit> {
        return try {
            collection.document(noticeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}