package com.game.assemble

import android.util.Log

class Instruction(
    other : Array<String?> = arrayOf(null, null, null, null)
) {
    private var instr: Array<String?> = arrayOf(null, null, null, null)
    init {
        for (i in other.indices){
            if (other[i] == "_") instr[i] = null
            else instr[i] = other[i]
        }
    }
    val size = instr.size
    operator fun get(idx: Int) : String? {
        if (idx < 0 || idx > instr.size) throw(IllegalArgumentException("Index out of bound"))
        else return instr[idx]
    }
    operator fun set(idx: Int, data: String) {
        instr[idx] = data
    }
    fun isLabel(label: String? = null) : Boolean {
        return when (instr[0]) {
            "add", "addu", "and", "nor", "or", "slt", "sltu", "sub", "subu","addi", "addiu", "andi", "ori", "slti", "sltiu", "sll", "srl", "beq", "bne", "lbu", "lhu", "lw", "sb", "sh", "sw", "j", "lui", "_" -> false
            else -> instr[0] == (label?:instr[0]!!.removeSuffix(":")) +  ":"
        }
    }
    fun getKeyboardFromOperator() : IntArray {
        return when(instr[0]?.removePrefix("\t")) {
            "add", "addu", "and", "nor", "or", "slt", "sltu", "sub", "subu" -> intArrayOf(keyboards[5], keyboards[4], keyboards[4])
            "addi", "addiu", "andi", "ori", "slti", "sltiu" -> intArrayOf(keyboards[5], keyboards[4], keyboards[2])
            "sll", "srl" -> intArrayOf(keyboards[5], keyboards[4], keyboards[1])
            "beq", "bne"-> intArrayOf(keyboards[4], keyboards[4], keyboards[3])
            "lbu", "lhu", "lw" -> intArrayOf(keyboards[5], keyboards[2], keyboards[4])
            "sb", "sh", "sw" -> intArrayOf(keyboards[4], keyboards[2], keyboards[4])
            "j" -> intArrayOf(keyboards[3])
            "lui" -> intArrayOf(keyboards[5], keyboards[2])
            "_", null -> intArrayOf()

            else -> {
                Log.i("oh no", instr[0]!!)
                throw(IllegalArgumentException("Invalid operator detected"))
            }
        }
    }
    fun getTemplateFromOperator() : Array<String> {
        return when (instr[0]?.removePrefix("\t")) {
            "add", "addu", "and", "nor", "or", "slt", "sltu", "sub", "subu", "addi", "addiu", "andi", "ori", "slti", "sltiu", "sll", "srl", "beq", "bne" -> templates[0]
            "lbu", "lhu", "lw", "sb", "sh", "sw" -> templates[1]
            "j" -> templates[2]
            "lui" -> templates[3]
            null -> arrayOf("_")
            "_" -> arrayOf("_")
            else -> arrayOf("_")
        }
    }
    fun hasNull() : Boolean {
        return instr.contains(null)
    }

    companion object {
        private var keyboards: IntArray = intArrayOf(
            R.id.operatorKeyboardLayout,
            R.id.shamtDigitKeyboardLayout,
            R.id.immedDigitKeyboardLayout,
            R.id.lineNumberKeyboardLayout,
            R.id.registersKeyboardLayout,
            R.id.registers2KeyboardLayout
        )
        private var templates: Array<Array<String>> = arrayOf(
            arrayOf("_", "\t", "_", ",\t", "_", ",\t", "_"),
            arrayOf("_", "\t", "_", ",\t", "_", "(", "_", ")"),
            arrayOf("_", "\t", "_"),    // j, jr
            arrayOf("_", "\t", "_", ",\t" , "_")   // lui
        )
    }
}