package com.marcuscastelo.invtweaks.util
object Assure {
    fun onlyOneTrue(vararg booleans: Boolean): Boolean {
        var count = 0
        for (b in booleans) {
            if (b) count++
            if (count > 1) return false
        }
        return true
    }

}