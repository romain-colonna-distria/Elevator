package fr.univ_amu;

import javafx.animation.PathTransition;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

import java.util.ArrayList;


public class ControlPanelController {
    @FXML
    private AnchorPane rootAnchorPane;

    @FXML
    private ImageView elevatorShaftImageView;

    @FXML
    private AnchorPane elevatorAnchorPane;

    @FXML
    private ImageView peopleInElevatorImageView;


    //----------------------------Button des différents étages----------------------------//
    private ArrayList<Button> stagesUpButtons = new ArrayList<>();
    private ArrayList<Button> stagesDownButtons = new ArrayList<>();

        //----------étage 4----------//
    @FXML
    private Button upStageFrom4Button;

    @FXML
    private Button downStageFrom4Button;

        //----------étage 3----------//
    @FXML
    private Button upStageFrom3Button;

    @FXML
    private Button downStageFrom3Button;

        //----------étage 2----------//
    @FXML
    private Button upStageFrom2Button;

    @FXML
    private Button downStageFrom2Button;

        //----------étage 1----------//
    @FXML
    private Button upStageFrom1Button;

    @FXML
    private Button downStageFrom1Button;

        //----------étage 0----------//
    @FXML
    private Button upStageFrom0Button;

    @FXML
    private Button downStageFrom0Button;
    //------------------------------------------------------------------------------------//



    public void init(){
        initLists();
        initImages();
    }

    public void initLists(){
        stagesUpButtons.add(upStageFrom0Button);
        stagesUpButtons.add(upStageFrom1Button);
        stagesUpButtons.add(upStageFrom2Button);
        stagesUpButtons.add(upStageFrom3Button);
        stagesUpButtons.add(upStageFrom4Button);

        stagesDownButtons.add(downStageFrom0Button);
        stagesDownButtons.add(downStageFrom1Button);
        stagesDownButtons.add(downStageFrom2Button);
        stagesDownButtons.add(downStageFrom3Button);
        stagesDownButtons.add(downStageFrom4Button);
    }

    private void initImages(){
        elevatorShaftImageView.setImage(new Image("image/elevatorShaft.png"));
        peopleInElevatorImageView.setImage(new Image("image/peopleInElevator.png"));

        for(Button button : stagesUpButtons)
            button.setGraphic(new ImageView(new Image("image/up_icon_16px.png")));

        for(Button button : stagesDownButtons)
            button.setGraphic(new ImageView(new Image("image/down_icon_16px.png")));
    }



    @FXML
    public void up(Event e){
        double actualX = elevatorAnchorPane.getLayoutX();
        double actualY = elevatorAnchorPane.getLayoutY();

        double excpectedY = actualY - 129;

        move(actualX, actualY, actualX, excpectedY);

        elevatorAnchorPane.setLayoutX(actualX);
        elevatorAnchorPane.setLayoutY(excpectedY - (elevatorAnchorPane.getHeight() / 2));
    }


    @FXML
    public void down(Event e){
        double actualX = elevatorAnchorPane.getLayoutX();
        double actualY = elevatorAnchorPane.getLayoutY();
        double excpectedX = actualX;
        double excpectedY = actualY + 129;

        move(actualX, actualY, excpectedX, excpectedY);

        elevatorAnchorPane.setLayoutX(excpectedX);
        elevatorAnchorPane.setLayoutY(excpectedY);
    }

    private void move(double actualX, double actualY, double excpectedX, double excpectedY){
        MoveTo moveTo = new MoveTo(actualX - elevatorAnchorPane.getWidth(), actualY);
        LineTo lineTo = new LineTo(excpectedX - elevatorAnchorPane.getHeight(), excpectedY);
        Path path = new Path();

        path.getElements().add(moveTo);
        path.getElements().add(lineTo);

        PathTransition transition = new PathTransition();
        transition.setNode(elevatorAnchorPane);
        transition.setPath(path);
        transition.setDuration(Duration.seconds(2));
        transition.play();
    }

}
