package jan.ondra.insights

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<InsightsApplication>().with(TestcontainersConfiguration::class).run(*args)
}
