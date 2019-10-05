package fr.univ_amu.object;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InternalControlPanel extends AnchorPane {
    @FXML
    private Label currentFloorLabel;

    private List<Button> buttonList = new ArrayList<>();


    public InternalControlPanel(short nbFloor){
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
            Button tmp = new Button(String.valueOf(i));
            tmp.setPrefHeight(40);
            tmp.setPrefWidth(70);
            tmp.setLayoutX(80);
            tmp.setLayoutY(120 + (70 * j--));
            this.getChildren().add(tmp);
            buttonList.add(tmp);
        }
    }
}
