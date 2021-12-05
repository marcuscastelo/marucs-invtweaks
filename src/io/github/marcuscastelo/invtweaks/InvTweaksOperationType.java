package io.github.marcuscastelo.invtweaks;

import io.github.marcuscastelo.invtweaks.client.behavior.IInvTweaksBehavior;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public enum InvTweaksOperationType {
    SORT, MOVE_ONE, MOVE_STACK, MOVE_ALL, MOVE_ALL_SAME_TYPE, DROP_ONE, DROP_STACK, DROP_ALL, DROP_ALL_SAME_TYPE;

    public OperationExecutor asOperationExecutor(IInvTweaksBehavior behavior) {
        switch (this) {
            case SORT:
                return behavior::sort;
            case DROP_ALL:
                return behavior::dropAll;
            case DROP_ONE:
                return behavior::dropOne;
            case DROP_STACK:
                return behavior::dropStack;
            case MOVE_ALL:
                return behavior::moveAll;
            case MOVE_ONE:
                return behavior::moveOne;
            case MOVE_STACK:
                throw new NotImplementedException();
            case DROP_ALL_SAME_TYPE:
                return behavior::dropAllSameType;
            case MOVE_ALL_SAME_TYPE:
                return behavior::moveAllSameType;
            default:
                throw new IllegalStateException("Unknown InvTweaksOperationType WTF");
        }
    }
}
