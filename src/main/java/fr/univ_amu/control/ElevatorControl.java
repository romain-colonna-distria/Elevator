package fr.univ_amu.control;

import fr.univ_amu.engine.CommandEngine;
import fr.univ_amu.utils.Direction;
import fr.univ_amu.ihm.ExternalControlPanel;
import fr.univ_amu.ihm.InternalControlPanel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static fr.univ_amu.utils.Constant.*;


public class ElevatorControl {
    private CommandEngine commandEngineRoot;
    private List<ExternalControlPanel> externalControlPanels = new LinkedList<>();
    private InternalControlPanel internalControlPanel;
    private AtomicBoolean isFloorChange = new AtomicBoolean(false);

    private List<Short> floorsWaitingLine = new ArrayList<>();
    private volatile Object lock = new Object();

    public ElevatorControl(CommandEngine commandEngine){
        this.commandEngineRoot = commandEngine;
        Thread control = new Thread(new Control_Runnable());
        control.start();
    }


    public void request(short targetFloor, Direction directionAfterReachingTargetFloor) {
        System.out.println("request: " + floorsWaitingLine);
        short currentFloor = (short)commandEngineRoot.getCurrentFloor();

        if(floorsWaitingLine.contains(targetFloor)) return; //si étage déja présent dans la file d'attente des étages on ne fais rien

        /* vérif quand on est dans la cabine */
        if(currentFloor == FLOOR_MAX && targetFloor > currentFloor) return; //si requete pour monter plus haut que max on fais rien
        if(currentFloor == FLOOR_MIN && targetFloor < currentFloor) return; //si requete pour descendre plus bas que min on fais rien

        /* vérif quand on est pas dans l'ascenseur */
        if(targetFloor == FLOOR_MAX && directionAfterReachingTargetFloor.equals(Direction.UP)) return; //si requete pour monter alors qu'on est au max
        if(targetFloor == FLOOR_MIN && directionAfterReachingTargetFloor.equals(Direction.DOWN)) return; //si requete pour descendre alors qu'on est au min

        orderRequest(targetFloor, directionAfterReachingTargetFloor);
    }


    private void orderRequest(short targetFloor, Direction directionAfterReachingTargetFloor){
        System.out.print("order request: ");
        synchronized (lock) {//TODO: mieux faire algo
            if(floorsWaitingLine.isEmpty()) {
                floorsWaitingLine.add(targetFloor);
            } else {
                if(commandEngineRoot.getDirection().equals(Direction.UP)) {
                    if (targetFloor < floorsWaitingLine.get(0)) {
                        floorsWaitingLine.add(0, targetFloor);
                    } else if (floorsWaitingLine.size() >= 2) {
                        for (short i = 1; i < floorsWaitingLine.size(); ++i) {
                            if (targetFloor < floorsWaitingLine.get(i)) {
                                floorsWaitingLine.add(i, targetFloor);
                            }
                        }
                    } else {
                        floorsWaitingLine.add(targetFloor);
                    }
                } else if(commandEngineRoot.getDirection().equals(Direction.DOWN)){
                    if (targetFloor > floorsWaitingLine.get(0)) {
                        floorsWaitingLine.add(0, targetFloor);
                    } else if (floorsWaitingLine.size() >= 2) {
                        for (short i = 1; i < floorsWaitingLine.size(); ++i) {
                            if (targetFloor > floorsWaitingLine.get(i)) {
                                floorsWaitingLine.add(i, targetFloor);
                            }
                        }
                    } else {
                        floorsWaitingLine.add(targetFloor);
                    }
                } else {
                    System.out.println("pas tout compris");
                }
            }

            System.out.println(floorsWaitingLine);
            lock.notifyAll();
        }
    }


    public void emergencyStop(){
        commandEngineRoot.emergencyStop();
    }

    public void notifyFloorChange(){
        isFloorChange.set(true);
        internalControlPanel.setCurrentFloorLabelText(String.valueOf(commandEngineRoot.getCurrentFloor()));
        synchronized (lock){
            lock.notifyAll();
        }
    }

    public CommandEngine getCommandEngineRoot() {
        return commandEngineRoot;
    }
    public void setExternalControlPanels(List<ExternalControlPanel> externalControlPanels) {
        this.externalControlPanels = externalControlPanels;
    }
    public void setInternalControlPanel(InternalControlPanel internalControlPanel) {
        this.internalControlPanel = internalControlPanel;
    }

    private class Control_Runnable implements Runnable {
        @Override
        public void run() {
            short currentFloor = 0;
            synchronized (lock) {
                while (true) {
                    try {
                        while (floorsWaitingLine.isEmpty()) {
                            lock.wait();
                        }

                        if(isFloorChange.get()){
                            isFloorChange.set(false);
                            currentFloor = (short)commandEngineRoot.getCurrentFloor();

                            if(currentFloor == floorsWaitingLine.get(0)){
                                floorsWaitingLine.remove(0);
                                commandEngineRoot.stop();
                                Thread.sleep(1000);
                                continue;
                            }
                        }

                        short targetFloor = floorsWaitingLine.get(0);
                        try {
                            if (targetFloor > currentFloor) {
                                commandEngineRoot.goUp();
                                lock.wait();
                            } else if (targetFloor < currentFloor) {
                                commandEngineRoot.goDown();
                                lock.wait();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
