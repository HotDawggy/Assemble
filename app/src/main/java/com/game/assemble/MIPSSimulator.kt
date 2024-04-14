package com.game.assemble
import android.content.Context
import android.util.Log
import java.util.Calendar

const val CODE_START = 0x00400000
const val STACK_START = 0x7ffffffc
class MIPSSimulator(
    context: Context,
    ) {
    private var regs: Registers = Registers()
    private val gameTask: GameTask = GameTask(context)
    private val stack: ByteArray = byteArrayOf()
    private fun parseLabel(label: String, instrList: Array<Instruction>) : Int {
        for (instr in instrList) {
            if (instr.hasLabel(label)) return instrList.indexOf(instr)
        }
        throw(IllegalArgumentException("Invalid label"))
    }

    private fun parseStackAddress(addr: Int) : Int? {
        val temp = STACK_START - addr - 1
        return if (temp < 0) {
            null
        } else {
            temp
        }
    }
    private fun zeroExtendImmediate(source: Int) : Int {
        return ((source shl 17) ushr 17)
    }

    private fun bigEndianConversion(bytes: ByteArray): Int {
        var result = 0
        for (i in bytes.indices) {
            result = (bytes[i].toInt() shl 8 * i) or result
        }
        return result
    }
    private fun addToStack(bytes: ByteArray, index: Int, size: Int, updateSp: Boolean = false) {
        var temp = index
        Log.i("addToStack", stack.toString())
        //printStack()
        for (i in 0..< size) {
            if (temp >= stack.size) stack + byteArrayOf((bytes[i]))
            else stack[index] = bytes[i]
            temp++
            if (updateSp) regs["\$sp"] = regs["\$sp"] - 1
        }
    }
    private fun convertIntToByteArray(input : Int) : ByteArray {
        var temp = input
        var res = byteArrayOf()
        for (i in 0..3) {
            res = byteArrayOf((temp.toByte())) + byteArrayOf()
            temp = temp ushr 8
        }
        return res
    }


    fun printStack() {
        Log.d("printStack", "Printing Stack")
        Log.d("printStack", "Size: " + stack.size.toString())
        for (byte in stack) {
            Log.d("printStack", byte.toString())
        }
    }

    fun generateTask() : String {
        gameTask.info["id"] = gameTask.getRandomTask()
        when (gameTask.info["id"]) {
            0 -> {  // LCM of a0, a1, return in v0
                regs["\$a0"] = (4..999).random()
                regs["\$a1"] = (4..999).random()
                gameTask.info["goal"]= gameTask.findLCM(regs["\$a0"], regs["\$a1"])
            }
            1 -> {  // Sort array in ascending order
                regs["\$a0"] = stack.size + STACK_START
                gameTask.info["addr"] = stack.size
                regs["\$a1"] = (5..20).random()
                gameTask.info["size"] = regs["\$a1"]
                for (i in (0..<regs["\$a1"])) {
                    addToStack(convertIntToByteArray((1..999).random()), stack.size, 4)
                }
            }
            2 -> {  // GCD of a0, a1, return in v0
                regs["\$a0"] = (4..999).random()
                regs["\$a1"] = (4..999).random()
                gameTask.info["goal"] = gameTask.findGCD(regs["\$a0"], regs["\$a1"])
            }
            3 -> { // Sum of all even primes
                gameTask.info["goal"] = 2
            }
        }
        return gameTask.info["text"] as String
    }
    fun validateTask() : Boolean {
        when (gameTask.info["id"] as Int) {
            0 -> {  // LCM of a0, a1, return in v0
                if (regs["\$v0"] == gameTask.info["goal"] as Int) return true
            }
            1 -> {  // Sort array in ascending order
                for (i in (gameTask.info["addr"] as Int)..< (gameTask.info["addr"] as Int) + (gameTask.info["size"] as Int) - 1) {
                    if (stack[i] > stack[i+1]) return false
                }
                return true
            }
            2 -> {  // GCD of a0, a1, return in v0
                if (regs["\$v0"] == gameTask.info["goal"] as Int) return true
            }
            3 -> { // Sum of all even primes into v0
                if (regs["\$v0"] == gameTask.info["goal"] as Int) return true
            }
        }
        return false
    }

    fun printState() {
        Log.d("printState", "Printing State")
        for (reg in regs) {
            Log.d("PrintState", reg.toString())
        }
    }

    fun run(instrList: Array<Instruction>) {
        val savedRegs = Registers(regs)
        var err = ""
        var line = 0
        val startTime = Calendar.getInstance().time.time
        while (true) {
            if ((line >= instrList.size) || (err.isNotBlank())) return
            val instr: Instruction = instrList[line]
            //Log.d("[*] MIPSSimulator.Run", "line " + line.toString())
            if (Calendar.getInstance().time.time - startTime > 2000) {
                regs = savedRegs
                err = "Timed out! Check if your code will result in infinite loop!"
                return
            }
            if (instr.hasNull()) {
                regs = savedRegs
                err = "Some fields are empty!"
            }
            line++
            when (instr[0]) {
                "add" -> {   // add
                    val temp: Long = regs[instr[2]].toLong() + regs[instr[3]].toLong()
                    if (temp <= Int.MAX_VALUE && temp >= Int.MIN_VALUE) {
                        regs[instr[1] as String] = temp.toInt()
                    } else {
                        err = "Overflow exception!"
                        return
                    }
                }

                "addu" -> {   // addu
                    regs[instr[1]] =
                        (regs[instr[2]].toUInt() + regs[instr[3]].toUInt()).toInt()
                }

                "and" -> {   // and
                    regs[instr[1]] = regs[instr[2]] and (regs[instr[3]])
                }

                "nor" -> {   // nor
                    regs[instr[1]] = (regs[instr[2]] or regs[instr[3]]).inv()
                }

                "or" -> {   // or
                    regs[instr[1]] = regs[instr[2]] or regs[instr[3]]
                }

                "slt" -> {   // slt
                    regs[instr[1]] = if (regs[instr[2]] < regs[instr[3]]) 1 else 0
                }

                "sltu" -> {   // sltu
                    regs[instr[1]] =
                        if (regs[instr[2]].toUInt() < regs[instr[3]].toUInt()) 1 else 0
                }

                "sll" -> {    // sll
                    regs[instr[1]] = regs[instr[2]] shl (instr[3]!!.toInt())
                }

                "srl" -> {    // srl
                    regs[instr[1]] = regs[instr[2]] shr (instr[3]!!.toInt())
                }

                "sub" -> {   // sub
                    val temp: Long = regs[instr[2]].toLong() - regs[instr[3]].toLong()
                    if (temp <= Int.MAX_VALUE && temp >= Int.MIN_VALUE) {
                        regs[instr[1]] = temp.toInt()
                    } else {
                        err = "Overflow exception"
                        return
                    }
                }

                "subu" -> {   // subu
                    regs[instr[1]] =
                        (regs[instr[2]].toUInt() - regs[instr[3]].toUInt()).toInt()
                }

                "addi" -> {    // addi
                    val temp: Long = regs[instr[2]].toLong() + instr[3]!!.toInt()
                    if (temp <= Int.MAX_VALUE && temp >= Int.MIN_VALUE) {
                        regs[instr[1]] = temp.toInt()
                    } else {
                        err = "Overflow exception"
                        return
                    }
                }

                "addiu" -> {    // addiu
                    regs[instr[1]] =
                        (regs[instr[2]].toUInt() + instr[3]!!.toUInt()).toInt()
                }

                "andi" -> {    // andi
                    regs[instr[1]] = regs[instr[2]].and(zeroExtendImmediate(instr[3]!!.toInt()))
                }

                "beq" -> {    // beq
                    if (regs[instr[2]] == regs[instr[1]]) {
                        line = parseLabel(instr[3]!!, instrList)
                    }
                }

                "bne" -> {    // bne
                    if (regs[instr[2]] != regs[instr[1]]) {
                        line = parseLabel(instr[3]!!, instrList)
                    }
                }

                "j" -> {    // j
                    line = parseLabel(instr[1]!!, instrList)
                }

                "lbu" -> {   // lbu
                    val temp = parseStackAddress(regs[instr[3]] + instr[2]!!.toInt())
                    if (temp == null) {
                        err = "Invalid stack address"
                        return
                    }
                    regs[instr[1]] = stack[temp].toInt() shl 24 ushr 24
                }

                "lhu" -> {   // lhu
                    val temp = parseStackAddress(regs[instr[3]] + instr[2]!!.toInt())
                    if (temp == null) {
                        err = "Invalid stack address"
                        return
                    }
                    regs[instr[1]] =
                        bigEndianConversion(stack.copyOfRange(temp - 1, temp + 1)) shl 16 ushr 16
                }

                "lui" -> {    // lui
                    regs[instr[1]] = instr[2]!!.toInt() shl 17 ushr 1
                }

                "lw" -> {   // lw
                    val temp = parseStackAddress(regs[instr[3]] + instr[2]!!.toInt())
                    if (temp == null) {
                        err = "Invalid stack address"
                        return
                    }
                    regs[instr[1]] = bigEndianConversion(stack.copyOfRange(temp - 3, temp + 1))
                }

                "ori" -> {    // ori
                    regs[instr[1]] = regs[instr[2]] or (zeroExtendImmediate(instr[3]!!.toInt()))
                }

                "slti" -> {    // slti
                    regs[instr[1]] = if (regs[instr[2]] < instr[3]!!.toInt()) 1 else 0
                }

                "sltiu" -> {    // sltiu
                    regs[instr[1]] =
                        if (regs[instr[2]].toUInt() < instr[3]!!.toUInt()) 1 else 0
                }

                "sb" -> {   // sb
                    val temp = parseStackAddress(regs[instr[3]] + instr[2]!!.toInt())
                    if (temp == null) {
                        err = "Invalid stack address"
                        return
                    }
                    val tempArr = byteArrayOf((regs[instr[1]] shl 24 ushr 24).toByte())
                    addToStack(tempArr, temp, 1)
                }

                "sh" -> {   // sh
                    val temp = parseStackAddress(regs[instr[3]] + instr[2]!!.toInt())
                    if (temp == null) {
                        err = "Invalid stack address"
                        return
                    }
                    val tempArr = byteArrayOf(
                        (regs[instr[1]] shl 16 ushr 24).toByte(),
                        (regs[instr[1]] shl 24 ushr 24).toByte()
                    )
                    addToStack(tempArr, temp, 2)
                }

                "sw" -> {   // sw
                    val temp = parseStackAddress(regs[instr[3]] + instr[2]!!.toInt())
                    if (temp == null) {
                        err = "Invalid stack address"
                        return
                    }
                    val tempArr = byteArrayOf(
                        (regs[instr[1]] ushr 24).toByte(),
                        (regs[instr[1]] shl 8 ushr 24).toByte(),
                        (regs[instr[1]] shl 16 ushr 24).toByte(),
                        (regs[instr[1]] shl 24 ushr 24).toByte()
                    )
                    addToStack(tempArr, temp, 4)
                }
                else -> {
                    err = "Unknown instruction"
                    return
                }
            }
        }
    }
}