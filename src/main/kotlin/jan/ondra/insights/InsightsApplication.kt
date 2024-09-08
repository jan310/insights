package jan.ondra.insights

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class InsightsApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<InsightsApplication>(*args)
}
