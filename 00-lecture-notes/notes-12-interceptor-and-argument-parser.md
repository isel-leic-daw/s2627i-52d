# Spring Arguments Binding

Following item H of the Lecture Notes 11, create a class implementing
the `HandlerMethodArgumentResolver` interface as a way of providing
the `Accept-Language` in a strongly-typed way:
* Use the `WebMvcConfigurer` implementation to register the `HandlerMethodArgumentResolver`.
* Take advantage of the information provided by the HandlerMethodArgumentResolver in the greetings controller.