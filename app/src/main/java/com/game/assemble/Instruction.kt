package com.game.assemble

import android.content.Context
import android.content.res.Resources
import android.util.Log

class Instruction(
    var instr : Array<String?> = arrayOf<String?>()
) {
    operator fun get(idx: Int) : String? {
        if (idx < 0 || idx > instr.size) throw(IllegalArgumentException("Index out of bound"))
        else return instr[idx]
    }
    operator fun set(idx: Int, data: String) {
        instr[idx] = data
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
    fun hasNull() : Boolean {
        return instr.contains(null)
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