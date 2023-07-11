package kr.kro.tripsketch.services

import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class NickNameService {
    private val actions = listOf("먹는", "마시는", "기다리는", "상상하는", "훔치는", "사고싶은", "먹고싶은", "싫은", "생각하는", "꿈꾸는")
    private val objects = listOf("과자", "음료수", "과일", "우유", "사탕", "초콜릿", "아이스크림", "커피", "라면", "빵")
    private val animals = listOf("곰돌이", "나비", "고양이", "강아지", "토끼", "사자", "호랑이", "코끼리", "원숭이", "앵무새", "개")

    fun generateRandomNickname(): String {
        val randomAction = actions.random()
        val randomObject = objects.random()
        val randomAnimal = animals.random()

        val randomQuantity = Random.nextInt(1, 99)
        val quantityNoun = if (randomQuantity == 1) "개" else "${randomQuantity}개"

        val randomNumber = Random.nextInt(0, 10000)
        val formattedNumber = String.format("%04d", randomNumber)

        return "$randomObject$quantityNoun$randomAction$randomAnimal$formattedNumber"
    }
}
