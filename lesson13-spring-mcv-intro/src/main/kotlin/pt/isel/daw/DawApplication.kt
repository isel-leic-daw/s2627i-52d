package pt.isel.daw

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DawApplication

fun main(args: Array<String>) {
    runApplication<DawApplication>(*args)
}
