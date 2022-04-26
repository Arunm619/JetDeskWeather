import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*

class Repository(
  private val apiKey: String,
  private val client: HttpClient = defaultHttpClient,
) {
  private val transformer = WeatherTransformer()
  private suspend fun getWeatherForCity(city: String): WeatherResponse =
    client.get(
      "https://api.weatherapi.com/v1/forecast.json" +
          "?key=$apiKey" +
          "&q=$city" +
          "&days=5" +
          "&aqi=no" +
          "&alerts=no"
    )

  suspend fun weatherForCity(city: String): Lce<WeatherResults> {
    return try {
      val result = getWeatherForCity(city)
      val content = transformer.transform(result)
      Lce.Content(content)
    } catch (e: Exception) {
      e.printStackTrace()
      Lce.Error(e)
    }
  }


  companion object {
    val defaultHttpClient = HttpClient(CIO) {
      install(JsonFeature) {
        serializer = KotlinxSerializer(
          json = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
          }
        )
      }
    }
  }
}
