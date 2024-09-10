package commands

import dev.kord.core.Kord
import dev.kord.rest.builder.interaction.string

suspend fun registerCommands(kord: Kord) {
    kord.createGlobalApplicationCommands {
        input("connect", "Conectar ao seu canal")
        input("pause", "Pausa a música")
        input("stop", "Para a música")
        input("leave", "Sai do canal atual")
        input("play", "Começa a tocar uma música") {
            string("musica", "Qual a música que você quer tocar?")
        }
        input("skip", "Pula para a próxima música da fila")
    }
}