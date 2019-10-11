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
    private Elevator elevator;

    private List<FloorObserver> floorObservers;

    private AtomicBoolean canMove = new AtomicBoolean(true);
    private AtomicBoolean stopNextFloor = new AtomicBoolean(false);
    private Direction direction = Direction.STAY;

    private static volatile Object lock = new Object();


    public CommandEngine(Elevator elevator) {
        this.floorObservers = new ArrayList<>();
        this.elevator = elevator;
        Thread engine = new Thread(new Engine_Runnable());
        engine.start();
    }


    public void goUp() {
        synchronized (lock) {
            if (canMove.get()) {
                direction = Direction.UP;
                lock.notifyAll();
            }
        }
    }

    public void goDown() {
        synchronized (lock) {
            if (canMove.get()) {
                direction = Direction.DOWN;
                lock.notifyAll();
            }
        }
    }

    public void stop() {
        direction = Direction.STAY;
    }

    public void emergencyStop() {
        canMove.set(false);
    }

    public void cancelEmergencyStop() {
        canMove.set(true);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    private void notifyPositionChange() {
        for (FloorObserver observer : floorObservers)
            observer.updateFloor();
    }

    public void addFloorObserver(FloorObserver observer) {
        this.floorObservers.add(observer);
    }

    public void stopNextFloor() {
        stopNextFloor.set(true);
    }

    public AtomicBoolean getCanMove() {
        return canMove;
    }

    public Direction getDirection() {
        return direction;
    }


    private class Engine_Runnable implements Runnable {
        private int heigth = 0;

        private void checkFloorChange() {
            if (heigth % (FLOOR_SIZE - 1) == 0) {
                heigth = 0;
                notifyPositionChange();
                if (stopNextFloor.get()) {
                    direction = Direction.STAY;
                    stopNextFloor.set(false);
                }
            }
        }

        private void startEngine() throws InterruptedException {
            System.out.println("Démarrage du moteur");
            while (true) {
                synchronized (lock) {
                    while (direction.equals(Direction.STAY))
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
                ++heigth;
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
