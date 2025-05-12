package pl.domain.application.mixology
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteCocktail(
    @PrimaryKey val cocktailId: String
)