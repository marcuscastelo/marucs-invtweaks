package com.marcuscastelo.invtweaks.input

interface IInputProvider {
    fun isDropOperation(): Boolean
    fun getPressedButton(): Int
}