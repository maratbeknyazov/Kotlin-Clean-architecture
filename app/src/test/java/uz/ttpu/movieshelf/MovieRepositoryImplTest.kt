package uz.ttpu.movieshelf

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uz.ttpu.movieshelf.data.local.InMemoryMovieLocalDataSource
import uz.ttpu.movieshelf.data.remote.MovieDto
import uz.ttpu.movieshelf.data.remote.MovieRemoteDataSource
import uz.ttpu.movieshelf.data.repository.MovieRepositoryImpl

private class FakeRemote(
    var movies: List<MovieDto> = emptyList(),
    var fail: Boolean = false,
) : MovieRemoteDataSource {
    override suspend fun fetchMovies(): List<MovieDto> {
        if (fail) throw IOException("offline")
        return movies
    }
}

class MovieRepositoryImplTest {
    private val dtos = listOf(
        MovieDto(1, "A", 2020, 8.0),
        MovieDto(2, "B", 2021, 7.0),
    )

    @Test
    fun online_returns_fresh_data_with_favorite_flags() = runBlocking<Unit> {
        val local = InMemoryMovieLocalDataSource()
        local.setFavorite(2, true)
        val repository = MovieRepositoryImpl(FakeRemote(dtos), local)

        val result = repository.getMovies()

        assertFalse(result.isFromCache)
        assertEquals(listOf(false, true), result.movies.map { it.isFavorite })
        assertEquals(2020, result.movies.first().year)
        assertEquals(8.0, result.movies.first().rating, 0.0)
        assertEquals(dtos, local.getCachedMovies())
    }

    @Test
    fun offline_after_a_successful_load_returns_cache() = runBlocking<Unit> {
        val local = InMemoryMovieLocalDataSource()
        val remote = FakeRemote(dtos)
        val repository = MovieRepositoryImpl(remote, local)
        val online = repository.getMovies()
        local.setFavorite(1, true)
        remote.fail = true

        val offline = repository.getMovies()

        assertTrue(offline.isFromCache)
        assertEquals(online.movies.size, offline.movies.size)
        assertEquals(listOf("A", "B"), offline.movies.map { it.title })
        assertTrue(offline.movies.first().isFavorite)
    }

    @Test(expected = IOException::class)
    fun offline_with_empty_cache_throws() = runBlocking<Unit> {
        val repository = MovieRepositoryImpl(FakeRemote(fail = true), InMemoryMovieLocalDataSource())
        repository.getMovies()
    }

    @Test
    fun toggle_returns_new_state() = runBlocking<Unit> {
        val repository = MovieRepositoryImpl(FakeRemote(dtos), InMemoryMovieLocalDataSource())

        assertTrue(repository.toggleFavorite(1))
        assertFalse(repository.toggleFavorite(1))
    }

    @Test
    fun fresh_data_replaces_old_cache() = runBlocking<Unit> {
        val local = InMemoryMovieLocalDataSource()
        local.saveMovies(listOf(MovieDto(99, "Old", 2000, 1.0)))
        val repository = MovieRepositoryImpl(FakeRemote(dtos), local)

        val result = repository.getMovies()

        assertFalse(result.isFromCache)
        assertEquals(listOf(1, 2), result.movies.map { it.id })
        assertEquals(dtos, local.getCachedMovies())
    }

    @Test(expected = CancellationException::class)
    fun cancellation_is_not_replaced_with_cache() = runBlocking<Unit> {
        val remote = object : MovieRemoteDataSource {
            override suspend fun fetchMovies(): List<MovieDto> {
                throw CancellationException("Request cancelled")
            }
        }
        val local = InMemoryMovieLocalDataSource()
        local.saveMovies(dtos)
        MovieRepositoryImpl(remote, local).getMovies()
    }
}
