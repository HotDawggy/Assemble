package com.game.assemble
import android.content.Context
import android.util.Log
import okhttp3.internal.toHexString
import java.util.Calendar
import kotlin.random.Random

const val CODE_START = 0x00400000
const val STACK_START = 0x7ffffffc
class MIPSSimulator(
    context: Context,
    ) {
    var regs: Registers = Registers(sp = STACK_START, ra= CODE_START)
    private var exit = false
    val gameTask: GameTask = GameTask(context)
    private var stack: ByteArray = ByteArray(10000)

    private fun parseLabel(label: String, instrList: MutableList<Instruction>) : Int? {
        if (label == "exit") {Log.d("parseLabel", "Exit!"); exit = true; return null}
        for (instr in instrList) {
            if (instr.isLabel(label)) {
                //Log.d("parseLabel", "label = $label, instr = " + instr[0])
                //Log.d("parseLabel", "Label at " + instrList.indexOf(instr).toString())
                return instrList.indexOf(instr)
            }
        }
        return null
    }
    private fun parseStackAddress(addr: Int) : Int? {
        Log.d("parseStackAddress()", addr.toHexString())
        val temp = STACK_START - addr
        return if (temp < 0) {
            null
        } else {
            temp
        }
    }

    private fun parseCodeAddress(addr: Int, instrList: MutableList<Instruction>) : Int? {
        Log.d("parseCodeAddress()", addr.toHexString())
        if ((addr - CODE_START) % 4 != 0) return null
        var temp: Int = (addr - CODE_START)/4
        for (i in instrList.indices) {
            instrList[i].logInstr()
            if (instrList[i].isLabel()) continue
            else if (instrList[i - 1][0] == "jal" || instrList[i - 1][0] == "jalr") {
                temp -= 2
            }
            else temp--
            Log.d("parseCodeAddress()", temp.toString())
            if (temp <= 0) {
                return i
            }
        }
        return null
    }
    private fun getCodeAddress(line: Int, instrList: MutableList<Instruction>) : Int {
        Log.d("parseCodeAddress()", line.toString())
        var temp = CODE_START
        for (i in 0..<line) {
            instrList[i].logInstr()
            if (instrList[i].isLabel()) continue
            else if (instrList[i - 1][0] == "jal" || instrList[i - 1][0] == "jalr") {
                temp += 8
            }
            else temp += 4
            Log.d("parseCodeAddress()", temp.toString())
        }
        return temp
    }
    private fun zeroExtendImmediate(source: Int) : Int {
        return ((source shl 16) ushr 16)
    }

    private fun bigEndianConversion(bytes: ByteArray): Int {
        var result = 0
        for (i in bytes.indices) {
            result = (bytes[i].toInt() shl 8 * i) or result
        }
        return result
    }
    private fun modifyStackInPlace(bytes: ByteArray, size: Int, index: Int) {
        for (i in 0..<size) {
            stack[i + index] = bytes[i]
        }
    }
    private fun addToStack(bytes: ByteArray, size: Int, index: Int? = null, updateSp: Boolean = false) {
        fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
        //Log.d("addToStack", bytes.toHexString())
        //Log.d("addToStack", stack.size.toString())
        //printStack()
        if (index == null || index >= stack.size) {
            if (index != null) {
                stack += ByteArray(index - stack.size)
            }
            stack += bytes
        }
        else modifyStackInPlace(bytes, size, index)
        if (updateSp) regs["\$sp"] = regs["\$sp"] - size
        printStack()
        //Log.d("addToStack", "sp: " + regs["\$sp"].toHexString())
    }
    private fun convertIntToByteArray(input : Int) : ByteArray {
        //Log.d("convertIntToByteArray", input.toString())
        val res = ByteArray(4)
        for (i in 0..3) res[i] = (input shr (i*8)).toByte()
        fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
        //Log.d("convertIntToByteArray", res.toHexString())
        return res
    }
    private fun convertByteArrayToInt(buffer: ByteArray, offset: Int): Int {
        return (buffer[offset + 3].toInt() shl 24) or
                (buffer[offset + 2].toInt() and 0xff shl 16) or
                (buffer[offset + 1].toInt() and 0xff shl 8) or
                (buffer[offset + 0].toInt() and 0xff)
    }
    private fun convertHalfWordToByteArray(input : Int) : ByteArray {
        //Log.d("convertIntToByteArray", input.toString())
        val res = ByteArray(2)
        for (i in 0..1) res[i] = (input shr (i*8)).toByte()
        fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
        //Log.d("convertIntToByteArray", res.toHexString())
        return res
    }
    private fun convertByteArrayToHalfWord(buffer: ByteArray, offset: Int): Int {
        return (buffer[offset + 1].toInt() shl 8) or
                (buffer[offset + 0].toInt() and 0xff)
    }

    private fun printStack() {
        Log.d("printStack", "Printing Stack")
        Log.d("printStack", "Size: " + stack.size.toString())
        for (i in 0..<stack.size / 4) {
            Log.d("printStack", convertByteArrayToInt(stack, i * 4).toString())
        }
    }

    fun generateTask(id: Int? = null) : String {
        //Log.i("generateTask()", "Generating task...")
        stack = byteArrayOf()
        regs["\$sp"] = STACK_START
        if (id == null) gameTask.getRandomTask()
        else gameTask.setTask(id)
        Log.d("generateTask()", (id ?: -1).toString())
        //gameTask.info["id"] = id?:0
        //Log.i("generateTask()", "Obtained task ID " + gameTask.info["id"].toString() + "...")
        when (gameTask.info["id"] as Int) {
            0 -> {  // LCM of a0, a1, return in v0
                regs["\$a0"] = (4..999).random()
                regs["\$a1"] = (4..999).random()
                // TODO: REVERT THIS
                //regs["\$a1"] = 1
                gameTask["goal"] = gameTask.findLCM(regs["\$a0"], regs["\$a1"])
                gameTask["input1"] = regs["\$a0"]
                gameTask["input2"] = regs["\$a1"]
            }
            1 -> {  // Sort array in ascending order
                Log.d("MIPSSimulator generateTask()", "generating task id 1")
                regs["\$a0"] = STACK_START
                gameTask["addr"] = 0
                regs["\$a1"] = (5..20).random()
                gameTask["size"] = regs["\$a1"]
                val tempList = mutableListOf<Int>()
                for (i in (0..<regs["\$a1"])) {
                    Log.d("MIPSSimulator generateTask()", "adding to stack")
                    val temp = (-999..999).random()
                    tempList.add(temp)
                    addToStack(convertIntToByteArray(temp), 4, updateSp = true)
                }
                (tempList).sort()
                for (num in tempList) {
                    Log.d("printGoal", num.toString())
                }
                gameTask["goal"] = tempList
            }
            2 -> {  // GCD of a0, a1, return in v0
                regs["\$a0"] = (4..999).random()
                regs["\$a1"] = (4..999).random()
                Log.d("generateTask(), id 2", regs["\$a0"].toString())
                Log.d("generateTask(), id 2", regs["\$a1"].toString())
                gameTask["goal"] = gameTask.findGCD(regs["\$a0"], regs["\$a1"])
                gameTask["input1"] = regs["\$a0"]
                gameTask["input2"] = regs["\$a1"]
            }
            3 -> {
                regs["\$a0"] = STACK_START
                gameTask["addr"] = 0;
                regs["\$a1"] = (5..20).random()
                gameTask["size"] = regs["\$a1"]
                addToStack(ByteArray(regs["\$a1"] * 4), regs["\$a1"] * 4, updateSp = false)
                val list: MutableList<Int> = mutableListOf<Int>(0, 1)
                for (i in (2..<(regs["\$a1"]))) {
                    Log.d("Fibonacci", (list[i - 1] + list[i - 2]).toString())
                    list.add(list[i - 1] + list[i - 2])
                }
                gameTask["goal"] = list
            }
            4 -> {
                regs["\$a0"] = (100..999).random()
                gameTask["input1"] = regs["\$a0"]
                regs["\$a1"] = STACK_START
                gameTask["addr"] = 0;
                val res = gameTask.findPrimeList(regs["\$a0"]).also { it.sort() }
                gameTask["goal"] = res
                addToStack(ByteArray(res.size * 4), res.size * 4, updateSp = false)
            }
            5 -> {
                regs["\$a0"] = (100..999).random()
                gameTask["input1"] = regs["\$a0"]
                //gameTask["goal"] = gameTask.findPrimeList(regs["\$a0"]).sort()
                val primeList = gameTask.findPrimeList(regs["\$a0"]).also { it.sort() }
                gameTask["goal"] = primeList[primeList.size - 1]
            }
            6 -> {
                val charPool = "abcdefghijklmnopqrstuvwxyz0123456789"
                regs["\$a0"] = STACK_START
                val arr1 = (1..(5..10).random()).map{
                    Random.nextInt(0, charPool.length).let { charPool[it] }
                }.joinToString("").toByteArray()
                addToStack(arr1, arr1.size, updateSp = true)
                addToStack(byteArrayOf(0), 1, updateSp = true)
                regs["\$a1"] = STACK_START - arr1.size - 1
                val arr2 = listOf((1..(5..10).random()).map{
                    Random.nextInt(0, charPool.length).let { charPool[it] }
                }.joinToString("").toByteArray(), arr1).random()
                addToStack(arr2, arr2.size, updateSp = true)
                addToStack(byteArrayOf(0), 1, updateSp = true)
                Log.d("generateTask()", arr1.joinToString(" "))
                Log.d("generateTask()", arr2.joinToString(" "))
                gameTask["goal"] = if (arr1.contentEquals(arr2)) 1 else 0
            }
        }
        //Log.i("generateTask()", "Returning...")
        Log.d("generateTask()", gameTask["goal"].toString())
        return gameTask["text"].toString()
    }
    fun validateTask(instrList: MutableList<Instruction>) : String {
        Log.i("WHERERU", "HEREAMI")
        val initState = Registers(regs)
        Log.i("validateTask()", "Running test case")
        //printState()
        val err = run(instrList)
        if (err.isNotBlank()) {
            Log.i("validateTask()", err)
            return "Error! $err"
        }
        Log.i("validateTask()", "Obtained output: " + regs["\$v0"].toString())
        when (gameTask.info["id"] as Int) {
            0 -> {  // LCM of a0, a1, return in v0
                gameTask["obtained"] = regs["\$v0"]
                if (regs["\$v0"] != gameTask["goal"] as Int) {
                    return "Failed!"
                }
            }

            1 -> {  // Sort array in ascending order
                val temp = mutableListOf<Int>()
                for (j in 0..<(gameTask["size"] as Int)) {
                    temp.add(convertByteArrayToInt(stack, (gameTask["addr"] as Int) + 4 * j))
                }
                gameTask["obtained"] = temp
                for (j in 0..<(gameTask["size"] as Int)) {
                    try {
                        if (convertByteArrayToInt(stack, (gameTask["addr"] as Int) + 4 * j) != (gameTask["goal"] as MutableList<Int>)[j]) return "Failed"
                    } catch (e: Exception) {
                        return "Failed!"
                    }
                }
            }

            2 -> {  // GCD of a0, a1, return in v0
                gameTask["obtained"] = regs["\$v0"].toString()
                if (regs["\$v0"] != gameTask["goal"] as Int) {
                    return "Failed!"
                }
            }

            3 -> {
                gameTask["obtained"] = mutableListOf<Int>()
                for (j in 0..<(gameTask["size"] as Int)) {
                    (gameTask["obtained"]as MutableList<Int>).add(convertByteArrayToInt(stack, gameTask["addr"] as Int + j * 4))
                }
                Log.d("validateTask()", gameTask["obtained"].toString())
                for (j in 0..<(gameTask["size"] as Int)) {
                    if ((gameTask["obtained"] as MutableList<Int>)[j] != (gameTask["goal"] as MutableList<Int>)[j]) return "Failed!";
                }
            }
            4 -> {
                gameTask["obtained"] = mutableListOf<Int>()
                for (j in 0..<(gameTask["goal"] as MutableList<Int>).size) {
                    (gameTask["obtained"]as MutableList<Int>).add(convertByteArrayToInt(stack, gameTask["addr"] as Int + j * 4))
                }
                (gameTask["obtained"] as MutableList<Int>).sort()
                Log.d("validateTask()", gameTask["obtained"].toString())
                for (j in 0..<(gameTask["goal"] as MutableList<Int>).size) {
                    if ((gameTask["obtained"] as MutableList<Int>)[j] != (gameTask["goal"] as MutableList<Int>)[j]) return "Failed!";
                }
            }
            5 -> {
                gameTask["obtained"] = regs["\$v0"].toString()
                if (regs["\$v0"] != gameTask["goal"] as Int) {
                    return "Failed!"
                }
            }
            6 -> {
                gameTask["obtained"] = regs["\$v0"].toString()
                if (regs["\$v0"] != gameTask["goal"] as Int) {
                    return "Failed!"
                }
            }
        }
        Log.i("validateTask", "Test case completed")
        regs = Registers(initState)
        stack = byteArrayOf()
        return "Success!"
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
        //Log.d("Run()","Ran")
        val savedRegs = Registers(regs)
        val startTime = Calendar.getInstance().time.time
        var line = 0
        exit = false
        //regs.printRegister()
        while (true) {

            // printState("\$v0")
            if (line >= instrList.size) return "The program never jumped to exit!"
            val instr: Instruction = instrList[line]
            instr.logInstr()
            //Log.d("[*] MIPSSimulator.Run", "line " + line.toString())
            if (Calendar.getInstance().time.time - startTime > 2000) { // TODO: REVERT BACK
                regs = savedRegs
                return "Timed out! Check if your code will result in infinite loop!"
            }
            if (instr.hasNull()) {
                regs = savedRegs
                return "Some fields are empty!"
            }
            line++
            //Log.d("Run", instr.isLabel().toString())
            if (instr.isLabel()) {
                continue
            }
            when (instr[0]?.removeSuffix(":")?.removePrefix("\t")) {
                "add" -> {   // add
                    val temp: Long = regs[instr[2]].toLong() + regs[instr[3]].toLong()
                    if (temp <= Int.MAX_VALUE && temp >= Int.MIN_VALUE) {
                        regs[instr[1] as String] = temp.toInt()
                        //Log.d("add", temp.toInt().toString())
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
                    //Log.d("div", regs[instr[1]].toString() + " " + regs[instr[2]].toString())
                    if (regs[instr[2]] == 0) {
                        regs = savedRegs
                        return "Divide by zero exception!"
                    }
                    regs["\$hi"] = regs[instr[1]] % regs[instr[2]]
                    regs["\$lo"] = regs[instr[1]].floorDiv(regs[instr[2]])
                }
                "divu" -> {
                    if (regs[instr[2]] == 0) {
                        regs = savedRegs
                        return "Divide by zero exception!"
                    }
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
                    //Log.d("blez", regs[instr[1]].toString())
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
                    regs["\$ra"] = getCodeAddress(line - 1, instrList) + 8

                    val temp = parseLabel(instr[1]!!, instrList)
                    if (exit == true) return "" // Exit is jumped to
                    else if (temp == null) {
                        return "Invalid label!"
                    }
                    else line = temp
                    regs.printRegister()
                }

                "jalr" -> {
                    regs["\$ra"] = getCodeAddress(line - 1, instrList) + 8
                    val temp = parseCodeAddress(regs[instr[1]], instrList)
                    if (temp == null) {
                        regs = savedRegs
                        return "Invalid code address"
                    }
                    line = temp
                }

                "jr" -> {
                    val temp = parseCodeAddress(regs[instr[1]], instrList)
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
                        convertByteArrayToHalfWord(stack, temp)
                }

                "lh" -> {   // lhu
                    val temp = parseStackAddress(regs[instr[3]] + instr[2]!!.toInt())
                    if (temp == null) {
                        regs = savedRegs
                        return "Invalid stack address"
                    }
                    regs[instr[1]] =
                        bigEndianConversion(stack.copyOfRange(temp, temp + 2)) shl 16 shr 16
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
                    regs[instr[1]] = convertByteArrayToInt(stack, temp)
                    Log.d("lw", regs[instr[1]].toHexString())
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
                    val tempArr = convertHalfWordToByteArray(regs[instr[1]])
                    addToStack(tempArr, 1, temp)
                }

                "sh" -> {   // sh
                    val temp = parseStackAddress(regs[instr[3]] + instr[2]!!.toInt())
                    if (temp == null) {
                        regs = savedRegs
                        return "Invalid stack address"
                    }
                    val tempArr = byteArrayOf(convertIntToByteArray(regs[instr[1]])[3])
                    addToStack(tempArr, 2, temp)
                }

                "sw" -> {   // sw
                    val temp = parseStackAddress(regs[instr[3]] + instr[2]!!.toInt())
                    if (temp == null) {
                        regs = savedRegs
                        return "Invalid stack address"
                    }
                    val tempArr = convertIntToByteArray(regs[instr[1]])
                    addToStack(tempArr, 4, temp)
                    Log.d("sw", regs[instr[1]].toHexString())
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