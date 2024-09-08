package services

import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.rest.loadItem

class MusicService(val lavalink: LavaKord) {
    suspend fun loadTrack(link: Link, search: String) = link.loadItem(search)
}