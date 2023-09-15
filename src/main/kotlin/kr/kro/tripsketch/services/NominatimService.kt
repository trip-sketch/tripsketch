package kr.kro.tripsketch.services

import com.fasterxml.jackson.databind.ObjectMapper
import kr.kro.tripsketch.dto.GeocodeResponseDto
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service

@Service
class NominatimService(private val objectMapper: ObjectMapper) {
    private val httpClient = OkHttpClient()

    fun reverseGeocodeWithLanguage(latitude: Double, longitude: Double, language: String): GeocodeResponseDto? {
        val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$latitude&lon=$longitude&accept-language=$language"
        val request = Request.Builder()
            .url(url)
            .build()

        val response = httpClient.newCall(request).execute()
        val jsonResponse = response.body?.string()

        // JSON 파싱 및 변환
        return try {
            parseJsonToGeocodeResponse(jsonResponse)
        } catch (e: Exception) {
            throw Exception("예외가 발생했습니다: ${e.message}")
        }
    }

    private fun parseJsonToGeocodeResponse(jsonResponse: String?): GeocodeResponseDto? {
        return try {
            val jsonNode = objectMapper.readTree(jsonResponse)
            val addressNode = jsonNode.path("address")

            GeocodeResponseDto(
                countryCode = addressNode.path("country_code").asText(null),
                country = addressNode.path("country").asText(null),
                city = addressNode.path("city").asText(null),
                municipality = addressNode.path("municipality").asText(null),
                name = addressNode.path("name").asText(null),
                displayName = jsonNode.path("display_name").asText(null),
                road = addressNode.path("road").asText(null),
                address = addressNode.path("address").asText(null),
            )
        } catch (e: Exception) {
            throw Exception("JSON 파싱 중 예외가 발생했습니다: ${e.message}", e)
        }
    }
}
