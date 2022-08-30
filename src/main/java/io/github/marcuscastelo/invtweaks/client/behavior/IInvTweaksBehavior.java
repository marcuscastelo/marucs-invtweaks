package io.github.marcuscastelo.invtweaks.client.behavior;

import io.github.marcuscastelo.invtweaks.InvTweaksOperationInfo;
import io.github.marcuscastelo.invtweaks.OperationResult;

public interface IInvTweaksBehavior {
    OperationResult sort(InvTweaksOperationInfo operationInfo);
    OperationResult moveAll(InvTweaksOperationInfo operationInfo);
    OperationResult dropAll(InvTweaksOperationInfo operationInfo);
    OperationResult moveAllSameType(InvTweaksOperationInfo operationInfo);
    OperationResult dropAllSameType(InvTweaksOperationInfo operationInfo);
    OperationResult moveOne(InvTweaksOperationInfo operationInfo);
    OperationResult dropOne(InvTweaksOperationInfo operationInfo);
    OperationResult moveStack(InvTweaksOperationInfo operationInfo);
    OperationResult dropStack(InvTweaksOperationInfo operationInfo);
    OperationResult craftOne(InvTweaksOperationInfo operationInfo);
    OperationResult craftStack(InvTweaksOperationInfo operationInfo);
    OperationResult craftAll(InvTweaksOperationInfo operationInfo);
    OperationResult craftAllSameType(InvTweaksOperationInfo operationInfo);
}
