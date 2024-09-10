package commands

import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.TrackEndEvent
import dev.schlaubi.lavakord.audio.on
import dev.schlaubi.lavakord.kord.getLink
import services.MusicService

val musicQueue = mutableListOf<Track>()

object MusicCommands {

    suspend fun handleMusicCommands(event: GuildChatInputCommandInteractionCreateEvent, musicService: MusicService) {
        val ack = event.interaction.deferPublicResponse()
        val link = musicService.lavalink.getLink(event.interaction.guildId)
        val player = link.player

        player.on<TrackEndEvent> {
            val track = playNextTrack(link)
            val message = if (track != null) "Tocando agora: ${track.info.title}" else "A fila de música está vazia :("

            val textChannel = event.interaction.channel.asChannel() as? TextChannel

            if (track != null) textChannel?.createMessage(message)

        }

        when (event.interaction.command.rootName) {
            "connect" -> {
                connect(event, ack, link)
            }
            "skip" -> {
                skipTrack(link, ack)
            }
            "play" -> {
                val query = event.interaction.command.options["musica"]?.value.toString()
                val search = if (query.startsWith("http")) query else "ytsearch:$query"

                if (link.state != dev.schlaubi.lavakord.audio.Link.State.CONNECTED) {
                    connect(event, ack, link)
                }

                var track: Track? = null
                val responseMessage = when (val item = musicService.loadTrack(link, search)) {
                    is LoadResult.TrackLoaded -> {
                        musicQueue.add(item.data)
                        if (player.playingTrack == null) {
                            track = playNextTrack(link)
                        }
                        if (track == null) "Adicionado à fila: ${item.data.info.title}"
                        else "Tocando agora: ${item.data.info.title}"
                    }
                    is LoadResult.PlaylistLoaded -> {
                        musicQueue.addAll(item.data.tracks)
                        if (player.playingTrack == null) {
                            track = playNextTrack(link)
                        }
                        if (track == null) "Adicionado à fila: ${item.data.tracks.first().info.title}"
                        else "Tocando agora: ${item.data.tracks.first().info.title}"
                    }
                    is LoadResult.SearchResult -> {
                        musicQueue.add(item.data.tracks.first())
                        if (player.playingTrack == null) {
                            track = playNextTrack(link)
                        }
                        if (track == null) "Adicionado à fila: ${item.data.tracks.first().info.title}"
                        else "Tocando agora: ${item.data.tracks.first().info.title}"
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
                ack.respond { content = "Até mais!" }
            }
        }
    }

    private suspend fun connect(
        event: GuildChatInputCommandInteractionCreateEvent,
        ack: DeferredPublicMessageInteractionResponseBehavior,
        link: Link
    ) {
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

    private suspend fun playNextTrack(link: Link): Track? {
        if (musicQueue.isNotEmpty()) {
            val nextTrack = musicQueue.removeAt(0)
            link.player.playTrack(track = nextTrack)
            return nextTrack
        }
        return null
    }

    private suspend fun skipTrack(link: Link, ack: DeferredPublicMessageInteractionResponseBehavior) {
        if (musicQueue.isEmpty()) {
            ack.respond { content = "A fila está vazia, não há músicas para pular" }
            return
        }

        val nextTrack = playNextTrack(link)

        val responseMessage = if (nextTrack != null) {
            "Próxima música: ${nextTrack.info.title}"
        } else {
            "A fila está vazia, não há mais músicas para tocar!"
        }

        ack.respond { content = responseMessage }
    }
}