package com.brunodegan.androidplayground.data.datasources.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.brunodegan.androidplayground.data.datasources.local.entities.UpcomingMoviesEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UpComingDao {
    @Query("SELECT * FROM upcoming")
    abstract fun getAllUpcoming(): Flow<List<UpcomingMoviesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertUpcomingMovies(upcomingMovies: List<UpcomingMoviesEntity>)

    @Query("DELETE FROM upcoming")
    abstract fun deleteAll()

    @Transaction
    open fun replaceUpcomingMovies(upcomingMovies: List<UpcomingMoviesEntity>) {
        deleteAll()
        insertUpcomingMovies(upcomingMovies)
    }
}
