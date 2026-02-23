package com.example.flightsearch

import android.app.Application
import com.example.flightsearch.data.FlightDatabase
import com.example.flightsearch.data.FlightRepository
import com.example.flightsearch.data.UserPreferencesRepository

/**
 * The main application class for the Flight Search app.
 * This class is responsible for initializing and providing dependencies.
 */
class FlightSearchApplication : Application() {

    /**
     * Lazily initialized database instance.
     */
    val database: FlightDatabase by lazy { FlightDatabase.getDatabase(this) }

    /**
     * Lazily initialized repository instance.
     * The repository provides a clean API for data access to the rest of the application.
     */
    val repository: FlightRepository by lazy {
        FlightRepository(database.airportDao(), database.favoriteDao())
    }

    /**
     * Lazily initialized user preferences repository.
     * This repository handles user preferences, such as the last search query.
     */
    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(this)
    }
}
