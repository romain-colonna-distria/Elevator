package fr.univ_amu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class GeneralPanelApplication extends Application {

    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("view/general_panel_view.fxml"));
        AnchorPane root = fxmlLoader.load();

        primaryStage.setScene(new Scene(root));

        GeneralPanelController controller = fxmlLoader.getController();
        controller.init();

        primaryStage.show();
    }
}
