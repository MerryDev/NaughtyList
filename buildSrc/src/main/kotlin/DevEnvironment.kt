import java.io.File

object DevEnvironment {

    const val LOBBY_PORT = "30066"
    const val GAME_PORT = "30067"

    fun ensureForwardingSecretFile(rootDir: File): File {
        val secretsDirectory = rootDir.resolve("secrets")
        secretsDirectory.mkdirs()

        val secretFile = secretsDirectory.resolve("forwarding.secret")
        if (!secretFile.exists()) {
            val secret = List(32) {
                ('A'..'Z') + ('a'..'z') + ('0'..'9')
            }.flatten().let { chars -> (1..32).map { chars.random() } }.joinToString("")

            secretFile.writeText(secret)
        }
        return secretFile
    }

    fun readForwardingSecret(rootDir: File): String {
        return ensureForwardingSecretFile(rootDir).readText().trim()
    }

}