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
    private var exit = false
    val gameTask: GameTask = GameTask(context)
    private var stack: ByteArray = byteArrayOf()

    private fun parseLabel(label: String, instrList: MutableList<Instruction>) : Int? {
        if (label == "exit") {Log.d("parseLabel", "Exit!"); exit = true; return null}
        for (instr in instrList) {
            if (instr.isLabel(label)) {
                Log.d("parseLabel", "label = $label, instr = " + instr[0])
                Log.d("parseLabel", "Label at " + instrList.indexOf(instr).toString())
                return instrList.indexOf(instr)
            }
        }
        return null
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
    private fun zeroExtendImmediate(source: Int, amount: Int = 16) : Int {
        return ((source shl amount) ushr amount)
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
        //Log.i("generateTask()", "Generating task...")
        gameTask.info["id"] = id?:gameTask.getRandomTask()
        //Log.i("generateTask()", "Obtained task ID " + gameTask.info["id"].toString() + "...")
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
                //Log.i("generateTask()", "Setting goal...")
                gameTask["goal"] = 2
            }
            4 -> {
                regs["\$a0"] = (4..999).random()
                regs["\$a1"] = (4..999).random()
                gameTask["goal"] = regs["\$a0"] * regs["\$a1"]
            }
        }
        //Log.i("generateTask()", "Returning...")
        return gameTask["text"].toString()
    }
    fun validateTask(instrList: MutableList<Instruction>) : Array<String> {
        val initState = Registers(regs)
        Log.i("validateTask()", "Running test case")
        //printState()
        val err = run(instrList)
        if (err.isNotBlank()) {
            Log.i("validateTask()", err)
            return arrayOf("Error!",err)
        }
        Log.i("validateTask()", "Obtained output: " + regs["\$v0"].toString())
        when (gameTask.info["id"] as Int) {
            0 -> {  // LCM of a0, a1, return in v0
                if (regs["\$v0"] != gameTask["goal"] as Int) {
                    return arrayOf("Failed!", "Expected: " + gameTask["goal"] + "\nObtained: " + regs["\$v0"])
                }
            }

            1 -> {  // Sort array in ascending order
                for (j in (gameTask["addr"] as Int)..<(gameTask["addr"] as Int) + (gameTask["size"] as Int) - 1) {
                    return arrayOf("Failed!", "Array not sorted")
                }
            }

            2 -> {  // GCD of a0, a1, return in v0
                if (regs["\$v0"] != gameTask.info["goal"] as Int) {
                    return arrayOf("Failed!", "Expected: " + gameTask["goal"] + "\nObtained: " + regs["\$v0"])
                }
            }

            3 -> { // Sum of all even primes into v0
                if (regs["\$v0"] != gameTask.info["goal"] as Int) {
                    return arrayOf("Failed!", "Expected: " + gameTask["goal"] + "\nObtained: " + regs["\$v0"])
                }
            }

            4 -> {  // Multiply a0 and a1 and return in v0
                if (regs["\$v0"] != gameTask.info["goal"] as Int) {
                    return arrayOf("Failed!", "Expected: " + gameTask["goal"] + "\nObtained: " + regs["\$v0"])
                }
            }
        }
        Log.i("validateTask", "Test case completed")
        regs = Registers(initState)
        stack = byteArrayOf()
        return arrayOf("Success!", "")
    }

    fun printState(idx: String? = null) {
        Log.d("printState", "Printing State")
        if (idx == null) {
            for (reg in regs) {
                Log.d("PrintState", reg.toString())
            }
        } else {
            Log.d("PrintSate", regs[idx].toString())
        }
    }

    fun run(instrList: MutableList<Instruction>) : String {
        val savedRegs = Registers(regs)
        val startTime = Calendar.getInstance().time.time
        var line = 0
        exit = false
        while (true) {
            // printState("\$v0")
            if (line >= instrList.size) return "The program never jumped to exit!"
            val instr: Instruction = instrList[line]
            instr.logInstr()
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
            Log.d("Run", instr.isLabel().toString())
            if (instr.isLabel()) {
                continue
            }
            when (instr[0]?.removeSuffix(":")?.removePrefix("\t")) {
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
                "div" -> {
                    regs["\$hi"] = regs[instr[1]] % regs[instr[2]]
                    regs["\$lo"] = regs[instr[1]].floorDiv(regs[instr[2]])
                }
                "divu" -> {
                    regs["\$hi"] = (regs[instr[1]].toUInt() % regs[instr[2]].toUInt()).toInt()
                    regs["\$lo"] = ((regs[instr[1]]).toUInt().floorDiv(regs[instr[2]].toUInt())).toInt()
                }
                "mult" -> {
                    val temp = regs[instr[1]].toLong() * regs[instr[2]].toLong()
                    regs["\$hi"] = (temp ushr 16).toInt()
                    regs["\$lo"] = (temp shl 16 ushr 16).toInt()
                }
                "multu" -> {
                    val temp = (regs[instr[1]].toULong() * regs[instr[2]].toULong()).toLong()
                    regs["\$hi"] = (temp ushr 16).toInt()
                    regs["\$lo"] = (temp shl 16 ushr 16).toInt()
                }
                "mfhi" -> {
                    regs[instr[1]] = regs["\$hi"]
                }
                "mflo" -> {
                    regs[instr[1]] = regs["\$lo"]
                }
                "mthi" -> {
                    regs["\$hi"] = regs[instr[1]]
                }
                "mtlo" -> {
                    regs["\$lo"] = regs[instr[1]]
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

                "sll" -> {
                    regs[instr[1]] = regs[instr[2]] shl (instr[3]!!.toInt())
                }
                "sllv" -> {
                    regs[instr[1]] = regs[instr[2]] shl regs[instr[3]]
                }

                "srl" -> {
                    regs[instr[1]] = regs[instr[2]] ushr (instr[3]!!.toInt())
                }

                "srlv" -> {
                    regs[instr[1]] = regs[instr[2]] ushr regs[instr[3]]
                }

                "sra" -> {
                    regs[instr[1]] = regs[instr[2]] shr (instr[3]!!.toInt())
                }

                "srav" -> {
                    regs[instr[1]] = regs[instr[2]] shr regs[instr[3]]
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
                        (regs[instr[2]].toUInt() + instr[3]!!.toInt().toUInt()).toInt()
                }

                "andi" -> {    // andi
                    regs[instr[1]] = regs[instr[2]].and(zeroExtendImmediate(instr[3]!!.toInt()))
                }

                "beq" -> {    // beq
                    if (regs[instr[2]] == regs[instr[1]]) {
                        val temp = parseLabel(instr[3]!!, instrList)
                        if (exit) return "" // Exit is jumped to
                        else if (temp == null) {
                            return "Invalid label!"
                        }
                        else line = temp
                    }
                }

                "bne" -> {    // bne
                    if (regs[instr[2]] != regs[instr[1]]) {
                        val temp = parseLabel(instr[3]!!, instrList)
                        if (exit) return "" // Exit is jumped to
                        else if (temp == null) {
                            return "Invalid label!"
                        }
                        else line = temp
                    }
                }
                "blez" -> {
                    if (regs[instr[1]] <= 0) {
                        val temp = parseLabel(instr[2]!!, instrList)
                        if (exit) return "" // Exit is jumped to
                        else if (temp == null) {
                            return "Invalid label!"
                        }
                        else line = temp
                    }
                }
                "bgtz" -> {
                    if (regs[instr[1]] > 0) {
                        val temp = parseLabel(instr[2]!!, instrList)
                        if (exit == true) return "" // Exit is jumped to
                        else if (temp == null) {
                            return "Invalid label!"
                        }
                        else line = temp
                    }
                }

                "j" -> {    // j
                    val temp = parseLabel(instr[1]!!, instrList)
                    if (exit == true) return "" // Exit is jumped to
                    else if (temp == null) {
                        return "Invalid label!"
                    }
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
                    if (exit == true) return "" // Exit is jumped to
                    else if (temp == null) {
                        return "Invalid label!"
                    }
                    else line = temp

                }

                "jalr" -> {
                    regs["\$ra"] = CODE_START
                    for (i in 0..<line) {
                        if (!instrList[i].isLabel()) {
                            regs["\$ra"] += 4
                        }
                    }
                    regs["\$ra"] + 8
                    val temp = parseCodeAddress(regs["\$rs"], instrList)
                    if (temp == null) {
                        regs = savedRegs
                        return "Invalid code address"
                    }
                    line = temp
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

                "lb" -> {   // lbu
                    val temp = parseStackAddress(regs[instr[3]] + instr[2]!!.toInt())
                    if (temp == null) {
                        regs = savedRegs
                        return "Invalid stack address"
                    }
                    regs[instr[1]] = stack[temp].toInt() shl 24 shr 24
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

                "lh" -> {   // lhu
                    val temp = parseStackAddress(regs[instr[3]] + instr[2]!!.toInt())
                    if (temp == null) {
                        regs = savedRegs
                        return "Invalid stack address"
                    }
                    regs[instr[1]] =
                        bigEndianConversion(stack.copyOfRange(temp - 1, temp + 1)) shl 16 shr 16
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
                        if (regs[instr[2]].toUInt() < instr[3]!!.toInt().toUInt()) 1 else 0
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
                "xor" -> {
                    regs[instr[1]] = regs[instr[2]] xor regs[instr[3]]
                }
                "xori" -> {
                    regs[instr[1]] = regs[instr[2]] xor zeroExtendImmediate(instr[3]!!.toInt())
                }

                else -> {
                    regs = savedRegs
                    return "Unknown instruction"
                }
            }
        }
    }
}