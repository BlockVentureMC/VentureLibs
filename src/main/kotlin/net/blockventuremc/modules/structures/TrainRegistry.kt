package net.blockventuremc.modules.structures

import net.blockventuremc.modules.rides.track.TrackRide
import net.blockventuremc.modules.structures.impl.Cart
import net.blockventuremc.modules.structures.impl.Seat
import net.blockventuremc.modules.structures.impl.Train
import net.blockventuremc.utils.itembuilder.ItemBuilder
import org.bukkit.Material
import org.bukkit.util.Vector

object TrainRegistry {
    var trains = mutableMapOf<String,AbstractTrain>()

    fun registerTrains() {

        //halloween spin
        trains.put("halloween_spin", object : AbstractTrain() {
            override fun train(trackRide: TrackRide, startPosition: Double): Train {
                val train = Train("halloween_spin", trackRide, 0.0)
                        repeat(5) {
                            val cart = Cart(2.3f, 0.5f)
                            cart.addChild(
                                ItemAttachment(
                                    "base",
                                    ItemBuilder(Material.DIAMOND_SWORD).customModelData(100).build(),
                                    Vector(0.0, 0.4, 0.0),
                                    Vector()
                                )
                            )
                            val rotator = Attachment("rotator", Vector(), Vector())
                            cart.addChild(rotator)

                            rotator.addChild(Seat("seat1", Vector(0.39, 0.6, 0.3), Vector()))
                            rotator.addChild(Seat("seat2", Vector(-0.39, 0.6, 0.3), Vector()))
                            rotator.addChild(Seat("seat3", Vector(0.39, 0.6, -0.3), Vector()))
                            rotator.addChild(Seat("seat4", Vector(-0.39, 0.6, -0.3), Vector()))

                            rotator.addChild(
                                ItemAttachment(
                                    "model",
                                    ItemBuilder(Material.DIAMOND_SWORD).customModelData(99).build(),
                                    Vector(0.0, 1.0, 0.0),
                                    Vector()
                                )
                            )

                            cart.animation = object : Animation() {
                                var prevDirection = Vector(0, 1, 0)
                                var rotationVelocity = 0.0
                                override fun animate() {
                                    val direction = Vector(cart.front.x, cart.front.y, cart.front.z)
                                    val crossProduct = prevDirection.crossProduct(direction)

                                    val spin = crossProduct.dot(Vector(0.0, 1.0, 0.0)) * -4.0f

                                    rotationVelocity += spin
                                    rotationVelocity *= 0.99f

                                    rotator.localRotation.add(Vector(0.0, rotationVelocity, 0.0))
                                    prevDirection = direction.clone()
                                }
                            }
                            train.addCart(cart)
                        }
                return train
            }
        })

        //lumbertrack_coaster
        trains.put("lumbertrack_coaster", object : AbstractTrain() {
            override fun train(trackRide: TrackRide, startPosition: Double): Train {
                val train = Train("lumbertrack_coaster", trackRide, 0.0)

                val frontCart = Cart(1.2f, 0.8f)
                frontCart.addChild(
                    ItemAttachment(
                        "base",
                        ItemBuilder(Material.DIAMOND_SWORD).customModelData(122).build(),
                        Vector(0.0, 0.9, 0.0),
                        Vector()
                    )
                )
                train.addCart(frontCart)
                repeat(5) { i -> //122 red
                    val cart = Cart(1.2f, 0.53f)
                    var itemId = if (i == 0) 123 else 124
                    if (i == 4 || i == 1) itemId = 125
                    cart.addChild(
                        ItemAttachment(
                            "base",
                            ItemBuilder(Material.DIAMOND_SWORD).customModelData(itemId).build(),
                            Vector(0.0, 0.9, 0.0),
                            Vector()
                        )
                    )
                    cart.addChild(Seat("seat1", Vector(0.4, 0.4, 0.0), Vector()))
                    cart.addChild(Seat("seat2", Vector(-0.4, 0.4, 0.0), Vector()))
                    train.addCart(cart)
                }
                return train
            }
        })
        //lumbertrack_coaster
        trains.put("feng_huang", object : AbstractTrain() {
            override fun train(trackRide: TrackRide, startPosition: Double): Train {
                val train = Train("feng_huang", trackRide, 0.0)

                repeat(7) { i ->
                    val cart = Cart(2.0f, 0.3f)
                    val rotator = Attachment("rotator", Vector(0.0,0.0,0.0), Vector())
                    cart.addChild(rotator)
                    rotator.addChild(
                        ItemAttachment(
                            "base",
                            ItemBuilder(Material.DIAMOND_SWORD).customModelData(24).build(),
                            Vector(0.0, 1.0, 0.0),
                            Vector()
                        )
                    )
                    cart.animation = object : Animation() {

                        var rotationVelocity = 0.0 + (i * 10)
                        override fun animate() {

                            rotationVelocity += 2.0

                            rotator.localRotation = Vector(0.0, 0.0, rotationVelocity)
                        }
                    }
                    rotator.addChild(Seat("seat1", Vector(1.4, -0.3, 0.0), Vector()))
                    rotator.addChild(Seat("seat2", Vector(2.1, -0.3, 0.0), Vector()))
                    rotator.addChild(Seat("seat3", Vector(-1.4, -0.3, 0.0), Vector()))
                    rotator.addChild(Seat("seat4", Vector(-2.1, -0.3, 0.0), Vector()))
                    train.addCart(cart)
                }
                return train
            }
        })

        //lumbertrack_coaster
        trains.put("halloween_coaster", object : AbstractTrain() {
            override fun train(trackRide: TrackRide, startPosition: Double): Train {
                val train = Train("halloween_coaster", trackRide, 0.0)

                repeat(3) { i ->
                    val cart = Cart(2.0f, 0.3f)
                    var itemId = if (i == 0) 73 else 72
                    cart.addChild(
                        ItemAttachment(
                            "base",
                            ItemBuilder(Material.DIAMOND_SWORD).customModelData(itemId).build(),
                            Vector(-0.4, 0.1, 1.0),
                            Vector()
                        )
                    )
                    cart.addChild(Seat("seat1", Vector(0.4, 0.0, 1.0), Vector()))
                    cart.addChild(Seat("seat2", Vector(-0.4, 0.0, 1.0), Vector()))
                    cart.addChild(Seat("seat3", Vector(0.4, 0.0, -0.1), Vector()))
                    cart.addChild(Seat("seat4", Vector(-0.4, 0.0, -0.1), Vector()))
                    train.addCart(cart)
                }
                return train
            }
        })

    }

}

abstract class AbstractTrain {
    abstract fun train(trackRide: TrackRide, startPosition: Double): Train
}
