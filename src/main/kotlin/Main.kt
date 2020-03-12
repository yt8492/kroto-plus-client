import com.yt8492.grpcsample.protobuf.MessageRequest
import com.yt8492.grpcsample.protobuf.MessageServiceCoroutineGrpc
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalCoroutinesApi::class)
fun main() {
    val channel = ManagedChannelBuilder.forAddress("localhost", 6565)
        .usePlaintext()
        .build()
    val client = MessageServiceCoroutineGrpc.newStub(channel)
    runBlocking {
        println("--- Unary Call start ---")
        val request = MessageRequest {
            message = "hoge"
        }
        val response = client.unary(request)
        println(response.message)
        println("--- Unary Call finish ---")
    }
    runBlocking {
        println("--- Client Stream start ---")
        val (requestChannel, response) = client.clientStream()
        listOf("hoge", "fuga", "piyo").forEach {
            val request = MessageRequest {
                message = it
            }
            requestChannel.send(request)
        }
        requestChannel.close()
        println(response.await().message)
        println("--- Client Stream finish ---")
    }
    runBlocking {
        println("--- Server Stream start ---")
        val request = MessageRequest {
            message = "hoge"
        }
        val responseChannel = client.serverStream(request)
        responseChannel.consumeEach {
            println(it.message)
        }
        println("--- Server Stream finish ---")
    }
    runBlocking {
        println("--- Bidirectional Stream start ---")
        val (requestChannel, responseChannel) = client.bidirectionalStream()
        listOf("hoge", "fuga", "piyo").forEach {
            val request = MessageRequest {
                message = it
            }
            requestChannel.send(request)
        }
        requestChannel.close()
        responseChannel.consumeEach {
            println(it.message)
        }
        println("--- Bidirectional Stream finish ---")
    }
}