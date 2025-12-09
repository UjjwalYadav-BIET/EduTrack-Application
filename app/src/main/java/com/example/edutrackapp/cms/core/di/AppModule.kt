package com.example.edutrackapp.cms.core.di

import android.app.Application
import androidx.room.Room
import com.example.edutrackapp.cms.core.data.local.EduTrackDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): EduTrackDatabase {
        return Room.databaseBuilder(
            app,
            EduTrackDatabase::class.java,
            "edutrack_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideUserDao(db: EduTrackDatabase) = db.userDao
}