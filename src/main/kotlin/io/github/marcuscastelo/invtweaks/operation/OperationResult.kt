package io.github.marcuscastelo.invtweaks.operation

class OperationResult(val success: Boolean, val message: String = "") {
    fun success(): Boolean {
        return success
    }

    companion object {
        val SUCCESS = OperationResult(true, "")
        val FAILURE = OperationResult(false, "")
    }
}
