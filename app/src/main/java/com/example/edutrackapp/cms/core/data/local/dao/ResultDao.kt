package com.example.edutrackapp.cms.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.edutrackapp.cms.core.data.local.entity.ResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: ResultEntity)

    @Query("SELECT * FROM results WHERE examType = :examType")
    suspend fun getResultsByExam(examType: String): List<ResultEntity>

    @Query("SELECT * FROM results WHERE rollNo = :rollNo")
    fun getResultsForStudent(rollNo: String): Flow<List<ResultEntity>>
}