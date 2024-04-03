package com.game.assemble
import java.util.Calendar
import java.util.Date

val CODE_START = 0x00400000
val STACK_START = 0x7ffffffc
class Instruction(
    val opcode: Int = -1,
    val rs: Int = -1,
    val rt: Int = -1,
    val rd: Int = -1,
    val shamt: Int = 0,
    val funct: Int = 0,
    val immediate: Int = 0,
    val address: Int = 0
) {}
class MIPSSimulator(
    val v0: Int = 0,
    val a0: Int = 0,
    val a1: Int = 0,
    val t0: Int = 0,
    val t1: Int = 0,
    val s0: Int = 0,
    val s1: Int = 0,
    val sp: Int = STACK_START
    ) {
    val regs: MutableMap<Int, Int> = mutableMapOf<Int, Int>()
    init {
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
    private fun parseOffset(offset: Int) : Int? {
        if (offset % 4 != 0) {
            return null
        } else {
            return offset / 4
        }
    }
    private fun parseStackAddress(addr: Int) : Int? {
        val temp = STACK_START - addr
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
            result = bytes[i].toInt() or (result shl 8 * i)
        }
        return result
    }

    var err: String = ""
    fun Run(code: List<Instruction?>, stack: ByteArray) {
        var line = 0
        val startTime = Calendar.getInstance().time.time
        while (true) {
            val instr = code[line] ?: return
            if (Calendar.getInstance().time.time - startTime > 2000) {
                err = "Timed out"
                return
            }
            when (instr.opcode) {
                0x0 -> {    // add, addu, and, jr, nor, or, slt, sltu, sll, srl, sub, subu
                    when (instr.funct) {
                        0x20 -> {   // add
                            val temp: Long = regs[instr.rs]!!.toLong() + regs[instr.rt]!!.toLong()
                            if (temp <= Int.MAX_VALUE && temp >= Int.MIN_VALUE) {
                                regs[instr.rd] = temp.toInt()
                            } else {
                                err = "Overflow exception"
                                return
                            }
                        }

                        0x21 -> {   // addu
                            regs[instr.rd] =
                                (regs[instr.rs]!!.toUInt() + regs[instr.rt]!!.toUInt()).toInt()
                        }

                        0x24 -> {   // and
                            regs[instr.rd] = regs[instr.rs]!!.and(regs[instr.rt]!!)
                        }

                        0x8 -> {    // jr
                            val temp: Int? = parseCodeAddress(regs[instr.rs]!!)
                            if (temp == null) {
                                err = "Invalid address"
                                return
                            }
                            line = temp
                        }

                        0x27 -> {   // nor
                            regs[instr.rd] = (regs[instr.rs]!! or regs[instr.rt]!!).inv()
                        }

                        0x25 -> {   // or
                            regs[instr.rd] = regs[instr.rs]!! or regs[instr.rt]!!
                        }

                        0x2a -> {   // slt
                            regs[instr.rd] = if (regs[instr.rs]!! < regs[instr.rt]!!) 1 else 0
                        }

                        0x2b -> {   // sltu
                            regs[instr.rd] =
                                if (regs[instr.rs]!!.toUInt() < regs[instr.rt]!!.toUInt()) 1 else 0
                        }

                        0x0 -> {    // sll
                            regs[instr.rd] = regs[instr.rt]!! shl instr.shamt
                        }

                        0x2 -> {    // srl
                            regs[instr.rd] = regs[instr.rt]!! shr instr.shamt
                        }

                        0x22 -> {   // sub
                            val temp: Long = regs[instr.rs]!!.toLong() - regs[instr.rt]!!.toLong()
                            if (temp <= Int.MAX_VALUE && temp >= Int.MIN_VALUE) {
                                regs[instr.rd] = temp.toInt()
                            } else {
                                err = "Overflow exception"
                                return
                            }
                        }

                        0x23 -> {   // subu
                            regs[instr.rd] =
                                (regs[instr.rs]!!.toUInt() - regs[instr.rt]!!.toUInt()).toInt()
                        }
                    }

                }

                0x8 -> {    // addi
                    val temp: Long = regs[instr.rs]!!.toLong() + instr.immediate!!.toLong()
                    if (temp <= Int.MAX_VALUE && temp >= Int.MIN_VALUE) {
                        regs[instr.rt] = temp.toInt()
                    } else {
                        err = "Overflow exception"
                        return
                    }
                }

                0x9 -> {    // addiu
                    regs[instr.rt] =
                        (regs[instr.rs]!!.toUInt() + instr.immediate!!.toUInt()).toInt()
                }

                0xc -> {    // andi
                    regs[instr.rt] = regs[instr.rs]!!.and(zeroExtendImmediate(instr.immediate!!))
                }

                0x4 -> {    // beq
                    if (regs[instr.rs]!! == regs[instr.rt]!!) {
                        val offset: Int? = parseOffset(instr.immediate)
                        if (offset == null) {
                            err = "Invalid branch address"
                            return
                        }
                        line += 1 + offset!!
                    }
                }

                0x5 -> {    // bne
                    if (regs[instr.rs]!! != regs[instr.rt]!!) {
                        val offset: Int? = parseOffset(instr.immediate)
                        if (offset == null) {
                            err = "Invalid branch address"
                            return
                        }
                        line += 1 + offset!!
                    }
                }

                0x2 -> {    // j
                    val temp: Int? = parseCodeAddress(instr.address)
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
                    val temp = parseStackAddress(regs[instr.rs]!! + instr.immediate!!)
                    if (temp == null) {
                        err = "Invalid stack address"
                        return
                    }
                    regs[instr.rt] = stack[temp!!].toInt() shl 24 ushr 24
                }

                0x25 -> {   // lhu
                    val temp = parseStackAddress(regs[instr.rs]!! + instr.immediate!!)
                    if (temp == null) {
                        err = "Invalid stack address"
                        return
                    }
                    regs[instr.rt] =
                        bigEndianConversion(stack.copyOfRange(temp, temp + 1)) shl 16 ushr 16
                }

                0x30 -> {   // ll
                    // Not implemented
                }

                0xf -> {    // lui
                    regs[instr.rt] = instr.immediate shl 17 ushr 1
                }

                0x23 -> {   // lw
                    val temp = parseStackAddress(regs[instr.rs]!! + instr.immediate!!)
                    if (temp == null) {
                        err = "Invalid stack address"
                        return
                    }
                    regs[instr.rt] = bigEndianConversion(stack.copyOfRange(temp, temp + 3))
                }

                0xd -> {    // ori
                    regs[instr.rt] = regs[instr.rs]!! or (zeroExtendImmediate(instr.immediate))
                }

                0xa -> {    // slti
                    regs[instr.rt] = if (regs[instr.rs]!! < instr.immediate) 1 else 0
                }

                0xb -> {    // sltiu
                    regs[instr.rt] =
                        if (regs[instr.rs]!!.toUInt() < instr.immediate.toUInt()) 1 else 0
                }

                0x28 -> {   // sb
                    val temp = parseStackAddress(regs[instr.rs]!! + instr.immediate!!)
                    if (temp == null) {
                        err = "Invalid stack address"
                        return
                    }
                    stack[temp!!] = (regs[instr.rt] shl 24 ushr 24).toByte()
                }

                0x38 -> {   // sc
                    // Not implemented
                }

                0x29 -> {   // sh
                    val temp = parseStackAddress(regs[instr.rs]!! + instr.immediate!!)
                    if (temp == null) {
                        err = "Invalid stack address"
                        return
                    }
                    val tempArr = byteArrayOf(
                        (regs[instr.rt]!! shl 8 ushr 24).toByte(),
                        (regs[instr.rt]!! ushr 24).toByte()
                    )
                    for (i in tempArr.indices)
                        stack[temp!! + i] = tempArr[i]
                }

                0x2b -> {   // sw
                    val temp = parseStackAddress(regs[instr.rs]!! + instr.immediate!!)
                    if (temp == null) {
                        err = "Invalid stack address"
                        return
                    }
                    val tempArr = byteArrayOf(
                        (regs[instr.rt]!! shl 24 ushr 24).toByte(),
                        (regs[instr.rt]!! shl 16 ushr 24).toByte(),
                        (regs[instr.rt]!! shl 8 ushr 24).toByte(),
                        (regs[instr.rt]!! ushr 24).toByte()
                    )
                    for (i in tempArr.indices)
                        stack[temp!! + i] = tempArr[i]
                }
            }
        }
    }
}