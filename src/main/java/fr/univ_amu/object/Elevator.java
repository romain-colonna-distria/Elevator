package fr.univ_amu.object;

import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;


public class Elevator extends AnchorPane {

    public Elevator(double x, double y, double width, double heigth, ImageView elevatorFont){
        setLayoutX(x);
        setLayoutY(y);
        setPrefWidth(width);
        setPrefHeight(heigth);
        setStyle("-fx-border-color: #000000;");

        elevatorFont.setFitWidth(width);
        elevatorFont.setFitHeight(heigth);
        elevatorFont.setLayoutX(1.0);
        elevatorFont.setLayoutY(1.0);

        getChildren().add(elevatorFont);
    }
}
