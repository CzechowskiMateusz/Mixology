package pl.domain.application.mixology

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import pl.domain.application.mixology.ui.theme.MixologyTheme
import pl.domain.application.mixology.Cocktail
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardColors
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.NoDrinks
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource


enum class SortOrder {
    NONE,
    ASCENDING,
    DESCENDING
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        val db by lazy { AppDatabase.getDatabase(applicationContext) }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MixologyTheme {

                val navController = rememberNavController()

                NavHost(navController, startDestination = "cocktailList"){
                    composable("cocktailList?isAlcoholicString={isAlcoholicString}"){ backStackEntry ->
                        val isAlcoholicString = backStackEntry.arguments?.getString("isAlcoholicString")
                        val isAlcoholic = when (isAlcoholicString) {
                            "true" -> true
                            "false" -> false
                            else -> null
                        }
                        CocktailsScreen(navController, isAlcoholic)
                    }
                    composable("cocktailDetail/{cocktailId}"){ backentry ->
                        val cocktailId = backentry.arguments?.getString("cocktailId")
                        if (cocktailId != null){
                            Log.d("CocktailDetail", "cocktailId: $cocktailId")
                            CocktailDetailScreen(cocktailId = cocktailId, navController= navController)
                        }
                    }

                    composable("favorites") {
                        CocktailsScreen(navController, isAlcoholicFilter = null, showOnlyFavorites = true)
                    }
                }
            }
        }
    }
}

fun FetchCocktails( onResult: (List<Cocktail>) -> Unit, onError: (Exception) -> Unit){
    val db = FirebaseFirestore.getInstance()
    db.collection("cocktails")
        .get()
        .addOnSuccessListener { snap ->
            val cocktails = snap.documents.mapNotNull{
                val cocktail = it.toObject(Cocktail::class.java)
                Log.d("Cocktail", "Alcoholic: ${cocktail?.Alcoholic}")
                cocktail
            }
            onResult(cocktails)
        }
        .addOnFailureListener{
            onError(it)
        }
}

@Composable
fun CocktailCard(
    cocktail: Cocktail,
    isFavorite: Boolean,
    onToggleFavorite: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .shadow(8.dp, shape = RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min) // <- ważne! sprawia, że karta dopasowuje się do wysokości treści
        ) {
            val context = LocalContext.current

            if (cocktail.ImageUrl.isNotBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        context.resources.getIdentifier(
                            cocktail.ImageUrl,
                            "drawable",
                            context.packageName
                        )
                    ),
                    contentDescription = cocktail.Name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(100.dp) // szerokość obrazka
                        .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                )
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = cocktail.Name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .width(1.dp)
                            .height(20.dp)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f))
                    )

                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .clickable { onToggleFavorite(!isFavorite) }
                            .size(22.dp)
                            .padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = cocktail.Description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Justify,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CocktailsScreen(
    navController: NavController,
    isAlcoholicFilter: Boolean? = null,
    showOnlyFavorites: Boolean = false
) {
    val cocktailsState = remember { mutableStateOf<List<Cocktail>>(emptyList()) }
    val errorState = remember { mutableStateOf<String?>(null) }
    val currentFilter = remember { mutableStateOf(isAlcoholicFilter) }
    val loadingCocktailsState = remember { mutableStateOf(true) }

    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).favoriteDao() }
    val scope = rememberCoroutineScope()
    val favoritesState = remember { mutableStateOf(emptySet<String>()) }

    val sortOrder = remember { mutableStateOf(SortOrder.NONE) }

    val searchQuery = remember { mutableStateOf("") }
    val isSearching = remember { mutableStateOf(false) }

    LaunchedEffect(showOnlyFavorites) {
        favoritesState.value = dao.getAll().map { it.cocktailId }.toSet()
    }

    LaunchedEffect(favoritesState.value, currentFilter.value, sortOrder.value) {
        loadingCocktailsState.value = true
        FetchCocktails(
            onResult = { cocktails ->
                val filtered = when {
                    showOnlyFavorites -> cocktails.filter { it.ID_Drink.toString() in favoritesState.value }
                    currentFilter.value == true -> cocktails.filter { it.Alcoholic == 1 }
                    currentFilter.value == false -> cocktails.filter { it.Alcoholic == 0 }
                    else -> cocktails
                }

                val sorted = when (sortOrder.value) {
                    SortOrder.ASCENDING -> filtered.sortedBy { it.Name }
                    SortOrder.DESCENDING -> filtered.sortedByDescending { it.Name }
                    else -> filtered
                }

                cocktailsState.value = sorted
                loadingCocktailsState.value = false
            },
            onError = { error ->
                errorState.value = error.localizedMessage
                loadingCocktailsState.value = false
            }
        )
    }

    if (loadingCocktailsState.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Center),
            )
        }
    } else if (errorState.value != null) {
        Text(text = "Error: ${errorState.value}", modifier = Modifier.padding(16.dp))
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        if (isSearching.value) {
                            androidx.compose.material3.TextField(
                                value = searchQuery.value,
                                onValueChange = { searchQuery.value = it },
                                placeholder = { Text("Szukaj drinka...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        } else {
                            Text(
                                text = "Mixology",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Szukaj",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clickable {
                                    isSearching.value = !isSearching.value
                                    if (!isSearching.value) {
                                        searchQuery.value = ""
                                    }
                                }
                                .padding(16.dp)
                        )

                        Icon(
                            imageVector = Icons.Default.SortByAlpha,
                            contentDescription = "Sortuj",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clickable {
                                    sortOrder.value = when (sortOrder.value) {
                                        SortOrder.NONE, SortOrder.DESCENDING -> SortOrder.ASCENDING
                                        SortOrder.ASCENDING -> SortOrder.DESCENDING
                                    }
                                }
                                .padding(16.dp)
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            navController.navigate("cocktailList?isAlcoholicString=null") {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        },
                        icon = { Icon(Icons.Default.List, contentDescription = "Lista", tint = MaterialTheme.colorScheme.onSurface) },
                        label = { Text("Drinki", color = MaterialTheme.colorScheme.onSurface) }
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            navController.navigate("cocktailList?isAlcoholicString=true") {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        },
                        icon = { Icon(Icons.Filled.LocalBar, contentDescription = "Alkoholowe", tint = MaterialTheme.colorScheme.onSurface) },
                        label = { Text("Alkoholowe", color = MaterialTheme.colorScheme.onSurface) }
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            navController.navigate("cocktailList?isAlcoholicString=false") {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.noalcohol),
                                contentDescription = "Bezalkoholowe",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(26.dp)
                            )
                               },
                        label = { Text("Bezalkoholowe", color = MaterialTheme.colorScheme.onSurface) }
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            navController.navigate("favorites") {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Ulubione", tint = MaterialTheme.colorScheme.onSurface) },
                        label = { Text("Ulubione", color = MaterialTheme.colorScheme.onSurface) }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                val displayedCocktails = cocktailsState.value.filter {
                    it.Name.contains(searchQuery.value, ignoreCase = true)
                }

                if (displayedCocktails.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(30.dp)) }

                        items(displayedCocktails) { cocktail ->
                            val isFav = favoritesState.value.contains(cocktail.ID_Drink.toString())
                            CocktailCard(
                                cocktail = cocktail,
                                isFavorite = isFav,
                                onToggleFavorite = { makeFav ->
                                    scope.launch {
                                        if (makeFav) {
                                            dao.insert(FavoriteCocktail(cocktail.ID_Drink.toString()))
                                        } else {
                                            dao.delete(FavoriteCocktail(cocktail.ID_Drink.toString()))
                                        }
                                        favoritesState.value = dao.getAll().map { it.cocktailId }.toSet()
                                    }
                                },
                                onClick = {
                                    navController.navigate("cocktailDetail/${cocktail.ID_Drink}")
                                }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(30.dp)) }
                    }
                } else {
                    Text(
                        text = "Brak wyników.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
