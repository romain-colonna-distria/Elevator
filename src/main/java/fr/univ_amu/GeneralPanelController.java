package fr.univ_amu;

import fr.univ_amu.control.ElevatorControl;
import fr.univ_amu.engine.CommandEngine;
import fr.univ_amu.ihm.Elevator;
import fr.univ_amu.ihm.ExternalControlPanel;
import fr.univ_amu.ihm.InternalControlPanel;
import fr.univ_amu.ihm.ElevatorShaft;

import fr.univ_amu.observer.WaitingLineObserver;
import fr.univ_amu.strategy.MinimumStrategy;
import fr.univ_amu.utils.Constant;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.List;


public class GeneralPanelController implements WaitingLineObserver {

    @FXML
    private AnchorPane rootAnchorPane;

    @FXML
    private Label floorWaintingLineLabel;


    public void init() {
        /* *********** Cage d'ascenseur *********** */
        ElevatorShaft elevatorShaft = new ElevatorShaft(Constant.NB_FLOORS);
        elevatorShaft.setLayoutX(30.0);
        elevatorShaft.setLayoutY(25.0);
        rootAnchorPane.getChildren().add(elevatorShaft);
        /* **************************************** */

        Elevator elevator = new Elevator(110.0, 530.0, 90.0, 110.0);
        CommandEngine engine = new CommandEngine(elevator);
        ElevatorControl control = new ElevatorControl(engine);

        engine.addFloorObserver(control);
        control.addWaitingLineObserver(this);
        control.setStrategy(new MinimumStrategy());

        elevatorShaft.addChildren(elevator);


        /* Panneau de controle externe à l'ascenseur */
        List<ExternalControlPanel> externalControlPanels = new ArrayList<>();
        int j = Constant.NB_FLOORS - 1;
        for(short i = 0; i < Constant.NB_FLOORS; ++i){
            ExternalControlPanel tmp = new ExternalControlPanel(i);
            tmp.setLayoutX(213.0);
            tmp.setLayoutY(45 + (129 * j--));

            externalControlPanels.add(tmp);
        }
        for(ExternalControlPanel panel : externalControlPanels) {
            panel.addPanelObserver(control);
            elevatorShaft.addChildren(panel);
        }
        /* ***************************************** */

        /* Panneau de controle interne à l'ascenseur */
        InternalControlPanel internalControlPanel = new InternalControlPanel(Constant.NB_FLOORS);
        internalControlPanel.setLayoutX(410.0);
        internalControlPanel.setLayoutY(25.0);
        internalControlPanel.addPanelObserver(control);
        control.setInternalControlPanel(internalControlPanel);
        rootAnchorPane.getChildren().add(internalControlPanel);
        /* ***************************************** */

    }

    @Override
    public void updateWaitingLine(List<Short> waitingLineList) {
        floorWaintingLineLabel.setText(waitingLineList.toString());
    }
}