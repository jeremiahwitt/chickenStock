package accountManagement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import main.NonSystemController;
import main.SystemController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.sql.*;

/**
 * Created by Jeremiah on 3/10/2017.
 * Appended by Sam on 3/12/2017
 */
public class AccountController extends NonSystemController {

    /* PRIVATE STATIC VARIABLES */

    /**
     * Private reference to AccountController instance. Used to enforce global access
     */
    private static AccountController _accountControllerInstance = null;

    /* Private JavaFX Fields */
    @FXML
    private TextField firstName;

    @FXML
    private TextField lastName;

    @FXML
    private TextField email;

    @FXML
    private Text emailValidity;

    @FXML
    private Text entryValidityDisplay;

    @FXML
    private TextField usernameEntry;

    @FXML
    private PasswordField passwordEntry;

    @FXML
    private PasswordField passwordConfirmation;

    private boolean loggedIn;
    

    //this is extracted by using function extractUserID
    private int userID;
    //the following to track connection for user
    Connection conn;
    

    //following to keep track when a user is logged into the system

    String username;
    String password;

    /* CONSTRUCTORS */

    /**
     * Private Constructor, used to enforce existence of only one instance
     */
    private AccountController(){ loggedIn = false;}

    /**
     * Allows for there to be only one instance of the AccountController. Will return the AccountController instance
     * being used by the system
     * @return AccountController
     */
    public static AccountController getInstance(){
        if(_accountControllerInstance == null){
            _accountControllerInstance = new AccountController();
        }

        return _accountControllerInstance;
    }

    /* METHODS */

    /**
     * Validates acount details and adds the requested account to the AccountDatabase, when the user clicks submit.
     * Only accessible via the CreateAccountView
     * @param event
     * Implemented by Sam. March 2017
     * @throws Exception if connection doesn't work
     */
    @FXML
    public void setupAccount(ActionEvent event) throws Exception {
        // first validate inputs
        // check if any of the inputs fields are empty
        if (checkInputFieldsEmpty()) {
            // if one or more fields are empty, tell them to fill the form out completely, and return
            displayIncompleteForm();
            return;
        }
        // check if email is a real email
        if (!verifyEmailFormat()) {
            // email is no good, tell them (user) and return
            displayInvalidEmail();
            return;
        }
        // check if passwords match. I don't think we should care about how secure their password is. That's on them.
        if (!checkPasswordsMatch()) {
            // passwords do not match
            displayPasswordsNotMatching();
            return;
        }
        // if we get here, the entry is validated. This process can be sophisticated later, but for now it's okay
        // clear the validation display
        clearValidityDisplay();
        clearEmailValidityDisplay();

        // Okay, inputs are fine, now let's connect to DB
        Connection c = getConnection();
        // now register the user
        registerUser(c, firstName.getText(), lastName.getText(), email.getText(), usernameEntry.getText(), passwordEntry.getText());
        // send them to login page
        launchLoginScreenView();
    }


    /**
     * Ensures that the username and password provided by the user are correct
     */
    @FXML
    public void validateLoginAttempt(ActionEvent event){
        boolean valid = false;
        // I will build off of Jeremiah's logic and structure here
        // first grab the inputs and connect to DB

        /*
         * khatibs modification, tracking the username and password in private feild, since
         * after login there does not seem to be a way to track who is logged in
         */

        username = usernameEntry.getText();
        password = passwordEntry.getText();
        // We need to check if there is a match in the database
        String query = "SELECT * FROM Users WHERE username = '" + username + "' AND password = '" + password + "' LIMIT 1";
        System.out.println(query);
        // create a prepared statement
        try {
            // connect first
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            // get result
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                valid = true;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(valid){
            // login has occurred
            loggedIn = true;
            // move them to the home screen
            launchGraphView();
            
            //khatibs code
            extractUserID();
        } else displayInvalidPasswordOrUsername(); // can also call displayInvalidUsername()
    }

    /**
     * Displays that the form is incomplete
     */
    @FXML
    private void displayIncompleteForm() {
        entryValidityDisplay.setFill(Color.RED);
        entryValidityDisplay.setText("One or more input fields is empty!");
        entryValidityDisplay.setVisible(true);
    }

    /**
     * Indicates to the user that their password is invalid
     */
    @FXML
    private void displayPasswordsNotMatching() {
        entryValidityDisplay.setFill(Color.RED);
        entryValidityDisplay.setText("Passwords do not match!");
        entryValidityDisplay.setVisible(true);
    }


    /**
     * Indicates to the user that their password is invalid
     */
    @FXML
    private void displayInvalidPassword() {
        entryValidityDisplay.setFill(Color.RED);
        entryValidityDisplay.setText("Invalid Password");
        entryValidityDisplay.setVisible(true);
    }

    /**
     * Indicates to the user that their username is invalid
     */
    @FXML
    private void displayInvalidUsername() {
        entryValidityDisplay.setFill(Color.RED);
        entryValidityDisplay.setText("Invalid Username");
        entryValidityDisplay.setVisible(true);
    }

    /**
     * Indicates to the user that their password is invalid
     */
    @FXML
    private void displayInvalidPasswordOrUsername() {
        entryValidityDisplay.setFill(Color.RED);
        entryValidityDisplay.setText("Invalid Username or Password");
        entryValidityDisplay.setVisible(true);
    }


    /**
     * Ensures that when a user starts providing a new username/password, they no longer
     * see that their account details are incorrect
     * @param event
     */
    @FXML
    public void clearValidityDisplay(KeyEvent event){
        clearValidityDisplay();
    }

    /**
     * Clears the entryValidityDisplay text area. Provides the ability to do so without
     * passing an event.
     */
    @FXML
    public void clearValidityDisplay(){
        entryValidityDisplay.setVisible(false);
    }

    /**
     * Checks if any of the input boxes are empty
     * Returns true if one or more of the fields is empty
     */
    @FXML
    public boolean checkInputFieldsEmpty(){
        return (firstName.getText().isEmpty() || lastName.getText().isEmpty() || email.getText().isEmpty() || usernameEntry.getText().isEmpty() || passwordEntry.getText().isEmpty() || passwordConfirmation.getText().isEmpty());
    }

    /**
     * Checks that passwords match, and displays the appropriate message to the user regarding their situation
     * @param event
     */
    public void checkPasswordsMatch(KeyEvent event){
        if (!checkPasswordsMatch()){
            displayPasswordsNotMatching();
        } else {
            displayGoodPasswordMatch();
        }
    }

    /**
     * Returns whether or not the passwords match
     * @return
     */
    @FXML
    public boolean checkPasswordsMatch() {
        return ( passwordEntry.getText().equals(passwordConfirmation.getText()));
    }

    /**
     * Displays message to the user that their passwords do not match.
     */
    @FXML
    private void displayPasswordMismatch() {
        entryValidityDisplay.setText("Passwords do not match!");
        entryValidityDisplay.setFill(Color.RED);
        entryValidityDisplay.setVisible(true);
    }

    /**
     * Displays message to the user that their passwords DO match.
     */
    @FXML
    private void displayGoodPasswordMatch() {
        entryValidityDisplay.setText("Passwords Match");
        entryValidityDisplay.setFill(Color.GREEN);
        entryValidityDisplay.setVisible(true);
    }

    /**
     * Clears all data that has been entered by the user.
     * @param event
     */
    @FXML
    public void clearUserEnteredData(ActionEvent event){
        usernameEntry.clear();
        passwordEntry.clear();
        clearValidityDisplay();
    }

    /**
     * Clears all data entered on the CreteAccountView, when the user requests to 'Clear'
     * @param event
     */
    @FXML
    public void clearCreateAccountData(ActionEvent event){
        firstName.clear();
        lastName.clear();
        email.clear();
        usernameEntry.clear();
        passwordEntry.clear();
        passwordConfirmation.clear();
        clearValidityDisplay();
        clearEmailValidityDisplay();
    }

    /**
     * Validates the email provided by the user to ensure that it is in a valid format. Displays to the user whether
     * or not it is valid
     * @param event
     */
    @FXML
    public void verifyEmailFormat(KeyEvent event){
        boolean match = verifyEmailFormat();
        if(match){
            displayValidEmail();
        } else {
            displayInvalidEmail();
        }
    }

    /**
     * Validates the email provided by the user to ensure that it is in a valid format. Displays to the user whether
     * or not it is valid
     */
    public boolean verifyEmailFormat() {
        String emailEntry = email.getText();
        // Credit for Regex goes to Jason Buberel on http://stackoverflow.com/questions/8204680/java-regex-email/13013056#13013056
        Pattern emailFormat = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher emailMatch = emailFormat.matcher(emailEntry);
        return emailMatch.matches();
    }

    /**
     * Displays to the user that their email is valid, via a checkmark
     */
    @FXML
    private void displayValidEmail(){
        emailValidity.setText("✓");
        emailValidity.setFill(Color.GREEN);
        entryValidityDisplay.setText("");
        emailValidity.setVisible(true);
    }

    /**
     * Displays to the user that their email is not valid, via an X
     * ADDED: also shows a message
     */
    @FXML
    private void displayInvalidEmail() {
        emailValidity.setText("X");
        emailValidity.setFill(Color.RED);
        emailValidity.setVisible(true);
        entryValidityDisplay.setFill(Color.RED);
        entryValidityDisplay.setText("Invalid Email!");
        entryValidityDisplay.setVisible(true);
    }

    /**
     * Clears messages regarding email validity
     */
    private void clearEmailValidityDisplay(){
        emailValidity.setVisible(false);
    }

    //TODO Remove in final build!
    public void bypassLogin(ActionEvent event){
        launchGraphView();
    }

    /**
     * Logs the user out of the system
     */
    public void logout() {
        //TODO Add logic for logging out
        try {conn.close();
            conn=null;}
        catch (Exception e){
            e.printStackTrace();
        }
        loggedIn = false;
    }

    /**
     * SQL METHODS
     */

    /**
     * Connects to our SQL database
     * @return
     * @throws Exception
     */
    public Connection getConnection() throws SQLException {
        String sqlHost = "jdbc:mysql://192.254.237.126:3306/samcream_comp354";
        String username = "samcream_354User";
        String password = "sam123";
        if(this.conn!=null)
        {
        	return this.conn;
        }
        this.conn = null;
        Properties connectionProps = new Properties();
        connectionProps.put("user", username);
        connectionProps.put("password", password);
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(sqlHost, connectionProps);
            
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("Connected to database");
        return this.conn;
    }

    /**
     * Adds a new user to the users table in our database.
     * @param conn
     */
    public void registerUser(Connection conn, String firstName, String lastName, String email, String username, String password) {
        try {
            // first create statement
            Statement s = conn.createStatement();
            // insert into DB. adding 0 for the ID because it autoincrements and java is weird about SQL syntax
            String query = "INSERT INTO Users " + "VALUES (0, '" + firstName + "', '" + lastName + "', '" + email + "', '" + username + "', '" + password + "' )";
            s.executeUpdate(query);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("User " + username + " added to db.");
        openAlertWindow("Your account has successfully been created! You can now login to your account.");
    }



    /**
     * Allows the user to opt to stop creating their account. This will return them
     * to the home screen.
     * @param event ActionEvent triggered by the system
     */
    @FXML
    public void cancelCreateAccount(ActionEvent event){
        launchLandingPageView();
    }

    @FXML
    public void deleteAccount(ActionEvent event){
    	//check is the logged in user info matches with info entered in delete account view.
    	if((this.username.equals(this.usernameEntry.getText()))&&(this.password.equals(this.passwordEntry.getText())))
    	{
    		String query="delete from Users where username = '"+this.username+"' and password = '"+this.password+"';";
    		System.out.println(query);
    		try {
                // connect first
                Connection conn = getConnection();
                if(conn==null)
                {
                	System.out.println("could not connect");
                }
                //if connection is successfull, delete account and show affected rows for debugging purposes
                else
                {
	               PreparedStatement ps = conn.prepareStatement(query);
	               // get result
	               int result = ps.executeUpdate();
	               //System.out.println(result.);
	               System.out.println("Affected: "+result);
	               loggedIn=false;
	               openAlertWindow("Your account has successfully been deleted. Hope you enjoyed using Chicken Stock.");
	               SystemController.getInstance().launchLoginScreenView();
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    	}
    	else
    	{
    		displayInvalidUsernamePasswordMatchForLoggedInUser();
    	}
    }
    
    public void clickedStock(String stockname)
    {
    	//get the stockID
    	int stockID = getStockID(stockname);
    	//System.out.println(stockID);
    	insertUpdateFavoriteStock(this.userID, stockID);
    	//seeAllInUFS();
    }
    
    //helper function, clears all rows in UserFavoriteStock table
    //to be used for debuggin purposes
    private void clearUserFavoriteStock()
    {
    	String query = "delete from UserFavoriteStock;";
   	 	try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            int result = ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //helper function to get stockID of the cliked stock
    //if the clicked stock is in database, then it will just return the id
    //if not in database, then it will first add then return the id
    private int getStockID(String stockname)
    {
    	//check if stockname exists in Stocks
    	int id=-1;
    	ResultSet result=null;
    	String query = "Select * from Stocks where stockName = '"+singleQuoteEscape(stockname)+"';";
        Connection conn;
		try 
		{
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(query);
            result = ps.executeQuery();
            if(result.next())
            {
            	System.out.println(result.getString("stockName")+" is in database");
            	id = result.getInt("ID");
            }
            else
       	 	{
       	 		insertIntoStocks(singleQuoteEscape(stockname));
    			System.out.println(stockname+" was not in database and has been inserted, below is updated table.");
       	 		seeAllInStocks();
       	 		
       	 		//now query again
       	 		try
       	 		{
	       	 		conn = getConnection();
	    			ps = conn.prepareStatement(query);
	                result = ps.executeQuery();
	                if(result.next())
	                {
	                	id =  result.getInt("ID");
	                }
	                else
	                {
	                	System.err.println("Error: "+stockname+" could not be added in database");
	                }
       	 		}
       	 		catch(SQLException e)
       	 		{
       	 			e.printStackTrace();
       	 		}
       	 	}
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
   	 	return id;
    }

    //helper function to escape the single quotes if any for sql queries
    private String singleQuoteEscape(String stockname)
    {
    	String x="";
    	for(int j=0; j<stockname.length(); j++)
    	{
    		if(stockname.charAt(j)=='\'')
    		{
    			x += "''";
    		}
    		else
    		{
    			x += stockname.charAt(j);
    		}
    	}
    	return x;
    }
    //helper function, insert a stock into Stocks if it is not already in
    private void insertIntoStocks(String stockname)
    {
    	String query = "insert into Stocks(stockName) values ('"+stockname+"');";
    	System.out.println(query);
    	
   	 	try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            int result = ps.executeUpdate();

        } catch (Exception e) {
        	System.out.println("error in insertIntoStocks");
            e.printStackTrace();
        }
    }
    //helper function just to see all data in Stocks table
    private void seeAllInStocks()
    {
    	String query = "select * from Stocks;";
   	 	try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                System.out.println(result.getString("ID")+" "+result.getString("stockName"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * indicate to user the username and password did not match for entity logged in
     */
    private void displayInvalidUsernamePasswordMatchForLoggedInUser()
    {
    	entryValidityDisplay.setText("Username and password dont match with logged in user!");
        entryValidityDisplay.setFill(Color.RED);
        entryValidityDisplay.setVisible(true);
    }

    //helper function just to see all data in Stocks table
    private void seeAllInUFS()
    {
    	String query = "select * from UserFavoriteStock;";
   	 	try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet result = ps.executeQuery();
            System.out.println("userID\tstockID\tvisited");
            while (result.next()) {
                System.out.println(result.getString("userID")+"\t"+result.getString("stockID")+"\t"+result.getInt("timeVisited"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //Helper function just describes the column name and data type for given table
    private void descTable(String tablename)
    {
    	String query = "desc "+tablename+";";
   	 	try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet result = ps.executeQuery();
            System.out.println("Table: "+tablename+" description");
            while (result.next()) {
                System.out.println(result.getString(1)+" "+result.getString(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //helper function extracts userID based on username and assigns it to the private variable
    //to be used only after user is logged in
    private void extractUserID()
    {
    	String query = "select ID from Users where username = '"+this.username+"';";
   	 	try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet result = ps.executeQuery();
            if(result.next())
            	this.userID = result.getInt(1);
            System.out.println(this.userID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public boolean getLoggedInStatus(){
        return loggedIn;
    }

    public List<String> getMostViewed()
    {
    	return getFavoriteStocks(this.userID);
    }
    /**
     * Gets the favorite stocks of the user.
     */
    private List<String> getFavoriteStocks(int userId){
        List<String> favoriteStocks = new ArrayList<String>();

        String query = "SELECT fus.stockId, s.stockName "
                        + " FROM UserFavoriteStock fus "
                        + " JOIN Stocks s ON s.ID = fus.stockId "
                        + " WHERE userId = " + userId + " "
                        + " ORDER BY timeVisited DESC "
                        + " LIMIT 3";

        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(query);

            ResultSet result = ps.executeQuery();

            while (result.next()) {
                favoriteStocks.add(result.getInt("fus.stockId") + ":" + result.getString("s.stockName"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return favoriteStocks;
    }

    private void insertUpdateFavoriteStock(int userId, int stockId) {
        int favoriteId = -1;
        int timeVisited = 0;

        String query = "SELECT stockId , timeVisited"
                        + " FROM UserFavoriteStock "
                        + " WHERE userId = " + userId + " AND stockId = " + stockId+";";
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(query);

            ResultSet result = ps.executeQuery();

            while (result.next()) {
              favoriteId = result.getInt("stockId");
              timeVisited = result.getInt("timeVisited");
            }

            if (favoriteId == -1) {
                query = " INSERT INTO UserFavoriteStock (userId, stockId, timeVisited) "
                      + " VALUES (" + userId + ", " + stockId + "," + 1 +");";
            } else {
            	int x=timeVisited+1;
                query = "UPDATE UserFavoriteStock "
                        + " SET timeVisited = " + x
                        + " WHERE stockId = " + favoriteId + " and userID = "+userID+";";
                System.out.println(query);
            }

            Statement s = conn.createStatement();
            s.executeUpdate(query);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
