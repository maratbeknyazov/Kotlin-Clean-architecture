package uz.ttpu.movieshelf.domain.model

data class MoviesResult(
    val movies: List<Movie>,
    val isFromCache: Boolean,
)
