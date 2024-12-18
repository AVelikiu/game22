package com.example.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlin.random.Random
import androidx.compose.foundation.background

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Navigation()
        }
    }
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val records = remember { mutableStateListOf<Int>() } // Список для хранения рекордов
    var backgroundColor by remember { mutableStateOf(Color.White) }

    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(navController, backgroundColor) }
        composable("game") { MemoryGameScreen(navController, records,backgroundColor) }
        composable("settings") {
            SettingsScreen(backgroundColor) { newColor ->
                backgroundColor = newColor
            }
        }
        composable("records") { RecordsScreen(records,backgroundColor) }
    }
}

@Composable
fun HomeScreen(navController: NavController, backgroundColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor), // Установка фона
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Memory Game", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("game") }) {
            Text("Start Game")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("settings") }) {
            Text("Settings")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("records") }) {
            Text("Records")
        }
    }
}

@Composable
fun MemoryGameScreen(navController: NavController, records: MutableList<Int>, backgroundColor: Color) {
    var movesCount by remember { mutableStateOf(0) }
    val images = remember { mutableStateListOf(*createShuffledImagePairs().toTypedArray()) } // Изменяемый список
    val revealedCards = remember { mutableStateListOf<Int>() }
    var firstSelectedIndex by remember { mutableStateOf<Int?>(null) }
    var secondSelectedIndex by remember { mutableStateOf<Int?>(null) }
    var mismatch by remember { mutableStateOf(false) }
    var isGameFinished by remember { mutableStateOf(false) }

    if (mismatch) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1000)
            revealedCards.remove(firstSelectedIndex!!)
            revealedCards.remove(secondSelectedIndex!!)
            firstSelectedIndex = null
            secondSelectedIndex = null
            mismatch = false
        }
    }

    // Проверка завершения игры
    if (revealedCards.size == images.size && !isGameFinished) {
        isGameFinished = true
        records.add(movesCount)
    }

    if (isGameFinished) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Congratulations!") },
            text = { Text("Game finished in $movesCount moves!") },
            confirmButton = {
                Button(onClick = { navController.navigate("home") }) {
                    Text("Back to Home")
                }
            },
            dismissButton = {
                Button(onClick = {
                    movesCount = 0
                    firstSelectedIndex = null
                    secondSelectedIndex = null
                    mismatch = false
                    isGameFinished = false
                    revealedCards.clear()
                    images.clear() // Очистить старые изображения
                    images.addAll(createShuffledImagePairs()) // Пересоздать изображения
                }) {
                    Text("Play Again")
                }
            }
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize() .background(backgroundColor),
    ) {
        Text(
            text = "Moves: $movesCount",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            modifier = Modifier.padding(16.dp)
        ) {
            items(images.size) { index ->
                val imageRes = images[index]
                val isRevealed = revealedCards.contains(index)
                Card(
                    imageRes = if (isRevealed) imageRes else R.drawable.close,
                    onClick = {
                        if (firstSelectedIndex == null) {
                            firstSelectedIndex = index
                            revealedCards.add(index)
                        } else if (secondSelectedIndex == null && index != firstSelectedIndex) {
                            secondSelectedIndex = index
                            revealedCards.add(index)
                            movesCount++
                            if (images[firstSelectedIndex!!] != images[secondSelectedIndex!!]) {
                                mismatch = true
                            } else {
                                firstSelectedIndex = null
                                secondSelectedIndex = null
                            }
                        }
                    }
                )
            }
        }
        Button(onClick = { navController.navigate("home") }) {
            Text("Back to Home")
        }
    }
}

@Composable
fun Card(imageRes: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .padding(4.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}

fun createShuffledImagePairs(): List<Int> {
    val images = listOf(
        R.drawable.animal25, R.drawable.animal24, R.drawable.animal23, R.drawable.animal22, R.drawable.animal26, R.drawable.animal27, R.drawable.animal28,
    )
    return (images + images).shuffled()
}

@Composable
fun SettingsScreen(backgroundColor: Color, onChangeColor: (Color) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor), // Установка фона
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onChangeColor(Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat())) }) {
            Text("Change Background Color")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /* Change Image Set */ }) {
            Text("Change Image Set")
        }
    }
}

@Composable
fun RecordsScreen(records: List<Int>,backgroundColor: Color) {
    Column(
        modifier = Modifier.fillMaxSize() .background(backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Records", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        if (records.isEmpty()) {
            Text("No records available")
        } else {
            records.forEachIndexed { index, record ->
                Text("Game ${index + 1}: $record moves")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Navigation()
}