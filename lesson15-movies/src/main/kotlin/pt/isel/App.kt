package pt.isel

/*
 * Class diagram (excluding App):
 *
 * +-------------+      finder       +--------------------+
 * | MovieLister |------------------>|   <<interface>>    |
 * +-------------+                   |    MovieFinder     |
 *                                   +--------------------+
 *                                            ^ implements
 *                                            |
 *                                   +--------------------+
 *                                   |   MovieFinderCsv   |
 *                                   +--------------------+
 *                                            | client
 *                                            v
 * +----------------------+ implements +--------------------+
 * | DataSourceClientVia  |------------>|   <<interface>>    |
 * | File                 |             | DataSourceClient   |
 * +----------------------+             +--------------------+
 *                                                ^
 *                                                | implements
 *                                   +-------------------------+
 *                                   | DataSourceClientViaUrl  |
 *                                   +-------------------------+
 *
 * MovieFinder.findAll() returns Movie instances.
 */
fun main() {
    // resolveDependenciesManuallyByPropertyInjection()
    resolveDependenciesManuallyByConstructorInjection()

    val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9)

    val even = numbers.filter { item -> item % 2 == 0 }

    println(even)
}

fun resolveDependenciesManuallyByPropertyInjection() {
//    val lister = MovieLister()
//    val finder = MovieFinderCsv()
//    lister.finder = finder
//    finder.client = DataSourceClientViaUrl()
//    lister
//        .moviesDirectedBy("nolan")
//        .take(5)
//        .forEach { println(it) }
}

fun resolveDependenciesManuallyByConstructorInjection() {
    val lister = MovieLister(MovieFinderCsv(DataSourceClientViaFile()))
    lister
        .moviesDirectedBy("nolan")
        .take(5)
        .forEach { println(it) }
}
