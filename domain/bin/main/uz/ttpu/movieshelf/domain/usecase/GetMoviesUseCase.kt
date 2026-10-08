package uz.ttpu.movieshelf.domain.usecase

import uz.ttpu.movieshelf.domain.model.Movie
import uz.ttpu.movieshelf.domain.model.MoviesResult
import uz.ttpu.movieshelf.domain.repository.MovieRepository

// В Task 5 зависимость от MovieRepositoryImpl нарушала бы Dependency Rule:
// внутренний domain зависел бы от внешнего data.
// После Task 6 используем интерфейс из domain. Data реализует этот интерфейс.
class GetMoviesUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(): MoviesResult {
        val result = repository.getMovies()
        val sortedMovies = result.movies.sortedWith(
            compareByDescending<Movie> { it.rating }.thenBy { it.title }
        )
        return result.copy(movies = sortedMovies)
    }
}
