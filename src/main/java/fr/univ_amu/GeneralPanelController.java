package fr.univ_amu;

import fr.univ_amu.ihm.ExternalControlPanel;
import fr.univ_amu.ihm.InternalControlPanel;
import fr.univ_amu.ihm.ElevatorShaft;

import fr.univ_amu.utils.Constant;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.List;


public class GeneralPanelController {

    @FXML
    private AnchorPane rootAnchorPane;

    @FXML
    private Label floorWaintingLineLabel;


    public void init() {
        /* *********** Cage d'ascenseur *********** */
        ElevatorShaft elevatorShaft = new ElevatorShaft(Constant.NB_FLOORS, this);
        elevatorShaft.setLayoutX(30.0);
        elevatorShaft.setLayoutY(25.0);
        rootAnchorPane.getChildren().add(elevatorShaft);
        /* **************************************** */

        /* Panneau de controle externe à l'ascenseur */
        List<ExternalControlPanel> externalControlPanels = new ArrayList<>();
        int j = Constant.NB_FLOORS - 1;
        for(short i = 0; i < Constant.NB_FLOORS; ++i){
            ExternalControlPanel tmp = new ExternalControlPanel(i, elevatorShaft.getElevatorControl());
            tmp.setLayoutX(213.0);
            tmp.setLayoutY(45 + (129 * j--));

            externalControlPanels.add(tmp);
        }
        elevatorShaft.getElevatorControl().setExternalControlPanels(externalControlPanels);
        elevatorShaft.addExternalsPanelsToChildren(externalControlPanels);
        /* ***************************************** */

        /* Panneau de controle interne à l'ascenseur */
        InternalControlPanel internalControlPanel = new InternalControlPanel(Constant.NB_FLOORS, elevatorShaft.getElevatorControl());
        internalControlPanel.setLayoutX(410.0);
        internalControlPanel.setLayoutY(25.0);
        elevatorShaft.getElevatorControl().setInternalControlPanel(internalControlPanel);
        rootAnchorPane.getChildren().add(internalControlPanel);
        /* ***************************************** */
    }

    public void updateWaitingLine(List<Short> waitingLineList){
        floorWaintingLineLabel.setText(waitingLineList.toString());
    }
}