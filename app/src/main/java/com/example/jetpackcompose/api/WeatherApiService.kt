package com.example.jetpackcompose.api

import android.util.Log
import com.example.jetpackcompose.data.ForecastData
import com.example.jetpackcompose.data.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object WeatherApiService {
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(WeatherApi::class.java)

    interface WeatherApi {
        @GET("weather")
        suspend fun fetchWeather(
            @Query("q") city: String,
            @Query("appid") apiKey: String,
            @Query("units") units: String = "metric"
        ): retrofit2.Response<WeatherData>

        @GET("forecast")
        suspend fun fetchForecast(
            @Query("q") city: String,
            @Query("appid") apiKey: String,
            @Query("units") units: String = "metric"
        ): retrofit2.Response<ForecastData>
    }

    suspend fun fetchWeather(city: String, apiKey: String): WeatherData? {
        return try {
            withContext(Dispatchers.Default) {
                val response = api.fetchWeather(city, apiKey)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    Log.e("WeatherApiService", "Failed to fetch data: ${response.code()}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("WeatherApiService", "Error fetching data: ${e.message}")
            null
        }
    }

    // fetches weather forecast data using the provided city and API key and returns ForecastData or null (if fetching failed)
    suspend fun fetchForecast(city: String, apiKey: String): ForecastData? {
        return try {
            // network request in the IO dispatcher to avoid blocking the main thread
            withContext(Dispatchers.IO) {
                // the reponse is the result fetched with the method fetchForecast using the given city and API key
                val response = api.fetchForecast(city, apiKey)
                // if the response is successfull then the response with the date is returned
                if (response.isSuccessful) {
                    response.body()
                } else { // if the response is unsuccessful then it will be logged and null will be returned
                    Log.e("WeatherApiService", "Failed to fetch forecast: ${response.code()}")
                    null
                }
            }
        } catch (e: Exception) { // if and exception occurs during fetching then it will be logged and null will be returned
            Log.e("WeatherApiService", "Error fetching forecast: ${e.message}")
            null
        }
    }
}
