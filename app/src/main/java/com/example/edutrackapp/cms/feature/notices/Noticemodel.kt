package com.example.edutrackapp.cms.feature.notices

import com.google.firebase.Timestamp

// ─── Priority ─────────────────────────────────────────────────────────────────
enum class NoticePriority(val label: String, val value: String) {
    LOW("Low", "low"),
    NORMAL("Normal", "normal"),
    URGENT("Urgent", "urgent")
}

// ─── Audience ─────────────────────────────────────────────────────────────────
enum class NoticeAudience(val label: String, val value: String) {
    ALL("Everyone", "all"),
    TEACHERS("Teachers Only", "teacher"),
    STUDENTS("Students Only", "student")
}

// ─── Shared Notice Model ──────────────────────────────────────────────────────
data class Notice(
    val id            : String    = "",
    val title         : String    = "",
    val message       : String    = "",
    val priority      : String    = NoticePriority.NORMAL.value,
    val audience      : String    = NoticeAudience.ALL.value,
    val postedBy      : String    = "",
    val postedByRole  : String    = "",
    val postedByUid   : String    = "",
    val attachmentUrl : String    = "",   // Base64-encoded file data
    val attachmentName: String    = "",   // Original filename e.g. "homework.pdf"
    val attachmentMime: String    = "",   // MIME type e.g. "application/pdf"
    val createdAt     : Timestamp = Timestamp.now()
)