package pl.domain.application.mixology
import androidx.room.*

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteCocktail)

    @Delete
    suspend fun delete(favorite: FavoriteCocktail)

    @Query("SELECT * FROM favorites")
    suspend fun getAll(): List<FavoriteCocktail>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE cocktailId = :id)")
    suspend fun isFavorite(id: String): Boolean
}
