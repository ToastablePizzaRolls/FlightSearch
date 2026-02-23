package com.example.flightsearch.data

import kotlinx.coroutines.flow.Flow

class FlightRepository(
    private val airportDao: AirportDao,
    private val favoriteDao: FavoriteDao
) {
    fun searchAirports(query: String): Flow<List<Airport>> =
        airportDao.searchAirports(query)

    fun getAllAirports(): Flow<List<Airport>> =
        airportDao.getAllAirports()

    fun getDestinationsExcept(code: String): Flow<List<Airport>> =
        airportDao.getAllAirportsExcept(code)

    suspend fun getAirportByCode(code: String): Airport? =
        airportDao.getAirportByCode(code)

    fun getAllFavorites(): Flow<List<Favorite>> =
        favoriteDao.getAllFavorites()

    suspend fun addFavorite(departureCode: String, destinationCode: String) {
        favoriteDao.insertFavorite(
            Favorite(departureCode = departureCode, destinationCode = destinationCode)
        )
    }

    suspend fun removeFavorite(departureCode: String, destinationCode: String) {
        val fav = favoriteDao.getFavorite(departureCode, destinationCode)
        if (fav != null) favoriteDao.deleteFavorite(fav)
    }

    suspend fun isFavorite(departureCode: String, destinationCode: String): Boolean =
        favoriteDao.getFavorite(departureCode, destinationCode) != null
}
