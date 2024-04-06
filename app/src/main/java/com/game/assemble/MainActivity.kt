package com.game.assemble

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sim: MIPSSimulator = MIPSSimulator(
            a0 = 0x4,
            sp = 0x7ffffff4
        )

        // Testing MIPSSimulator, remove on production
        val temp = byteArrayOf(0x10, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x20)
        Log.i("printStack", temp.toString())
        sim.stack = temp
        sim.Run(listOf(
            Instruction(opcode = 0x28, rt = 4, rs = 29, immediate = 4)
            //Instruction(),
            //Instruction(opcode = 0, rd = 2, rs = 4, rt = 5, funct = 0x20)
            ))
        if (sim.err.isNotBlank()) {
            Log.i("sim.err", sim.err)
            sim.printState()
        }
        else {
            Log.i("sim.v0", sim.regs[2].toString())
            //sim.printStack()
        }
    }
}