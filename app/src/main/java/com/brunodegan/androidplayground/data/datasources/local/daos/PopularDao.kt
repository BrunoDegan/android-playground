package com.brunodegan.androidplayground.data.datasources.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.brunodegan.androidplayground.data.datasources.local.entities.PopularMoviesEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PopularDao {
    @Query("SELECT * FROM popular")
    abstract fun getAllPopular(): Flow<List<PopularMoviesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertPopularMovies(upcomingMovies: List<PopularMoviesEntity>)

    @Query("DELETE FROM popular")
    abstract fun deleteAll()

    @Transaction
    open fun replacePopularMovies(popularMovies: List<PopularMoviesEntity>) {
        deleteAll()
        insertPopularMovies(popularMovies)
    }
}
