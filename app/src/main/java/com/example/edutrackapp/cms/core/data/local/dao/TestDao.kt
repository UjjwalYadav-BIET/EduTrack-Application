package com.example.edutrackapp.cms.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.edutrackapp.cms.core.data.local.entity.TestEntity

@Dao
interface TestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: TestEntity): Long

    @Query("SELECT * FROM tests ORDER BY testId DESC")
    suspend fun getAllTests(): List<TestEntity>

    @Query("SELECT * FROM tests WHERE testId = :testId")
    suspend fun getTestById(testId: Int): TestEntity?

    @Delete
    suspend fun deleteTest(test: TestEntity)

    @Query("SELECT * FROM tests WHERE teacherId = :teacherId ORDER BY date DESC")
    suspend fun getTestsByTeacher(teacherId: Int): List<TestEntity>
}