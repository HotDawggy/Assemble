package com.game.assemble

import android.content.Context
import android.content.res.Resources
import android.util.Log

class Instruction(
    var opcode: Int? = null,    // 0
    var rs: Int? = null,        // 1
    var rt: Int? = null,        // 2
    var rd: Int? = null,        // 3
    var shamt: Int? = null,     // 4
    var funct: Int? = null,     // 5
    var immediate: Int? = null, // 6
    var address: Int? = null,   // 7
    var label: String? = null   // 8
) {

    fun updateField(field: Int, data: Any){
        when (field) {
            1 -> rs = data as Int
            2 -> rt = data as Int
            3 -> rd = data as Int
            4 -> shamt = data as Int
            6 -> immediate = data as Int
            8 -> label = data as String
            else -> throw(IllegalArgumentException("Incorrect field"))
        }
    }
    companion object {
        private var registerLookup = mutableMapOf<Int?, String>(null to "_")
        private var opcodeLookup = mutableMapOf<Int?, String>(null to "_")
        private var functLookup = mutableMapOf<Int?, String>(null to "_")
        private var bitLookup = mutableMapOf<String, Int>()
        private var init: Boolean = false
        fun isInit(): Boolean {
            return init
        }

        fun initLookup(context: Context) {
            val regsKey = context.resources.getIntArray(R.array.regsKey)
            val regsString = context.resources.getStringArray(R.array.regsString)
            val opcodeKey = context.resources.getIntArray(R.array.opcodeKey)
            val opcodeString = context.resources.getStringArray(R.array.opcodeString)
            val functKey = context.resources.getIntArray(R.array.functKey)
            val functString = context.resources.getStringArray(R.array.functString)
            val bitString = context.resources.getStringArray(R.array.instr_r) + context.resources.getStringArray(R.array.instr_i) + context.resources.getStringArray(R.array.instr_j)
            val bitKey = context.resources.getIntArray(R.array.funct_r) + context.resources.getIntArray(R.array.opcode_i) + context.resources.getIntArray(R.array.opcode_j)
            for (i in regsKey.indices) {
                registerLookup[regsKey[i]] = regsString[i]
            }
            for (i in opcodeKey.indices) {
                opcodeLookup[opcodeKey[i]] = opcodeString[i]
            }
            for (i in functKey.indices) {
                functLookup[functKey[i]] = functString[i]
            }
            for (i in bitKey.indices) {
                bitLookup[bitString[i]] = bitKey[i]
            }
            init = true
        }

        fun getField(instr: Instruction): IntArray {
            return when (instr.opcode) {
                0x0 -> {
                    intArrayOf(5) + when (instr.funct) {
                        0x20, 0x21, 0x24, 0x27, 0x25, 0x2a, 0x2b, 0x22, 0x23 -> intArrayOf(3,1,2)
                        0x08 -> intArrayOf(1)
                        0x00, 0x02 -> intArrayOf(3,2,4)
                        else -> throw IllegalArgumentException("Incorrect funct!")
                    }
                }
                else -> intArrayOf(0) + when(instr.opcode) {
                    0x8, 0x9, 0xc, 0xd, 0xa, 0xb -> intArrayOf(2,1,6)
                    0x24, 0x25, 0x23, 0x28, 0x29, 0x2b -> intArrayOf(2,6,1)
                    0x4, 0x5 -> intArrayOf(1,2,8)
                    0xf -> intArrayOf(1,6)
                    0x2, 0x3 -> intArrayOf(8)
                    null -> intArrayOf()
                    else -> throw IllegalArgumentException("Incorrect opcode!")
                }
            }
        }
        private fun parseField(instr: Instruction, field: Int) : String {
            return when (field) {
                0 -> when(instr.opcode) {
                    0x0 -> functLookup[instr.funct]!!
                    null -> "_"
                    else -> opcodeLookup[instr.opcode]!!
                }
                1 -> registerLookup[instr.rs]!!
                2 -> registerLookup[instr.rt]!!
                3 -> registerLookup[instr.rd]!!
                4 -> instr.shamt.toString()
                6 -> instr.immediate.toString()
                8 -> instr.label ?: "_"
                else -> throw(IllegalArgumentException("Incorrect field"))
            }
        }
        fun stringify(instr: Instruction, context: Context) : Array<String> {
            var counter = 0
            var template = arrayOf<String>()
            var res = arrayOf<String>()
            var fieldList: IntArray = getField(instr)
            when (instr.opcode) {
                0x0 -> {
                    when (instr.funct) {
                        0x8 -> template = context.resources.getStringArray(R.array.template_j_jr)
                        else -> template = context.resources.getStringArray(R.array.template)
                    }
                }
                null -> {
                    return arrayOf(instr.label?:"_")
                }
                else -> {
                    when (instr.opcode) {
                        0x2 -> template = context.resources.getStringArray(R.array.template_j_jr)
                        0xf -> template = context.resources.getStringArray(R.array.template_lui)
                        0x24, 0x25, 0x23, 0x28, 0x29, 0x2b -> template = context.resources.getStringArray(R.array.template_m)
                        0x24, 0x25, 0x23, 0x28, 0x29, 0x2b -> template = context.resources.getStringArray(R.array.template_m)
                        else -> return res
                    }
                }
            }
            for (i in template.indices) {
                if (template[i] == "_") {
                    Log.i("before parseField", fieldList[counter].toString())
                    res += parseField(instr, fieldList[counter])
                    counter++
                }
                else res += template[i]
            }
            return res
        }
        fun getKeyboardLayout(field: Int): Int {
            return when (field) {
                0 -> R.id.operatorKeyboardLayout
                1,2,3 -> R.id.gameInstructionRegisterLayout2
                4,6 -> R.id.digitsKeyboardLayout
                8 -> R.id.lineNumberKeyboardLayout
                else -> throw(IllegalArgumentException("Invalid pos given"))
            }
        }
    }
}