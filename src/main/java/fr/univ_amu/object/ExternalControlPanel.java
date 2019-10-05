package fr.univ_amu.object;

import fr.univ_amu.command.Direction;
import fr.univ_amu.control.ElevatorControl;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class ExternalControlPanel extends AnchorPane {
    private ElevatorControl elevatorControl;
    private short floor;
    private Button upButton;
    private Button downButton;

    private EventHandler<ActionEvent> callEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            Direction direction = ((Button)event.getSource()).getId().equals("up") ? Direction.UP : Direction.DOWN;
            elevatorControl.call(floor, direction);
        }
    };

    public ExternalControlPanel(short floor, ElevatorControl elevatorControl){
        this.elevatorControl = elevatorControl;
        this.floor = floor;
        loadFXML();
        initButtons();

        setWidth(upButton.getWidth());
        setHeight(upButton.getHeight() + downButton.getHeight());

        setStyle("-fx-border-color: #000000; -fx-background-color: #ffffff;");

    }

    private void loadFXML(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("view/external_control_panel_view.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void initButtons(){
        for(Node node : this.getChildren()){
            node.setLayoutX(2.0);
            ((Button) node).setPrefWidth(11.0);
            ((Button) node).setPrefHeight(18.0);
            ((Button) node).setOnAction(callEvent);
        }

        upButton = (Button)this.getChildren().get(0);
        upButton.setGraphic(new ImageView(new Image("image/up_icon_16px.png")));
        upButton.setLayoutY(2.0);

        downButton = (Button)this.getChildren().get(1);
        downButton.setGraphic(new ImageView(new Image("image/down_icon_16px.png")));
        downButton.setLayoutY(27.0);
    }
}
