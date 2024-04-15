package com.game.assemble
import android.content.Context
import android.util.Log
import java.util.Calendar

const val CODE_START = 0x00400000
const val STACK_START = 0x7ffffffc
class MIPSSimulator(
    context: Context,
    ) {
    var regs: Registers = Registers()
    private val gameTask: GameTask = GameTask(context)
    private val stack: ByteArray = byteArrayOf()
    private fun parseLabel(label: String, instrList: MutableList<Instruction>) : Int? {
        if (label == "exit") return null
        for (instr in instrList) {
            if (instr.isLabel(label)) return instrList.indexOf(instr)
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

    private fun parseCodeAddress(addr: Int, instrList: MutableList<Instruction>) : Int? {
        if ((CODE_START - addr) % 4 != 0) return null
        var temp: Int = (CODE_START - addr)/4
        for (i in instrList.indices) {
            if (temp == 0) return i
            if (instrList[i].isLabel()) continue
            else if (instrList[i - 1][0] == "jal") temp -= 2
            else temp--
        }
        return null
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

    fun generateTask(id: Int? = null) : String {
        Log.i("generateTask()", "Generating task...")
        gameTask.info["id"] = id?:gameTask.getRandomTask()
        Log.i("generateTask()", "Obtained task ID " + gameTask.info["id"].toString() + "...")
        when (gameTask.info["id"]) {
            0 -> {  // LCM of a0, a1, return in v0
                regs["\$a0"] = (4..999).random()
                regs["\$a1"] = (4..999).random()
                gameTask["goal"] = gameTask.findLCM(regs["\$a0"], regs["\$a1"])
            }
            1 -> {  // Sort array in ascending order
                regs["\$a0"] = stack.size + STACK_START
                gameTask["addr"] = stack.size
                regs["\$a1"] = (5..20).random()
                gameTask["size"] = regs["\$a1"]
                for (i in (0..<regs["\$a1"])) {
                    addToStack(convertIntToByteArray((-999..999).random()), stack.size, 4)
                }
                gameTask["goal"] = stack.copyOfRange(gameTask["addr"] as Int, (gameTask["addr"] as Int) + (gameTask["size"] as Int) - 1).sort()
            }
            2 -> {  // GCD of a0, a1, return in v0
                regs["\$a0"] = (4..999).random()
                regs["\$a1"] = (4..999).random()
                gameTask["goal"] = gameTask.findGCD(regs["\$a0"], regs["\$a1"])
            }
            3 -> { // Sum of all even primes
                Log.i("generateTask()", "Setting goal...")
                gameTask["goal"] = 2
            }
        }
        Log.i("generateTask()", "Returning...")
        return gameTask["text"].toString()
    }
    fun validateTask(instrList: MutableList<Instruction>) : Boolean {
        val initState = Registers(regs)
        for (i in 0 until 10) {
            Log.i("validateTask()", "Running test case $0")
            regs = Registers(initState)
            val err = run(instrList)
            if (err.isNotBlank()) {
                Log.i("validateTask()", err)
                return false
            }
            when (gameTask.info["id"] as Int) {
                0 -> {  // LCM of a0, a1, return in v0
                    if (regs["\$v0"] != gameTask["goal"] as Int) return false
                }

                1 -> {  // Sort array in ascending order
                    for (j in (gameTask["addr"] as Int)..<(gameTask["addr"] as Int) + (gameTask["size"] as Int) - 1) {
                        if (stack[j] > stack[j + 1]) return false
                    }
                }

                2 -> {  // GCD of a0, a1, return in v0
                    if (regs["\$v0"] != gameTask.info["goal"] as Int) return false
                }

                3 -> { // Sum of all even primes into v0
                    if (regs["\$v0"] != gameTask.info["goal"] as Int) return false
                }
            }
            Log.i("validateTask", "Test case $i completed")
            GameActivity.currentTask = generateTask(gameTask.info["id"] as Int?)
        }
        return true
    }

    fun printState() {
        Log.d("printState", "Printing State")
        for (reg in regs) {
            Log.d("PrintState", reg.toString())
        }
    }

    fun run(instrList: MutableList<Instruction>) : String {
        val savedRegs = Registers(regs)
        val startTime = Calendar.getInstance().time.time
        var line = 0
        while (true) {
            if (line >= instrList.size) return "The program never jumped to exit!"
            val instr: Instruction = instrList[line]
            //Log.d("[*] MIPSSimulator.Run", "line " + line.toString())
            if (Calendar.getInstance().time.time - startTime > 2000) {
                regs = savedRegs
                return "Timed out! Check if your code will result in infinite loop!"
            }
            if (instr.hasNull()) {
                regs = savedRegs
                return "Some fields are empty!"
            }
            line++
            if (instr.isLabel()) continue
            when (instr[0]) {
                "add" -> {   // add
                    val temp: Long = regs[instr[2]].toLong() + regs[instr[3]].toLong()
                    if (temp <= Int.MAX_VALUE && temp >= Int.MIN_VALUE) {
                        regs[instr[1] as String] = temp.toInt()
                    } else {
                        regs = savedRegs
                        return "Overflow exception!"
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
                        regs = savedRegs
                        return "Overflow exception"
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
                        regs = savedRegs
                        return "Overflow exception"
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
                        val temp = parseLabel(instr[3]!!, instrList)
                        if (temp == null) return "" // Exit is jumped to
                        else line = temp
                    }
                }

                "bne" -> {    // bne
                    if (regs[instr[2]] != regs[instr[1]]) {
                        val temp = parseLabel(instr[3]!!, instrList)
                        if (temp == null) return "" // Exit is jumped to
                        else line = temp
                    }
                }

                "j" -> {    // j
                    val temp = parseLabel(instr[1]!!, instrList)
                    if (temp == null) return "" // Exit is jumped to
                    else line = temp
                }

                "jal" -> {
                    regs["\$ra"] = CODE_START
                    for (i in 0..<line) {
                        if (!instrList[i].isLabel()) {
                            regs["\$ra"] += 4
                        }
                    }
                    regs["\$ra"] + 8

                    val temp = parseLabel(instr[1]!!, instrList)
                    if (temp == null) return "" // Exit is jumped to
                    else line = temp

                }

                "jr" -> {
                    val temp = parseCodeAddress(regs["\$rs"], instrList)
                    if (temp == null) {
                        regs = savedRegs
                        return "Invalid code address"
                    }
                    line = temp
                }

                "lbu" -> {   // lbu
                    val temp = parseStackAddress(regs[instr[3]] + instr[2]!!.toInt())
                    if (temp == null) {
                        regs = savedRegs
                        return "Invalid stack address"
                    }
                    regs[instr[1]] = stack[temp].toInt() shl 24 ushr 24
                }

                "lhu" -> {   // lhu
                    val temp = parseStackAddress(regs[instr[3]] + instr[2]!!.toInt())
                    if (temp == null) {
                        regs = savedRegs
                        return "Invalid stack address"
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
                        regs = savedRegs
                        return "Invalid stack address"
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
                        regs = savedRegs
                        return "Invalid stack address"
                    }
                    val tempArr = byteArrayOf((regs[instr[1]] shl 24 ushr 24).toByte())
                    addToStack(tempArr, temp, 1)
                }

                "sh" -> {   // sh
                    val temp = parseStackAddress(regs[instr[3]] + instr[2]!!.toInt())
                    if (temp == null) {
                        regs = savedRegs
                        return "Invalid stack address"
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
                        regs = savedRegs
                        return "Invalid stack address"
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
                    regs = savedRegs
                    return "Unknown instruction"
                }
            }
        }
    }
}