/* 
 * Copyright (c) 2022 Alexander Majka (mfnalex) / JEFF Media GbR
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * If you need help or have any suggestions, feel free to join my Discord and head to #programming-help:
 *
 * Discord: https://discord.jeff-media.com/
 *
 * If you find this library helpful or if you're using it one of your paid plugins, please consider leaving a donation
 * to support the further development of this project :)
 *
 * Donations: https://paypal.me/mfnalex
 */

package net.blockventuremc.modules.customblockdata.events;

import net.blockventuremc.modules.customblockdata.CustomBlockData;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.plugin.Plugin;

open class CustomBlockDataEvent(private val plugin: Plugin, private val block: Block, private val bukkitEvent: Event) :
    Event(), Cancellable {

    companion object {
        val HANDLERS = HandlerList()

        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }

    var cbd: CustomBlockData = CustomBlockData(block, plugin)
    private var cancelled: Boolean = false

    fun getBlock(): Block {
        return block
    }

    fun getBukkitEvent(): Event {
        return bukkitEvent
    }

    fun getCustomBlockData(): CustomBlockData {
        return cbd
    }

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    fun getReason(): Reason {
        for (reason in Reason.entries) {
            if (reason != Reason.UNKNOWN && reason.eventClasses.stream().anyMatch { clazz -> clazz == bukkitEvent.javaClass }) {
                return reason
            }
        }
        return Reason.UNKNOWN
    }

    enum class Reason(vararg eventClasses: Class<out Event>) {
        BLOCK_BREAK(BlockBreakEvent::class.java),
        BLOCK_PLACE(BlockPlaceEvent::class.java, BlockMultiPlaceEvent::class.java),
        EXPLOSION(EntityExplodeEvent::class.java, BlockExplodeEvent::class.java),
        PISTON(BlockPistonExtendEvent::class.java, BlockPistonRetractEvent::class.java),
        BURN(BlockBurnEvent::class.java),
        ENTITY_CHANGE_BLOCK(EntityChangeBlockEvent::class.java),
        FADE(BlockFadeEvent::class.java),
        STRUCTURE_GROW(StructureGrowEvent::class.java),
        FERTILIZE(BlockFertilizeEvent::class.java),
        LEAVES_DECAY(LeavesDecayEvent::class.java),
        UNKNOWN();

        val eventClasses: List<Class<out Event>> = listOf(*eventClasses)
        fun getApplicableEvents(): List<Class<out Event>> {
            return this.eventClasses
        }
    }
}