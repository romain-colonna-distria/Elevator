package fr.univ_amu.control;

import fr.univ_amu.MinimumStrategy;
import fr.univ_amu.SatisfactionStrategy;
import fr.univ_amu.observer.PanelObserver;
import fr.univ_amu.observer.FloorObserver;
import fr.univ_amu.observer.WaitingLineObserver;
import fr.univ_amu.engine.CommandEngine;
import fr.univ_amu.utils.Direction;
import fr.univ_amu.ihm.InternalControlPanel;
import javafx.application.Platform;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static fr.univ_amu.utils.Constant.*;


public class ElevatorControl implements FloorObserver, PanelObserver {
    private CommandEngine commandEngine;
    private List<WaitingLineObserver> waitingLineObservers;
    //private SatisfactionStrategy strategy;

    private InternalControlPanel internalControlPanel;

    private AtomicBoolean isFloorChange = new AtomicBoolean(false);
    private AtomicBoolean isCancelEmergency = new AtomicBoolean(false);
    private AtomicInteger currentFloor = new AtomicInteger(0);

    private List<Short> upWaitingLine;
    private List<Short> downWaitingLine;

    private List<Short> upLateWaitingLine;
    private List<Short> downLateWaitingLine;

    private static volatile Object lock = new Object();



    public ElevatorControl(CommandEngine commandEngine){
        this.commandEngine = commandEngine;
        this.waitingLineObservers = new ArrayList<>();

        upWaitingLine = new LinkedList<>();
        downWaitingLine = new LinkedList<>();

        upLateWaitingLine = new LinkedList<>();
        downLateWaitingLine = new LinkedList<>();

        Thread control = new Thread(new Control_Runnable());
        control.start();
    }



    private void request(short targetFloor, Direction directionAfterReachingTargetFloor) {
        if(isCancelEmergency.get()) return;
        //if(targetFloor == currentFloor.get()) return;

        if(directionAfterReachingTargetFloor.equals(Direction.UP) && upWaitingLine.contains(targetFloor)) return;
        if(directionAfterReachingTargetFloor.equals(Direction.DOWN) && downWaitingLine.contains(targetFloor)) return;

        /* vérif quand on est dans la cabine */
        if(currentFloor.get() == FLOOR_MAX && targetFloor > currentFloor.get()) return; //si requete pour monter plus haut que max on fais rien
        if(currentFloor.get() == FLOOR_MIN && targetFloor < currentFloor.get()) return; //si requete pour descendre plus bas que min on fais rien

        /* vérif quand on est pas dans l'ascenseur */
        if(targetFloor == FLOOR_MAX && directionAfterReachingTargetFloor.equals(Direction.UP)) return;
        if(targetFloor == FLOOR_MIN && directionAfterReachingTargetFloor.equals(Direction.DOWN)) return;

        orderRequest(targetFloor, directionAfterReachingTargetFloor);
    }

    private void orderRequest(short targetFloor, Direction directionAfterReachingTargetFloor){
        if(directionAfterReachingTargetFloor.equals(Direction.UP)){
            if(commandEngine.getDirection().equals(Direction.STAY) && getActualWaitingLine().isEmpty()) {
                System.out.println("1");
                addToUpWaitingList(upWaitingLine, targetFloor);
            } else if (currentFloor.get() < targetFloor) {
                System.out.println("2");
                addToUpWaitingList(upWaitingLine, targetFloor);
            } else {
                System.out.println("3");
                addToUpWaitingList(upLateWaitingLine, targetFloor);
            }
        } else if(directionAfterReachingTargetFloor.equals(Direction.DOWN)){
            if(commandEngine.getDirection().equals(Direction.STAY) && getActualWaitingLine().isEmpty()) {
                System.out.println("a");
                addToDownWaitingList(downWaitingLine, targetFloor);
            } else if (currentFloor.get() > targetFloor) {
                System.out.println("b");
                addToDownWaitingList(downWaitingLine, targetFloor);
            } else {
                System.out.println("c");
                addToDownWaitingList(downLateWaitingLine, targetFloor);
            }
        } else {
            if (currentFloor.get() < targetFloor) {
                addToUpWaitingList(upWaitingLine, targetFloor);
            } else if (currentFloor.get() > targetFloor){
                addToDownWaitingList(downWaitingLine, targetFloor);
            } else {
                System.out.println();
                addToActualWaitingList(targetFloor);
            }
        }

        Platform.runLater(this::notifyWaitingLineChange);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    private void addToActualWaitingList(short targetFloor){
        if(getActualWaitingLine().equals(upWaitingLine))
            addToDownWaitingList(downWaitingLine, targetFloor);
        else if(getActualWaitingLine().equals(downWaitingLine))
            addToUpWaitingList(upWaitingLine, targetFloor);
        else
            System.out.println("?????");
    }

    private void addToUpWaitingList(List<Short> upWaitingLine, short targetFloor){
        synchronized (lock) {
            if(upWaitingLine.contains(targetFloor)) return;
            boolean isAdded = false;
            for (int i = 0; i < upWaitingLine.size(); ++i) {
                if (targetFloor < upWaitingLine.get(i)) {
                    upWaitingLine.add(i, targetFloor);
                    isAdded = true;
                    break;
                }
            }
            if (!isAdded) upWaitingLine.add(targetFloor);
        }
    }

    private void addToDownWaitingList(List<Short> downWaitingLine, short targetFloor){
        synchronized (lock) {
            if(downWaitingLine.contains(targetFloor)) return;
            boolean isAdded = false;
            for (int i = 0; i < downWaitingLine.size(); ++i) {
                if (targetFloor > downWaitingLine.get(i)) {
                    downWaitingLine.add(i, targetFloor);
                    isAdded = true;
                    break;
                }
            }
            if (!isAdded) downWaitingLine.add(targetFloor);
        }
    }

    //private void

    private void emergencyStop(){
        if(commandEngine.getCanMove().get()) {
            commandEngine.emergencyStop();
            isCancelEmergency.set(false);

            upWaitingLine.clear();
            downWaitingLine.clear();

            Platform.runLater(this::notifyWaitingLineChange);
        }
        else {
            commandEngine.cancelEmergencyStop();
            isCancelEmergency.set(true);
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

    private List<Short> getCompleteWaitingLine(){
        List<Short> floorsWaitingLine = new ArrayList<>(upWaitingLine);
        floorsWaitingLine.addAll(downWaitingLine);
        return floorsWaitingLine;
    }


    private List<Short> getActualWaitingLine(){
        synchronized (lock) {
            if (upWaitingLine.isEmpty() && downWaitingLine.isEmpty())
                return new LinkedList<>();
            else {
                if (!upWaitingLine.isEmpty() && !downWaitingLine.isEmpty()) {
                    return commandEngine.getDirection().equals(Direction.UP) ? upWaitingLine : downWaitingLine;
                } else if (upWaitingLine.isEmpty()) return downWaitingLine;
                else return upWaitingLine;
            }
        }
    }

    private void notifyWaitingLineChange(){
        for(WaitingLineObserver observer : waitingLineObservers)
            observer.updateWaitingLine(getCompleteWaitingLine());
    }

    public void addWaitingLineObserver(WaitingLineObserver observer){
        this.waitingLineObservers.add(observer);
    }

    public void setInternalControlPanel(InternalControlPanel internalControlPanel) {
        this.internalControlPanel = internalControlPanel;
    }

    @Override
    public void updateFloor() {
        Direction direction = commandEngine.getDirection();
        isFloorChange.set(true);

        if(direction.equals(Direction.UP)) currentFloor.incrementAndGet();
        else if(direction.equals(Direction.DOWN)) currentFloor.decrementAndGet();
        else return;

        internalControlPanel.setCurrentFloorLabelText(String.valueOf(currentFloor.get()));

        synchronized (lock){
            lock.notifyAll();
        }
    }

    @Override
    public void updateRequest(short floor, Direction direction) {
        this.request(floor, direction);
    }

    @Override
    public void notifyEmergencyStop() {
        this.emergencyStop();
    }

    /*
    public void setStrategy(SatisfactionStrategy strategy) {
        this.strategy = strategy;
    }
    */

    private class Control_Runnable implements Runnable {
        @Override
        public void run() {
            synchronized (lock) {
                while (true) {
                    try {
                        while (getCompleteWaitingLine().isEmpty()) {
                            lock.wait();
                        }

                        List<Short> actualWaitingLine = getActualWaitingLine();

                        while(!actualWaitingLine.isEmpty()) {
                            if (isFloorChange.get()) {
                                System.out.println("floor change");
                                isFloorChange.set(false);

                                if (actualWaitingLine.get(0) != null && currentFloor.get() == actualWaitingLine.get(0)) {
                                    actualWaitingLine.remove(0);
                                    commandEngine.stop();
                                    Platform.runLater(ElevatorControl.this::notifyWaitingLineChange);
                                    Thread.sleep(1000);
                                    continue;
                                }
                            }

                            short targetFloor = actualWaitingLine.get(0);
                            System.out.println("prochain: " + targetFloor);
                            try {
                                if (targetFloor > currentFloor.get()) {
                                    commandEngine.goUp();
                                    while (!isFloorChange.get()) {
                                        lock.wait();
                                        if(isCancelEmergency.get()) break;
                                    }
                                } else if (targetFloor < currentFloor.get()) {
                                    commandEngine.goDown();
                                    while (!isFloorChange.get()) {
                                        lock.wait();
                                        if(isCancelEmergency.get()) break;
                                    }
                                } else {
                                    actualWaitingLine.remove(0);
                                    commandEngine.stop();
                                    Platform.runLater(ElevatorControl.this::notifyWaitingLineChange);
                                    continue;
                                }
                                if(isCancelEmergency.get()) isCancelEmergency.set(false);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        if(actualWaitingLine.equals(upWaitingLine)) {
                            upWaitingLine.addAll(upLateWaitingLine);
                            upLateWaitingLine = new LinkedList<>();

                            for(int i = 0; i < downLateWaitingLine.size(); ++i)
                                addToDownWaitingList(downWaitingLine, downLateWaitingLine.get(i));

                        }
                        else if(actualWaitingLine.equals(downWaitingLine)) {
                            downWaitingLine.addAll(downLateWaitingLine);
                            downLateWaitingLine = new LinkedList<>();

                            for(int i = 0; i < upLateWaitingLine.size(); ++i)
                                addToUpWaitingList(upWaitingLine, upLateWaitingLine.get(i));
                        }

                        Platform.runLater(ElevatorControl.this::notifyWaitingLineChange);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}



/*



        if(directionAfterReachingTargetFloor.equals(Direction.UP)){
            if(commandEngine.getDirection().equals(Direction.STAY)) {
                if(directionAfterReachingTargetFloor.equals(Direction.UP)){

                } else if(directionAfterReachingTargetFloor.equals(Direction.DOWN)){

                } else {

                }
            } else if (currentFloor.get() < targetFloor) {
                if(directionAfterReachingTargetFloor.equals(Direction.UP)){

                } else if(directionAfterReachingTargetFloor.equals(Direction.DOWN)){

                } else {

                }
            } else {
                if(directionAfterReachingTargetFloor.equals(Direction.UP)){

                } else if(directionAfterReachingTargetFloor.equals(Direction.DOWN)){

                } else {

                }
            }
        } else if(directionAfterReachingTargetFloor.equals(Direction.DOWN)){
            if(commandEngine.getDirection().equals(Direction.STAY)) {
                if(directionAfterReachingTargetFloor.equals(Direction.UP)){

                } else if(directionAfterReachingTargetFloor.equals(Direction.DOWN)){

                } else {

                }
            } else if (currentFloor.get() < targetFloor) {
                 if(directionAfterReachingTargetFloor.equals(Direction.UP)){

                } else if(directionAfterReachingTargetFloor.equals(Direction.DOWN)){

                } else {

                }
            } else {
                addToDownWaitingList(downWaitingLine, targetFloor);
            }
        } else {
            if (currentFloor.get() < targetFloor) {
                addToUpWaitingList(upWaitingLine, targetFloor);
            } else {
                addToDownWaitingList(downWaitingLine, targetFloor);
            }
        }




 */