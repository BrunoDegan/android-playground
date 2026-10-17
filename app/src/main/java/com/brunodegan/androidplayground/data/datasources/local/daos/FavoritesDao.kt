package com.brunodegan.androidplayground.data.datasources.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.brunodegan.androidplayground.data.datasources.local.entities.FavoriteMoviesEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FavoritesDao {
    @Query("SELECT * FROM favorites")
    abstract fun getFavoriteMovies(): Flow<List<FavoriteMoviesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertFavorite(favoriteMovie: List<FavoriteMoviesEntity>)

    @Query("DELETE FROM favorites WHERE id = :id")
    abstract fun deleteFavoriteById(id: Int)

    @Query("DELETE FROM favorites")
    abstract fun deleteAll()

    // Remote list is the source of truth: drop rows it no longer contains
    @Transaction
    open fun replaceFavorites(favoriteMovie: List<FavoriteMoviesEntity>) {
        deleteAll()
        insertFavorite(favoriteMovie)
    }
}
