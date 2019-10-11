package fr.univ_amu.engine;

import fr.univ_amu.ihm.Elevator;
import fr.univ_amu.observer.FloorObserver;
import fr.univ_amu.utils.Direction;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static fr.univ_amu.utils.Constant.FLOOR_SIZE;


public class CommandEngine {
    private List<FloorObserver> floorObservers;
    private Elevator elevator;
    private AtomicBoolean canMove = new AtomicBoolean(true);
    private boolean stopNextFloor = false;
    private Direction direction = Direction.STAY;

    private Thread engine;
    private Engine_Runnable engine_runnable = new Engine_Runnable();

    private static volatile Object lock = new Object();

    public CommandEngine(Elevator elevator){
        this.floorObservers = new ArrayList<>();
        this.elevator = elevator;
        engine = new Thread(engine_runnable);
        engine.start();
    }

    public void goUp(){
        synchronized (lock){
            if(canMove.get()) {
                direction = Direction.UP;
                lock.notifyAll();
            }
        }
    }
    public void goDown(){
        synchronized (lock){
            if(canMove.get()) {
                direction = Direction.DOWN;
                lock.notifyAll();
            }
        }
    }

    public void stop(){
        direction = Direction.STAY;
    }

    public void emergencyStop(){
        canMove.set(false);
    }

    public void cancelEmergencyStop(){
        canMove.set(true);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    private void notifyPositionChange(){
        for (FloorObserver observer : floorObservers)
            observer.updateFloor();
    }

    public void addObserver(FloorObserver observer){
        this.floorObservers.add(observer);
    }

    public void stopNextFloor(){
        stopNextFloor = true;
    }

    public AtomicBoolean getCanMove() {
        return canMove;
    }
    public Direction getDirection() {
        return direction;
    }


    private class Engine_Runnable implements Runnable {
        private int i = 0;

        private void stopEngine() throws InterruptedException {
            synchronized (lock){
                while (!canMove.get()) {
                    System.out.println("Arrêt du moteur");
                    stop();
                    lock.wait();
                }
                startEngine();
            }
        }

        private void checkFloorChange(){
            if (i % (FLOOR_SIZE - 1) == 0) {
                i = 0;
                notifyPositionChange();
                if (stopNextFloor) {
                    direction = Direction.STAY;
                    stopNextFloor = false;
                }
            }
        }

        private void startEngine() throws InterruptedException {
            System.out.println("Démarrage du moteur");
            while (true) {
                synchronized (lock){
                    while(direction.equals(Direction.STAY))
                        lock.wait();
                }
                short j = (short) (direction.equals(Direction.UP) ? 1 : -1);

                while (!canMove.get()) {
                    System.out.println("can't move");
                    stop();
                    synchronized (lock) {
                        lock.wait();
                    }
                }

                Platform.runLater(() -> elevator.setLayoutY(elevator.getLayoutY() - j));
                ++i;
                checkFloorChange();

                Thread.sleep(10);
            }
        }

        @Override
        public void run() {
            try {
                startEngine();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
