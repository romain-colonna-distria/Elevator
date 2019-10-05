package fr.univ_amu.object;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;


public class Elevator extends AnchorPane {
    private InternalControlPanel internalControlPanel;

    public Elevator(){
        double x = 108.0;
        double y = 529.0;
        double width = 90.0;
        double heigth = 110.0;
        ImageView elevatorFont = new ImageView(new Image("image/peopleInElevator.png"));
        setLayoutX(x);
        setLayoutY(y);
        setPrefWidth(width);
        setPrefHeight(heigth);
        setStyle("-fx-border-color: #000000;");

        elevatorFont.setFitWidth(width);
        elevatorFont.setFitHeight(heigth);
        elevatorFont.setLayoutX(1.0);
        elevatorFont.setLayoutY(1.0);

        this.getChildren().add(elevatorFont);
    }

    public void setInternalControlPanel(InternalControlPanel internalControlPanel) {
        this.internalControlPanel = internalControlPanel;
    }
}
