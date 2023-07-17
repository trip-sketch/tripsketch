package kr.kro

import kr.kro.tripsketch.utils.EnvLoader
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    EnvLoader.getAllProperties().forEach {
        System.setProperty(it.key, it.value)
    }
    runApplication<Application>(*args)
}
