package io.github.marcuscastelo.invtweaks.operation

class OperationResult(
        val success: Boolean,
        val message: String = "",
        val nextOperations: List<OperationInfo> = listOf()
) {
    fun success(): Boolean {
        return success
    }

    companion object {
        val SUCCESS = OperationResult(true, "")
        val FAILURE = OperationResult(false, "")
    }
}
