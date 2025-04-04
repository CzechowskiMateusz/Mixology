package pl.domain.application.mixology

import android.annotation.SuppressLint
import android.os.Bundle
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
import androidx.compose.material3.Scaffold
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
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale

class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MixologyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    CocktailsScreen()
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
fun CocktailCard(cocktail: Cocktail) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if(cocktail.ImageUrl.isNotBlank()){
                Image(
                    painter = rememberAsyncImagePainter(cocktail.ImageUrl),
                    contentDescription = cocktail.Name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
                Text(
                    text = cocktail.Name
                )

                Text(
                    text = cocktail.Description,
                    maxLines = 3
                )

        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CocktailsScreen() {
    val cocktailsState = remember { mutableStateOf<List<Cocktail>>(emptyList()) }
    val errorState = remember { mutableStateOf<String?>(null) }

    FetchCocktails(
        onResult = { cocktails ->
            cocktailsState.value = cocktails
        },
        onError = { error ->
            errorState.value = error.localizedMessage
        }
    )

    Scaffold(modifier = Modifier.fillMaxSize()) {
        if (errorState.value != null) {
            Text(text = "Error: ${errorState.value}", modifier = Modifier.padding(16.dp))
        } else

            if (cocktailsState.value.isNotEmpty()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    cocktailsState.value.forEach { cocktail ->
                        CocktailCard(cocktail)
                    }
                }
            } else {
                Text(text = "No cocktails available", modifier = Modifier.padding(16.dp))
            }
        }
    }


