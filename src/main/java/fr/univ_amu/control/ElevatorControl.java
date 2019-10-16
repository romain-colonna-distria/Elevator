package fr.univ_amu.control;

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

    private InternalControlPanel internalControlPanel;

    private AtomicBoolean isFloorChange = new AtomicBoolean(false);
    private AtomicBoolean isCancelEmergency = new AtomicBoolean(false);
    private AtomicInteger currentFloor = new AtomicInteger(0);

    private Queue<Short> upWaitingLine;
    private Queue<Short> downWaitingLine;

    private static volatile Object lock = new Object();



    public ElevatorControl(CommandEngine commandEngine){
        this.commandEngine = commandEngine;
        this.waitingLineObservers = new ArrayList<>();

        Comparator<Short> upComparator = Comparator.comparingInt(o -> o);
        upWaitingLine = new PriorityQueue<>(upComparator);
        Comparator<Short> downComparator = (o1, o2) -> o2 - o1;
        downWaitingLine = new PriorityQueue<>(downComparator);

        Thread control = new Thread(new Control_Runnable());
        control.start();
    }



    private void request(short targetFloor, Direction directionAfterReachingTargetFloor) {
        if(isCancelEmergency.get()) return;
        if(targetFloor == currentFloor.get()) return;

        if(directionAfterReachingTargetFloor.equals(Direction.UP) && upWaitingLine.contains(targetFloor)) return;
        if(directionAfterReachingTargetFloor.equals(Direction.DOWN) && downWaitingLine.contains(targetFloor)) return;
        //if(upWaitingLine.contains(targetFloor)) return;
        //if(downWaitingLine.contains(targetFloor)) return;

        /* vérif quand on est dans la cabine */
        if(currentFloor.get() == FLOOR_MAX && targetFloor > currentFloor.get()) return; //si requete pour monter plus haut que max on fais rien
        if(currentFloor.get() == FLOOR_MIN && targetFloor < currentFloor.get()) return; //si requete pour descendre plus bas que min on fais rien

        /* vérif quand on est pas dans l'ascenseur */
        if(targetFloor == FLOOR_MAX && directionAfterReachingTargetFloor.equals(Direction.UP)) return;
        if(targetFloor == FLOOR_MIN && directionAfterReachingTargetFloor.equals(Direction.DOWN)) return;

        orderRequest(targetFloor, directionAfterReachingTargetFloor);
    }

    private void orderRequest(short targetFloor, Direction directionAfterReachingTargetFloor){
        synchronized (lock) {
            if(directionAfterReachingTargetFloor.equals(Direction.UP)){
                upWaitingLine.add(targetFloor);
            } else if(directionAfterReachingTargetFloor.equals(Direction.DOWN)){
                downWaitingLine.add(targetFloor);
            } else {
                if (currentFloor.get() < targetFloor) {
                    upWaitingLine.add(targetFloor);
                } else {
                    downWaitingLine.add(targetFloor);
                }
            }

            Platform.runLater(this::notifyWaitingLineChange);
            lock.notifyAll();
        }
    }

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


    private Queue<Short> getActualWaitingLine(){
        if(upWaitingLine.isEmpty() && downWaitingLine.isEmpty())
            return new PriorityQueue<>();
        else {
            if(!upWaitingLine.isEmpty() && !downWaitingLine.isEmpty()) {
                return commandEngine.getDirection().equals(Direction.UP) ? upWaitingLine : downWaitingLine;
            }
            else if(upWaitingLine.isEmpty()) return downWaitingLine;
            else return upWaitingLine;
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


    private class Control_Runnable implements Runnable {
        @Override
        public void run() {
            synchronized (lock) {
                while (true) {
                    try {
                        while (getCompleteWaitingLine().isEmpty()) {
                            lock.wait();
                        }

                        Queue<Short> actualWaitingLine = getActualWaitingLine();
                        while(!actualWaitingLine.isEmpty()) {
                            if (isFloorChange.get()) {
                                System.out.println("floor change");
                                isFloorChange.set(false);

                                if (actualWaitingLine.peek() != null && currentFloor.get() == actualWaitingLine.peek()) {
                                    actualWaitingLine.poll();
                                    commandEngine.stop();
                                    Platform.runLater(ElevatorControl.this::notifyWaitingLineChange);
                                    Thread.sleep(1000);
                                    continue;
                                }
                            }

                            short targetFloor = actualWaitingLine.peek();
                            System.out.println("prochain: " + targetFloor);
                            try {
                                if (targetFloor > currentFloor.get()) {
                                    commandEngine.goUp();
                                    while (!isFloorChange.get()) {
                                        System.out.println("11111");
                                        lock.wait();
                                        System.out.println("22222");
                                        if(isCancelEmergency.get()) break;
                                    }
                                } else if (targetFloor < currentFloor.get()) {
                                    commandEngine.goDown();
                                    while (!isFloorChange.get()) {
                                        System.out.println("33333");
                                        lock.wait();
                                        System.out.println("44444");
                                        if(isCancelEmergency.get()) break;
                                    }
                                }
                                if(isCancelEmergency.get()) isCancelEmergency.set(false);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}