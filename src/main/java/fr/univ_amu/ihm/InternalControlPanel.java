package fr.univ_amu.ihm;

import fr.univ_amu.control.ElevatorControl;

import fr.univ_amu.utils.Direction;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InternalControlPanel extends AnchorPane {
    //private ElevatorControl elevatorControl;
    private List<PanelObserver> observers = new ArrayList<>();

    private short floor;
    private Direction direction;

    @FXML
    private Label currentFloorLabel;
    private List<Button> buttonList = new ArrayList<>();
    private EventHandler<ActionEvent> goToEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            //System.out.println("panneau interne: " + elevatorControl.getCommandEngineRoot().getCurrentFloor() + " --> " + ((Button) event.getSource()).getText());
            //elevatorControl.request((short) Integer.parseInt(((Button) event.getSource()).getText()), Direction.STAY);
            floor = (short) Integer.parseInt(((Button) event.getSource()).getText());
            direction = Direction.STAY;
            notifyObservers();
        }
    };
    private EventHandler<ActionEvent> emergencyEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            //elevatorControl.emergencyStop();
            notifyEmergencyStop();
        }
    };


    public InternalControlPanel(short nbFloor, ElevatorControl elevatorControl){
        //this.elevatorControl = elevatorControl;
        addObserver(elevatorControl);
        loadFXML();
        initButtons(nbFloor);
    }




    private void loadFXML(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("view/internal_control_panel_view.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void initButtons(short nbFloor){
        short j = (short)(nbFloor - 1); //sert a inverser la place des boutons sur le panneau (au lieux de 0 à 4 --> 4 à 0)
        for(short i = 0; i < nbFloor; ++i){
            Button floor = new Button(String.valueOf(i));
            floor.setPrefHeight(40);
            floor.setPrefWidth(70);
            floor.setLayoutX(80);
            floor.setLayoutY(120 + (70 * j--));
            floor.setOnAction(goToEvent);
            this.getChildren().add(floor);
            buttonList.add(floor);
        }

        Button emergency = new Button("Emergency emergencyStop");
        emergency.setLayoutX(32.0);
        emergency.setLayoutY(465.0);
        emergency.setPrefWidth(165.0);
        emergency.setPrefHeight(40.0);
        emergency.setOnAction(emergencyEvent);
        this.getChildren().add(emergency);

    }

    public void setCurrentFloorLabelText(String currentFloor) {
        Platform.runLater(() -> currentFloorLabel.setText(currentFloor));
    }

    public void addObserver(PanelObserver observer) {
        this.observers.add(observer);
    }

    public void removeObserver(PanelObserver observer) {
        this.observers.remove(observer);
    }

    public void notifyObservers() {
        for (PanelObserver observer : this.observers) {
            observer.updateInternalControlPanel(this);
        }
    }

    private void notifyEmergencyStop() {
        for (PanelObserver observer : this.observers) {
            observer.notifyEmergencyStop();
        }
    }

    public short getFloor() {
        return floor;
    }

    public Direction getDirection() {
        return direction;
    }

}
