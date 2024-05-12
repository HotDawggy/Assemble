package com.game.assemble

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class OperatorsInstrumentedTest {
    private fun get_rand(a: Int, b: Int): Int {
        return (a..b).random()
    }
    
    @Test
    fun test_add() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // add small numbers
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            val x = get_rand(-100, 100)
            val y = get_rand(-100, 100)
            sim.regs["\$t0"] = x
            sim.regs["\$t1"] = y

            instrList += Instruction(arrayOf("add", "\$t2", "\$t0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)
            assertEquals(x + y, sim.regs["\$t2"])
        }

        // edge case - add register with itself
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            val x = get_rand(-100, 100)
            sim.regs["\$t0"] = x

            instrList += Instruction(arrayOf("add", "\$t0", "\$t0", "\$t0"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)
            assertEquals(2 * x, sim.regs["\$t0"])
        }

        // edge case - overflow handling (exceed 0x7FFFFFFF)
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val instrList = mutableListOf(Instruction(arrayOf("main:")))

            val x = 0x80000000 - (0..0x80000000).random() + get_rand(0, 100)
            val y = 0x80000000 - x // such that x + y == 0x80000000 + epsilon
            sim.regs["\$t0"] = x.toInt()
            sim.regs["\$t1"] = y.toInt()

            instrList += Instruction(arrayOf("add", "\$t2", "\$t0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            assertEquals(sim.run(instrList), "Overflow exception!")
        }
    }

    @Test
    fun test_addu() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // adding small numbers
        repeat(100) {
            val x = get_rand(-100, 100)
            val y = get_rand(-100, 100)

            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = x
            sim.regs["\$t1"] = y

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("addu", "\$t2", "\$t0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(x + y, sim.regs["\$t2"])
        }

        // adding to same register
        repeat(100) {
            val x = get_rand(-100, 100)

            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = x

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("addu", "\$t0", "\$t0", "\$t0"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(2 * x, sim.regs["\$t0"])
        }

        // "wrap-around"
        repeat(100) {
            val x = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val y = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)

            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = x
            sim.regs["\$t1"] = y

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("addu", "\$t2", "\$t0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals((x + y).toUInt(), sim.regs["\$t2"].toUInt())
        }
    }

    @Test
    fun test_and() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // test random values
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val y = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)

            sim.regs["\$t0"] = x
            sim.regs["\$t1"] = y

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("and", "\$t2", "\$t1", "\$t0"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$t2"], x and y)
        }

        // test and-ing the same register
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            sim.regs["\$t0"] = x

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("and", "\$t0", "\$t0", "\$t0"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$t0"], x and x)
        }
    }

    @Test
    fun test_div() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // divide by non-zero values
        repeat(100) {
            val x = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            var y = get_rand(-100, 100)
            while (y == 0) {
                y = get_rand(-100, 100)
            }

            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = x
            sim.regs["\$t1"] = y

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("div", "\$t0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(x % y, sim.regs["\$hi"])
            assertEquals(x.floorDiv(y), sim.regs["\$lo"])
        }

        // test division by 0
        repeat(100) {
            val x = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = x
            sim.regs["\$t1"] = 0

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("div", "\$t0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))

            assertEquals(sim.run(instrList), "Divide by zero exception!")
        }
    }

    @Test
    fun test_divu() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // divide by non-zero values
        repeat(100) {
            val x = get_rand(0, Int.MAX_VALUE)
            val y = get_rand(1, 100)

            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = x
            sim.regs["\$t1"] = y

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("div", "\$t0", "\$t1"))

            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(x % y, sim.regs["\$hi"])
            assertEquals(x.floorDiv(y), sim.regs["\$lo"])
        }

        // test division by 0
        repeat(100) {
            val x = get_rand(0, Int.MAX_VALUE)
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = x
            sim.regs["\$t1"] = 0

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("div", "\$t0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            assertEquals(sim.run(instrList), "Divide by zero exception!")
        }
    }

    @Test
    fun test_mult() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // test mult with random ints
        repeat(100) {
            val x = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val y = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = x
            sim.regs["\$t1"] = y

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("mult", "\$t0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$hi"], ((x.toLong() * y.toLong()) ushr 16).toInt())
            assertEquals(sim.regs["\$lo"], ((x.toLong() * y.toLong()) shl 16 ushr 16).toInt())
        }

        // test mult on same register
        repeat(100) {
            val x = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = x

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("mult", "\$t0", "\$t0"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$hi"], ((x.toLong() * x.toLong()) ushr 16).toInt())
            assertEquals(sim.regs["\$lo"], ((x.toLong() * x.toLong()) shl 16 ushr 16).toInt())
        }
    }

    @Test
    fun test_multu() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // test mult with random ints
        repeat(100) {
            val x = get_rand(0, Int.MAX_VALUE)
            val y = get_rand(0, Int.MAX_VALUE)
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = x
            sim.regs["\$t1"] = y

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("mult", "\$t0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$hi"], ((x.toLong() * y.toLong()) ushr 16).toInt())
            assertEquals(sim.regs["\$lo"], ((x.toLong() * y.toLong()) shl 16 ushr 16).toInt())
        }

        // test mult on same register
        repeat(100) {
            val x = get_rand(0, Int.MAX_VALUE)
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = x

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("mult", "\$t0", "\$t0"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$hi"], ((x.toLong() * x.toLong()) ushr 16).toInt())
            assertEquals(sim.regs["\$lo"], ((x.toLong() * x.toLong()) shl 16 ushr 16).toInt())
        }
    }

    @Test
    fun test_mfhi() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // just test moving random values
        repeat(100) {
            val x = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val sim = MIPSSimulator(appContext)
            sim.regs["\$hi"] = x

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("mfhi", "\$t0"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$hi"], x)
        }
    }
    @Test
    fun test_mflo() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // just test with moving random values
        repeat(100) {
            val x = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val sim = MIPSSimulator(appContext)
            sim.regs["\$lo"] = x

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("mflo", "\$t0"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$lo"], x)
        }
    }

    @Test
    fun test_mthi() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // just test with moving random values
        repeat(100) {
            val x = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = x

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("mthi", "\$t0"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$hi"], x)
        }
    }

    @Test
    fun test_mtlo() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // just test with moving random values
        repeat(100) {
            val x = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = x

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("mtlo", "\$t0"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$lo"], x)
        }
    }

    @Test
    fun test_nor() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        repeat(100) {
            val x = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val y = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = x
            sim.regs["\$t1"] = y

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("nor", "\$t2", "\$t0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$t2"], (x.toUInt() or y.toUInt()).inv().toInt())
        }
    }

    @Test
    fun test_or() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        repeat(100) {
            val x = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val y = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = x
            sim.regs["\$t1"] = y

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("or", "\$t2", "\$t0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$t2"], (x.toUInt() or y.toUInt()).toInt())
        }
    }

    @Test
    fun test_slt() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        repeat(100) {// bool(x < y)
            val x = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val y = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = x
            sim.regs["\$t1"] = y

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("slt", "\$t2", "\$t0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$t2"], if (x < y) 1 else 0)
        }
    }

    @Test
    fun test_sltu() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        repeat(100) {// bool(x < y)
            val x = (UInt.MIN_VALUE..UInt.MAX_VALUE).random()
            val y = (UInt.MIN_VALUE..UInt.MAX_VALUE).random()
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = x.toInt()
            sim.regs["\$t1"] = y.toInt()

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("sltu", "\$t2", "\$t0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$t2"], if (x < y) 1 else 0)
        }
    }

    @Test
    fun test_sll() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        repeat(100) {
            val base = get_rand(0, 1000)
            val exp = get_rand(0, 5)
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = base

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("sll", "\$t2", "\$t0", exp.toString()))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$t2"], base shl exp)
        }
    }

    @Test
    fun test_sllv() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        repeat(100) {// same as above, but exp in reg instead
            val base = get_rand(0, 1000)
            val exp = get_rand(0, 5)
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = base
            sim.regs["\$t1"] = exp

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("sllv", "\$t2", "\$t0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$t2"], base shl exp)
        }
    }

    @Test
    fun test_srl() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        repeat(100) {
            val base = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val exp = get_rand(0, 5)
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = base

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("srl", "\$t2", "\$t0", exp.toString()))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$t2"], base ushr exp)
        }
    }

    @Test
    fun test_srlv() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        repeat(100) {// same as above, but exp in reg instead
            val base = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val exp = get_rand(0, 5)
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = base
            sim.regs["\$t1"] = exp

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("srlv", "\$t2", "\$t0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$t2"], base ushr exp)
        }
    }

    @Test
    fun test_sra() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        repeat(100) {// same as above, but exp in reg instead
            val base = get_rand(-1000, 1000)
            val exp = get_rand(0, 5)
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = base

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("sra", "\$t2", "\$t0", exp.toString()))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$t2"], base shr exp)
        }
    }

    @Test
    fun test_srav() { // srav $d, $t, $s $d = $t >> $s
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        repeat(100) {// same as above, but exp in reg instead
            val base = get_rand(-1000, 1000)
            val exp = get_rand(0, 5)
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = base
            sim.regs["\$t1"] = exp

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("srav", "\$t2", "\$t0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$t2"], base shr exp)
        }
    }

    @Test
    fun test_sub() { // sub $d, $s, $t $d = $s - $t
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        repeat(100) {
            val x = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val y = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = x
            sim.regs["\$t1"] = y

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("sub", "\$t2", "\$t0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            val res = sim.run(instrList)

            val sum = x.toLong() - y.toLong()
            if (Int.MIN_VALUE <= sum && sum <= Int.MAX_VALUE) {
                assertEquals(sim.regs["\$t2"], x - y)
            }
            else {
                assertEquals(res, "Overflow exception")
            }
        }
    }

    @Test
    fun test_subu() { // subu $d, $s, $t $d = $s - $t
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        repeat(100) {
            val x = (UInt.MIN_VALUE..UInt.MAX_VALUE).random()
            val y = (UInt.MIN_VALUE..UInt.MAX_VALUE).random()
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = x.toInt()
            sim.regs["\$t1"] = y.toInt()

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("subu", "\$t2", "\$t0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$t2"].toUInt(), x - y)
        }
    }

    @Test
    fun test_addi() { // addi $t, $s, i $t = $s + SE(i)
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        repeat(100) {
            val x = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val y = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = x

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("addi", "\$t2", "\$t0", y.toString()))
            instrList += Instruction(arrayOf("j", "exit"))
            val res = sim.run(instrList)

            val sum = x.toLong() + y.toLong()
            if (Int.MIN_VALUE <= sum && sum <= Int.MAX_VALUE) {
                assertEquals(sim.regs["\$t2"], sum.toInt())
            }
            else {
                assertEquals(res, "Overflow exception")
            }
        }
    }

    @Test
    fun test_addiu() { // addiu $t, $s, i $t = $s + SE(i)
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        repeat(100) {
            val x = (UInt.MIN_VALUE..UInt.MAX_VALUE).random()
            val y = (UInt.MIN_VALUE..UInt.MAX_VALUE).random()
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = x.toInt()

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("addiu", "\$t2", "\$t0", y.toInt().toString()))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            val sum = x.toLong() + y.toLong()
            assertEquals(sim.regs["\$t2"], sum.toInt())
        }
    }

    @Test
    fun test_andi() { // andi $t, $s, i $t = $s & ZE(i)
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        repeat(100) {
            val x = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val y = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = x

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("andi", "\$t2", "\$t0", y.toString()))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            val sum = x and ((y shl 16) ushr 16)
            assertEquals(sim.regs["\$t2"], sum)
        }
    }

    @Test
    fun test_beq() { // beq $s, $t, label if ($s == $t) pc += i << 2
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // test for exit label
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = get_rand(4, 5)
            val y = get_rand(4, 5)
            sim.regs["\$t1"] = x
            sim.regs["\$t2"] = y

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("beq", "\$t1", "\$t2", "exit"))
            instrList += Instruction(arrayOf("add", "\$t1", "\$t1", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            if (x == y) {
                assertEquals(sim.regs["\$t1"], x)
            }
            else {
                assertNotEquals(sim.regs["\$t1"], x)
            }
        }

        // test for arbitrary label
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = get_rand(4, 5)
            val y = get_rand(4, 5)
            sim.regs["\$t1"] = x
            sim.regs["\$t2"] = y

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("beq", "\$t1", "\$t2", "luna"))
            instrList += Instruction(arrayOf("add", "\$t1", "\$t1", "\$t1"))
            instrList += Instruction(arrayOf("luna:"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            if (x == y) {
                assertEquals(sim.regs["\$t1"], x)
            }
            else {
                assertNotEquals(sim.regs["\$t1"], x)
            }
        }

        // test for non-existent label
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = get_rand(4, 5)
            val y = get_rand(4, 5)
            sim.regs["\$t1"] = x
            sim.regs["\$t2"] = y

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("beq", "\$t1", "\$t2", "rawr"))
            instrList += Instruction(arrayOf("add", "\$t1", "\$t1", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            val res = sim.run(instrList)

            if (x == y) {
                assertEquals(res, "Invalid label!")
            }
            else {
                assertEquals(sim.regs["\$t1"], 2 * x)
            }
        }
    }

    @Test
    fun test_bne() { // bne $s, $t, label if ($s != $t) pc += i << 2
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // test for exit label
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = get_rand(4, 5)
            val y = get_rand(4, 5)
            sim.regs["\$t1"] = x
            sim.regs["\$t2"] = y

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("bne", "\$t1", "\$t2", "exit"))
            instrList += Instruction(arrayOf("add", "\$t1", "\$t1", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            if (x != y) {
                assertEquals(sim.regs["\$t1"], x)
            }
            else {
                assertEquals(sim.regs["\$t1"], x + x)
            }
        }

        // test for arbitrary label
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = get_rand(4, 5)
            val y = get_rand(4, 5)
            sim.regs["\$t1"] = x
            sim.regs["\$t2"] = y

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("bne", "\$t1", "\$t2", "luna"))
            instrList += Instruction(arrayOf("add", "\$t1", "\$t1", "\$t1"))
            instrList += Instruction(arrayOf("luna:"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            if (x != y) {
                assertEquals(sim.regs["\$t1"], x)
            }
            else {
                assertEquals(sim.regs["\$t1"], x + x)
            }
        }

        // test for non-existent label
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = get_rand(4, 5)
            val y = get_rand(4, 5)
            sim.regs["\$t1"] = x
            sim.regs["\$t2"] = y

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("bne", "\$t1", "\$t2", "rawr"))
            instrList += Instruction(arrayOf("add", "\$t1", "\$t1", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            val res = sim.run(instrList)

            if (x != y) {
                assertEquals(res, "Invalid label!")
            }
            else {
                assertEquals(sim.regs["\$t1"], 2 * x)
            }
        }
    }

    @Test
    fun test_blez() { // blez $s, label if ($s <= 0) pc += i << 2
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // test for exit label
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = get_rand(-3, 3)
            sim.regs["\$t1"] = x

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("blez", "\$t1", "exit"))
            instrList += Instruction(arrayOf("add", "\$t1", "\$t1", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            if (x <= 0) {
                assertEquals(sim.regs["\$t1"], x)
            }
            else {
                assertEquals(sim.regs["\$t1"], x + x)
            }
        }

        // test for arbitrary label
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = get_rand(-3, 3)
            sim.regs["\$t1"] = x

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("blez", "\$t1", "luna"))
            instrList += Instruction(arrayOf("add", "\$t1", "\$t1", "\$t1"))
            instrList += Instruction(arrayOf("luna:"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            if (x <= 0) {
                assertEquals(sim.regs["\$t1"], x)
            }
            else {
                assertEquals(sim.regs["\$t1"], x + x)
            }
        }

        // test for non-existent label
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = get_rand(-3, 3)
            sim.regs["\$t1"] = x

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("blez", "\$t1", "rawr"))
            instrList += Instruction(arrayOf("add", "\$t1", "\$t1", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            val res = sim.run(instrList)

            if (x <= 0) {
                assertEquals(res, "Invalid label!")
            }
            else {
                assertEquals(sim.regs["\$t1"], x + x)
            }
        }
    }

    @Test
    fun test_bgtz() { // bgtz $s, label if ($s > 0) pc += i << 2
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // test for exit label
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = get_rand(-3, 3)
            sim.regs["\$t1"] = x

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("bgtz", "\$t1", "exit"))
            instrList += Instruction(arrayOf("add", "\$t1", "\$t1", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            if (x > 0) {
                assertEquals(sim.regs["\$t1"], x)
            }
            else {
                assertEquals(sim.regs["\$t1"], x + x)
            }
        }

        // test for arbitrary label
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = get_rand(-3, 3)
            sim.regs["\$t1"] = x

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("bgtz", "\$t1", "luna"))
            instrList += Instruction(arrayOf("add", "\$t1", "\$t1", "\$t1"))
            instrList += Instruction(arrayOf("luna:"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            if (x > 0) {
                assertEquals(sim.regs["\$t1"], x)
            }
            else {
                assertEquals(sim.regs["\$t1"], x + x)
            }
        }

        // test for non-existent label
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = get_rand(-3, 3)
            sim.regs["\$t1"] = x

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("bgtz", "\$t1", "rawr"))
            instrList += Instruction(arrayOf("add", "\$t1", "\$t1", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            val res = sim.run(instrList)

            if (x > 0) {
                assertEquals(res, "Invalid label!")
            }
            else {
                assertEquals(sim.regs["\$t1"], x + x)
            }
        }
    }

    @Test
    fun test_j() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // test jump to exit
        run { // use run-block for scoping (convenience)
            val sim = MIPSSimulator(appContext)
            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("j", "exit"))
            instrList += Instruction(arrayOf("addi", "\$t0", "\$zero", "1"))
            sim.run(instrList)

            assertNotEquals(sim.regs["\$t0"], 1)
        }

        // test jump to arbitrary label
        run {
            val sim = MIPSSimulator(appContext)
            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("j", "luna"))
            instrList += Instruction(arrayOf("addi", "\$t0", "\$zero", "1"))
            instrList += Instruction(arrayOf("luna:"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertNotEquals(sim.regs["\$t0"], 1)
        }

        // test jump to invalid label
        run {
            val sim = MIPSSimulator(appContext)
            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("j", "rawrrr"))
            instrList += Instruction(arrayOf("addi", "\$t0", "\$zero", "1"))
            instrList += Instruction(arrayOf("j", "exit"))

            assertEquals(sim.run(instrList), "Invalid label!")
        }
    }

    @Test
    fun test_jal() { // jal label $31 = pc; pc += i << 2
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // test jump to exit
        run { // use run-block for scoping (convenience)
            val sim = MIPSSimulator(appContext)
            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("jal", "exit"))
            instrList += Instruction(arrayOf("addi", "\$t0", "\$zero", "1"))
            sim.run(instrList)

            assertEquals(sim.regs["\$ra"], 0x00400000 + 8)
        }

        // test jump to arbitrary label
        run {
            val sim = MIPSSimulator(appContext)
            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("jal", "luna"))
            instrList += Instruction(arrayOf("addi", "\$t0", "\$zero", "1"))
            instrList += Instruction(arrayOf("luna:"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$ra"], 0x00400000 + 8)
        }


        // test jump to invalid label
        run {
            val sim = MIPSSimulator(appContext)
            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("jal", "rawrrr"))
            instrList += Instruction(arrayOf("addi", "\$t0", "\$zero", "1"))
            instrList += Instruction(arrayOf("j", "exit"))

            assertEquals(sim.run(instrList), "Invalid label!")
        }
    }

    @Test
    fun test_jalr() { // jalr $s $31 = pc; pc = $s
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // store the desired address into $t0
        val sim = MIPSSimulator(appContext)
        sim.regs["\$t0"] = 0x00400000 + 8 + 4 + 4 // jump to the 2nd addi

        // jalr to $t0
        val instrList = mutableListOf(Instruction(arrayOf("main:")))
        instrList += Instruction(arrayOf("jalr", "\$t0"))

        // instruction that should be jumped over
        instrList += Instruction(arrayOf("addi", "\$t2", "\$zero", "23"))

        // check for $ra and jump instr
        instrList += Instruction(arrayOf("addi", "\$t2", "\$t2", "4"))
        instrList += Instruction(arrayOf("j", "exit"))

        sim.run(instrList)
        Log.i("MYDEBUG", sim.regs["\$ra"].toString())
        assertEquals(sim.regs["\$ra"], 0x00400000 + 8)
        assertEquals(sim.regs["\$t2"], 4)
    }

    @Test
    fun test_jr() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // jump to valid address
        run {
            // store the desired address into $t0
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = 0x00400000 + 4 + 4 + 4 // jump to the 2nd addi

            // jr to $t0
            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("jr", "\$t0"))

            // instruction that should be jumped over
            instrList += Instruction(arrayOf("addi", "\$t2", "\$zero", "23"))

            // should jump to here
            instrList += Instruction(arrayOf("addi", "\$t2", "\$t2", "4"))
            instrList += Instruction(arrayOf("j", "exit"))

            sim.run(instrList)
            assertEquals(sim.regs["\$t2"], 4)
        }

        // jump to invalid address
        run {
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t0"] = 1234321
            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("jr", "\$t0"))
            instrList += Instruction(arrayOf("j", "exit"))

            assertEquals(sim.run(instrList), "Invalid code address")
        }
    }

    @Test
    fun test_lbu() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // insert byte into memory and load it
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t1"] = 0x7ffffffc
            sim.regs["\$t0"] = get_rand(0, 255)

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("sb", "\$t0", "0", "\$t1"))
            instrList += Instruction(arrayOf("lbu", "\$t2", "0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$t2"], sim.regs["\$t0"])
        }

        // jump to invalid address
        run {
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t1"] = 0x7ffffffc + 1

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("lbu", "\$t2", "0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))

            assertEquals(sim.run(instrList), "Invalid stack address")
        }
    }

    @Test
    fun test_lb() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // insert byte into memory and load it
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t1"] = 0x7ffffffc
            sim.regs["\$t0"] = get_rand(0, 255)

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("sb", "\$t0", "0", "\$t1"))
            instrList += Instruction(arrayOf("lb", "\$t2", "0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            // cast signed byte to unsigned for checking
            assertEquals(sim.regs["\$t2"].toUByte(), sim.regs["\$t0"].toUByte())
        }

        // jump to invalid address
        run {
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t1"] = 0x7ffffffc + 1

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("lb", "\$t2", "0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))

            assertEquals(sim.run(instrList), "Invalid stack address")
        }
    }

    @Test
    fun test_lhu() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // insert half-unsigned into memory and load it
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = get_rand(0, 1000)
            sim.regs["\$t1"] = 0x7ffffffc
            sim.regs["\$t0"] = x

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("sh", "\$t0", "0", "\$t1"))
            instrList += Instruction(arrayOf("lhu", "\$t2", "0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            // cast signed byte to unsigned for checking
            assertEquals(sim.regs["\$t2"].toUInt() and 0xFFFFu, (x).toUInt() and 0xFFFFu)
        }
        // jump to invalid address
        run {
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t1"] = 0x7ffffffc + 1

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("lhu", "\$t2", "0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))

            assertEquals(sim.run(instrList), "Invalid stack address")
        }
    }

    @Test
    fun test_lh() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // insert half-word into memory and load it
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = get_rand(0, 1000)
            sim.regs["\$t1"] = 0x7ffffffc
            sim.regs["\$t0"] = x

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("sh", "\$t0", "0", "\$t1"))
            instrList += Instruction(arrayOf("lh", "\$t2", "0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$t2"], x)
        }

        // jump to invalid address
        run {
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t1"] = 0x7ffffffc + 1

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("lh", "\$t2", "0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))

            assertEquals(sim.run(instrList), "Invalid stack address")
        }
    }

    @Test
    fun test_lui() { // $t = i << 16
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = get_rand(0, 1000)
            sim.regs["\$t0"] = x

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("lui", "\$t0", x.toString()))
            instrList += Instruction(arrayOf("j", "exit"))

            sim.run(instrList)
            assertEquals(sim.regs["\$t0"], x shl 16)
        }
    }

    @Test
    fun test_lw() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // insert word into memory and load it
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = get_rand(0, Int.MAX_VALUE)
            sim.regs["\$t1"] = 0x7ffffffc
            sim.regs["\$t0"] = x

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("sw", "\$t0", "0", "\$t1"))
            instrList += Instruction(arrayOf("lw", "\$t2", "0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            // cast signed byte to unsigned for checking
            assertEquals(sim.regs["\$t2"].toUInt(), (x).toUInt())
        }

        // jump to invalid address
        run {
            val sim = MIPSSimulator(appContext)
            sim.regs["\$t1"] = 0x7ffffffc + 1

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("lw", "\$t2", "0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))

            assertEquals(sim.run(instrList), "Invalid stack address")
        }
    }

    @Test
    fun test_ori() { // ori $t, $s, i $t = $s | ZE(i)
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val y = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            sim.regs["\$t0"] = y

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("ori", "\$t1", "\$t0", x.toString()))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$t1"], (y or ((x shl 16) ushr 16)))
        }
    }

    @Test
    fun test_slti() { // slti $t, $s, i $t = ($s < SE(i))
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val y = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            sim.regs["\$t1"] = y

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("slti", "\$t0", "\$t1", x.toString()))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$t0"], if (y < x) 1 else 0)
        }
    }

    @Test
    fun test_sltiu() { // sltiu $t, $s, i $t = ($s < SE(i))
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = (UInt.MIN_VALUE..UInt.MAX_VALUE).random()
            val y = (UInt.MIN_VALUE..UInt.MAX_VALUE).random()
            sim.regs["\$t1"] = y.toInt()

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("sltiu", "\$t0", "\$t1", x.toInt().toString()))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$t0"], if (y < x) 1 else 0)
        }
    }

    @Test
    fun test_sb() {
        this.test_lb()
    }

    @Test
    fun test_sh() {
        this.test_lh()
    }

    @Test
    fun test_sw() {
        this.test_lw()
    }

    @Test
    fun test_xor() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val y = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            sim.regs["\$t1"] = y
            sim.regs["\$t0"] = x

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("xor", "\$t2", "\$t0", "\$t1"))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$t2"], x xor y)
        }
    }

    @Test
    fun test_xori() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val x = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            val y = get_rand(Int.MIN_VALUE, Int.MAX_VALUE)
            sim.regs["\$t0"] = x

            val instrList = mutableListOf(Instruction(arrayOf("main:")))
            instrList += Instruction(arrayOf("xori", "\$t2", "\$t0", y.toString()))
            instrList += Instruction(arrayOf("j", "exit"))
            sim.run(instrList)

            assertEquals(sim.regs["\$t2"], x xor ((y shl 16) ushr 16))
        }
    }
}