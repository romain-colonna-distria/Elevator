package fr.univ_amu.observer;

import java.util.List;

public interface WaitingLineObserver {
    void updateWaitingLine(List<Short> waitingLineList);
}
