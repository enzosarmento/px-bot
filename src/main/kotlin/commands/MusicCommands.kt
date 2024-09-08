package commands

import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.schlaubi.lavakord.kord.getLink
import services.MusicService

object MusicCommands {

    suspend fun handleMusicCommands(event: GuildChatInputCommandInteractionCreateEvent, musicService: MusicService) {
        val ack = event.interaction.deferPublicResponse()
        val link = musicService.lavalink.getLink(event.interaction.guildId)
        val player = link.player

        when (event.interaction.command.rootName) {
            "connect" -> {
                val voiceState = event.interaction.user.asMember(event.interaction.guildId).getVoiceState()
                val channelId = voiceState.channelId ?: run {
                    ack.respond { content = "Por favor, conecte-se a um canal de voz" }
                    return
                }
                link.connectAudio(channelId.value)
                val voiceChannel = event.kord.getChannelOf<VoiceChannel>(channelId)
                val channelName = voiceChannel?.name ?: "Desconhecido"
                ack.respond { content = "Conectado ao canal: $channelName" }
            }
            "play" -> {
                val query = event.interaction.command.options["musica"]?.value.toString()
                val search = if (query.startsWith("http")) query else "ytsearch:$query"

                if (link.state != dev.schlaubi.lavakord.audio.Link.State.CONNECTED) {
                    ack.respond { content = "Não estou conectado em um canal! Utilize o comando /connect" }
                    return
                }

                val responseMessage = when (val item = musicService.loadTrack(link, search)) {
                    is LoadResult.TrackLoaded -> {
                        player.playTrack(track = item.data)
                        "Tocando agora: ${item.data.info.title}"
                    }
                    is LoadResult.PlaylistLoaded -> {
                        player.playTrack(track = item.data.tracks.first())
                        "Tocando agora: ${item.data.tracks.first().info.title}"
                    }
                    is LoadResult.SearchResult -> {
                        player.playTrack(track = item.data.tracks.first())
                        "Tocando agora: ${item.data.tracks.first().info.title}"
                    }
                    is LoadResult.NoMatches -> "Nenhuma música encontrada"
                    is LoadResult.LoadFailed -> item.data.message ?: "Erro ao carregar a música"
                }

                ack.respond { content = responseMessage }
            }
            "pause" -> {
                player.pause(!player.paused)
                ack.respond { content = if (player.paused) "Música pausada!" else "Música despausada!" }
            }
            "stop" -> {
                player.stopTrack()
                ack.respond { content = "Música parada!" }
            }
            "leave" -> {
                link.destroy()
                ack.respond { content = "Saiu do canal de voz." }
            }
        }
    }
}