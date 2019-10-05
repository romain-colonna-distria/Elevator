package fr.univ_amu.control;

import fr.univ_amu.command.CommandEngine;
import fr.univ_amu.command.Direction;

import java.util.ArrayList;
import java.util.List;


public class ElevatorControl {
    private final short FLOOR_MAX = 5;
    private final short FLOOR_MIN = 0;
    private List<Integer> floorsWaitingLine = new ArrayList<>();
    private CommandEngine commandEngine;

    private volatile Object object = new Object();

    public ElevatorControl(CommandEngine commandEngine){
        this.commandEngine = commandEngine;
        Thread control = new Thread(new Control_Runnable());
        control.start();
    }

    public void goTo(int floorNumber){ //plutot appelé par le panneau interne
        int currentFloor = commandEngine.getCurrentFloor();
        if(currentFloor == FLOOR_MAX && floorNumber > currentFloor) return; //si requete pour monter plus haut que max on fais rien
        if(currentFloor == FLOOR_MIN && floorNumber < currentFloor) return; //si requete pour descendre plus bas que min on fais rien

        synchronized (object) {
            floorsWaitingLine.add(floorNumber);
            object.notify();
        }
    }

    public void call(int from, Direction to){ //plutot appelé par les panneaux externe
        if(from == FLOOR_MAX && to.equals(Direction.UP)) return; //si requete pour monter alors qu'on est au max
        if(from == FLOOR_MIN && to.equals(Direction.DOWN)) return; //si requete pour descendre alors qu'on est au min
        if(floorsWaitingLine.contains(from)) return; //si étage déja présent dans la file d'attente des étages on ne fais rien

        synchronized (object) {
            floorsWaitingLine.add(from);
            object.notify();
        }
    }


    public void notifyFloorChange(int targetFloor){
        int currentFloor = commandEngine.getCurrentFloor();
        if(targetFloor > currentFloor){
            for(int i = currentFloor; i < targetFloor; ++i)
                commandEngine.goUp();

        } else if(targetFloor < currentFloor){
            for(int i = targetFloor; i < currentFloor; ++i)
                commandEngine.goDown();
        }
    }

    private class Control_Runnable implements Runnable {

        @Override
        public void run() {
            synchronized (object) {
                while (true) {
                    if (floorsWaitingLine.isEmpty()) {
                        try {
                            object.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    notifyFloorChange(floorsWaitingLine.get(0));
                    floorsWaitingLine.remove(0);
                }
            }
        }
    }
}
