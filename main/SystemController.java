package main;

import accountManagement.AccountController;
import analysisSystem.GraphController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class SystemController extends Controller {

	//the current to faciltate accessing widgets by id
	private static Parent currentScene=null;
	
    /* STRING CONSTANTS */
    private static final String LANDING_PAGE_TITLE = "Chicken Stock - Welcome";
    private static final String LANDING_PAGE_PATH = "../main/landingPageView.fxml";

    private static final String CREATE_ACCOUNT_TITLE = "Chicken Stock - Create Account";
    private static final String CREATE_ACCOUNT_PATH = "../accountManagement/createAccountView.fxml";

    private static final String LOGIN_TITLE = "Chicken Stock - Login";
    private static final String LOGIN_PATH = "../accountManagement/loginView.fxml";

    private static final String ANALYSIS_TITLE = "Chicken Stock";
    private static final String ANALYSIS_PATH = "../analysisSystem/graphView.fxml";

    private static final String ABOUT_TITLE = "Chicken Stock - About";
    private static final String ABOUT_PATH = "aboutView.fxml";

    private static final String ALERT_TITLE = "Chicken Stock - Important Info";
    private static final String ALERT_PATH = "alertView.fxml";

    private static final String DELETE_ACCOUNT_TITLE = "Chicken Stock - Delete Account";
    private static final String DELETE_ACCOUNT_PATH = "../accountManagement/deleteAccountView.fxml";


    /* PRIVATE STATIC VARIABLES */

    /**
     * Private instance of SystemController, allowing only one SystemController to exist in the system
     */
    private static SystemController _systemControllerInstance = null;

    /* PRIVATE ATTRIBUTES */
    private static Stage currentStage;

    /* CONSTRUCTORS */
    /**
     * Private Constructor, forcing anyone wanting to use SystemController to use the
     * getInstance() method.
     */
    protected SystemController(){}

    /**
     * Allows you to get the global instance of the SystemController, making use of
     * lazy instantiation
     * @return SystemController instance
     */
    public static SystemController getInstance(){
        if(_systemControllerInstance == null){
            _systemControllerInstance = new SystemController();
        }

        return _systemControllerInstance;
    }

    /* STATIC METHODS */

    /**
     * Opens up a Dialogue Window with the desired message.
     * @param message String to be displayed in the window
     */
    @Override
    public void openAlertWindow(String message) {
        Stage alertStage = new Stage();
        Parent alertScene = getParentSceneFromPathWithController(ALERT_PATH, SystemController.getInstance());
        Text alertMessage = (Text) alertScene.lookup("#alertText");
        alertMessage.setText(message);
        alertStage.setAlwaysOnTop(true);
        setParentToStageWithTitle(alertScene, alertStage, ALERT_TITLE);
    }

    /**
     * Will set the stage that the Application is running on.
     * @param theStage Stage to be tracked
     */
    public static void setCurrentStage(Stage theStage){
        currentStage = theStage;
    }

    /**
     * Provides access to the Stage upon which the application is being displayed
     * @return Stage, which is the current Stage of the system
     */
    public static Stage getCurrentStage(){
        return currentStage;
    }

    /**
     * Displays the program landing page
     * @throws Exception
     */
    public static void launchLandingPageView() {
        Parent landingPageScene = getParentSceneFromPathWithController(LANDING_PAGE_PATH, SystemController.getInstance());
        setParentToStageWithTitle(landingPageScene, currentStage, LANDING_PAGE_TITLE);
    }

    /**
     * Will perform the necessary actions to launch the CreateAccountView
     */
    public static void launchCreateAccountView(){
        Parent createAccountView = getParentSceneFromPathWithController(CREATE_ACCOUNT_PATH, AccountController.getInstance());
        setParentToStageWithTitle(createAccountView, currentStage, CREATE_ACCOUNT_TITLE);
    }

    @Override
    public void launchDeleteAccountView() {
        Parent deleteAccountView = getParentSceneFromPathWithController(DELETE_ACCOUNT_PATH, AccountController.getInstance());
        setParentToStageWithTitle(deleteAccountView, currentStage, DELETE_ACCOUNT_TITLE);
    }

    /**
     * Displays the LoginScreenView when requested, allowing the user to log into their account
     */
    public static void launchLoginScreenView() {
        Parent loginScene = getParentSceneFromPathWithController(LOGIN_PATH, AccountController.getInstance());
        setParentToStageWithTitle(loginScene, currentStage, LOGIN_TITLE);
    }

    /**
     * Displays the GraphView when requested, ensuring that a user has actually logged in in the process
     */
    public static void launchGraphView(){
        // TODO Add logic to make sure it is only launched when AccountController indicates user is logged in

        Parent analysisScene = getParentSceneFromPathWithController(ANALYSIS_PATH, GraphController.getInstance());
        
        //setup the current scene variable
        currentScene = analysisScene;
        
        setParentToStageWithTitle(analysisScene, currentStage, ANALYSIS_TITLE);
        GraphController.getInstance().setupGraph();
    }
    /**
     * access current scene
     */
    public static Parent getCurrentScene()
    {
    	return currentScene;
    }

    /**
     * Displays the AboutView when requested, displaying information about the project
     */
    public static void launchAboutView() {
        Stage aboutStage = new Stage();
        Parent aboutScene = getParentSceneFromPathWithController(ABOUT_PATH, SystemController.getInstance());
        setParentToStageWithTitle(aboutScene, aboutStage, ABOUT_TITLE);
    }

    /**
     * Will return a Parent scene, based on path and desired Controller.
     * Using the path to an FXML file, this method will load the desired Scene as a Parent and attach the provided
     * Controller to it. If there are any issues in loading the Scene, the System will exit.
     * @param path String representing the path to the desired Scene
     * @param controller Controller Object to be attached to the desired Scene
     * @return Parent
     */
    private static Parent getParentSceneFromPathWithController(String path, Object controller){
        FXMLLoader loader = new FXMLLoader(SystemController.class.getResource(path));
        loader.setController(controller);
        Parent par = null;
        try {
            par = loader.load();
        } catch (IOException e) {
            System.err.println("There was an error loading " + " path.");
            System.err.println(e.getMessage());
            System.exit(0);
        }
        return par;
    }

    /**
     * Attaches the provided Parent to the Stage, and sets it up with the desired title.
     * Using the Parent and Stage provided, the Parent Scene gets staged on the Stage, its title is set to what is
     * provided, and then it is made visible
     * @param parentScene Parent, a loaded Scene with Controller
     * @param primaryStage Stage to load the scene on
     * @param title String, title of the Screen
     */
    private static void setParentToStageWithTitle(Parent parentScene, Stage primaryStage, String title){
        primaryStage.setTitle(title);
        primaryStage.setScene(new Scene(parentScene));
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    /**
     * Completely closes the system
     */
    public static void exitSystem() {
        Platform.exit();
    }

    /* FXML EVENT METHODS */

    @Override
    @FXML
    public void launchHome(ActionEvent event) {
        if(AccountController.getInstance().getLoggedInStatus() == true){
            launchGraphView();
        } else {
            launchLandingPageView();
        }
    }
    /**
     * Launches the Landing Page after an event gets thrown
     * @param event ActionEvent triggered by the System
     */
    @Override
    @FXML
    public void launchLandingPageView(ActionEvent event) {
        launchLandingPageView();
    }

    /**
     * Will launch the CreateAccountView when an event is triggered by the System, requesting to do so
     * @param event ActionEvent which has been triggered
     */
    @Override
    @FXML
    public void launchCreateAccountView(ActionEvent event) {
        launchCreateAccountView();
    }

    /**
     * Will launch the LoginScreenView when the System makes a request to do so
     * @param event ActionEvent triggered by the system
     */
    @Override
    @FXML
    public void launchLoginScreenView(ActionEvent event) {
        launchLoginScreenView();
    }

    /**
     * Will launch the GraphView when the System makes a request to do so
     * @param event ActionEvent triggered by the system
     */
    @Override
    @FXML
    public void launchGraphView(ActionEvent event) throws Exception {
        Parent analysisScene = getParentSceneFromPathWithController(ANALYSIS_PATH, SystemController.getInstance());
        setParentToStageWithTitle(analysisScene, currentStage, ANALYSIS_TITLE);
    }

    /**
     * Will launch the AboutView when the System makes a request to do so
     * @param event ActionEvent triggered by the system
     */
    @Override
    @FXML
    public void launchAboutView(ActionEvent event) {
        launchAboutView();
    }

    /**
     * Will close the AboutView when the System makes a request to do so
     * @param event ActionEvent triggered by the system
     */
    @Override
    @FXML
    public void closeAboutView(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }

    /**
     * Closes an Alert Window when the user clicks "Okay"
     * @param event
     */
    @Override
    @FXML
    public void closeAlertWindow(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }

    /**
     * Logs the user out of the System, and returns them to the Start Screen.
     * @param event
     */
    @Override
    @FXML
    public void logout(ActionEvent event) {
        AccountController.getInstance().logout();
        launchLandingPageView();
    }

    /**
     * Shuts the System down
     * @param event ActionEvent triggered by the System
     */
    @Override
    @FXML
    public void exitSystem(ActionEvent event){
        // TODO add logic that checks if the user is logged in before exiting
        exitSystem();
    }

}
