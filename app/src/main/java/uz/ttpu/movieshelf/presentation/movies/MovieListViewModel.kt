package uz.ttpu.movieshelf.presentation.movies

/*
Task 15. Порядок вызовов при нажатии на сердце:
MovieItem -> MovieListViewModel -> ToggleFavoriteUseCase ->
MovieRepositoryImpl -> SharedPrefsMovieLocalDataSource.

Зависимости исходного кода:
MovieItem использует модель Movie и переданный callback, а не ViewModel.
Activity связывает callback с MovieListViewModel.
MovieListViewModel -> ToggleFavoriteUseCase -> MovieRepository (интерфейс).
MovieRepositoryImpl -> MovieRepository и MovieLocalDataSource (интерфейсы).
SharedPrefsMovieLocalDataSource -> MovieLocalDataSource.

Use case вызывает метод репозитория, но реализация репозитория зависит
от интерфейса в domain. Здесь направление зависимости противоположно
направлению вызова. То же происходит между репозиторием и локальным источником:
репозиторий знает интерфейс, а конкретный источник его реализует.

В domain нельзя вызвать Log.d: в его classpath нет Android.
Чистый вариант: объявить интерфейс Logger в domain и передать реализацию
из внешнего слоя. Для временной проверки можно использовать println.
В финальном коде временные логи удалены.
*/

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.ttpu.movieshelf.domain.usecase.GetFavoriteMoviesUseCase
import uz.ttpu.movieshelf.domain.usecase.GetMoviesUseCase
import uz.ttpu.movieshelf.domain.usecase.ToggleFavoriteUseCase

class MovieListViewModel(
    private val getMovies: GetMoviesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val getFavoriteMovies: GetFavoriteMoviesUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<MovieListUiState>(MovieListUiState.Loading)
    val state: StateFlow<MovieListUiState> = _state.asStateFlow()
    private var loadJob: Job? = null
    private var favoritesOnly: Boolean = false

    init {
        load()
    }

    fun onRefresh() {
        load()
    }

    fun onFavoritesOnlyChange(enabled: Boolean) {
        favoritesOnly = enabled
        load()
    }

    fun onFavoriteClick(movieId: Int) {
        if (_state.value !is MovieListUiState.Success) return

        viewModelScope.launch {
            val isFavorite = toggleFavorite(movieId)
            val current = _state.value
            if (current is MovieListUiState.Success) {
                val updatedMovies = current.movies.map { movie ->
                    if (movie.id == movieId) movie.copy(isFavorite = isFavorite) else movie
                }
                val visibleMovies = if (current.favoritesOnly) {
                    updatedMovies.filter { it.isFavorite }
                } else {
                    updatedMovies
                }
                // Обновляем список сразу, без запроса к сети.
                _state.value = current.copy(movies = visibleMovies)
            }
        }
    }

    private fun load() {
        // Быстрое повторное Refresh отменяет предыдущую загрузку.
        loadJob?.cancel()
        _state.value = MovieListUiState.Loading
        val onlyFavorites = favoritesOnly
        loadJob = viewModelScope.launch {
            try {
                val result = if (onlyFavorites) getFavoriteMovies() else getMovies()
                _state.value = MovieListUiState.Success(
                    movies = result.movies,
                    isFromCache = result.isFromCache,
                    favoritesOnly = onlyFavorites,
                )
            } catch (error: IOException) {
                _state.value = MovieListUiState.Error(
                    "Can't load movies. Check your connection and try again."
                )
            }
        }
    }
}
