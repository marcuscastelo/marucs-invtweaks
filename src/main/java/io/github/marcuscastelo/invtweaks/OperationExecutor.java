package io.github.marcuscastelo.invtweaks;

@FunctionalInterface
public interface OperationExecutor {
    OperationResult execute(InvTweaksOperationInfo operationInfo);
}
