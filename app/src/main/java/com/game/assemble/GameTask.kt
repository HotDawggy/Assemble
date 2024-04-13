package com.game.assemble

import android.content.Context

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