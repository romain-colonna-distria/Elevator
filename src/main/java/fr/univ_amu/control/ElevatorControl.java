package fr.univ_amu.control;

import fr.univ_amu.strategy.SatisfactionStrategy;
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
    private SatisfactionStrategy strategy;

    private InternalControlPanel internalControlPanel;

    private AtomicBoolean isFloorChange = new AtomicBoolean(false);
    private AtomicBoolean isCancelEmergency = new AtomicBoolean(false);
    private AtomicInteger currentFloor = new AtomicInteger(0);

    private List<Short> waitingLine = new LinkedList<>();

    private static volatile Object lock = new Object();



    public ElevatorControl(CommandEngine commandEngine){
        this.commandEngine = commandEngine;
        this.waitingLineObservers = new ArrayList<>();

        Thread control = new Thread(new Control_Runnable());
        control.start();
    }



    private void request(short targetFloor, Direction directionAfterReachingTargetFloor) {
        if(!commandEngine.getCanMove().get()) return;

        /* vérif quand on est dans la cabine */
        if(currentFloor.get() == FLOOR_MAX && targetFloor > currentFloor.get()) return; //si requete pour monter plus haut que max on fais rien
        if(currentFloor.get() == FLOOR_MIN && targetFloor < currentFloor.get()) return; //si requete pour descendre plus bas que min on fais rien

        /* vérif quand on est pas dans l'ascenseur */
        if(targetFloor == FLOOR_MAX && directionAfterReachingTargetFloor.equals(Direction.UP)) return;
        if(targetFloor == FLOOR_MIN && directionAfterReachingTargetFloor.equals(Direction.DOWN)) return;

        synchronized (lock) {
            strategy.orderRequest(waitingLine, targetFloor, (short) currentFloor.get(), directionAfterReachingTargetFloor, commandEngine.getDirection());
            Platform.runLater(this::notifyWaitingLineChange);
            lock.notifyAll();
        }
    }

    private void emergencyStop(){
        if(commandEngine.getCanMove().get()) {
            commandEngine.emergencyStop();
            isCancelEmergency.set(false);

            waitingLine.clear();

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


    private void notifyWaitingLineChange(){
        for(WaitingLineObserver observer : waitingLineObservers)
            observer.updateWaitingLine(waitingLine);
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


    public void setStrategy(SatisfactionStrategy strategy) {
        this.strategy = strategy;
    }


    private class Control_Runnable implements Runnable {
        @Override
        public void run() {
            synchronized (lock) {
                while (true) {
                    try {
                        while (waitingLine.isEmpty())
                            lock.wait();

                        while(!waitingLine.isEmpty()) {
                            if (isFloorChange.get()) {
                                System.out.println("floor change");
                                isFloorChange.set(false);

                                if (waitingLine.get(0) != null && currentFloor.get() == waitingLine.get(0)) {
                                    waitingLine.remove(0);
                                    commandEngine.stop();
                                    Platform.runLater(ElevatorControl.this::notifyWaitingLineChange);
                                    Thread.sleep(1000);
                                    continue;
                                }
                            }

                            short targetFloor = waitingLine.get(0);
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
                                    waitingLine.remove(0);
                                    Platform.runLater(ElevatorControl.this::notifyWaitingLineChange);
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