package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        SystemController.setCurrentStage(primaryStage);
        SystemController.launchLandingPageView();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
