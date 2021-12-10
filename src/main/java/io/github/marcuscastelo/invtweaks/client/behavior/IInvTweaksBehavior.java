package io.github.marcuscastelo.invtweaks.client.behavior;

import io.github.marcuscastelo.invtweaks.InvTweaksOperationInfo;

public interface IInvTweaksBehavior {
    void sort(InvTweaksOperationInfo operationInfo);
    void moveAll(InvTweaksOperationInfo operationInfo);
    void dropAll(InvTweaksOperationInfo operationInfo);
    void moveAllSameType(InvTweaksOperationInfo operationInfo);
    void dropAllSameType(InvTweaksOperationInfo operationInfo);
    void moveOne(InvTweaksOperationInfo operationInfo);
    void dropOne(InvTweaksOperationInfo operationInfo);
    void moveStack(InvTweaksOperationInfo operationInfo);
    void dropStack(InvTweaksOperationInfo operationInfo);
}
