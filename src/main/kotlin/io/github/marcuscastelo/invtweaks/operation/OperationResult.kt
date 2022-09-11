package io.github.marcuscastelo.invtweaks.operation

class OperationResult(
        val success: Boolean,
        val message: String = "",
        val nextOperations: Iterable<OperationInfo> = listOf()
) {
    companion object {
        val SUCCESS = OperationResult(true, "")
        val FAILURE = OperationResult(false, "")

        fun success(message: String): OperationResult {
            return OperationResult(true, message)
        }

        fun failure(message: String): OperationResult {
            return OperationResult(false, message)
        }
    }

    override fun toString(): String {
        return "OperationResult(success=$success, message='$message', nextOperations=$nextOperations)"
    }
}
