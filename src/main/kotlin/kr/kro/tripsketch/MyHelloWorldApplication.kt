package kr.kro.tripsketch

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController {

    @GetMapping("/hello")
    fun helloWorld(): String {
        return "Hello, World!"
    }
}
