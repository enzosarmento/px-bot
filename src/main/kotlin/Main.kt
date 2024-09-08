import lavalink.setupLavaLink
import dev.kord.core.Kord
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.schlaubi.lavakord.LavaKord
import io.github.cdimascio.dotenv.dotenv
import services.CommandHandler

lateinit var lavalink: LavaKord

@OptIn(PrivilegedIntent::class)
suspend fun main() {
    val dotenv = dotenv()
    val kord = Kord(dotenv["BOT_TOKEN"])
    lavalink = setupLavaLink(kord)

    kord.on<GuildChatInputCommandInteractionCreateEvent> {
        CommandHandler.handleCommand(this)
    }

    kord.login {
        intents += Intent.MessageContent
    }
}