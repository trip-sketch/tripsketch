package kr.kro.tripsketch.repositories

import kr.kro.tripsketch.domain.TokenCode
import org.springframework.data.mongodb.repository.MongoRepository

interface TokenCodeRepository : MongoRepository<TokenCode, String> {
    fun findByOneTimeCode(oneTimeCode: String): TokenCode?
}