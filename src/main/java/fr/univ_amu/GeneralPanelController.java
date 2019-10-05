package fr.univ_amu;

import fr.univ_amu.command.Direction;
import fr.univ_amu.object.InternalControlPanel;

import fr.univ_amu.object.ElevatorShaft;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;


public class GeneralPanelController {

    @FXML
    private AnchorPane rootAnchorPane;


    public void init() {
        /* *********** Cage d'ascenseur *********** */
        ElevatorShaft elevatorShaft = new ElevatorShaft(Constant.NB_FLOORS);
        elevatorShaft.setLayoutX(30.0);
        elevatorShaft.setLayoutY(25.0);
        rootAnchorPane.getChildren().add(elevatorShaft);
        /* **************************************** */

        /* Panneau de controle interne à l'ascenseur */
        InternalControlPanel internalControlPanel = new InternalControlPanel(Constant.NB_FLOORS, elevatorShaft.getElevatorControl());
        internalControlPanel.setLayoutX(410.0);
        internalControlPanel.setLayoutY(25.0);
        rootAnchorPane.getChildren().add(internalControlPanel);
        /* ***************************************** */

        elevatorShaft.getElevator().setInternalControlPanel(internalControlPanel);
    }
}