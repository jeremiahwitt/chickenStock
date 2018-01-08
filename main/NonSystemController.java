package main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Created by Jeremiah on 3/31/2017.
 */
public class NonSystemController extends Controller {
    @FXML
    public void launchDeleteAccountView(ActionEvent event){
        SystemController.getInstance().launchDeleteAccountView();
    }

    public void launchLoginScreenView(){
        SystemController.getInstance().launchLoginScreenView();
    }

    public void launchGraphView() {
        SystemController.getInstance().launchGraphView();
    }

    public void launchLandingPageView(){
        SystemController.getInstance().launchLandingPageView();
    }

    public void openAlertWindow(String message){
        SystemController.getInstance().openAlertWindow(message);
    }

    @Override
    public void launchDeleteAccountView() {
        SystemController.getInstance().launchDeleteAccountView();
    }

    @FXML
    public void exitSystem(ActionEvent event){
        SystemController.getInstance().exitSystem(event);
    }

    @FXML
    public void launchAboutView(ActionEvent event){
        SystemController.getInstance().launchAboutView(event);
    }

    @Override
    public void closeAboutView(ActionEvent event) {
        SystemController.getInstance().closeAboutView(event);
    }

    @Override
    public void closeAlertWindow(ActionEvent event) {
        SystemController.getInstance().closeAboutView(event);
    }

    @Override
    public void logout(ActionEvent event) {
        SystemController.getInstance().logout(event);
    }

    @FXML
    public void launchCreateAccountView(ActionEvent event){
        SystemController.getInstance().launchCreateAccountView(event);
    }

    @Override
    public void launchLoginScreenView(ActionEvent event) {
        SystemController.getInstance().launchLoginScreenView(event);
    }

    @Override
    public void launchGraphView(ActionEvent event) throws Exception {
        SystemController.getInstance().launchGraphView(event);
    }

    @FXML
    public void launchHome(ActionEvent event){
        SystemController.getInstance().launchHome(event);
    }

    @Override
    public void launchLandingPageView(ActionEvent event) {
        SystemController.launchLandingPageView();
    }
}
