package com.game.assemble
import android.content.Context
import android.util.Log
import java.util.Calendar

const val CODE_START = 0x00400000
const val STACK_START = 0x7ffffffc

class GameTask(ctx: Context) {
    val info: MutableMap<String, Any?> = mutableMapOf<String, Any?>(
        "id" to null,
        "text" to null
    )
    private val taskList: Array<String> = ctx.resources.getStringArray(R.array.taskList)
    fun getRandomTask() : Int {
        val idx = taskList.indices.random()
        info["text"] = taskList[idx]
        return idx
    }
    fun findLCM(a: Int, b: Int): Int {
        val larger = if (a > b) a else b
        val maxLcm = a * b
        var lcm = larger
        while (lcm <= maxLcm) {
            if (lcm % a == 0 && lcm % b == 0) {
                return lcm
            }
            lcm += larger
        }
        return maxLcm
    }
    fun findGCD(a: Int, b: Int): Int {
        var num1 = a
        var num2 = b
        while (num2 != 0) {
            val temp = num2
            num2 = num1 % num2
            num1 = temp
        }
        return num1
    }
}
class Instruction(
    val opcode: Int? = null,    // 0
    val rs: Int? = null,        // 1
    val rt: Int? = null,        // 2
    val rd: Int? = null,        // 3
    val shamt: Int? = null,     // 4
    val funct: Int? = null,     // 5
    val immediate: Int? = null, // 6
    val address: Int? = null,   // 7
    val label: String? = null   // 8
) {
    companion object {
        private var registerLookup = mutableMapOf<Int, String>()
        private var opcodeLookup = mutableMapOf<Int, String>()
        private var functLookup  = mutableMapOf<Int, String>()
        private var init: Boolean = false
        fun initLookup(ctx: Context) {
            val regsKey = ctx.resources.getIntArray(R.array.regsKey)
            val regsString = ctx.resources.getStringArray(R.array.regsString)
            val opcodeKey = ctx.resources.getIntArray(R.array.opcodeKey)
            val opcodeString = ctx.resources.getStringArray(R.array.opcodeString)
            val functKey = ctx.resources.getIntArray(R.array.functKey)
            val functString = ctx.resources.getStringArray(R.array.functString)
            for (i in regsKey.indices) {
                registerLookup[regsKey[i]] = regsString[i]
            }
            for (i in opcodeKey.indices) {
                opcodeLookup[opcodeKey[i]] = opcodeString[i]
            }
            for (i in functKey.indices) {
                functLookup[functKey[i]] = functString[i]
            }
            init = true
        }
    }
    fun stringify() : String {
        if (!init) return "Resources haven't been initialized"
        return when (opcode) {
            null -> "_"
            0x0 -> when (funct) {
                null -> "_"
                else -> functLookup[funct] + " " + when (funct) {
                    0x08 -> registerLookup[(rs ?: -1)]
                    0x00, 0x02 -> registerLookup[(rd ?: -1)] + ", " + registerLookup[(rs ?: -1)] + ", " + (shamt ?: "_").toString()
                    else ->
                        registerLookup[(rd ?: -1)] + ", " + registerLookup[(rs ?: -1)] + ", " + registerLookup[(rt ?: -1)]
                }
            }
            else -> {
                opcodeLookup[opcode] + " " + when (opcode) {
                    0x4, 0x5 ->
                        registerLookup[(rt ?: -1)] + ", " + registerLookup[(rs ?: -1)] + ", " + (label ?: (address ?: "_").toString())
                    0x2 ->
                        label ?: (address ?: "_").toString()
                    0x24, 0x25, 0xf, 0x23, 0x28, 0x29, 0x2b ->
                        registerLookup[(rt ?: -1)] + ", " + (immediate ?: "_").toString() + "(" + registerLookup[(rs ?: -1)] + ")"
                    else ->
                        registerLookup[(rt ?: -1)] + ", " + registerLookup[(rs ?: -1)] + ", " + (immediate ?: "_").toString()
                }
            }
        }
    }
}
class MIPSSimulator(
    val ctx: Context,
    val v0: Int = 0,
    val a0: Int = 0,
    val a1: Int = 0,
    val t0: Int = 0,
    val t1: Int = 0,
    val s0: Int = 0,
    val s1: Int = 0,
    val sp: Int = STACK_START
    ) {
    private val regs: MutableMap<Int, Int> = mutableMapOf<Int, Int>()
        get() = field
    private val gameTask: GameTask = GameTask(ctx)
        get() = field
    private val stack: ByteArray = byteArrayOf()
        get() = field
    init {
        Instruction.initLookup(ctx)
        regs[0] = 0    // $zero
        regs[2] = v0
        regs[4] = a0
        regs[5] = a1
        regs[8] = t0
        regs[9] = t1
        regs[16] = s0
        regs[17] = s1
        regs[29] = sp
    }

    private fun parseCodeAddress(addr: Int) : Int? {
        val temp = addr - CODE_START
        if (temp % 4 != 0) {
            return null
        } else {
            return temp / 4
        }
    }

    private fun parseStackAddress(addr: Int) : Int? {
        val temp = STACK_START - addr - 1
        if (temp < 0) {
            return null
        } else {
            return temp
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
            if (updateSp) regs[29] = regs[29]!! - 1
        }
    }
    private fun convertIntToByteArray(input : Int) : ByteArray {
        var temp = input
        var res = byteArrayOf()
        for (i in 0..3) {
            res = byteArrayOf((temp.toByte())) + byteArrayOf()
            temp ushr 8
        }
        return res
    }

    // Uncoment when the keyboardViews are completed
    /*
    fun getFieldAndKeyboard(instr : Instruction) : Array<IntArray> {
        return when (instr.opcode) {
            // keyboardRegisterView is the view showing all selectable registers
            // keyboardKeypadView is the view showing a keypad (perhaps in hexidecimal?)
            // The value right after is the max allowable size of the input, might not be needed
            // keyboardLineView is the view to select line number for jump instruction
            // There is actually no need for a stack keyboard view!

            0x0 -> {
                when (instr.funct) {
                    0x20, 0x21, 0x24, 0x27, 0x25, 0x2a, 0x2b, 0x22, 0x23 -> arrayOf(
                        intArrayOf(3, R.id.keyboardRegisterView),
                        intArrayOf(1, R.id.keyboardRegisterView),
                        intArrayOf(2, R.id.keyboardRegisterView))
                    0x08 -> arrayOf(
                        intArrayOf(1, R.id.keyboardRegisterView))
                    0x00, 0x02 -> arrayOf(
                        intArrayOf(3, R.id.keyboardRegisterView),
                        intArrayOf(2, R.id.keyboardRegisterView),
                        intArrayOf(4, R.id.keyboardKeypadView, (1 shl 4) - 1))
                    else -> throw IllegalArgumentException("Incorrect funct!")
                }
            }
            0x8, 0x9, 0xc, 0x24, 0x25, 0x30, 0x23, 0xd, 0xa, 0xb, 0x28, 0x38, 0x29, 0x2b -> arrayOf(
                intArrayOf(2, R.id.keyboardRegisterView),
                intArrayOf(1, R.id.keyboardRegisterView),
                intArrayOf(6, R.id.keyboardKeypadView, (1 shl 15) - 1)
            )
            0x4, 0x5, 0x2, 0x3, 0xf -> arrayOf(
                intArrayOf(1, R.id.keyboardRegisterView),
                intArrayOf(2, R.id.keyboardRegisterView),
                intArrayOf(7, R.id.keyboardLineView)
            )
            else -> throw IllegalArgumentException("Incorrect opcode!")
        }
    }
     */

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
                regs[4] = (4..999).random()
                regs[5] = (4..999).random()
                gameTask.info["goal"]= gameTask.findLCM(regs[4]!!, regs[5]!!)
            }
            1 -> {  // Sort array in ascending order
                regs[4] = stack.size + STACK_START
                gameTask.info["addr"] = stack.size
                regs[5] = (5..20).random()
                gameTask.info["size"] = regs[5]
                for (i in (0..<regs[5]!!)) {
                    addToStack(convertIntToByteArray((1..999).random()), stack.size, 4)
                }
            }
            2 -> {  // GCD of a0, a1, return in v0
                regs[4] = (4..999).random()
                regs[5] = (4..999).random()
                gameTask.info["goal"] = gameTask.findGCD(regs[4]!!, regs[5]!!)
            }
        }
        return gameTask.info["text"] as String
    }
    fun validateTask() : Boolean {
        when (gameTask.info["id"] as Int) {
            0 -> {  // LCM of a0, a1, return in v0
                if (regs[2] == gameTask.info["goal"] as Int) return true
            }
            1 -> {  // Sort array in ascending order
                for (i in (gameTask.info["addr"] as Int)..< (gameTask.info["addr"] as Int) + (gameTask.info["size"] as Int) - 1) {
                    if (stack[i] > stack[i+1]) return false
                }
                return true
            }
            2 -> {  // GCD of a0, a1, return in v0
                if (regs[2] == gameTask.info["goal"] as Int) return true
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

    fun Run(code: List<Instruction?>) {
        var err: String = ""
        var line = 0
        val startTime = Calendar.getInstance().time.time
        while (true) {
            if ((line >= code.size) || (err.isNotBlank())) return
            val instr: Instruction = code[line]!!
            //Log.d("[*] MIPSSimulator.Run", "line " + line.toString())
            if (Calendar.getInstance().time.time - startTime > 2000) {
                err = "Timed out"
                return
            }
            line++
            when (instr.opcode) {
                0x0 -> {    // add, addu, and, jr, nor, or, slt, sltu, sll, srl, sub, subu
                    when (instr.funct) {
                        0x20 -> {   // add
                            Log.d("MIPSSimulator.Run", "add $" + instr.rd + ", $" + instr.rs + ", $" + instr.rt)
                            val temp: Long = regs[instr.rs]!!.toLong() + regs[instr.rt]!!.toLong()
                            if (temp <= Int.MAX_VALUE && temp >= Int.MIN_VALUE) {
                                regs[instr.rd!!] = temp.toInt()
                            } else {
                                err = "Overflow exception"
                                return
                            }
                        }

                        0x21 -> {   // addu
                            Log.d("MIPSSimulator.Run", "addu $" + instr.rd + ", $" + instr.rs + ", $" + instr.rt)
                            regs[instr.rd!!] =
                                (regs[instr.rs]!!.toUInt() + regs[instr.rt]!!.toUInt()).toInt()
                        }

                        0x24 -> {   // and
                            Log.d("MIPSSimulator.Run", "and $" + instr.rd + ", $" + instr.rs + ", $" + instr.rt)
                            regs[instr.rd!!] = regs[instr.rs]!! and (regs[instr.rt]!!)
                        }

                        0x8 -> {    // jr
                            Log.d("MIPSSimulator.Run", "jr $" + instr.rs)
                            val temp: Int? = parseCodeAddress(regs[instr.rs]!!)
                            if (temp == null) {
                                err = "Invalid address"
                                return
                            }
                            line = temp
                        }

                        0x27 -> {   // nor
                            Log.d("MIPSSimulator.Run", "nor $" + instr.rd + ", $" + instr.rs + ", $" + instr.rt)
                            regs[instr.rd!!] = (regs[instr.rs]!! or regs[instr.rt]!!).inv()
                        }

                        0x25 -> {   // or
                            Log.d("MIPSSimulator.Run", "or $" + instr.rd + ", $" + instr.rs + ", $" + instr.rt)
                            regs[instr.rd!!] = regs[instr.rs]!! or regs[instr.rt]!!
                        }

                        0x2a -> {   // slt
                            Log.d("MIPSSimulator.Run", "slt $" + instr.rd + ", $" + instr.rs + ", $" + instr.rt)
                            regs[instr.rd!!] = if (regs[instr.rs]!! < regs[instr.rt]!!) 1 else 0
                        }

                        0x2b -> {   // sltu
                            Log.d("MIPSSimulator.Run", "sltu $" + instr.rd + ", $" + instr.rs + ", $" + instr.rt)
                            regs[instr.rd!!] =
                                if (regs[instr.rs]!!.toUInt() < regs[instr.rt]!!.toUInt()) 1 else 0
                        }

                        0x0 -> {    // sll
                            Log.d("MIPSSimulator.Run", "sll $" + instr.rd + ", $" + instr.rt + ", " + instr.shamt)
                            regs[instr.rd!!] = regs[instr.rt]!! shl instr.shamt!!
                        }

                        0x2 -> {    // srl
                            Log.d("MIPSSimulator.Run", "srl $" + instr.rd + ", $" + instr.rt + ", " + instr.shamt)
                            regs[instr.rd!!] = regs[instr.rt]!! shr instr.shamt!!
                        }

                        0x22 -> {   // sub
                            Log.d("MIPSSimulator.Run", "sub $" + instr.rd + ", $" + instr.rs + ", $" + instr.rt)
                            val temp: Long = regs[instr.rs]!!.toLong() - regs[instr.rt]!!.toLong()
                            if (temp <= Int.MAX_VALUE && temp >= Int.MIN_VALUE) {
                                regs[instr.rd!!] = temp.toInt()
                            } else {
                                err = "Overflow exception"
                                return
                            }
                        }

                        0x23 -> {   // subu
                            Log.d("MIPSSimulator.Run", "subu $" + instr.rd + ", $" + instr.rs + ", $" + instr.rt)
                            regs[instr.rd!!] =
                                (regs[instr.rs]!!.toUInt() - regs[instr.rt]!!.toUInt()).toInt()
                        }
                    }

                }

                0x8 -> {    // addi
                    Log.d("MIPSSimulator.Run", "addi $" + instr.rt + ", $" + instr.rs + ", " + instr.immediate)
                    val temp: Long = regs[instr.rs]!!.toLong() + instr.immediate!!.toLong()
                    if (temp <= Int.MAX_VALUE && temp >= Int.MIN_VALUE) {
                        regs[instr.rt!!] = temp.toInt()
                    } else {
                        err = "Overflow exception"
                        return
                    }
                }

                0x9 -> {    // addiu
                    Log.d("MIPSSimulator.Run", "addiu $" + instr.rt + ", $" + instr.rs + ", " + instr.immediate)
                    regs[instr.rt!!] =
                        (regs[instr.rs]!!.toUInt() + instr.immediate!!.toUInt()).toInt()
                }

                0xc -> {    // andi
                    Log.d("MIPSSimulator.Run", "andi $" + instr.rt + ", $" + instr.rs + ", " + instr.immediate)
                    regs[instr.rt!!] = regs[instr.rs]!!.and(zeroExtendImmediate(instr.immediate!!))
                }

                0x4 -> {    // beq
                    Log.d("MIPSSimulator.Run", "beq $" + instr.rt + ", $" + instr.rs + ", " + instr.immediate)
                    if (regs[instr.rs]!! == regs[instr.rt]!!) {
                        line += instr.immediate!!
                    }
                }

                0x5 -> {    // bne
                    Log.d("MIPSSimulator.Run", "bne $" + instr.rt + ", $" + instr.rs + ", " + instr.immediate)
                    if (regs[instr.rs]!! != regs[instr.rt]!!) {
                        line += instr.immediate!!
                    }
                }

                0x2 -> {    // j
                    Log.d("MIPSSimulator.Run", "j " + instr.address)
                    val temp: Int? = parseCodeAddress(instr.address!!)
                    if (temp == null) {
                        err = "Invalid jump address"
                        return
                    }
                    line = temp
                }

                0x3 -> {    // jal
                    // Not Implemented
                }

                0x24 -> {   // lbu
                    val msg = "lbu $" + instr.rt + ", " + instr.immediate + "($" + instr.rs + ")"
                    Log.d("MIPSSimulator.Run", msg)
                    val temp = parseStackAddress(regs[instr.rs]!! + instr.immediate!!)
                    if (temp == null) {
                        err = "Invalid stack address"
                        return
                    }
                    //Log.i("MIPSSimulator.Run temp:", temp.toString())
                    regs[instr.rt!!] = stack[temp!!].toInt() shl 24 ushr 24
                }

                0x25 -> {   // lhu
                    val msg = "lhu $" + instr.rt + ", " + instr.immediate + "($" + instr.rs + ")"
                    Log.d("MIPSSimulator.Run", msg)
                    val temp = parseStackAddress(regs[instr.rs]!! + instr.immediate!!)
                    if (temp == null) {
                        err = "Invalid stack address"
                        return
                    }
                    //Log.i("MIPSSimulator.Run temp:", temp.toString())
                    regs[instr.rt!!] =
                        bigEndianConversion(stack.copyOfRange(temp - 1, temp + 1)) shl 16 ushr 16
                }

                0x30 -> {   // ll
                    // Not implemented
                }

                0xf -> {    // lui
                    Log.d("MIPSSimulator.Run", "lui $" + instr.rt + ", " + instr.immediate)
                    regs[instr.rt!!] = instr.immediate!! shl 17 ushr 1
                }

                0x23 -> {   // lw
                    val msg = "lw $" + instr.rt + ", " + instr.immediate + "($" + instr.rs + ")"
                    Log.d("MIPSSimulator.Run", msg)
                    val temp = parseStackAddress(regs[instr.rs]!! + instr.immediate!!)
                    if (temp == null) {
                        err = "Invalid stack address"
                        return
                    }
                    regs[instr.rt!!] = bigEndianConversion(stack.copyOfRange(temp - 3, temp + 1))
                }

                0xd -> {    // ori
                    Log.d("MIPSSimulator.Run", "ori $" + instr.rt + ", $" + instr.rs + ", " + instr.immediate)
                    regs[instr.rt!!] = regs[instr.rs]!! or (zeroExtendImmediate(instr.immediate!!))
                }

                0xa -> {    // slti
                    Log.d("MIPSSimulator.Run", "slti $" + instr.rt + ", $" + instr.rs + ", " + instr.immediate)
                    regs[instr.rt!!] = if (regs[instr.rs]!! < instr.immediate!!) 1 else 0
                }

                0xb -> {    // sltiu
                    Log.d("MIPSSimulator.Run", "sltiu $" + instr.rt + ", $" + instr.rs + ", " + instr.immediate)
                    regs[instr.rt!!] =
                        if (regs[instr.rs]!!.toUInt() < instr.immediate!!.toUInt()) 1 else 0
                }

                0x28 -> {   // sb
                    val msg = "sb $" + instr.rt + ", " + instr.immediate + "($" + instr.rs + ")"
                    Log.d("MIPSSimulator.Run", msg)
                    val temp = parseStackAddress(regs[instr.rs]!! + instr.immediate!!)
                    if (temp == null) {
                        err = "Invalid stack address"
                        return
                    }
                    val tempArr = byteArrayOf((regs[instr.rt]!! shl 24 ushr 24).toByte())
                    addToStack(tempArr, temp, 1)
                }

                0x38 -> {   // sc
                    // Not implemented
                }

                0x29 -> {   // sh
                    val msg = "sh $" + instr.rt + ", " + instr.immediate + "($" + instr.rs + ")"
                    Log.d("MIPSSimulator.Run", msg)
                    val temp = parseStackAddress(regs[instr.rs]!! + instr.immediate!!)
                    if (temp == null) {
                        err = "Invalid stack address"
                        return
                    }
                    val tempArr = byteArrayOf(
                        (regs[instr.rt]!! shl 16 ushr 24).toByte(),
                        (regs[instr.rt]!! shl 24 ushr 24).toByte()
                    )
                    addToStack(tempArr, temp, 2)
                }

                0x2b -> {   // sw
                    val msg = "sw $" + instr.rt + ", " + instr.immediate + "($" + instr.rs + ")"
                    Log.d("MIPSSimulator.Run", msg)
                    val temp = parseStackAddress(regs[instr.rs]!! + instr.immediate!!)
                    if (temp == null) {
                        err = "Invalid stack address"
                        return
                    }
                    val tempArr = byteArrayOf(
                        (regs[instr.rt]!! ushr 24).toByte(),
                        (regs[instr.rt]!! shl 8 ushr 24).toByte(),
                        (regs[instr.rt]!! shl 16 ushr 24).toByte(),
                        (regs[instr.rt]!! shl 24 ushr 24).toByte()
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