package io.github.marcuscastelo.invtweaks;

import io.github.marcuscastelo.invtweaks.client.behavior.IInvTweaksBehavior;

import java.util.Optional;

public record InvTweaksOperationType(Nature nature, Modifier modifier) { ;

    public enum Nature {
        IGNORE,
        SORT,
        MOVE,
        DROP,
        CRAFT,
    }

    public enum Modifier {
        ONE,
        STACK,
        ALL,
        ALL_SAME_TYPE,
        NORMAL,
    }

    public boolean isMove() {
        return nature == Nature.MOVE;
    }

    public boolean isDrop() {
        return nature == Nature.DROP;
    }

    public boolean isSort() {
        return nature == Nature.SORT;
    }

    public boolean isAll() {
        return modifier == Modifier.ALL;
    }

    public boolean isAllSameType() {
        return modifier == Modifier.ALL_SAME_TYPE;
    }

    public boolean isIgnore() { return nature == Nature.IGNORE; }

    public Optional<OperationExecutor> asOperationExecutor(IInvTweaksBehavior behavior) {
        return switch (this.nature) {
            case IGNORE -> Optional.empty();
            case SORT -> switch (this.modifier) {
                case NORMAL -> Optional.of(behavior::sort); // TODO: sort
                case ONE -> Optional.of(behavior::sort); // TODO: sort one
                case STACK -> Optional.of(behavior::sort); // TODO: sort stack
                case ALL -> Optional.of(behavior::sort); // TODO: sort all
                case ALL_SAME_TYPE -> Optional.of(behavior::sort); // TODO: sort all same type
            };
            case DROP -> switch (this.modifier) {
                case NORMAL -> Optional.empty();
                case ALL -> Optional.of(behavior::dropAll);
                case ONE -> Optional.of(behavior::dropOne);
                case STACK -> Optional.of(behavior::dropStack);
                case ALL_SAME_TYPE -> Optional.of(behavior::dropAllSameType);
            };
            case MOVE -> switch (this.modifier) {
                case NORMAL -> Optional.empty();
                case ALL -> Optional.of(behavior::moveAll);
                case ONE -> Optional.of(behavior::moveOne);
                case STACK -> Optional.of(behavior::moveStack);
                case ALL_SAME_TYPE -> Optional.of(behavior::moveAllSameType);
            };
            case CRAFT -> switch (this.modifier) {
                case NORMAL -> Optional.empty();
                case ALL -> Optional.of(behavior::craftAll);
                case ONE -> Optional.of(behavior::craftOne);
                case STACK -> Optional.of(behavior::craftStack);
                case ALL_SAME_TYPE -> Optional.of(behavior::craftAllSameType);
            };
        };
    }
}
