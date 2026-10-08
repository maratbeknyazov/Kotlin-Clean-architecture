package uz.ttpu.movieshelf.data.remote

// suspend позволяет ждать I/O без блокировки главного потока.
// Для настоящего блокирующего I/O внутри нужен Dispatchers.IO.
interface MovieRemoteDataSource {
    suspend fun fetchMovies(): List<MovieDto>
}
