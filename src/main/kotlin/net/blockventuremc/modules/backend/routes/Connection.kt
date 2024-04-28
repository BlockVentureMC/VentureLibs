package net.blockventuremc.modules.backend.routes

import dev.fruxz.ascend.extension.logging.getItsLogger
import io.javalin.apibuilder.ApiBuilder.post
import io.javalin.apibuilder.EndpointGroup

class Connection: EndpointGroup {
    override fun addEndpoints() {
        post("/connect") { ctx ->
            val id = ctx.queryParam("id")
            getItsLogger().info(id)
            ctx.status(200)
        }
    }

}