package io.github.marcuscastelo.invtweaks;

@FunctionalInterface
public interface OperationExecutor {
    void execute(InvTweaksOperationInfo operationInfo);
}
