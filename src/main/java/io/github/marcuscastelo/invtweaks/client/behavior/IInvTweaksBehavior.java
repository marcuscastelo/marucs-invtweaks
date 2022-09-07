package io.github.marcuscastelo.invtweaks.client.behavior;

import io.github.marcuscastelo.invtweaks.operation.OperationInfo;
import io.github.marcuscastelo.invtweaks.operation.OperationResult;

public interface IInvTweaksBehavior {
    OperationResult sort(OperationInfo operationInfo);
    OperationResult moveAll(OperationInfo operationInfo);
    OperationResult dropAll(OperationInfo operationInfo);
    OperationResult moveAllSameType(OperationInfo operationInfo);
    OperationResult dropAllSameType(OperationInfo operationInfo);
    OperationResult moveOne(OperationInfo operationInfo);
    OperationResult dropOne(OperationInfo operationInfo);
    OperationResult moveStack(OperationInfo operationInfo);
    OperationResult dropStack(OperationInfo operationInfo);
    OperationResult craftOne(OperationInfo operationInfo);
    OperationResult craftStack(OperationInfo operationInfo);
    OperationResult craftAll(OperationInfo operationInfo);
    OperationResult craftAllSameType(OperationInfo operationInfo);
}
