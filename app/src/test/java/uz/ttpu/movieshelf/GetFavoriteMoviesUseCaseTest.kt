package uz.ttpu.movieshelf

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uz.ttpu.movieshelf.domain.model.Movie
import uz.ttpu.movieshelf.domain.model.MoviesResult
import uz.ttpu.movieshelf.domain.usecase.GetFavoriteMoviesUseCase
import uz.ttpu.movieshelf.domain.usecase.GetMoviesUseCase

class GetFavoriteMoviesUseCaseTest {
    @Test
    fun only_favorites_remain_sorted_and_cache_flag_is_preserved() = runBlocking<Unit> {
        val movies = listOf(
            Movie(1, "B", 2020, 8.0, true),
            Movie(2, "Not favorite", 2021, 10.0),
            Movie(3, "A", 2022, 8.0, true),
        )
        val repository = FakeMovieRepository(MoviesResult(movies, true))
        val useCase = GetFavoriteMoviesUseCase(GetMoviesUseCase(repository))

        val result = useCase()

        assertEquals(listOf("A", "B"), result.movies.map { it.title })
        assertTrue(result.movies.all { it.isFavorite })
        assertTrue(result.isFromCache)
    }

    @Test
    fun no_favorites_returns_empty_list() = runBlocking<Unit> {
        val movies = listOf(Movie(1, "A", 2020, 8.0))
        val repository = FakeMovieRepository(MoviesResult(movies, false))

        val result = GetFavoriteMoviesUseCase(GetMoviesUseCase(repository))()

        assertTrue(result.movies.isEmpty())
    }
}
