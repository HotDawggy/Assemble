package com.game.assemble

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val sim = MIPSSimulator(appContext)
        sim.generateTask(0)
    }

    @SdkSuppress(minSdkVersion = 29)
    @Test
    fun testModelSolution0() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val sim = MIPSSimulator(appContext)
        val taskID = 0
        sim.generateTask(taskID)
        sim.gameTask.setTask(taskID)

        val instrList = mutableListOf(Instruction((arrayOf("main:"))))
        instrList += Instruction(arrayOf("add", "\$s0", "\$a0", "\$zero"))
        instrList += Instruction(arrayOf("add", "\$s1", "\$a1", "\$zero"))
        instrList += Instruction(arrayOf("jal", "Luna"))
        instrList += Instruction(arrayOf("div", "\$s0", "\$v0"))
        instrList += Instruction(arrayOf("mflo", "\$t0"))
        instrList += Instruction(arrayOf("mult", "\$t0", "\$s1"))
        instrList += Instruction(arrayOf("mflo", "\$v0"))
        instrList += Instruction(arrayOf("j", "exit"))
        instrList += Instruction(arrayOf("Luna:"))
        instrList += Instruction(arrayOf("sw", "\$ra", "0", "\$sp"))
        instrList += Instruction(arrayOf("addi", "\$sp", "\$sp", "-4"))
        instrList += Instruction(arrayOf("add", "\$t0", "\$a0", "\$zero"))
        instrList += Instruction(arrayOf("add", "\$t1", "\$a1", "\$zero"))
        instrList += Instruction(arrayOf("beq", "\$t1", "\$zero", "Fifi"))
        instrList += Instruction(arrayOf("add", "\$a0", "\$t1", "\$zero"))
        instrList += Instruction(arrayOf("div", "\$t0", "\$t1"))
        instrList += Instruction(arrayOf("mfhi", "\$a1"))
        instrList += Instruction(arrayOf("jal", "Luna"))
        instrList += Instruction(arrayOf("Fifi:"))
        instrList += Instruction(arrayOf("addi", "\$sp", "\$sp", "4"))
        instrList += Instruction(arrayOf("lw", "\$ra", "0", "\$sp"))
        instrList += Instruction(arrayOf("add", "\$v0", "\$t0", "\$zero"))
        instrList += Instruction(arrayOf("jr", "\$ra"))

        assertEquals(sim.validateTask(instrList), "Success!")
    }

    @Test
    fun testModelSolution1() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val sim = MIPSSimulator(appContext)
        val taskID = 1
        sim.generateTask(taskID)
        sim.gameTask.setTask(taskID)

        val instrList = mutableListOf(Instruction((arrayOf("main:"))))
        instrList += Instruction(arrayOf("add", "\$s0", "\$zero", "\$a0"))
        instrList += Instruction(arrayOf("add", "\$s1", "\$zero", "\$a1"))
        instrList += Instruction(arrayOf("add", "\$t0", "\$zero", "\$zero"))    // let i = 0
        instrList += Instruction(arrayOf("Luna:"))                              // for
        instrList += Instruction(arrayOf("beq", "\$t0", "\$s1", "exit"))            // i < size
        instrList += Instruction(arrayOf("addi", "\$t1", "\$zero", "1"))          // let j = i + 1
        instrList += Instruction(arrayOf("Fifi:"))                              // for
        instrList += Instruction(arrayOf("beq", "\$t1", "\$s1", "Muff"))            // j < size
        instrList += Instruction(arrayOf("addi", "\$t2", "\$zero", "-4"))
        instrList += Instruction(arrayOf("mult", "\$t2", "\$t1"))
        instrList += Instruction(arrayOf("mflo", "\$t2"))
        instrList += Instruction(arrayOf("add", "\$a1", "\$s0", "\$t2"))        // addr array[j]
        instrList += Instruction(arrayOf("addi", "\$a0", "\$a1", "4"))          // addr array[j - 1]
        instrList += Instruction(arrayOf("lw", "\$t2", "0", "\$a0"))            // array[j - 1]
        instrList += Instruction(arrayOf("lw", "\$t3", "0", "\$a1"))            // array[j]
        instrList += Instruction(arrayOf("sub", "\$v0", "\$t2", "\$t3"))
        instrList += Instruction(arrayOf("blez", "\$v0", "Pudd"))
        instrList += Instruction(arrayOf("sw", "\$t2", "0", "\$a1"))
        instrList += Instruction(arrayOf("sw", "\$t3", "0", "\$a0"))
        instrList += Instruction(arrayOf("Pudd:"))
        instrList += Instruction(arrayOf("addi", "\$t1", "\$t1", "1"))
        instrList += Instruction(arrayOf("j", "Fifi"))
        instrList += Instruction(arrayOf("Muff:"))
        instrList += Instruction(arrayOf("addi", "\$t0", "\$t0", "1"))
        instrList += Instruction(arrayOf("j", "Luna"))

        assertEquals(sim.validateTask(instrList), "Success!")
    }

    @Test
    fun testModelSolution2() { // note - timed out
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val sim = MIPSSimulator(appContext)
        val taskID = 2
        //sim.generateTask(taskID)
        //sim.gameTask.setTask(taskID)

        val instrList = mutableListOf(Instruction((arrayOf("main:"))))
        instrList += Instruction(arrayOf("add", "\$s0", "\$a0", "\$zero"))
        instrList += Instruction(arrayOf("add", "\$s1", "\$a1", "\$zero"))
        instrList += Instruction(arrayOf("jal", "Luna"))
        instrList += Instruction(arrayOf("j", "exit"))
        instrList += Instruction(arrayOf("Luna:"))
        instrList += Instruction(arrayOf("sw", "\$ra", "0", "\$sp"))
        instrList += Instruction(arrayOf("addi", "\$sp", "\$sp", "-4"))
        instrList += Instruction(arrayOf("add", "\$t0", "\$a0", "\$zero"))
        instrList += Instruction(arrayOf("add", "\$t1", "\$a1", "\$zero"))
        instrList += Instruction(arrayOf("beq", "\$t1", "\$zero", "Fifi"))
        instrList += Instruction(arrayOf("add", "\$a0", "\$t1", "\$zero"))
        instrList += Instruction(arrayOf("div", "\$t0", "\$t1"))
        instrList += Instruction(arrayOf("mfhi", "\$a1"))
        instrList += Instruction(arrayOf("jal", "Luna"))
        instrList += Instruction(arrayOf("Fifi:"))
        instrList += Instruction(arrayOf("addi", "\$sp", "\$sp", "4"))
        instrList += Instruction(arrayOf("lw", "\$ra", "0", "\$sp"))
        instrList += Instruction(arrayOf("add", "\$v0", "\$t0", "\$zero"))
        instrList += Instruction(arrayOf("jr", "\$ra"))


        //assertEquals(sim.validateTask(instrList), "Success!")
    }

    @Test
    fun testModelSolution3() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val sim = MIPSSimulator(appContext)
        val taskID = 3
        sim.generateTask(taskID)
        sim.gameTask.setTask(taskID)

        val instrList = mutableListOf(Instruction((arrayOf("main:"))))
        instrList += Instruction(arrayOf("add", "\$t0", "\$zero", "\$zero"))
        instrList += Instruction(arrayOf("addi", "\$t1", "\$zero", "1"))
        instrList += Instruction(arrayOf("sw", "\$t0", "0", "\$a0"))
        instrList += Instruction(arrayOf("addi", "\$a0", "\$a0", "-4"))
        instrList += Instruction(arrayOf("sw", "\$t1", "0", "\$a0"))
        instrList += Instruction(arrayOf("addi", "\$a0", "\$a0", "-4"))
        instrList += Instruction(arrayOf("addi", "\$a1", "\$a1", "-2"))
        instrList += Instruction(arrayOf("Luna:"))
        instrList += Instruction(arrayOf("beq", "\$a1", "\$zero", "Fifi"))
        instrList += Instruction(arrayOf("add", "\$t2", "\$t0", "\$t1"))
        instrList += Instruction(arrayOf("add", "\$t0", "\$t1", "\$zero"))
        instrList += Instruction(arrayOf("add", "\$t1", "\$t2", "\$zero"))
        instrList += Instruction(arrayOf("sw", "\$t2", "0", "\$a0"))
        instrList += Instruction(arrayOf("addi", "\$a1", "\$a1", "-1"))
        instrList += Instruction(arrayOf("addi", "\$a0", "\$a0", "-4"))
        instrList += Instruction(arrayOf("j", "Luna"))
        instrList += Instruction(arrayOf("Fifi:"))
        instrList += Instruction(arrayOf("j", "exit"))

        assertEquals(sim.validateTask(instrList), "Success!")
    }

    @Test
    fun testModelSolution4() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val sim = MIPSSimulator(appContext)
        val taskID = 4
        sim.generateTask(taskID)
        sim.gameTask.setTask(taskID)

        val instrList = mutableListOf(Instruction((arrayOf("main:"))))
        instrList += Instruction(arrayOf("add", "\$s0", "\$zero", "\$a0"))
        instrList += Instruction(arrayOf("addi", "\$s1", "\$zero", "2"))
        instrList += Instruction(arrayOf("Luna:"))
        instrList += Instruction(arrayOf("mult", "\$s1", "\$s1"))
        instrList += Instruction(arrayOf("mflo", "\$t0"))
        instrList += Instruction(arrayOf("sub", "\$t0", "\$t0", "\$s0"))
        instrList += Instruction(arrayOf("bgtz", "\$t0", "Muff"))
        instrList += Instruction(arrayOf("Fifi:"))
        instrList += Instruction(arrayOf("div", "\$s0", "\$s1"))
        instrList += Instruction(arrayOf("mfhi", "\$t0"))
        instrList += Instruction(arrayOf("bne", "\$t0", "\$zero", "Pudd"))
        instrList += Instruction(arrayOf("sw", "\$s1", "0", "\$a1"))
        instrList += Instruction(arrayOf("addi", "\$a1", "\$a1", "-4"))
        instrList += Instruction(arrayOf("mflo", "\$t0"))
        instrList += Instruction(arrayOf("add", "\$s0", "\$t0", "\$zero"))
        instrList += Instruction(arrayOf("j", "Fifi"))
        instrList += Instruction(arrayOf("Pudd:"))
        instrList += Instruction(arrayOf("addi", "\$s1", "\$s1", "1"))
        instrList += Instruction(arrayOf("j", "Luna"))
        instrList += Instruction(arrayOf("Muff:"))
        instrList += Instruction(arrayOf("addi", "\$t0", "\$s0", "-1"))
        instrList += Instruction(arrayOf("blez", "\$t0", "exit"))
        instrList += Instruction(arrayOf("sw", "\$s0", "0", "\$a1"))
        instrList += Instruction(arrayOf("addi", "\$a1", "\$a1", "-4"))
        instrList += Instruction(arrayOf("j", "exit"))


        assertEquals(sim.validateTask(instrList), "Success!")
    }

    @Test
    fun testModelSolution5() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val sim = MIPSSimulator(appContext)
        val taskID = 5
        sim.generateTask(taskID)
        sim.gameTask.setTask(taskID)

        val instrList = mutableListOf(Instruction((arrayOf("main:"))))
        instrList += Instruction(arrayOf("add", "\$s0", "\$zero", "\$a0"))
        instrList += Instruction(arrayOf("addi", "\$s1", "\$zero", "2"))
        instrList += Instruction(arrayOf("add", "\$v0", "\$zero", "\$zero"))
        instrList += Instruction(arrayOf("Luna:"))
        instrList += Instruction(arrayOf("mult", "\$s1", "\$s1"))
        instrList += Instruction(arrayOf("mflo", "\$t0"))
        instrList += Instruction(arrayOf("sub", "\$t0", "\$t0", "\$s0"))
        instrList += Instruction(arrayOf("bgtz", "\$t0", "Muff"))
        instrList += Instruction(arrayOf("Fifi:"))
        instrList += Instruction(arrayOf("div", "\$s0", "\$s1"))
        instrList += Instruction(arrayOf("mfhi", "\$t0"))
        instrList += Instruction(arrayOf("bne", "\$t0", "\$zero", "Pudd"))
        instrList += Instruction(arrayOf("sub", "\$t0", "\$s1", "\$v0"))
        instrList += Instruction(arrayOf("blez", "\$t0", "Bubs"))
        instrList += Instruction(arrayOf("add", "\$v0", "\$s1", "\$zero"))
        //instrList += Instruction(arrayOf("sw", "\$s1", "0", "\$a1"))
        //instrList += Instruction(arrayOf("addi", "\$a1", "\$a1", "-4"))
        instrList += Instruction(arrayOf("Bubs:"))
        instrList += Instruction(arrayOf("mflo", "\$t0"))
        instrList += Instruction(arrayOf("add", "\$s0", "\$t0", "\$zero"))
        instrList += Instruction(arrayOf("j", "Fifi"))
        instrList += Instruction(arrayOf("Pudd:"))
        instrList += Instruction(arrayOf("addi", "\$s1", "\$s1", "1"))
        instrList += Instruction(arrayOf("j", "Luna"))
        instrList += Instruction(arrayOf("Muff:"))
        instrList += Instruction(arrayOf("addi", "\$t0", "\$s0", "-1"))
        instrList += Instruction(arrayOf("blez", "\$t0", "exit"))
        instrList += Instruction(arrayOf("sub", "\$t0", "\$s1", "\$v0"))
        instrList += Instruction(arrayOf("blez", "\$t0", "exit"))
        instrList += Instruction(arrayOf("add", "\$v0", "\$s0", "\$zero"))
        //instrList += Instruction(arrayOf("sw", "\$s0", "0", "\$a1"))
        //instrList += Instruction(arrayOf("addi", "\$a1", "\$a1", "-4"))
        instrList += Instruction(arrayOf("j", "exit"))


        assertEquals(sim.validateTask(instrList), "Success!")
    }

    @Test
    fun testModelSolution6() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val sim = MIPSSimulator(appContext)
        val taskID = 6
        sim.generateTask(taskID)
        sim.gameTask.setTask(taskID)

        val instrList = mutableListOf(Instruction((arrayOf("main:"))))
        instrList += Instruction(arrayOf("jal", "Luna"))
        instrList += Instruction(arrayOf("j", "exit"))
        instrList += Instruction(arrayOf("Luna:"))
        instrList += Instruction(arrayOf("sw", "\$ra", "0", "\$sp"))
        instrList += Instruction(arrayOf("addi", "\$sp", "\$sp", "-4"))
        instrList += Instruction(arrayOf("lb", "\$t0", "0", "\$a0"))
        instrList += Instruction(arrayOf("lb", "\$t1", "0", "\$a1"))
        instrList += Instruction(arrayOf("beq", "\$t0", "\$t1", "Fifi"))
        instrList += Instruction(arrayOf("xor", "\$v0", "\$v0", "\$v0"))
        instrList += Instruction(arrayOf("addi", "\$sp", "\$sp", "4"))
        instrList += Instruction(arrayOf("lw", "\$ra", "0", "\$sp"))
        instrList += Instruction(arrayOf("jr", "\$ra"))
        instrList += Instruction(arrayOf("Fifi:"))
        instrList += Instruction(arrayOf("beq", "\$t0", "\$zero", "Pudd"))
        instrList += Instruction(arrayOf("addi", "\$a0", "\$a0", "-1"))
        instrList += Instruction(arrayOf("addi", "\$a1", "\$a1", "-1"))
        instrList += Instruction(arrayOf("jal", "Luna"))
        instrList += Instruction(arrayOf("addi", "\$sp", "\$sp", "4"))
        instrList += Instruction(arrayOf("lw", "\$ra", "0", "\$sp"))
        instrList += Instruction(arrayOf("jr", "\$ra"))
        instrList += Instruction(arrayOf("Pudd:"))
        instrList += Instruction(arrayOf("addi", "\$v0", "\$zero", "1"))
        instrList += Instruction(arrayOf("addi", "\$sp", "\$sp", "4"))
        instrList += Instruction(arrayOf("lw", "\$ra", "0", "\$sp"))
        instrList += Instruction(arrayOf("jr", "\$ra"))


        assertEquals(sim.validateTask(instrList), "Success!")
    }
}