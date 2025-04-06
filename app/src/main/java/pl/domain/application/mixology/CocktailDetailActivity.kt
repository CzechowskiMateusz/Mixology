package pl.domain.application.mixology

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import pl.domain.application.mixology.ui.theme.MixologyTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.text.style.TextAlign

class CocktailDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MixologyTheme {
                val navController = rememberNavController()

                val cocktailId = intent.getStringExtra("cocktailId")
                if (cocktailId != null) {
                    CocktailDetailScreen(cocktailId = cocktailId, navController = navController)
                }
            }
        }
    }
}

@Composable
fun FetchCocktailById(cocktailId: String, onResult: (Cocktail) -> Unit, onError: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val cocktailState = remember { mutableStateOf<Cocktail?>(null) }

    val id = cocktailId.toInt()

    db.collection("cocktails")
        .whereEqualTo("ID_Drink", id)
        .get()
        .addOnSuccessListener { snap ->
            val cocktail = snap.documents.firstOrNull()?.toObject(Cocktail::class.java)
            Log.d("CocktailDetail", "ck: $cocktail")
            cocktailState.value = cocktail
            onResult(cocktail ?: Cocktail())
        }
        .addOnFailureListener { exception ->
            onError(exception)
        }

    cocktailState.value?.let { cocktail ->
        onResult(cocktail)
    }
}

@Composable
fun CocktailDetailScreen(cocktailId: String, navController: NavController) {
    val cocktailState = remember { mutableStateOf<Cocktail?>(null) }
    val errorState = remember { mutableStateOf<String?>(null) }

    FetchCocktailById(
        cocktailId = cocktailId,
        onResult = { cocktail ->
            cocktailState.value = cocktail
        },
        onError = { error ->
            errorState.value = error.localizedMessage
        }
    )

    if (cocktailState.value == null) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        ){
            CircularProgressIndicator(modifier = Modifier
                .padding(16.dp)
                .align(Alignment.Center)
            )
        }
    } else if (errorState.value != null) {
        Text(text = "Error: ${errorState.value}", modifier = Modifier.padding(16.dp))
    } else {
        cocktailState.value?.let { cocktail ->
            LazyColumn(modifier = Modifier
                .padding(20.dp)
            ) {
                item{
                    Row(modifier = Modifier
                            .padding(top=10.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ){
                        Text(
                            text = cocktail.Name,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                if (cocktail.ImageUrl.isNotBlank()) {
                    item{
                        Image(
                            painter = rememberAsyncImagePainter(cocktail.ImageUrl),
                            contentDescription = cocktail.Name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                item{
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = cocktail.Description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(bottom = 20.dp),
                        textAlign = TextAlign.Justify
                    )
                }

                item{
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Składniki: ",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = cocktail.List_of_mixture
                            .joinToString("\n") { "• $it"},
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                item{
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Sposób przygotowania: ",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = cocktail.Step_Guide
                            .mapIndexed{ index, step -> "${index+1}. $step" }
                            .joinToString("\n"),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                item{
                    Button(onClick = {
                        navController.popBackStack()
                    }) {
                        Text(text = "Back to List")
                    }
                }
            }
        }
    }
}


