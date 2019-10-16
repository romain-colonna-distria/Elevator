package fr.univ_amu.ihm;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class ElevatorShaft extends AnchorPane {

    public ElevatorShaft(short nbFloor){
        initView();
        initLabels(nbFloor);

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
    private void initLabels(short nbFloor){
        int j = nbFloor - 1;
        for(short i = 0; i < nbFloor; ++i){
            Label tmp = new Label(String.valueOf(i));
            tmp.setLayoutX(30);
            tmp.setLayoutY(70 + (125 * j--));
            this.getChildren().add(tmp);
        }
    }

    public void addChildren(Node children){
        this.getChildren().add(children);
    }
}
