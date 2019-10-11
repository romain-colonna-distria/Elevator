package fr.univ_amu.observer;

import fr.univ_amu.utils.Direction;

public interface PanelObserver {

    void updateRequest(short floor, Direction direction);
    void notifyEmergencyStop();

}
