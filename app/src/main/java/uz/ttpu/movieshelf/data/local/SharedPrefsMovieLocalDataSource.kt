package uz.ttpu.movieshelf.data.local

import android.content.Context
import uz.ttpu.movieshelf.data.remote.MovieDto

class SharedPrefsMovieLocalDataSource(context: Context) : MovieLocalDataSource {
    private val prefs = context.getSharedPreferences("movieshelf", Context.MODE_PRIVATE)
    private var cached: List<MovieDto>? = null

    override suspend fun getCachedMovies(): List<MovieDto>? = cached

    override suspend fun saveMovies(movies: List<MovieDto>) {
        cached = movies.toList()
    }

    override suspend fun getFavoriteIds(): Set<Int> {
        val saved = prefs.getStringSet("favorites", emptySet()) ?: emptySet()
        return saved.mapNotNull { it.toIntOrNull() }.toSet()
    }

    override suspend fun setFavorite(id: Int, favorite: Boolean) {
        // Нельзя менять Set, который вернул SharedPreferences. Сначала копируем.
        val saved = prefs.getStringSet("favorites", emptySet()) ?: emptySet()
        val newFavorites = saved.toMutableSet()
        if (favorite) {
            newFavorites.add(id.toString())
        } else {
            newFavorites.remove(id.toString())
        }
        prefs.edit().putStringSet("favorites", newFavorites).apply()
    }
}
