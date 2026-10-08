package uz.ttpu.movieshelf.presentation.movies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uz.ttpu.movieshelf.domain.model.Movie

// Экран получает состояние и события. Данные сам он не загружает.
@Composable
fun MovieListScreen(
    state: MovieListUiState,
    onFavoriteClick: (Int) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        MovieListUiState.Loading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is MovieListUiState.Error -> {
            Column(
                modifier = modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(state.message)
                Button(onClick = onRefresh, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Retry")
                }
            }
        }
        is MovieListUiState.Success -> {
            Column(modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "MovieShelf",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    TextButton(onClick = onRefresh) { Text("Refresh") }
                }
                if (state.isFromCache) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Offline — showing saved data", Modifier.padding(16.dp))
                    }
                }
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(state.movies, key = { it.id }) { movie ->
                        MovieItem(movie, onFavoriteClick = { onFavoriteClick(movie.id) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun MovieItem(movie: Movie, onFavoriteClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(movie.title, fontWeight = FontWeight.Bold)
            Text("${movie.year} · Rating ${movie.rating}")
        }
        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (movie.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (movie.isFavorite) {
                    "Remove ${movie.title} from favorites"
                } else {
                    "Add ${movie.title} to favorites"
                },
                tint = if (movie.isFavorite) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val previewMovies = listOf(
    Movie(6, "Echoes of Khiva", 2022, 9.0, true),
    Movie(3, "Registan Rhapsody", 2018, 8.4),
    Movie(1, "The Silk Road Express", 2019, 8.4),
)

@Preview(showBackground = true, name = "Loading", widthDp = 360, heightDp = 640)
@Composable
private fun LoadingPreview() {
    MovieShelfTheme { MovieListScreen(MovieListUiState.Loading, {}, {}) }
}

@Preview(showBackground = true, name = "Success", widthDp = 360, heightDp = 640)
@Composable
private fun SuccessPreview() {
    MovieShelfTheme { MovieListScreen(MovieListUiState.Success(previewMovies, false), {}, {}) }
}

@Preview(showBackground = true, name = "Success from cache", widthDp = 360, heightDp = 640)
@Composable
private fun CachedPreview() {
    MovieShelfTheme { MovieListScreen(MovieListUiState.Success(previewMovies, true), {}, {}) }
}

@Preview(showBackground = true, name = "Error", widthDp = 360, heightDp = 640)
@Composable
private fun ErrorPreview() {
    MovieShelfTheme {
        MovieListScreen(
            MovieListUiState.Error("Can't load movies. Check your connection and try again."), {}, {}
        )
    }
}
