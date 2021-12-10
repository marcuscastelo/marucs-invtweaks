package io.github.marcuscastelo.invtweaks;

import io.github.marcuscastelo.invtweaks.client.behavior.IInvTweaksBehavior;

import java.util.Optional;

public enum InvTweaksOperationType {
    NONE, SORT, MOVE_ONE, MOVE_STACK, MOVE_ALL, MOVE_ALL_SAME_TYPE, DROP_ONE, DROP_STACK, DROP_ALL, DROP_ALL_SAME_TYPE;

    public boolean isMove() {
        return this == MOVE_ONE || this == MOVE_STACK || this == MOVE_ALL || this == MOVE_ALL_SAME_TYPE;
    }

    public boolean isDrop() {
        return this == DROP_ONE || this == DROP_STACK || this == DROP_ALL || this == DROP_ALL_SAME_TYPE;
    }

    public boolean isSort() {
        return this == SORT;
    }

    public boolean isAll() {
        return this == MOVE_ALL || this == DROP_ALL || this == MOVE_ALL_SAME_TYPE || this == DROP_ALL_SAME_TYPE;
    }

    public boolean isAllSameType() {
        return this == MOVE_ALL_SAME_TYPE || this == DROP_ALL_SAME_TYPE;
    }

    public Optional<OperationExecutor> asOperationExecutor(IInvTweaksBehavior behavior) {
        return switch (this) {
            case NONE -> Optional.empty();
            case SORT -> Optional.of(behavior::sort);
            case DROP_ALL -> Optional.of(behavior::dropAll);
            case DROP_ONE -> Optional.of(behavior::dropOne);
            case DROP_STACK -> Optional.of(behavior::dropStack);
            case MOVE_ALL -> Optional.of(behavior::moveAll);
            case MOVE_ONE -> Optional.of(behavior::moveOne);
            case MOVE_STACK -> Optional.of(behavior::moveStack);
            case DROP_ALL_SAME_TYPE -> Optional.of(behavior::dropAllSameType);
            case MOVE_ALL_SAME_TYPE -> Optional.of(behavior::moveAllSameType);
        };
    }
}
