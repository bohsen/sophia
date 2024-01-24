import kotlinx.serialization.Serializable
import org.apache.logging.log4j.kotlin.logger

@Serializable
data class TokenCard(
    val userId: Int,
    val username: String,
    val expDate: String,
    private val tokens: Map<String, List<String>>
) {
    fun getToken(coordinate: String): String {
        check(coordinate.length == 2) { "coordinate length should be equal to 2 but was: ${coordinate.length}" }
        val first = coordinate.first().toString().toInt()
        val second = coordinate.last().toString()
        check((1..8).contains(first)) { "coordinate not in range 1 to 8. Was $first" }
        check(("A".."H").contains(second)) { "coordinate not in range A to H. Was $second" }

        return tokens.getValue(second).get(first - 1)
    }
}