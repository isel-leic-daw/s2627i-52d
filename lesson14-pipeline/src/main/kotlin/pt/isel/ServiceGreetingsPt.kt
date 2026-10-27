package pt.isel

import org.springframework.stereotype.Component

@Component
class ServiceGreetingsPt(
    // val dummy: IDummy,
) : ServiceGreetings {
    override fun greeting(): String = "Ola Mundo"
}
