package io.github.marcuscastelo.invtweaks.operation

class OperationResult(
        val success: SuccessType,
        val message: String = "",
        val nextOperations: Iterable<OperationInfo> = listOf()
) {
    enum class SuccessType {
        SUCCESS,
        FAILURE,
        PASS
    }

    companion object {
        val SUCCESS = OperationResult(SuccessType.SUCCESS, "")
        val FAILURE = OperationResult(SuccessType.FAILURE, "")
        val PASS = OperationResult(SuccessType.PASS, "")

        fun success(message: String): OperationResult {
            return OperationResult(SuccessType.SUCCESS, message)
        }

        fun failure(message: String): OperationResult {
            return OperationResult(SuccessType.FAILURE, message)
        }

        fun pass(message: String): OperationResult {
            return OperationResult(SuccessType.PASS, message)
        }
    }

    override fun toString(): String {
        return "OperationResult(success=$success, message='$message', nextOperations=$nextOperations)"
    }
}
