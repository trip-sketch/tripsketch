package kr.kro.tripsketch.dto

data class GeocodeResponseDto(
    val countryCode: String?,
    val country: String?,
    val city: String?,
    val municipality: String?,
    val name: String?,
    val displayName: String?,
    val road: String?,
    val address: String?,
)
