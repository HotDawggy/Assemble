package com.game.assemble

class Registers (
    v0: Int = 0,
    a0: Int = 0,
    a1: Int = 0,
    t0: Int = 0,
    t1: Int = 0,
    t2: Int = 0,
    t3: Int = 0,
    s0: Int = 0,
    s1: Int = 0,
    sp: Int = STACK_START,
    ra: Int = CODE_START
) {
    constructor(other: Registers): this(other["\$v0"], other["\$a0"], other["\$a1"], other["\$t0"], other["\$t1"], other["\$t2"], other["\$t3"], other["\$s0"], other["\$s1"], other["\$sp"], other["\$ra"])
    private val regs: MutableMap<String, Int> = mutableMapOf()
    init {
        regs["\$zero"] = 0    // $zero
        regs["\$v0"] = v0
        regs["\$a0"] = a0
        regs["\$a1"] = a1
        regs["\$t0"] = t0
        regs["\$t1"] = t1
        regs["\$t2"] = t2
        regs["\$t3"] = t3
        regs["\$s0"] = s0
        regs["\$s1"] = s1
        regs["\$sp"] = sp
        regs["\$ra"] = ra
    }
    operator fun get(idx: String?) : Int {
        if (idx.isNullOrEmpty() || !regs.contains(idx)) throw(IllegalArgumentException("Invalid register"))
        return regs[idx]!!
    }
    operator fun set(idx: String?, data: Int) {
        if (idx.isNullOrEmpty() || !regs.contains(idx)) throw(IllegalArgumentException("Invalid register"))
        regs[idx] = data
    }
    operator fun iterator(): MutableIterator<MutableMap.MutableEntry<String, Int>> {
        return regs.iterator()
    }

    fun getMap() : MutableMap<String, Int> {
        return regs
    }
}