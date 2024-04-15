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
            "add", "addu", "and", "nor", "or", "slt", "sltu", "sub", "subu", "addi", "addiu", "andi", "ori", "slti", "sltiu", "sll", "srl", "beq", "bne" -> false
            "sra", "sllv", "srlv", "srav", "xor", "xori", "lbu", "lhu", "lw", "sb", "sh", "sw", "lb", "lh", "j", "jr", "jal", "jalr", "mfhi", "mflo", "mthi", "mtlo" -> false
            "lui", "mult", "multu", "div", "divu", "blez", "bgtz" -> false
            else -> instr[0] == ((label?:instr[0]!!.removeSuffix(":")) + ":")
        }
    }
    fun getKeyboardFromOperator() : IntArray {
        return when(instr[0]?.removePrefix("\t")) {
            "add", "addu", "and", "nor", "or", "slt", "sltu", "sub", "subu", "sllv", "srlv", "srav", "xor" -> intArrayOf(keyboards[5], keyboards[4], keyboards[4])
            "addi", "addiu", "andi", "ori", "slti", "sltiu", "xori" -> intArrayOf(keyboards[5], keyboards[4], keyboards[2])
            "sll", "srl", "sra" -> intArrayOf(keyboards[5], keyboards[4], keyboards[1])
            "beq", "bne"-> intArrayOf(keyboards[4], keyboards[4], keyboards[3])
            "blez", "bgtz" -> intArrayOf(keyboards[4], keyboards[3])
            "lbu", "lhu", "lw", "lb", "lh" -> intArrayOf(keyboards[5], keyboards[2], keyboards[4])
            "sb", "sh", "sw" -> intArrayOf(keyboards[4], keyboards[2], keyboards[4])
            "jr", "jalr", "mfhi", "mflo"-> intArrayOf(keyboards[5])
            "j", "jal" -> intArrayOf(keyboards[3])
            "lui" -> intArrayOf(keyboards[5], keyboards[2])
            "mthi", "mtlo" -> intArrayOf(keyboards[4])
            "mult", "multu", "div", "divu" -> intArrayOf(keyboards[4], keyboards[4])
            else -> intArrayOf()
        }
    }
    fun getTemplateFromOperator() : Array<String> {
        return when (instr[0]?.removePrefix("\t")) {
            "add", "addu", "and", "nor", "or", "slt", "sltu", "sub", "subu", "addi", "addiu", "andi", "ori", "slti", "sltiu", "sll", "srl", "beq", "bne" -> templates[0]
            "sra", "sllv", "srlv", "srav", "xor", "xori" -> templates[0]
            "lbu", "lhu", "lw", "sb", "sh", "sw", "lb", "lh" -> templates[1]
            "j", "jr", "jal", "jalr", "mfhi", "mflo", "mthi", "mtlo" -> templates[2]
            "lui", "mult", "multu", "div", "divu", "blez", "bgtz" -> templates[3]
            null -> arrayOf("_")
            "_" -> arrayOf("_")
            else -> arrayOf("_")
        }
    }
    fun hasNull() : Boolean {
        if (instr[0] == null) return true
        else {
            for (i in getKeyboardFromOperator().indices) {
                if (instr[i + 1] == null) return true
            }
        }
        return false
    }

    fun logInstr() {
        val template = getTemplateFromOperator()
        var res = ""
        for (i in template.indices) {
            res += if (i % 2 == 0) instr[i/2]!!
            else template[i]
        }
        Log.d("logInstr()", res)
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
            arrayOf("_", "\t", "_"),
            arrayOf("_", "\t", "_", ",\t" , "_")
        )
    }
}