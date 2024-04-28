package net.blockventuremc.modules.backend

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.path
import net.blockventuremc.modules.backend.routes.AudioServer

class Server {
    private lateinit var apiServer: Javalin

    fun start() {
        apiServer = Javalin.create { config ->
            config.router.apiBuilder {
                path("/audio", AudioServer())
            }
        }
            .get("/") { ctx -> ctx.result("Hello world") }
            .start()
    }

    fun stop() {
        apiServer.stop()
    }
}