package fr.univ_amu.command;

import fr.univ_amu.object.Elevator;
import fr.univ_amu.object.ElevatorShaft;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static fr.univ_amu.Constant.FLOOR_SIZE;

public class CommandEngine{
    private ExecutorService enginePool = Executors.newSingleThreadExecutor();
    private ElevatorShaft elevatorShaft;
    private Elevator elevator;
    private AtomicInteger currentFloor = new AtomicInteger(0);
    private AtomicBoolean canKeepMoving = new AtomicBoolean(true);

    private static volatile Object object = new Object();

    public CommandEngine(Elevator elevator, ElevatorShaft elevatorShaft){
        this.elevator = elevator;
        this.elevatorShaft = elevatorShaft;
    }

    public void goUp(short nbFloor){
        Thread upThread = new Thread(new Engine_Runnable(Direction.UP, nbFloor));
        enginePool.execute(upThread);
        //Platform.runLater(new Engine_Runnable(Direction.UP, nbFloor));
    }

    public void goDown(short nbFloor){
        Thread downThread = new Thread(new Engine_Runnable(Direction.DOWN, nbFloor));
        enginePool.execute(downThread);
        //Platform.runLater(new Engine_Runnable(Direction.DOWN, nbFloor));
    }

    public void stopNextFloor(){

    }

    public void emergencyStop(){
        System.out.println();
        canKeepMoving.set(!canKeepMoving.get());
        synchronized (object) {
            if(canKeepMoving.get()) object.notifyAll();
        }
    }

    public void updateCurrentFloor(short newCurrentFloor){
        currentFloor.set(newCurrentFloor);
        elevatorShaft.getElevatorControl().notifyFloorChange();
    }

    public int getCurrentFloor() {
        return currentFloor.get();
    }


    private class Engine_Runnable implements Runnable {
        private short nbFloor;
        private Direction direction;

        public Engine_Runnable(Direction direction, short nbFloor){
            this.direction = direction;
            this.nbFloor = nbFloor;
        }

        private void move() throws InterruptedException {
            short j = (short) (direction.equals(Direction.UP) ? 1 : -1);
            boolean stopNextFloor = false;
            for (int i = 0; i < FLOOR_SIZE * nbFloor; ++i) {
                while (!canKeepMoving.get()) {
                    //nbFloor = 1;
                    synchronized (object) {
                        System.out.println("stop");
                        stopNextFloor = true;
                        object.wait();
                    }
                }
                elevator.setLayoutY(elevator.getLayoutY() - j);
                Thread.sleep(10);

                if (i != 0 && i % (FLOOR_SIZE - 1) == 0) {
                    if(stopNextFloor) break;
                    updateCurrentFloor((short) (currentFloor.get() + j));
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
