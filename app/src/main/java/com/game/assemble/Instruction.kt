package com.game.assemble

import android.content.Context
import android.content.res.Resources
import android.util.Log

class Instruction(
    other : Array<String> = arrayOf<String>()
) {
    var instr : Array<Any?> = arrayOf<Any?>(null, null, null, null)
    init {
        for (i in other.indices) {
            instr[i] = other[i]
        }
    }
    fun getKeyboardFromOperator() : IntArray {
        return when(instr[0]) {
            "add", "addu", "and", "nor", "or", "slt", "sltu", "sub", "subu" -> intArrayOf(keyboards[0], keyboards[0], keyboards[0])
            "addi", "addiu", "andi", "ori", "slti", "sltiu", "sll", "srl" -> intArrayOf(keyboards[0], keyboards[0], keyboards[1])
            "beq", "bne"-> intArrayOf(keyboards[0], keyboards[0], keyboards[2])
            "lbu", "lhu", "lw", "sb", "sh", "sw" -> intArrayOf(keyboards[0], keyboards[1], keyboards[0])
            "j" -> intArrayOf(keyboards[2])
            "jr" -> intArrayOf(keyboards[1])
            "lui" -> intArrayOf(keyboards[0], keyboards[1])

            else -> throw(IllegalArgumentException("Invalid operator or null detected"))
        }
    }
    fun getTemplateFromOperator() : Array<String> {
        return when (instr[0]) {
            "add", "addu", "and", "nor", "or", "slt", "sltu", "sub", "subu", "addi", "addiu", "andi", "ori", "slti", "sltiu", "sll", "srl", "beq", "bne" -> templates[0]
            "lbu", "lhu", "lw", "sb", "sh", "sw" -> templates[1]
            "j", "jr" -> templates[2]
            "lui" -> templates[3]
            null -> arrayOf("_")

            else -> throw(IllegalArgumentException("Invalid operator detected"))
        }
    }

    companion object {
        private var keyboards: IntArray = intArrayOf(
            R.id.operatorKeyboardLayout,
            R.id.digitsKeyboardLayout,
            R.id.lineNumberKeyboardLayout,
            R.id.registersKeyboardLayout
        )
        private var templates: Array<Array<String>> = arrayOf(
            arrayOf("_", "\t", "_", ",\t", "_", ",\t", "_"),
            arrayOf("_", "\t", "_", ",\t", "_", "(", "_", ")"),
            arrayOf("_", "\t", "_"),    // j, jr
            arrayOf("_", "\t", "_", ",\t" , "_")   // lui
        )
    }
}