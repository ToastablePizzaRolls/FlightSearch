package com.example.flightsearch.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flightsearch.data.Airport
import com.example.flightsearch.data.Favorite
import com.example.flightsearch.ui.theme.FlightSearchTheme

/**
 * Main composable that displays the flight search screen.
 *
 * @param uiState The current state of the UI.
 * @param onQueryChange Callback invoked when the search query changes.
 * @param onAirportSelect Callback invoked when an airport is selected from suggestions.
 * @param onClearSearch Callback invoked when the search query is cleared.
 * @param onToggleFavorite Callback invoked when a favorite route is toggled.
 * @param isFavoriteRoute Function to check if a given route is in the favorites.
 * @param modifier Modifier for this composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightSearchScreen(
    uiState: FlightUiState,
    onQueryChange: (String) -> Unit,
    onAirportSelect: (Airport) -> Unit,
    onClearSearch: () -> Unit,
    onToggleFavorite: (String, String) -> Unit,
    isFavoriteRoute: (String, String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }
    val showSuggestions = isFocused
            && uiState.suggestions.isNotEmpty()
            && uiState.selectedAirport == null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "✈ Flight Search",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onQueryChange,
                placeholder = { Text("Enter departure airport or city") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            focusManager.clearFocus()
                            onClearSearch()
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(visible = showSuggestions, enter = fadeIn(), exit = fadeOut()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    LazyColumn {
                        items(uiState.suggestions) { airport ->
                            AirportSuggestionItem(
                                airport = airport,
                                query = uiState.searchQuery,
                                onClick = {
                                    focusManager.clearFocus()
                                    onAirportSelect(airport)
                                }
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }

            when {
                uiState.selectedAirport != null -> {
                    FlightResultsSection(
                        departure = uiState.selectedAirport,
                        destinations = uiState.destinations,
                        isFavoriteRoute = isFavoriteRoute,
                        onToggleFavorite = onToggleFavorite
                    )
                }
                uiState.showingFavorites -> {
                    if (uiState.favorites.isNotEmpty()) {
                        FavoritesSection(
                            favorites = uiState.favorites,
                            allAirports = uiState.allAirports,
                            isFavoriteRoute = isFavoriteRoute,
                            onToggleFavorite = onToggleFavorite
                        )
                    } else {
                        EmptyHomeState()
                    }
                }
                else -> {
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        text = "Select an airport from the suggestions above",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
                    )
                }
            }
        }
    }
}

/**
 * Displays a single airport in the suggestion list.
 *
 * @param airport The airport to display.
 * @param query The user's search query, used for highlighting matching text.
 * @param onClick Callback invoked when the item is clicked.
 */
@Composable
fun AirportSuggestionItem(
    airport: Airport,
    query: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = airport.iataCode,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(52.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = buildAnnotatedString {
                val lowerName = airport.name.lowercase()
                val lowerQuery = query.lowercase()
                val start = lowerName.indexOf(lowerQuery)
                if (start >= 0 && query.isNotEmpty()) {
                    append(airport.name.substring(0, start))
                    withStyle(
                        SpanStyle(
                            background = MaterialTheme.colorScheme.primaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append(airport.name.substring(start, start + query.length))
                    }
                    append(airport.name.substring(start + query.length))
                } else {
                    append(airport.name)
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Displays the list of available flights from the selected departure airport.
 *
 * @param departure The selected departure airport.
 * @param destinations List of destination airports.
 * @param isFavoriteRoute Function to check if a route is a favorite.
 * @param onToggleFavorite Callback to toggle a route's favorite status.
 */
@Composable
fun FlightResultsSection(
    departure: Airport,
    destinations: List<Airport>,
    isFavoriteRoute: (String, String) -> Boolean,
    onToggleFavorite: (String, String) -> Unit
) {
    Column {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Flights from ${departure.iataCode}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = departure.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(destinations) { dest ->
                FlightRouteCard(
                    departureCode = departure.iataCode,
                    departureName = departure.name,
                    destinationCode = dest.iataCode,
                    destinationName = dest.name,
                    isFavorite = isFavoriteRoute(departure.iataCode, dest.iataCode),
                    onToggleFavorite = { onToggleFavorite(departure.iataCode, dest.iataCode) }
                )
            }
        }
    }
}

/**
 * Displays the list of saved favorite routes.
 *
 * @param favorites List of favorite routes.
 * @param allAirports A map of all airports, used to look up airport names.
 * @param isFavoriteRoute Function to check if a route is a favorite.
 * @param onToggleFavorite Callback to toggle a route's favorite status.
 */
@Composable
fun FavoritesSection(
    favorites: List<Favorite>,
    allAirports: Map<String, Airport>,
    isFavoriteRoute: (String, String) -> Boolean,
    onToggleFavorite: (String, String) -> Unit
) {
    Column {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Favorite Routes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(favorites) { fav ->
                val depAirport  = allAirports[fav.departureCode]
                val destAirport = allAirports[fav.destinationCode]
                FlightRouteCard(
                    departureCode   = fav.departureCode,
                    departureName   = depAirport?.name ?: "",
                    destinationCode = fav.destinationCode,
                    destinationName = destAirport?.name ?: "",
                    isFavorite = true,
                    onToggleFavorite = { onToggleFavorite(fav.departureCode, fav.destinationCode) }
                )
            }
        }
    }
}

/**
 * Displays a card with details for a single flight route (departure and destination).
 *
 * @param departureCode IATA code of the departure airport.
 * @param departureName Name of the departure airport.
 * @param destinationCode IATA code of the destination airport.
 * @param destinationName Name of the destination airport.
 * @param isFavorite Whether this route is currently a favorite.
 * @param onToggleFavorite Callback to toggle the favorite status.
 */
@Composable
fun FlightRouteCard(
    departureCode: String,
    departureName: String,
    destinationCode: String,
    destinationName: String,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("DEPART", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                Text(departureCode, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                if (departureName.isNotEmpty()) {
                    Text(departureName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(28.dp))
                HorizontalDivider(Modifier.width(40.dp).padding(top = 4.dp), color = MaterialTheme.colorScheme.secondary, thickness = 1.5.dp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("ARRIVE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                Text(destinationCode, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.tertiary)
                if (destinationName.isNotEmpty()) {
                    Text(destinationName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                }
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Displays a prompt to the user when the search is empty and there are no favorites.
 */
@Composable
fun EmptyHomeState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.FlightTakeoff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Where are you flying from?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Search for an airport or city to see available flights",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
            lineHeight = 20.sp
        )
    }
}

// --- Previews ---

private val sampleAirports = listOf(
    Airport(1, "MUC", "Munich Airport", 100),
    Airport(2, "MCI", "Kansas City International Airport", 200),
    Airport(3, "SFO", "San Francisco International Airport", 300),
    Airport(4, "LAX", "Los Angeles International Airport", 400)
)

private val sampleFavorites = listOf(
    Favorite(1, "MUC", "SFO"),
    Favorite(2, "MUC", "LAX")
)

@Preview(showBackground = true, widthDp = 360)
@Composable
fun EmptyStatePreview() {
    FlightSearchTheme {
        FlightSearchScreen(
            uiState = FlightUiState(),
            onQueryChange = {},
            onAirportSelect = {},
            onClearSearch = {},
            onToggleFavorite = { _, _ -> },
            isFavoriteRoute = { _, _ -> false }
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun SuggestionsPreview() {
    FlightSearchTheme {
        FlightSearchScreen(
            uiState = FlightUiState(
                searchQuery = "M",
                suggestions = sampleAirports
            ),
            onQueryChange = {},
            onAirportSelect = {},
            onClearSearch = {},
            onToggleFavorite = { _, _ -> },
            isFavoriteRoute = { _, _ -> false }
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun FlightResultsPreview() {
    FlightSearchTheme {
        FlightSearchScreen(
            uiState = FlightUiState(
                searchQuery = "MUC",
                selectedAirport = sampleAirports[0],
                destinations = sampleAirports.drop(1)
            ),
            onQueryChange = {},
            onAirportSelect = {},
            onClearSearch = {},
            onToggleFavorite = { _, _ -> },
            isFavoriteRoute = { d, a -> d == "MUC" && a == "LAX" }
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun FavoritesPreview() {
    FlightSearchTheme {
        FlightSearchScreen(
            uiState = FlightUiState(
                favorites = sampleFavorites,
                allAirports = sampleAirports.associateBy { it.iataCode }
            ),
            onQueryChange = {},
            onAirportSelect = {},
            onClearSearch = {},
            onToggleFavorite = { _, _ -> },
            isFavoriteRoute = { _, _ -> true }
        )
    }
}
