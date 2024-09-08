package lavalink

import dev.kord.core.Kord
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.kord.lavakord
import dev.schlaubi.lavakord.plugins.lavasrc.LavaSrc
import dev.schlaubi.lavakord.plugins.sponsorblock.Sponsorblock
import io.github.cdimascio.dotenv.dotenv


fun setupLavaLink(kord: Kord): LavaKord {
    val dotenv = dotenv()
    val lavalink = kord.lavakord {
        plugins {
            install(LavaSrc)
            install(Sponsorblock)
        }
    }
    lavalink.addNode(dotenv["LAVALINK_URL"], dotenv["LAVALINK_PASSWORD"])
    return lavalink
}