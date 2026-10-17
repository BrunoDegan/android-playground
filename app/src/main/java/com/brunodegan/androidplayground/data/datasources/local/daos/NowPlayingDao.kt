package com.brunodegan.androidplayground.data.datasources.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.brunodegan.androidplayground.data.datasources.local.entities.NowPlayingMoviesEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class NowPlayingDao {
    @Query("SELECT * FROM now_playing")
    abstract fun getAllNowPlaying(): Flow<List<NowPlayingMoviesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertNowPlayingMovies(nowPlayingMovies: List<NowPlayingMoviesEntity>)

    @Query("DELETE FROM now_playing")
    abstract fun deleteAll()

    @Transaction
    open fun replaceNowPlayingMovies(nowPlayingMovies: List<NowPlayingMoviesEntity>) {
        deleteAll()
        insertNowPlayingMovies(nowPlayingMovies)
    }
}
