package fr.univ_amu.strategy;

import fr.univ_amu.utils.Direction;

import java.util.List;

/**
 * Implémente la stratégie de desserte donnant la priorité au premier (premier arrivé,
 * premier servi).
 */
public class FirstStrategy implements SatisfactionStrategy {
    @Override
    public void orderRequest(List<Short> waitingLine, short targetFloor, short actualFloor, Direction directionAfterReachingTargetFloor, Direction actualDirection) {
        waitingLine.add(targetFloor);
    }
}
