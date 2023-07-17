package kr.kro

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kr.kro.tripsketch.utils.EnvLoader

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    EnvLoader.getAllProperties().forEach {
        System.setProperty(it.key, it.value)
    }
    runApplication<Application>(*args)
}
