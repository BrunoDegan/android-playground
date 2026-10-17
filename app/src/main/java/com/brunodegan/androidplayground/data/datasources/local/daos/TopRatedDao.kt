package com.brunodegan.androidplayground.data.datasources.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.brunodegan.androidplayground.data.datasources.local.entities.TopRatedMoviesEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TopRatedDao {
    @Query("SELECT * FROM top_rated")
    abstract fun getAllTopRated(): Flow<List<TopRatedMoviesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertTopRatedMovies(upcomingMovies: List<TopRatedMoviesEntity>)

    @Query("DELETE FROM top_rated")
    abstract fun deleteAll()

    @Transaction
    open fun replaceTopRatedMovies(topRatedMovies: List<TopRatedMoviesEntity>) {
        deleteAll()
        insertTopRatedMovies(topRatedMovies)
    }
}
