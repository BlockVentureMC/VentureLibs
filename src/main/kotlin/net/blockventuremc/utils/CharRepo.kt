package net.blockventuremc.utils

import net.blockventuremc.utils.CharRepo.SpacingCharacters
import java.lang.StringBuilder
import kotlin.math.abs
import kotlin.math.pow


/**
 * Thanks to MitchGB for the great tutorial on how to create custom GUIs!
 * https://www.spigotmc.org/threads/custom-inventory-uis-updated.635897/
 * https://github.com/MitchGB/CustomInventoryUI/tree/main
 */
enum class CharRepo(val literal: String) {
    //Spacing Characters
    NEG1("\uF801"),
    NEG2("\uF802"),
    NEG4("\uF804"),
    NEG8("\uF808"),
    NEG16("\uF809"),
    NEG32("\uF80A"),
    NEG64("\uF80B"),
    NEG128("\uF80C"),
    NEG256("\uF80D"),
    NEG512("\uF80E"),
    NEG1024("\uF80F"),

    POS1("\uF821"),
    POS2("\uF822"),
    POS4("\uF824"),
    POS8("\uF828"),
    POS16("\uF829"),
    POS32("\uF82A"),
    POS64("\uF82B"),
    POS128("\uF82C"),
    POS256("\uF82D"),
    POS512("\uF82E"),
    POS1024("\uF82F"),

    MENU_CONTAINER_27("\uF001"),
    MENU_BUTTON("\uF002"),
    MENU_SLIM_54("\uF003"),
    MENU_GENERIC_54("\uF004")
    ;

    override fun toString(): String {
        return this.literal
    }

    private enum class SpacingCharacters(val weight: Int, val charRef: CharRepo) {
        NEG1(-1, CharRepo.NEG1),
        NEG2(-2, CharRepo.NEG2),
        NEG4(-4, CharRepo.NEG4),
        NEG8(-8, CharRepo.NEG8),
        NEG16(-16, CharRepo.NEG16),
        NEG32(-32, CharRepo.NEG32),
        NEG64(-64, CharRepo.NEG64),
        NEG128(-128, CharRepo.NEG128),
        NEG256(-256, CharRepo.NEG256),
        NEG512(-512, CharRepo.NEG512),
        NEG1024(-1024, CharRepo.NEG1024),

        POS1(1, CharRepo.POS1),
        POS2(2, CharRepo.POS2),
        POS4(4, CharRepo.POS4),
        POS8(8, CharRepo.POS8),
        POS16(16, CharRepo.POS16),
        POS32(32, CharRepo.POS32),
        POS64(64, CharRepo.POS64),
        POS128(128, CharRepo.POS128),
        POS256(256, CharRepo.POS256),
        POS512(512, CharRepo.POS512),
        POS1024(1024, CharRepo.POS1024);
    }

    companion object {
        fun getCharacterByWeight(weight: Int): CharRepo? {
            for (ch in SpacingCharacters.entries) {
                if (ch.weight == weight) return ch.charRef
            }
            return null
        }

        fun getSpacing(pixelAmount: Int): String {
            //convert amount to binary string
            val binary = StringBuilder(Integer.toBinaryString(abs(pixelAmount))).reverse().toString()
            val sb = StringBuilder()
            val chArr = binary.toCharArray()
            for (index in chArr.indices) {
                val ch = chArr[index]
                if (ch == '0') continue

                var weight = 2.0.pow(index.toDouble()).toInt()
                //if we are getting negative, flip weight
                weight = if (pixelAmount < 0) -weight else weight
                val ref = getCharacterByWeight(weight)

                if (ref != null) sb.append(ref.literal)
            }
            return sb.toString()
        }

        fun getNeg(pixelAmount: Int): String {
            return CharRepo.Companion.getSpacing(-abs(pixelAmount))
        }

        fun getPos(pixelAmount: Int): String {
            return CharRepo.Companion.getSpacing(abs(pixelAmount))
        }
    }
}