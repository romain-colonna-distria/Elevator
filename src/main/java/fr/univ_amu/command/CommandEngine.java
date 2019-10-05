package fr.univ_amu.command;

import fr.univ_amu.object.Elevator;
import fr.univ_amu.control.ElevatorControl;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandEngine{
    private ExecutorService enginePool = Executors.newSingleThreadExecutor();
    private ElevatorControl elevatorController;
    private final int FLOOR_SIZE = 129;
    private Elevator elevator;
    private int currentFloor = 0;

    public CommandEngine(Elevator elevator){
        this.elevator = elevator;
        this.elevatorController = new ElevatorControl(this);
    }

    public synchronized void goUp(){
        Thread upThread = new Thread(new Engine_Runnable(Direction.UP));
        enginePool.execute(upThread);
    }

    public synchronized void goDown(){
        Thread downThread = new Thread(new Engine_Runnable(Direction.DOWN));
        enginePool.execute(downThread);
    }

    public void stopNextFloor(){}

    public void emergencyStop(){}

    public void cancelEmergencyStop(){}

    public int getCurrentFloor() {
        return currentFloor;
    }

    private class Engine_Runnable implements Runnable {
        private Direction direction;

        public Engine_Runnable(Direction direction){
            this.direction = direction;
        }

        public void up(){
            for(int i = 0; i < FLOOR_SIZE; ++i){
                elevator.setLayoutY(elevator.getLayoutY() - 1);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            ++currentFloor;
        }

        public void down(){
            for(int i = 0; i < FLOOR_SIZE; ++i){
                elevator.setLayoutY(elevator.getLayoutY() + 1);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            --currentFloor;
        }

        @Override
        public void run() {
            if(direction.equals(Direction.UP)) {
                up();
            }
            else {
                down();
            }


        }
    }
}
