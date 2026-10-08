package uz.ttpu.movieshelf.data.mapper

import uz.ttpu.movieshelf.data.remote.MovieDto
import uz.ttpu.movieshelf.domain.model.Movie

// Mapper находится в data, потому что domain не должен знать о DTO.
fun MovieDto.toDomain(isFavorite: Boolean): Movie {
    return Movie(
        id = id,
        title = title,
        year = releaseYear,
        rating = score,
        isFavorite = isFavorite,
    )
}
