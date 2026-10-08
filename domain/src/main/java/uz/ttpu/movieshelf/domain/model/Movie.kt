package uz.ttpu.movieshelf.domain.model

data class Movie(
    val id: Int,
    val title: String,
    val year: Int,
    val rating: Double,
    val isFavorite: Boolean = false,
)
