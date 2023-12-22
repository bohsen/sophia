import kotlinx.serialization.Serializable

@Serializable
data class TokenCard(
    val userId: Int,
    val username: String,
    val expDate: String,
    private val tokens: Map<String, List<String>>
) {
    fun getToken(coordinate: String): String {
        val first = coordinate.first().toString()
        val second = coordinate.substringAfter(coordinate.first()).toInt()
        check(("A".."H").contains(first)) { "coordinate not in range A to H. Was $first" }
        check((1..8).contains(second)) { "coordinate not in range 1 to 8. Was $second" }

        return tokens.getValue(first).get(second - 1)
    }
}