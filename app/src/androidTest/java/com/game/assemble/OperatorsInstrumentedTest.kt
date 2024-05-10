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
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val sim = MIPSSimulator(appContext)
        sim.generateTask(0)
    }

    private fun get_rand(a: Int, b: Int): Int {
        return (a..b).random()
    }

    private fun get_rand_long(a: Long, b: Long): Long {
        return (a..b).random()
    }
    @Test
    fun test_add() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // add small numbers
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val instrList = mutableListOf<Instruction>(Instruction(arrayOf("main:")))
            val x = get_rand(-100, 100)
            val y = get_rand(-100, 100)
            sim.regs["\$t0"] = x
            sim.regs["\$t1"] = y

            instrList += Instruction(arrayOf("add", "\$t2", "\$t0", "\$t1"))
            sim.run(instrList)
            assertEquals(x + y, sim.regs["\$t2"])
        }

        // edge case - add register with itself
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val instrList = mutableListOf<Instruction>(Instruction(arrayOf("main:")))
            val x = get_rand(-100, 100)
            sim.regs["\$t0"] = x

            instrList += Instruction(arrayOf("add", "\$t0", "\$t0", "\$t0"))
            sim.run(instrList)
            assertEquals(2 * x, sim.regs["\$t0"])
        }

        // edge case - overflow handling (exceed 0x7FFFFFFF)
        repeat(100) {
            val sim = MIPSSimulator(appContext)
            val instrList = mutableListOf<Instruction>(Instruction(arrayOf("main:")))

            val x = 0x80000000 - get_rand_long(0, 0x80000000) + get_rand(0, 100)
            val y = 0x80000000 - x // such that x + y == 0x80000000 + epsilon
            sim.regs["\$t0"] = x.toInt()
            sim.regs["\$t1"] = y.toInt()

            instrList += Instruction(arrayOf("add", "\$t2", "\$t0", "\$t1"))
            assertEquals(sim.run(instrList), "Overflow exception!")
        }
    }
}