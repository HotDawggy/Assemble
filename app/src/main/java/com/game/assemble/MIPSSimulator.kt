package com.game.assemble
import android.content.Context
import android.content.res.Resources
import android.util.Log
import java.util.Calendar

const val CODE_START = 0x00400000
const val STACK_START = 0x7ffffffc
class MIPSSimulator() {
    /*
    private val context: Context,
    private val v0: Int = 0,
    private val a0: Int = 0,
    private val a1: Int = 0,
    private val t0: Int = 0,
    private val t1: Int = 0,
    private val s0: Int = 0,
    private val s1: Int = 0,
    private val sp: Int = STACK_START
    ) {
    private val regs: MutableMap<Int, Int> = mutableMapOf<Int, Int>()
        get() = field
    private val gameTask: GameTask = GameTask(context)
        get() = field
    private val stack: ByteArray = byteArrayOf()
        get() = field
    init {
        if (!Instruction.isInit()) Instruction.initLookup(context)
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
            3 -> { // Sum of all even primes
                gameTask.info["goal"] = 2
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
            3 -> { // Sum of all even primes into v0
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

     */
}