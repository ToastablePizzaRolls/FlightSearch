package com.example.flightsearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flightsearch.ui.FlightSearchScreen
import com.example.flightsearch.ui.FlightSearchViewModel
import com.example.flightsearch.ui.theme.FlightSearchTheme

/**
 * Main activity for the Flight Search app.
 */
class MainActivity : ComponentActivity() {
    /**
     * Sets up the UI content for the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlightSearchTheme {
                val viewModel: FlightSearchViewModel =
                    viewModel(factory = FlightSearchViewModel.Factory)
                val uiState by viewModel.uiState.collectAsState()

                FlightSearchScreen(
                    uiState = uiState,
                    onQueryChange = viewModel::onSearchQueryChange,
                    onAirportSelect = viewModel::selectAirport,
                    onClearSearch = viewModel::clearSearch,
                    onToggleFavorite = viewModel::toggleFavorite,
                    isFavoriteRoute = viewModel::isFavoriteRoute
                )
            }
        }
    }
}
