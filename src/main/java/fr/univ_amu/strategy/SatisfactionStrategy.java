package fr.univ_amu.strategy;

import fr.univ_amu.utils.Direction;

import java.util.List;

public interface SatisfactionStrategy {

    void orderRequest(List<Short> waitingLine, short targetFloor, short actualFloor, Direction directionAfterReachingTargetFloor, Direction actualDirection);
}
