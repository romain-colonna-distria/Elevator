package fr.univ_amu.control;

import fr.univ_amu.command.CommandEngine;
import fr.univ_amu.command.Direction;
import fr.univ_amu.object.ExternalControlPanel;
import fr.univ_amu.object.InternalControlPanel;

import java.util.ArrayList;
import java.util.List;

import static fr.univ_amu.Constant.*;


public class ElevatorControl {
    private List<Integer> floorsWaitingLine = new ArrayList<>();
    private CommandEngine commandEngineRoot;

    private List<ExternalControlPanel> externalControlPanels = new ArrayList<>();
    private InternalControlPanel internalControlPanel;

    private volatile Object object = new Object();

    public ElevatorControl(CommandEngine commandEngine){
        this.commandEngineRoot = commandEngine;
        Thread control = new Thread(new Control_Runnable());
        control.start();
    }


    public void goTo(int targetFloor){ //plutot appelé par le panneau interne
        short currentFloor = (short)commandEngineRoot.getCurrentFloor();
        if(currentFloor == FLOOR_MAX && targetFloor > currentFloor) return; //si requete pour monter plus haut que max on fais rien
        if(currentFloor == FLOOR_MIN && targetFloor < currentFloor) return; //si requete pour descendre plus bas que min on fais rien

        synchronized (object) {
            floorsWaitingLine.add(targetFloor);
            object.notifyAll();
        }
    }
    public void call(int from, Direction to){ //plutot appelé par les panneaux externe
        if(from == FLOOR_MAX && to.equals(Direction.UP)) return; //si requete pour monter alors qu'on est au max
        if(from == FLOOR_MIN && to.equals(Direction.DOWN)) return; //si requete pour descendre alors qu'on est au min
        if(floorsWaitingLine.contains(from)) return; //si étage déja présent dans la file d'attente des étages on ne fais rien

        synchronized (object) {
            floorsWaitingLine.add(from);
            object.notifyAll();
        }
    }
    public void emergencyStop(){
        commandEngineRoot.emergencyStop();
    }

    public void notifyFloorChange(){
        internalControlPanel.setCurrentFloorLabelText(String.valueOf(commandEngineRoot.getCurrentFloor()));
    }

    public CommandEngine getCommandEngineRoot() {
        return commandEngineRoot;
    }
    public void setExternalControlPanels(List<ExternalControlPanel> externalControlPanels) {
        this.externalControlPanels = externalControlPanels;
    }
    public void setInternalControlPanel(InternalControlPanel internalControlPanel) {
        System.out.println("");
        this.internalControlPanel = internalControlPanel;
    }

    private class Control_Runnable implements Runnable {

        @Override
        public void run() {
            synchronized (object) {
                while (true) {
                    if (floorsWaitingLine.isEmpty()) {
                        try {
                            object.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    short currentFloor = (short)commandEngineRoot.getCurrentFloor();
                    short targetFloor = floorsWaitingLine.get(0).shortValue();
                    if(targetFloor > currentFloor){
                        commandEngineRoot.goUp((short)(Math.abs(targetFloor - currentFloor)));
                    } else if(targetFloor < currentFloor){
                        commandEngineRoot.goDown((short)(Math.abs(currentFloor - targetFloor)));
                    }
                    floorsWaitingLine.remove(0);
                }
            }
        }
    }
}
