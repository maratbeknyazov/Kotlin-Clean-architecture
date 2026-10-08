package uz.ttpu.movieshelf

import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uz.ttpu.movieshelf.domain.model.Movie
import uz.ttpu.movieshelf.domain.model.MoviesResult
import uz.ttpu.movieshelf.domain.repository.MovieRepository
import uz.ttpu.movieshelf.domain.usecase.GetFavoriteMoviesUseCase
import uz.ttpu.movieshelf.domain.usecase.GetMoviesUseCase
import uz.ttpu.movieshelf.domain.usecase.ToggleFavoriteUseCase
import uz.ttpu.movieshelf.presentation.movies.MovieListUiState
import uz.ttpu.movieshelf.presentation.movies.MovieListViewModel

private class ViewModelFakeRepository : MovieRepository {
    var loadCount = 0
    var fail = false
    private var movies = listOf(
        Movie(1, "A", 2020, 8.0, true),
        Movie(2, "B", 2021, 7.0),
    )

    override suspend fun getMovies(): MoviesResult {
        loadCount++
        if (fail) throw IOException("offline")
        return MoviesResult(movies, false)
    }

    override suspend fun toggleFavorite(movieId: Int): Boolean {
        val newFavorite = !movies.first { it.id == movieId }.isFavorite
        movies = movies.map { movie ->
            if (movie.id == movieId) movie.copy(isFavorite = newFavorite) else movie
        }
        return newFavorite
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MovieListViewModelTest {
    @Before
    fun setUp() {
        // В JVM-тесте нет Android main thread. Подставляем тестовый dispatcher.
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(repository: MovieRepository): MovieListViewModel {
        val getMovies = GetMoviesUseCase(repository)
        return MovieListViewModel(
            getMovies,
            ToggleFavoriteUseCase(repository),
            GetFavoriteMoviesUseCase(getMovies),
        )
    }

    @Test
    fun favorite_updates_without_network_reload() = runTest {
        val repository = ViewModelFakeRepository()
        val viewModel = createViewModel(repository)
        assertEquals(MovieListUiState.Loading, viewModel.state.value)
        runCurrent()

        viewModel.onFavoriteClick(2)
        runCurrent()

        val state = viewModel.state.value as MovieListUiState.Success
        assertTrue(state.movies.first { it.id == 2 }.isFavorite)
        assertTrue(state.movies.first { it.id == 1 }.isFavorite)
        assertEquals(1, repository.loadCount)
    }

    @Test
    fun removing_favorite_hides_movie_immediately_and_filter_can_be_disabled() = runTest {
        val repository = ViewModelFakeRepository()
        val viewModel = createViewModel(repository)
        runCurrent()
        viewModel.onFavoritesOnlyChange(true)
        runCurrent()
        val filtered = viewModel.state.value as MovieListUiState.Success
        assertTrue(filtered.favoritesOnly)
        assertEquals(listOf(1), filtered.movies.map { it.id })

        viewModel.onFavoriteClick(1)
        runCurrent()

        assertTrue((viewModel.state.value as MovieListUiState.Success).movies.isEmpty())
        assertEquals(2, repository.loadCount)
        viewModel.onFavoritesOnlyChange(false)
        runCurrent()
        val unfiltered = viewModel.state.value as MovieListUiState.Success
        assertEquals(2, unfiltered.movies.size)
        assertFalse(unfiltered.favoritesOnly)
        assertFalse(unfiltered.movies.first().isFavorite)
    }

    @Test
    fun retry_after_network_error_loads_movies() = runTest {
        val repository = ViewModelFakeRepository()
        repository.fail = true
        val viewModel = createViewModel(repository)
        runCurrent()
        assertTrue(viewModel.state.value is MovieListUiState.Error)

        repository.fail = false
        viewModel.onRefresh()
        assertEquals(MovieListUiState.Loading, viewModel.state.value)
        runCurrent()

        assertTrue(viewModel.state.value is MovieListUiState.Success)
    }
}
