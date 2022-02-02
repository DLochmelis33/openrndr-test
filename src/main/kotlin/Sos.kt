import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val j = launch {
            delay(1000)
            println("haha")
        }
        println("hehe")
    }
}


