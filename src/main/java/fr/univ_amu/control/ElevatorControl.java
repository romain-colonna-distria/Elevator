package fr.univ_amu.control;

import fr.univ_amu.engine.CommandEngine;
import fr.univ_amu.utils.Direction;
import fr.univ_amu.ihm.ExternalControlPanel;
import fr.univ_amu.ihm.InternalControlPanel;
import javafx.application.Platform;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static fr.univ_amu.utils.Constant.*;


public class ElevatorControl {
    private CommandEngine commandEngineRoot;
    private List<ExternalControlPanel> externalControlPanels = new LinkedList<>();
    private InternalControlPanel internalControlPanel;
    private AtomicBoolean isFloorChange = new AtomicBoolean(false);

    private Queue<Short> upWaitingLine;
    private Queue<Short> downWaitingLine;
    private volatile Object lock = new Object();


    public ElevatorControl(CommandEngine commandEngine){
        this.commandEngineRoot = commandEngine;

        Comparator<Short> upComparator = (o1, o2) -> o1 - o2;
        upWaitingLine = new PriorityQueue<>(upComparator);
        Comparator<Short> downComparator = (o1, o2) -> o2 - o1;
        downWaitingLine = new PriorityQueue<>(downComparator);

        Thread control = new Thread(new Control_Runnable());
        control.start();
    }


    public void request(short targetFloor, Direction directionAfterReachingTargetFloor) {
        short currentFloor = (short)commandEngineRoot.getCurrentFloor();

        if(targetFloor == currentFloor) return;
        if(upWaitingLine.contains(targetFloor)) return; //si étage déja présent dans la file d'attente des étages on ne fais rien
        if(downWaitingLine.contains(targetFloor)) return;

        /* vérif quand on est dans la cabine */
        if(currentFloor == FLOOR_MAX && targetFloor > currentFloor) return; //si requete pour monter plus haut que max on fais rien
        if(currentFloor == FLOOR_MIN && targetFloor < currentFloor) return; //si requete pour descendre plus bas que min on fais rien

        /* vérif quand on est pas dans l'ascenseur */
        if(targetFloor == FLOOR_MAX && directionAfterReachingTargetFloor.equals(Direction.UP)) return; //si requete pour monter alors qu'on est au max
        if(targetFloor == FLOOR_MIN && directionAfterReachingTargetFloor.equals(Direction.DOWN)) return; //si requete pour descendre alors qu'on est au min

        orderRequest(targetFloor, directionAfterReachingTargetFloor);
    }


    @SuppressWarnings("Duplicates")    //TODO: mieux faire algo
    private void orderRequest(short targetFloor, Direction directionAfterReachingTargetFloor){
        System.out.print("order request: ");
        synchronized (lock) {
            if(commandEngineRoot.getCurrentFloor() < targetFloor){
                System.out.println("ajouté up: " + targetFloor);
                upWaitingLine.add(targetFloor);
                System.out.println("---up---" + upWaitingLine);
            } else if(commandEngineRoot.getCurrentFloor() > targetFloor){
                System.out.println("ajouté down: " + targetFloor);
                downWaitingLine.add(targetFloor);
                System.out.println("---do---" + downWaitingLine);
            } else {
                System.out.println("??????????????????????");
            }

            Platform.runLater(() -> commandEngineRoot.getElevatorShaft().getRoot().updateWaitingLine(getCompleteWaitingLine()));
            lock.notifyAll();
        }
    }

    public void emergencyStop(){
        if(commandEngineRoot.getCanMove().get()) {
            upWaitingLine.clear();
            downWaitingLine.clear();

            commandEngineRoot.emergencyStop();

            Platform.runLater(() -> commandEngineRoot.getElevatorShaft().getRoot().updateWaitingLine(getCompleteWaitingLine()));
        }
        else
            commandEngineRoot.cancelEmergencyStop();
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

    public List<Short> getCompleteWaitingLine(){
        List<Short> floorsWaitingLine = new ArrayList<>(upWaitingLine);
        floorsWaitingLine.addAll(downWaitingLine);
        return floorsWaitingLine;
    }

    public Queue<Short> getActualWaitingLine(){
        if(upWaitingLine.isEmpty() && downWaitingLine.isEmpty())
            return new PriorityQueue<>();
        else {
            if(upWaitingLine.isEmpty()) return downWaitingLine;
            else return upWaitingLine;
        }
    }


    public void notifyFloorChange(){
        isFloorChange.set(true);
        internalControlPanel.setCurrentFloorLabelText(String.valueOf(commandEngineRoot.getCurrentFloor()));
        synchronized (lock){
            lock.notifyAll();
        }
    }


    private class Control_Runnable implements Runnable {
        @Override
        public void run() {
            short currentFloor = 0;
            synchronized (lock) {
                while (true) {
                    try {
                        while (getCompleteWaitingLine().isEmpty()) {
                            System.out.println("dodo1");
                            lock.wait();
                            System.out.println("-dodo1");
                        }

                        if(isFloorChange.get()){
                            isFloorChange.set(false);
                            currentFloor = (short)commandEngineRoot.getCurrentFloor();

                            if(getActualWaitingLine().peek() != null && currentFloor == getActualWaitingLine().peek()){
                                getActualWaitingLine().poll();
                                commandEngineRoot.stop();
                                Platform.runLater(() -> commandEngineRoot.getElevatorShaft().getRoot().updateWaitingLine(getCompleteWaitingLine()));
                                Thread.sleep(1000);
                                continue;
                            }
                        }

                        short targetFloor = getActualWaitingLine().peek();
                        try {
                            if (targetFloor > currentFloor) {
                                commandEngineRoot.goUp();
                                while (!isFloorChange.get())
                                    lock.wait();
                            } else if (targetFloor < currentFloor) {
                                commandEngineRoot.goDown();
                                while (!isFloorChange.get())
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


/*

            if(commandEngineRoot.getDirection().equals(Direction.UP)){
                if(directionAfterReachingTargetFloor.equals(Direction.UP)){
                    if(commandEngineRoot.getCurrentFloor() > targetFloor){
                        downWaitingLine.add(targetFloor);
                    } else if(commandEngineRoot.getCurrentFloor() < targetFloor) {
                        upWaitingLine.add(targetFloor);
                    } else {
                        //je sais pas
                    }
                } else if(directionAfterReachingTargetFloor.equals(Direction.DOWN)) {
                    if(commandEngineRoot.getCurrentFloor() > targetFloor){
                        downWaitingLine.add(targetFloor);
                    } else if(commandEngineRoot.getCurrentFloor() < targetFloor) {
                        downWaitingLine.add(targetFloor);
                    } else {
                        downWaitingLine.add(targetFloor);
                    }
                } else {
                    if(commandEngineRoot.getCurrentFloor() > targetFloor){
                        downWaitingLine.add(targetFloor);
                    } else if(commandEngineRoot.getCurrentFloor() < targetFloor) {
                        upWaitingLine.add(targetFloor);
                    } else {
                        //étage raté car si égale déja passé
                        downWaitingLine.add(targetFloor);
                    }
                }
            } else if(commandEngineRoot.getDirection().equals(Direction.DOWN)) {
                if(directionAfterReachingTargetFloor.equals(Direction.UP)){
                    if(commandEngineRoot.getCurrentFloor() > targetFloor){
                        //je sais pas
                    } else if(commandEngineRoot.getCurrentFloor() < targetFloor) {
                        upWaitingLine.add(targetFloor);
                    } else {
                        upWaitingLine.add(targetFloor);
                    }
                } else if(directionAfterReachingTargetFloor.equals(Direction.DOWN)) {
                    if(commandEngineRoot.getCurrentFloor() > targetFloor){

                    } else if(commandEngineRoot.getCurrentFloor() < targetFloor) {
                        downWaitingLine.add(targetFloor);
                    } else {
                        upWaitingLine.add(targetFloor);
                    }
                } else {
                    if(commandEngineRoot.getCurrentFloor() > targetFloor){
                        downWaitingLine.add(targetFloor);
                    } else if(commandEngineRoot.getCurrentFloor() < targetFloor) {
                        upWaitingLine.add(targetFloor);
                    } else {
                        upWaitingLine.add(targetFloor);
                    }
                }
            } else {
                if(directionAfterReachingTargetFloor.equals(Direction.UP)){
                    if(commandEngineRoot.getCurrentFloor() > targetFloor){
                        downWaitingLine.add(targetFloor);
                    } else if(commandEngineRoot.getCurrentFloor() < targetFloor) {
                        upWaitingLine.add(targetFloor);
                    } else {
                        System.out.println("Déjà présent à l'étage");
                    }
                } else if(directionAfterReachingTargetFloor.equals(Direction.DOWN)) {
                    if(commandEngineRoot.getCurrentFloor() > targetFloor){
                        downWaitingLine.add(targetFloor);
                    } else if(commandEngineRoot.getCurrentFloor() < targetFloor) {
                        upWaitingLine.add(targetFloor);
                    } else {
                        System.out.println("Déjà présent à l'étage");
                    }
                } else {
                    if(commandEngineRoot.getCurrentFloor() > targetFloor){
                        downWaitingLine.add(targetFloor);
                    } else if(commandEngineRoot.getCurrentFloor() < targetFloor) {
                        upWaitingLine.add(targetFloor);
                    } else {
                        System.out.println("Déjà présent à l'étage");
                    }
                }
            }


 */

            /*
            if(floorsWaitingLine.isEmpty()) {
                floorsWaitingLine.add(targetFloor);
            } else {
                if(commandEngineRoot.getDirection().equals(Direction.UP)) {
                    if (targetFloor < floorsWaitingLine.get(0)) { //dois etre sup a l'etage actuel
                        floorsWaitingLine.add(0, targetFloor);
                    } else if (floorsWaitingLine.size() >= 2) {
                        boolean isAdd = false;
                        for (short i = 1; i < floorsWaitingLine.size(); ++i) {
                            if (targetFloor < floorsWaitingLine.get(i)) {
                                isAdd = true;
                                floorsWaitingLine.add(i, targetFloor);
                            }
                        }
                        if (!isAdd) floorsWaitingLine.add(targetFloor);
                    } else {
                        floorsWaitingLine.add(targetFloor);
                    }
                } else if(commandEngineRoot.getDirection().equals(Direction.DOWN)){
                    if (targetFloor > floorsWaitingLine.get(0)) {
                        floorsWaitingLine.add(0, targetFloor);
                    } else if (floorsWaitingLine.size() >= 2) {
                        boolean isAdd = false;
                        for (short i = 1; i < floorsWaitingLine.size(); ++i) {
                            if (targetFloor > floorsWaitingLine.get(i)) {
                                isAdd = true;
                                floorsWaitingLine.add(i, targetFloor);
                                break;
                            }
                        }
                        if (!isAdd) floorsWaitingLine.add(targetFloor);
                    } else {
                        floorsWaitingLine.add(targetFloor);
                    }
                } else {
                    System.out.println("pas tout compris");
                    floorsWaitingLine.add(targetFloor);
                }
            }
            //floorsWaitingLine.add(targetFloor);
            */