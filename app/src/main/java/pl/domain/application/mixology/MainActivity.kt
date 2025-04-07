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
import androidx.compose.material3.CardColors
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MixologyTheme {

                val navController = rememberNavController()

                NavHost(navController, startDestination = "cocktailList"){
                    composable("cocktailList"){
                        CocktailsScreen(navController)
                    }
                    composable("cocktailDetail/{cocktailId}"){ backentry ->
                        val cocktailId = backentry.arguments?.getString("cocktailId")
                        if (cocktailId != null){
                            Log.d("CocktailDetail", "cocktailId: $cocktailId")
                            CocktailDetailScreen(cocktailId = cocktailId, navController= navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FetchCocktails( onResult: (List<Cocktail>) -> Unit, onError: (Exception) -> Unit){
    val db = FirebaseFirestore.getInstance()
    db.collection("cocktails")
        .get()
        .addOnSuccessListener { snap ->
            val cocktails = snap.documents.mapNotNull{
                it.toObject(Cocktail::class.java)
            }
            onResult(cocktails)
        }
        .addOnFailureListener{
            onError(it)
        }
}

@Composable
fun CocktailCard(cocktail: Cocktail, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            if(cocktail.ImageUrl.isNotBlank()){
                Image(
                    painter = rememberAsyncImagePainter(cocktail.ImageUrl),
                    contentDescription = cocktail.Name,
                    modifier = Modifier
                        .width(100.dp)
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp)
            ) {

                Text(
                    text = cocktail.Name,
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    text = cocktail.Description,
                    textAlign = TextAlign.Justify,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CocktailsScreen(navController: NavController) {
    val cocktailsState = remember { mutableStateOf<List<Cocktail>>(emptyList()) }
    val errorState = remember { mutableStateOf<String?>(null) }
    val loadingState = remember { mutableStateOf(true) }

    FetchCocktails(
        onResult = { cocktails ->
            cocktailsState.value = cocktails
            loadingState.value = false
        },
        onError = { error ->
            errorState.value = error.localizedMessage
            loadingState.value = false
        }
    )

    // Loading State
    if (loadingState.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Center)
            )
        }
    } else if (errorState.value != null) {
        Text(text = "Error: ${errorState.value}", modifier = Modifier.padding(16.dp))
    } else {

        // Main code
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Mixology",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    // ...
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (cocktailsState.value.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 30.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                    ) {
                        items(cocktailsState.value) { cocktail ->
                            CocktailCard(cocktail) {
                                navController.navigate("cocktailDetail/${cocktail.ID_Drink}")
                            }
                        }
                    }
                } else {
                    Text(text = "No cocktails available", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}


