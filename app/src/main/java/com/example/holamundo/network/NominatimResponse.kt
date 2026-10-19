package com.example.holamundo.network

data class NominatimResponse(
    val display_name: String = "",
    val address: NominatimAddress? = null
)

data class NominatimAddress(
    val country: String? = null,
    val country_code: String? = null,
    val state: String? = null,
    val city: String? = null,
    val town: String? = null,
    val village: String? = null
)
