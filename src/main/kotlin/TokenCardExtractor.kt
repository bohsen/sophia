internal class TokenCardExtractor {

    fun extractTokenCard(inputString: String): TokenCard {
        val userId = inputString.substringAfter("(").substringBefore(")").toInt()
        val userName = inputString.substringBefore(" (")
        val expDate = inputString.substringAfter("Expires: ").substringBefore("\n")

        val mapKeys = setOf("A", "B", "C", "D", "E", "F", "G", "H")
        val tokens: Map<String, List<String>> = mapKeys.associateBy(
            keySelector = { it },
            valueTransform = { inputString.substringAfter("$it ").substringBefore("\n").split(" ") }
        )

        return TokenCard(userId, userName, expDate, tokens)
    }
}