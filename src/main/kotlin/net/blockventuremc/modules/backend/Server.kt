package net.blockventuremc.modules.backend

import io.javalin.Javalin

class Server {
    private lateinit var apiServer: Javalin

    fun start() {
        apiServer = Javalin.create()
            .get("/") { ctx -> ctx.result("Hello world") }
            .start()
    }

    fun stop() {
        apiServer.stop()
    }
}