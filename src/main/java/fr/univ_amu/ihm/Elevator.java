package fr.univ_amu.ihm;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;


public class Elevator extends AnchorPane {

    public Elevator(double x, double y, double width, double heigth){
        ImageView elevatorFont = new ImageView(new Image("image/elevator.png"));
        setLayoutX(x);
        setLayoutY(y);
        setPrefWidth(width);
        setPrefHeight(heigth);

        elevatorFont.setFitWidth(width);
        elevatorFont.setFitHeight(heigth);
        elevatorFont.setLayoutX(1.0);
        elevatorFont.setLayoutY(1.0);

        this.getChildren().add(elevatorFont);
    }
}
