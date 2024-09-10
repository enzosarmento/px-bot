package services

import commands.MusicCommands
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import lavalink


object CommandHandler {
    private val musicService = MusicService(lavalink)

    suspend fun handleCommand(event: GuildChatInputCommandInteractionCreateEvent) {

        when (event.interaction.command.rootName) {
            "play", "pause", "skip", "stop", "connect", "leave" -> MusicCommands.handleMusicCommands(event, musicService)
        }
    }
}