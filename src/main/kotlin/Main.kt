import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.launch

private const val API_KEY = "0eefab4b89c14b4a92565355222604"

@Composable
fun WeatherScreen(repository: Repository) {
    var queriedCity by remember { mutableStateOf("") }
    var weatherState by remember { mutableStateOf<Lce<WeatherResults>?>(null) }
    val scope = rememberCoroutineScope()


    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            TextField(
                value = queriedCity,
                onValueChange = { queriedCity = it },
                modifier = Modifier.padding(end = 16.dp),
                placeholder = { Text("Any city, really...") },
                label = { Text(text = "Search for a city") },
                leadingIcon = { Icon(Icons.Filled.LocationOn, "Location") },
            )
            Button(onClick = {
                weatherState = Lce.Loading
                scope.launch {
                    weatherState = repository.weatherForCity(queriedCity)
                }
            }) {
                Icon(Icons.Outlined.Search, "Search")
            }
        }
        when (val state = weatherState) {
            is Lce.Loading -> LoadingUI()
            is Lce.Error -> Unit
            is Lce.Content -> ContentUI(state.data, queriedCity)
        }
    }

}

@Composable
fun ContentUI(data: WeatherResults, queriedCity: String) {
    var imageState by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(data.currentWeather.iconUrl) {
        imageState = ImageDownloader.downloadImage(data.currentWeather.iconUrl)
    }
    Text(
        text = "Current weather in $queriedCity",
        modifier = Modifier.padding(all = 16.dp),
        style = MaterialTheme.typography.h6,
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 72.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = data.currentWeather.condition,
                style = MaterialTheme.typography.h6,
            )

            imageState?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 128.dp, minHeight = 128.dp)
                        .padding(top = 8.dp)
                )
            }

            Text(
                text = "Temperature in °C: ${data.currentWeather.temperature}",
                modifier = Modifier.padding(all = 8.dp),
            )
            Text(
                text = "Feels like: ${data.currentWeather.feelsLike}",
                style = MaterialTheme.typography.caption,
            )
        }
    }

}

@Composable
fun LoadingUI() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier = Modifier.align(alignment = Alignment.Center).defaultMinSize(minWidth = 96.dp, minHeight = 96.dp)
        )
    }
}


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose for Desktop",
        state = rememberWindowState(width = 800.dp, height = 600.dp)
    ) {
        val repository = Repository(API_KEY)
        MaterialTheme {
            WeatherScreen(repository)
        }
    }
}