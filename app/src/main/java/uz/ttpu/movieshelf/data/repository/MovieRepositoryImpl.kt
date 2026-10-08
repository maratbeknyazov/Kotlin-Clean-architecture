package uz.ttpu.movieshelf.data.repository

import java.io.IOException
import uz.ttpu.movieshelf.data.local.MovieLocalDataSource
import uz.ttpu.movieshelf.data.mapper.toDomain
import uz.ttpu.movieshelf.data.remote.MovieRemoteDataSource
import uz.ttpu.movieshelf.domain.model.MoviesResult
import uz.ttpu.movieshelf.domain.repository.MovieRepository

class MovieRepositoryImpl(
    private val remote: MovieRemoteDataSource,
    private val local: MovieLocalDataSource,
) : MovieRepository {
    override suspend fun getMovies(): MoviesResult {
        val favoriteIds = local.getFavoriteIds()
        val movies = try {
            remote.fetchMovies()
        } catch (error: IOException) {
            val cached = local.getCachedMovies() ?: throw error
            return MoviesResult(
                movies = cached.map { it.toDomain(it.id in favoriteIds) },
                isFromCache = true,
            )
        }

        // Свежие данные заменяют кеш. Ошибку сохранения не считаем ошибкой сети.
        local.saveMovies(movies)
        return MoviesResult(
            movies = movies.map { it.toDomain(it.id in favoriteIds) },
            isFromCache = false,
        )
    }

    override suspend fun toggleFavorite(movieId: Int): Boolean {
        val newFavorite = movieId !in local.getFavoriteIds()
        local.setFavorite(movieId, newFavorite)
        return newFavorite
    }
}
