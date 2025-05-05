package pl.domain.application.mixology

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import com.chargemap.compose.numberpicker.NumberPicker
import android.media.MediaPlayer
import android.provider.Telephony.Sms
import android.widget.Toast
import androidx.compose.material.icons.filled.Send
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri



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
fun TimerSect(){
    val minutes = remember { mutableStateOf(0) }
    val seconds = remember { mutableStateOf(10) }

    val totalSeconds = remember { mutableStateOf(1f)}
    val remains = remember { mutableStateOf((totalSeconds.value * 60).toInt()) }
    val isRuning = remember { mutableStateOf(false)}
    val hasStarted = remember { mutableStateOf(false)}

    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.alarm) }

    LaunchedEffect(isRuning.value) {
        if(isRuning.value){
            while (isRuning.value && remains.value > 0){
                delay(1000)
                remains.value -= 1
            }
            isRuning.value = false
            hasStarted.value = false
            mediaPlayer.start()
        }
    }

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        Column(horizontalAlignment = Alignment.CenterHorizontally){
            if(hasStarted.value){
                Text (
                    text = String.format(
                        "%02d:%02d",
                        remains.value/60,
                        remains.value%60
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if(!hasStarted.value){
                Row(
                    horizontalArrangement =  Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Column(horizontalAlignment = Alignment.CenterHorizontally){
                        Text("Minutes")
                        NumberPicker(
                            value = minutes.value,
                            range = 0..59,
                            onValueChange = {minutes.value = it}
                        )
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally){
                        Text("Seconds")
                        NumberPicker(
                            value = seconds.value,
                            range = 0..59,
                            onValueChange = {seconds.value = it}
                        )
                    }
                }
            }

        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){
            Button(onClick = {
                if(!hasStarted.value){
                    remains.value = (minutes.value * 60) + seconds.value
                    hasStarted.value = true
                }
                isRuning.value = true
            },
                enabled = (minutes.value > 0 || seconds.value > 0)
            ) {
                Text(if(hasStarted.value)"Resume" else "Start")
            }

            Button(onClick = {
                isRuning.value = false
                },
                enabled = isRuning.value
            ) {
                Text("Pause")
            }

            Button(onClick = {
                isRuning.value = false
                hasStarted.value = false
                remains.value = (minutes.value * 60) + seconds.value
                },
                enabled = hasStarted.value
            ) {
                Text("Stop")
            }
        }
    }
}



fun sendIngredientsSMS(ingredients: List<String>, context: Context) {
    val message = "Składniki: \n" + ingredients.joinToString("\n") { "• $it" }

    val smsUri = Uri.parse("smsto:")
    val intent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
        putExtra("sms_body", message)
    }

    context.startActivity(intent)
}


@OptIn(ExperimentalMaterial3Api::class)
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
        cocktailState.value?.let { cocktail ->

            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = cocktail.Name,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                navController.popBackStack()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                floatingActionButton = {
                    val context = LocalContext.current
                    FloatingActionButton(
                        onClick = {
                            sendIngredientsSMS(cocktail.List_of_mixture, context)
                        },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Send Ingredients"
                        )
                    }
                }
                ,
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {

                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {

                    LazyColumn(
                        modifier = Modifier
                            .padding(top = 0.dp, start = 50.dp, end = 50.dp, bottom = 0.dp)
                    ) {

                        if (cocktail.ImageUrl.isNotBlank()) {
                            item {
                                Spacer(modifier = Modifier.height(50.dp))

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

                        item {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = cocktail.Description,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .padding(bottom = 20.dp),
                                textAlign = TextAlign.Justify
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Składniki: ",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = cocktail.List_of_mixture
                                    .joinToString("\n") { "• $it" },
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        item{
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Minutnik",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            TimerSect()
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Sposób przygotowania: ",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = cocktail.Step_Guide
                                    .mapIndexed { index, step -> "${index + 1}. $step" }
                                    .joinToString("\n"),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(60.dp))
                        }

                    }
                }
            }
        }
    }
}

