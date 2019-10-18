package fr.univ_amu.strategy;

import fr.univ_amu.utils.Direction;

import java.util.List;

public class FirstStrategy implements SatisfactionStrategy {
    @Override
    public void orderRequest(List<Short> waitingLine, short targetFloor, short actualFloor, Direction directionAfterReachingTargetFloor, Direction actualDirection) {
        waitingLine.add(targetFloor);
    }
}
