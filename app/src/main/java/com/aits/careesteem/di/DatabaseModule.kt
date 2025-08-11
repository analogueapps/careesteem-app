package com.aits.careesteem.di

import android.content.Context
import androidx.room.Room
import com.aits.careesteem.BuildConfig
import com.aits.careesteem.room.dao.VisitDao
import com.aits.careesteem.room.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "careesteem_db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideVisitDao(database: AppDatabase): VisitDao = database.visitDao()
}

