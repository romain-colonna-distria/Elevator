package fr.univ_amu.ihm;

import fr.univ_amu.engine.CommandEngine;
import fr.univ_amu.control.ElevatorControl;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.util.List;

public class ElevatorShaft extends AnchorPane {
    private ElevatorControl elevatorControl;
    private Elevator elevator;


    public ElevatorShaft(short nbFloor){
        initView();
        initElevator();
        initLabels(nbFloor);

        CommandEngine commandEngine = new CommandEngine(this);
        elevatorControl = new ElevatorControl(commandEngine);

        setWidth(400.0);
        setHeight(650.0);
        setStyle("-fx-border-color: #000000; -fx-background-color: #ffffff;");
    }

    private void initView(){
        ImageView view = new ImageView(new Image("image/elevatorShaft.png"));
        view.setX(100.0);
        view.setY(10.0);
        view.setFitWidth(200.0);
        view.setFitHeight(640.0);
        this.getChildren().add(view);
    }
    private void initElevator(){
        elevator = new Elevator();
        this.getChildren().add(elevator);
    }
    private void initLabels(short nbFloor){
        int j = nbFloor - 1;
        for(short i = 0; i < nbFloor; ++i){
            Label tmp = new Label(String.valueOf(i));
            tmp.setLayoutX(30);
            tmp.setLayoutY(70 + (125 * j--));
            this.getChildren().add(tmp);
        }
    }

    public Elevator getElevator() {
        return elevator;
    }

    public ElevatorControl getElevatorControl() {
        return elevatorControl;
    }

    public void addExternalsPanelsToChildren(List<ExternalControlPanel> externalControlPanels){
        this.getChildren().addAll(externalControlPanels);
    }
}
