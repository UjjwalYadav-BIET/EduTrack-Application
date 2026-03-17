package com.example.edutrackapp.cms.core.data.local.dao

import androidx.room.Embedded
import androidx.room.Relation
import com.example.edutrackapp.cms.core.data.local.entity.AssignmentEntity
import com.example.edutrackapp.cms.core.data.local.entity.AssignmentSubmissionEntity

data class AssignmentWithSubmissions(

    @Embedded
    val assignment: AssignmentEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "assignmentId"
    )
    val submissions: List<AssignmentSubmissionEntity>
)