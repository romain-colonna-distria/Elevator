package fr.univ_amu;

import fr.univ_amu.command.Direction;
import fr.univ_amu.object.InternalControlPanel;

import fr.univ_amu.object.ElevatorShaft;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;


public class GeneralPanelController {
    private final short NB_FLOOR = 5;

    @FXML
    private AnchorPane rootAnchorPane;


    public void init(){
        /* *********** Cage d'ascenseur *********** */
        ElevatorShaft elevatorShaft = new ElevatorShaft(NB_FLOOR);
        elevatorShaft.setLayoutX(30.0);
        elevatorShaft.setLayoutY(25.0);
        rootAnchorPane.getChildren().add(elevatorShaft);
        /* **************************************** */

        /* Panneau de controle interne à l'ascenseur */
        InternalControlPanel internalControlPanel = new InternalControlPanel(NB_FLOOR);
        internalControlPanel.setLayoutX(410.0);
        internalControlPanel.setLayoutY(25.0);
        rootAnchorPane.getChildren().add(internalControlPanel);
        /* ***************************************** */
    }


    //@FXML
    public void call(Event e){
        String callButtonId = ((Button)e.getSource()).getId();
        int callStage;
        Direction direction;
        if(callButtonId.charAt(0) == 'u'){
            callStage = Integer.parseInt(String.valueOf(callButtonId.charAt(11)));
            direction = Direction.UP;
            //commandEngine.goUp();
        } else {
            callStage = Integer.parseInt(String.valueOf(callButtonId.charAt(13)));
            direction = Direction.DOWN;
            //commandEngine.goDown();
        }
    }
}
