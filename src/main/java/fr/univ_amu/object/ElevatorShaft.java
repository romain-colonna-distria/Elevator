package fr.univ_amu.object;

import fr.univ_amu.command.CommandEngine;
import fr.univ_amu.control.ElevatorControl;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.List;

public class ElevatorShaft extends AnchorPane {
    private List<ExternalControlPanel> externalControlPanels = new ArrayList<>();
    private CommandEngine commandEngine;
    private ElevatorControl elevatorControl;
    private Elevator elevator;


    public ElevatorShaft(short nbFloor){
        initView();
        initElevator();
        initLabels(nbFloor);

        commandEngine = new CommandEngine(elevator);
        elevatorControl = new ElevatorControl(commandEngine);

        initExternalPanels(nbFloor);

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
    private void initExternalPanels(short nbFloor){
        int j = nbFloor - 1;
        for(short i = 0; i < nbFloor; ++i){
            ExternalControlPanel tmp = new ExternalControlPanel(i, elevatorControl);
            tmp.setLayoutX(213.0);
            tmp.setLayoutY(45 + (129 * j--));

            externalControlPanels.add(tmp);
        }
        this.getChildren().addAll(externalControlPanels);
    }

    public Elevator getElevator() {
        return elevator;
    }

    public ElevatorControl getElevatorControl() {
        return elevatorControl;
    }

}
