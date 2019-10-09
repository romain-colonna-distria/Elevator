package fr.univ_amu.engine;

import fr.univ_amu.ihm.ElevatorShaft;
import fr.univ_amu.utils.Direction;
import javafx.application.Platform;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static fr.univ_amu.utils.Constant.FLOOR_SIZE;

public class CommandEngine{
    private ElevatorShaft elevatorShaft;
    private AtomicInteger currentFloor = new AtomicInteger(0);
    private AtomicBoolean canMove = new AtomicBoolean(true);
    private boolean stopNextFloor = false;
    private Direction direction = Direction.STAY;

    private static volatile Object lock = new Object();

    public CommandEngine(ElevatorShaft elevatorShaft){
        this.elevatorShaft = elevatorShaft;
        new Thread(new Engine_Runnable()).start();
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

    public void stopNextFloor(){
        stopNextFloor = true;
    }


    public void updateCurrentFloor(short newCurrentFloor){
        currentFloor.set(newCurrentFloor);
        elevatorShaft.getElevatorControl().notifyFloorChange();
    }

    public int getCurrentFloor() {
        return currentFloor.get();
    }

    public Direction getDirection() {
        return direction;
    }

    public ElevatorShaft getElevatorShaft() {
        return elevatorShaft;
    }

    public AtomicBoolean getCanMove() {
        return canMove;
    }

    private class Engine_Runnable implements Runnable {
        private void move() throws InterruptedException {
            int i = 0;
            while (true) {
                synchronized (lock){
                    while(direction.equals(Direction.STAY)){
                        System.out.println("--------------------------");
                        System.out.println("Ascenseur en pause");
                        lock.wait();
                        System.out.println("Fin pause");
                        System.out.println("--------------------------");
                    }
                }
                short j = (short) (direction.equals(Direction.UP) ? 1 : -1);

                while (!canMove.get()) {
                    synchronized (lock) {
                        //stopNextFloor = true;
                        direction = Direction.STAY;
                        System.out.println("wait2");
                        lock.wait();
                        System.out.println("fin wait2");
                    }
                }
                Platform.runLater(() -> elevatorShaft.getElevator().setLayoutY(elevatorShaft.getElevator().getLayoutY() - j));
                ++i;//i += j;
                Thread.sleep(10);

                if (i % (FLOOR_SIZE - 1) == 0) {
                    updateCurrentFloor((short) (currentFloor.get() + j));
                    i = currentFloor.get() * FLOOR_SIZE;
                    if (stopNextFloor) {
                        direction = Direction.STAY;
                        stopNextFloor = false;
                    }
                }
            }
        }

        @Override
        public void run() {
            try {
                move();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
