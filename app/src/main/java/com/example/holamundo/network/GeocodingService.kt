package com.example.holamundo.network

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface GeocodingService {
    @Headers("User-Agent: MercadoLocalEC-App/1.0 (soporte@mercadolocalec.com)")
    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("format") format: String = "json",
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): NominatimResponse
}
