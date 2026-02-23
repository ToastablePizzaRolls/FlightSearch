package com.example.flightsearch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.flightsearch.FlightSearchApplication
import com.example.flightsearch.data.Airport
import com.example.flightsearch.data.Favorite
import com.example.flightsearch.data.FlightRepository
import com.example.flightsearch.data.UserPreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Represents the UI state for the flight search screen.
 */
data class FlightUiState(
    val searchQuery: String = "",
    val suggestions: List<Airport> = emptyList(),
    val selectedAirport: Airport? = null,
    val destinations: List<Airport> = emptyList(),
    val favorites: List<Favorite> = emptyList(),
    // Map of iata_code -> Airport for resolving names in favorite cards
    val allAirports: Map<String, Airport> = emptyMap()
)

// Derived property to determine if the UI should show the favorites list.
// True when the search query is blank and no airport is selected.
val FlightUiState.showingFavorites: Boolean
    get() = searchQuery.isBlank() && selectedAirport == null

/**
 * ViewModel for the flight search screen.
 */
class FlightSearchViewModel(
    private val repository: FlightRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // The UI state for the flight search screen.
    private val _uiState = MutableStateFlow(FlightUiState())
    val uiState: StateFlow<FlightUiState> = _uiState.asStateFlow()

    // Job for the search query.
    private var searchJob: Job? = null
    // Job for the destination query.
    private var destinationJob: Job? = null

    init {
        // Collect all airports into a lookup map for favorite card name resolution
        viewModelScope.launch {
            repository.getAllAirports().collect { airports ->
                _uiState.update { it.copy(allAirports = airports.associateBy { a -> a.iataCode }) }
            }
        }
        // Collect favorites continuously
        viewModelScope.launch {
            repository.getAllFavorites().collect { favorites ->
                _uiState.update { it.copy(favorites = favorites) }
            }
        }
        // Restore saved search query from DataStore
        viewModelScope.launch {
            val savedQuery = preferencesRepository.savedSearchQuery.first()
            if (savedQuery.isNotEmpty()) {
                _uiState.update { it.copy(searchQuery = savedQuery) }
                // Try to find a matching airport to restore flight results
                val airport = repository.getAirportByCode(savedQuery)
                if (airport != null) {
                    selectAirport(airport)
                } else {
                    loadSuggestions(savedQuery)
                }
            }
        }
    }

    /**
     * Called when the search query changes.
     *
     * @param query The new search query.
     */
    fun onSearchQueryChange(query: String) {
        // Update the UI state with the new query and reset the selected airport,
        // destinations, and suggestions.
        _uiState.update {
            it.copy(
                searchQuery = query,
                selectedAirport = null,
                destinations = emptyList(),
                suggestions = emptyList()
            )
        }
        // Save the search query to the preferences repository.
        viewModelScope.launch {
            preferencesRepository.saveSearchQuery(query)
        }
        // Cancel the destination job.
        destinationJob?.cancel()
        // If the query is blank, cancel the search job.
        if (query.isBlank()) {
            searchJob?.cancel()
        } else {
            // Otherwise, load suggestions for the query.
            loadSuggestions(query)
        }
    }

    /**
     * Loads suggestions for the given query.
     *
     * @param query The query to load suggestions for.
     */
    private fun loadSuggestions(query: String) {
        // Cancel the previous search job.
        searchJob?.cancel()
        // Start a new search job.
        searchJob = viewModelScope.launch {
            repository.searchAirports(query)
                .catch { _uiState.update { it.copy(suggestions = emptyList()) } }
                .collect { airports ->
                    _uiState.update { it.copy(suggestions = airports) }
                }
        }
    }

    /**
     * Called when an airport is selected.
     *
     * @param airport The selected airport.
     */
    fun selectAirport(airport: Airport) {
        // Cancel the search job.
        searchJob?.cancel()
        // Update the UI state with the selected airport and reset the suggestions.
        _uiState.update {
            it.copy(
                selectedAirport = airport,
                searchQuery = airport.iataCode,
                suggestions = emptyList()
            )
        }
        // Save the selected airport to the preferences repository.
        viewModelScope.launch {
            preferencesRepository.saveSearchQuery(airport.iataCode)
        }
        // Cancel the destination job.
        destinationJob?.cancel()
        // Start a new destination job.
        destinationJob = viewModelScope.launch {
            repository.getDestinationsExcept(airport.iataCode)
                .catch { _uiState.update { it.copy(destinations = emptyList()) } }
                .collect { destinations ->
                    _uiState.update { it.copy(destinations = destinations) }
                }
        }
    }

    /**
     * Clears the search query and resets the UI state.
     */
    fun clearSearch() {
        // Cancel the search and destination jobs.
        searchJob?.cancel()
        destinationJob?.cancel()
        // Reset the UI state.
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedAirport = null,
                suggestions = emptyList(),
                destinations = emptyList()
            )
        }
        // Clear the search query from the preferences repository.
        viewModelScope.launch {
            preferencesRepository.saveSearchQuery("")
        }
    }

    /**
     * Toggles the favorite status of a route.
     *
     * @param departureCode The departure airport code.
     * @param destinationCode The destination airport code.
     */
    fun toggleFavorite(departureCode: String, destinationCode: String) {
        viewModelScope.launch {
            if (repository.isFavorite(departureCode, destinationCode)) {
                repository.removeFavorite(departureCode, destinationCode)
            } else {
                repository.addFavorite(departureCode, destinationCode)
            }
        }
    }

    /**
     * Checks if a route is a favorite.
     *
     * @param departureCode The departure airport code.
     * @param destinationCode The destination airport code.
     * @return True if the route is a favorite, false otherwise.
     */
    fun isFavoriteRoute(departureCode: String, destinationCode: String): Boolean {
        return _uiState.value.favorites.any {
            it.departureCode == departureCode && it.destinationCode == destinationCode
        }
    }

    companion object {
        // Factory for creating the ViewModel.
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FlightSearchApplication)
                FlightSearchViewModel(
                    repository = application.repository,
                    preferencesRepository = application.userPreferencesRepository
                )
            }
        }
    }
}
