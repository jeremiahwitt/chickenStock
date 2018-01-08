package main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Created by Jeremiah on 3/31/2017.
 */
public abstract class Controller {
    public abstract void openAlertWindow(String message);

    public abstract void launchDeleteAccountView();

    @FXML
    public abstract void launchHome(ActionEvent event);

    @FXML
    public abstract void launchLandingPageView(ActionEvent event);

    @FXML
    public abstract void launchCreateAccountView(ActionEvent event);

    @FXML
    public abstract void launchLoginScreenView(ActionEvent event);

    @FXML
    public abstract void launchGraphView(ActionEvent event) throws Exception;

    @FXML
    public abstract void launchAboutView(ActionEvent event);

    @FXML
    public abstract void closeAboutView(ActionEvent event);

    @FXML
    public abstract void closeAlertWindow(ActionEvent event);

    @FXML
    public abstract void logout(ActionEvent event);

    @FXML
    public abstract void exitSystem(ActionEvent event);
}
