package kr.kro.tripsketch.controllers

import kr.kro.tripsketch.services.NominatimService
import kr.kro.tripsketch.dto.GeocodeRequestDto
import kr.kro.tripsketch.dto.GeocodeResponseDto
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api")
class GeocodingController(private val nominatimService: NominatimService) {

    @PostMapping("/geocode")
    fun reverseGeocode(
        @RequestBody request: GeocodeRequestDto
    ): GeocodeResponseDto? {
        return nominatimService.reverseGeocodeWithLanguage(request.latitude, request.longitude, "ko") // "ko"는 한국어
    }
}
