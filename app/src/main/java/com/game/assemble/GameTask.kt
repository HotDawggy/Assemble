package com.game.assemble

import android.content.Context

class GameTask(context: Context) {
    val info: MutableMap<String, Any?> = mutableMapOf(
        "id" to null,
        "text" to null
    )
    private val taskList: Array<String> = context.resources.getStringArray(R.array.taskList)
    fun getRandomTask() : Int {
        val idx = 3 //taskList.indices.random()
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
    operator fun get(key: String) : Any? {
        if (!info.containsKey(key)) throw(IllegalArgumentException("Invalid key"))
        return info[key]
    }
    operator fun set(key: String, value: Any?) {
        info[key] = value
    }
}