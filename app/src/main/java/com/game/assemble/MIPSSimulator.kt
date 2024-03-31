package com.game.assemble

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
    var err: String = ""
    fun Run(code: List<Instruction?>, stack: MutableList<ByteArray>) {
        var line = 0
        while (true) {
            val instr = code[line] ?: return
            when (instr.opcode) {
                0x0 -> {    // add, addu, and, jr, nor, or, slt, sltu, sll, srl, sub, subu
                    when (instr.funct) {
                        0x20 -> {   // add
                            val temp = regs[instr.rs]!! + regs[instr.rt]!!
                            if (temp.shr(31) == (regs[instr.rs]!!.shr(31).and(regs[instr.rt]!!.shr(31))))
                            regs[instr.rd] = temp
                        }
                        0x21 -> {   // addu
                            regs[instr.rd] = regs[instr.rs]!! + regs[instr.rt]!!
                        }
                        0x24 -> {   // and
                            regs[instr.rd] = regs[instr.rs]!!.and(regs[instr.rt]!!)
                        }
                        0x8 -> {    // jr

                        }
                        0x27 -> {   // nor

                        }
                        0x25 -> {   // or

                        }
                        0x2a -> {   // slt

                        }
                        0x2b -> {   // sltu

                        }
                        0x0 -> {    // sll

                        }
                        0x2 -> {    // srl

                        }
                        0x22 -> {   // sub

                        }
                        0x23 -> {   // subu

                        }
                    }

                }
                0x8 -> {    // addi

                }
                0x9 -> {    // addiu

                }
                0xc -> {    // andi

                }
                0x4 -> {    // beq

                }
                0x5 -> {    // bne

                }
                0x2 -> {    // j

                }
                0x3 -> {    // jal
                    // Not Implemented
                }
                0x24 -> {   // lbu

                }
                0x25 -> {   // lhu

                }
                0x30 -> {   // ll
                    // Not implemented
                }
                0xf -> {    // lui

                }
                0x23 -> {   // lw

                }
                0xd -> {    // ori

                }
                0xa -> {    // slti

                }
                0xb -> {    // sltiu

                }
                0x28 -> {   // sb

                }
                0x38 -> {   // sc
                    // Not implemented
                }
                0x29 -> {   // sh

                }
                0x2b -> {   // sw

                }
            }
        }
    }
}