package com.game.assemble

import android.content.Context
import android.util.Log

class GameTask(context: Context) {
    val info: MutableMap<String, Any?> = mutableMapOf(
        "id" to null,
        "text" to null
    )
    private val taskList: Array<String> = context.resources.getStringArray(R.array.taskList)
    fun getRandomTask() : Int {
        val idx = 0 //taskList.indices.random()
        info["id"] = idx
        info["text"] = taskList[idx]
        return idx
    }

    fun setTask(idx: Int) {
        info["id"] = idx
        info["text"] = taskList[idx]
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

    fun findPrimeList(_n: Int): MutableList<Int> {
        var res = mutableListOf<Int>()
        var n = _n

        var p: Int = 2
        while(p * p <= n) {
            while(n % p == 0) {
                res.add(p)
                n /= p
            }
            p += 1
        }
        if (n > 1) res.add(n)
        return res
    }

    operator fun get(key: String) : Any? {
        Log.d("GameTask", key)
        Log.d("GameTask", info.containsKey(key).toString())
        //if (!info.containsKey(key)) throw(IllegalArgumentException("Invalid key"))
        return info[key]
    }
    operator fun set(key: String, value: Any?) {
        info[key] = value
    }
}