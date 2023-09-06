package kr.kro

import kr.kro.tripsketch.utils.EnvLoader
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class Application

fun main(args: Array<String>) {
    EnvLoader.getAllProperties().forEach {
        System.setProperty(it.key, it.value)
    }
    runApplication<Application>(*args)
}
