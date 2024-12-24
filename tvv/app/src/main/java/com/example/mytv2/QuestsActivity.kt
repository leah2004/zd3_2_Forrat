package com.example.mytv2
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import com.example.mytv2.Movie
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.squareup.picasso.Picasso

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

data class MovieResponse(
    @SerializedName("docs") val docs: List<Movie>
)

data class Movie(
    @SerializedName("alternativeName") val alternativeName: String,
    @SerializedName("year") val year: Int,
    @SerializedName("genres") val genres: List<Genre>,
    @SerializedName("poster") val poster: Poster
)

data class Genre(
    @SerializedName("name") val name: String
)

data class Poster(
    @SerializedName("url") val url: String
)

class QuestsActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    private val gson = Gson()
    private lateinit var gridLayout: GridLayout
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var genreSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quests)

        gridLayout = findViewById(R.id.gridLayout)
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        genreSpinner = findViewById(R.id.genreSpinner)

        // Устанавливаем жанры
        val genres = listOf("драма", "комедия", "аниме")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genreSpinner.adapter = adapter

        // Обработчик выбора жанра
        genreSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedGenre = genres[position]
                fetchMovies(selectedGenre)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Обработчик кнопки поиска
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            if (query.isNotEmpty()) {
                searchMovie(query)
            } else {
                Toast.makeText(this, "Введите название фильма", Toast.LENGTH_SHORT).show()
            }
        }
    }

   /* private fun fetchMoviesByGenre(genre: String) {
        gridLayout.removeAllViews()

        // Добавляем временную метку к запросу
        val timestamp = System.currentTimeMillis()
        val request = Request.Builder()
            .url("https://api.kinopoisk.dev/v1.4/movie/search?page=1&limit=10&genres.name=$genre&timestamp=$timestamp")
            .get()
            .addHeader("accept", "application/json")
            .addHeader("X-API-KEY", "YF53VNS-HXAMCHH-GYG66Q5-GZ7JKZD") // Вставьте ваш токен сюда
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("QuestsActivity", "Failed to fetch data", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    val movieResponse = gson.fromJson(responseData, MovieResponse::class.java)

                    runOnUiThread {
                        *//*displayMovies(movieResponse.docs)*//*
                    }
                } else {
                    Log.e("QuestsActivity", "Response error: ${response.code}")
                }
            }
        })
    }*/

    private fun fetchMovies(selectedGenre: String) {
        gridLayout.removeAllViews() // Очистка GridLayout перед добавлением новых элементов

        val itemWidth = 1200 / 2 // Ширина для 3 колонок
        val itemHeight = 600// Высота для 2 строк

        val request = Request.Builder()
            .url("https://api.kinopoisk.dev/v1.4/movie/search?page=1&limit=10&genres.name=${selectedGenre}") // Добавление жанра в запрос
            .get()
            .addHeader("accept", "application/json")
            .addHeader("X-API-KEY", "YF53VNS-HXAMCHH-GYG66Q5-GZ7JKZD")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("QuestsActivity", "Failed to fetch data", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    Log.d("QuestsActivity", "Response: $responseData") // Логирование ответа

                    // Десериализация JSON в объект MovieResponse
                    val movieResponse = gson.fromJson(responseData, MovieResponse::class.java)

                    runOnUiThread {
                        // Проверка на наличие фильмов и фильтрация по жанрам
                        var rows = 0
                        var columns = 0

                        movieResponse.docs.forEach { movie ->
                            Log.d("QuestsActivity", "Response: ${movie.genres}")

                            if (selectedGenre == "All" || movie.genres.any {
                                    it.name.equals(
                                        selectedGenre,
                                        ignoreCase = true
                                    )
                                }) {
                                val linearLayout = LinearLayout(this@QuestsActivity).apply {
                                    orientation = LinearLayout.VERTICAL
                                    layoutParams = GridLayout.LayoutParams().apply {
                                        rowSpec = GridLayout.spec(rows)
                                        columnSpec = GridLayout.spec(columns)
                                    }
                                    setPadding(16, 16, 16, 16)
                                    setBackgroundResource(R.drawable.ramka)
                                    val layoutParams = layoutParams as GridLayout.LayoutParams
                                    layoutParams.setMargins(8, 8, 8, 8)
                                }

                                val imageView = ImageView(this@QuestsActivity).apply {
                                    layoutParams = LinearLayout.LayoutParams(itemWidth, itemHeight)
                                    scaleType = ImageView.ScaleType.FIT_CENTER
                                    movie.poster?.let { // Используйте movie.poster напрямую
                                        Picasso.get().load(it.url)
                                            .into(this) // Загрузка изображения с помощью Picasso
                                    }
                                }

                                linearLayout.addView(imageView)

                                val titleTextView = TextView(this@QuestsActivity).apply {
                                    text = movie.alternativeName // Используйте alternativeName
                                    setTextColor(Color.parseColor("#FFFFFF"))
                                    layoutParams =
                                        LinearLayout.LayoutParams(itemWidth, itemHeight / 10)
                                    gravity = Gravity.CENTER
                                }

                                val yearTextView = TextView(this@QuestsActivity).apply {
                                    text = movie.year.toString()
                                    setTextColor(Color.parseColor("#FFFFFF"))
                                    layoutParams =
                                        LinearLayout.LayoutParams(itemWidth, itemHeight / 10)
                                    gravity = Gravity.CENTER
                                }

                                linearLayout.addView(titleTextView)
                                linearLayout.addView(yearTextView)

                                gridLayout.addView(linearLayout)

                                columns += 1
                                if (columns == 3) {
                                    rows += 1
                                    columns = 0
                                }
                            }
                        }
                    }
                } else {
                    Log.e("QuestsActivity", "Response error: ${response.code}")
                }
            }
        })
    }
    private fun searchMovie(query: String) {
        gridLayout.removeAllViews()

        val request = Request.Builder()
            .url("https://api.kinopoisk.dev/v1.4/movie/search?page=1&limit=1&name=$query")
            .get()
            .addHeader("accept", "application/json")
            .addHeader("X-API-KEY", "YF53VNS-HXAMCHH-GYG66Q5-GZ7JKZD")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("QuestsActivity", "Failed to fetch data", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    val movieResponse = gson.fromJson(responseData, MovieResponse::class.java)

                    runOnUiThread {
                        if (movieResponse.docs.isNotEmpty()) {
                            /*displayMovies(movieResponse.docs)*/
                        } else {
                            Toast.makeText(this@QuestsActivity, "Фильм не найден", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("QuestsActivity", "Response error: ${response.code}")
                }
            }
        })
    }

    /*private fun displayMovies(movies: List<Movie>) {
        gridLayout.removeAllViews()

        movies.forEach { movie ->
            val linearLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = GridLayout.LayoutParams().apply {
                    width = resources.displayMetrics.widthPixels / 3
                    height = resources.displayMetrics.heightPixels / 3
                }
                setPadding(8, 8, 8, 8)
            }

            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    400
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
                Picasso.get().load(movie.poster.url).into(this)
            }

            val titleTextView = TextView(this).apply {
                text = movie.alternativeName
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
            }

            linearLayout.addView(imageView)
            linearLayout.addView(titleTextView)

            gridLayout.addView(linearLayout)
        }
    }*/
}