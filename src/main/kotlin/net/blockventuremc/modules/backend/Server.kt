package net.blockventuremc.modules.backend

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.path
import net.blockventuremc.modules.backend.routes.AudioServer
import net.blockventuremc.modules.backend.routes.Connection

class Server {
    private lateinit var apiServer: Javalin

    fun start() {
        apiServer = Javalin.create { config ->
            config.router.apiBuilder {
                path("/audio", AudioServer())
                path("/", Connection())
            }
        }
            .get("/") { ctx -> ctx.result("Hello world") }
            .start(27386)
    }

    fun stop() {
        apiServer.stop()
    }
}