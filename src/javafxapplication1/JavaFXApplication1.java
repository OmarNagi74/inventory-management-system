/*
 * JavaFX Login + Sign Up Application
 * Handles switching between scenes and interacting with User_Dol for authentication.
 */
package javafxapplication1;

import bullet_club.entity.User;
import bullet_club.model.Dol.User_Dol;
import java.io.IOException;
import java.io.InputStream;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class JavaFXApplication1 extends Application {

    private Stage primaryStage;
    private Scene loginScene;
    private Scene signUpScene;
    private Scene Home;
    
    // Dark mode state
    private static boolean isDarkMode = false;
    private BorderPane loginRoot;
    private BorderPane signUpRoot;

    // Login form fields
    private TextField loginUsernameField;
    private PasswordField loginPasswordField;
    private Label loginMessageLabel;

    // Sign up form fields
    private TextField signUpUsernameField;
    private PasswordField signUpPasswordField;
    private PasswordField signUpConfirmPasswordField;
    private TextField signUpEmailField;
    private TextField signUpAddressField;
    private TextField signUpPhoneField;
    private Label signUpMessageLabel;

    @Override
    public void start(Stage stage) {
        // Main window setup
        this.primaryStage = stage;
        primaryStage.setTitle("Bullet Club Portal");
        primaryStage.setResizable(true);

        // Create the two scenes
        loginRoot = buildLoginLayout();
        signUpRoot = buildSignUpLayout();
        loginScene = new Scene(loginRoot, 480, 480);
        signUpScene = new Scene(signUpRoot, 520, 560);

        try {
            String css = getClass().getResource("bulletclub-theme.css").toExternalForm();
            loginScene.getStylesheets().add(css);
            signUpScene.getStylesheets().add(css);
        } catch (Exception ignore) {
        }

        try {
            InputStream iconStream = getClass().getResourceAsStream("bulletclub-icon.png");
            if (iconStream != null) {
                Image icon = new Image(iconStream);
                primaryStage.getIcons().add(icon);
            }
        } catch (Exception ignore) {
        }

        // Apply initial theme based on current dark mode state
        applyTheme(loginRoot);
        applyTheme(signUpRoot);

        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    // Builds the login UI layout - CENTERED
    private BorderPane buildLoginLayout() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // Header with dark mode toggle
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_RIGHT);
        header.setPadding(new Insets(0, 0, 10, 0));
        
        ToggleButton darkModeToggle = new ToggleButton("🌙 Dark");
        darkModeToggle.setSelected(isDarkMode());
        darkModeToggle.setText(isDarkMode() ? "🌙 Dark" : "☀️ Light");
        darkModeToggle.setOnAction(e -> {
            boolean dark = darkModeToggle.isSelected();
            setDarkMode(dark);
            darkModeToggle.setText(dark ? "🌙 Dark" : "☀️ Light");
            applyTheme(loginRoot);
            applyTheme(signUpRoot);
        });
        header.getChildren().add(darkModeToggle);
        root.setTop(header);

        // Center container using StackPane for true centering
        StackPane centerWrapper = new StackPane();
        
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setMaxWidth(350);
        container.getStyleClass().add("login-container");

        Label title = new Label("Bullet Club Login");
        title.getStyleClass().add("login-title");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        // Login form grid
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(12);
        form.setPadding(new Insets(20, 10, 10, 10));
        form.setAlignment(Pos.CENTER);

        loginUsernameField = new TextField();
        loginUsernameField.setPromptText("Enter your username");
        loginUsernameField.setPrefWidth(160);

        loginPasswordField = new PasswordField();
        loginPasswordField.setPromptText("Enter your password");
        loginPasswordField.setPrefWidth(160);

        form.add(new Label("Username"), 0, 0);
        form.add(loginUsernameField, 1, 0);
        form.add(new Label("Password"), 0, 1);
        form.add(loginPasswordField, 1, 1);

        // Login button action
        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(e -> attemptLogin());
        form.add(loginButton, 0, 2, 2, 1);

        // Link to sign-up page
        Hyperlink signUpLink = new Hyperlink("Create a new account");
        signUpLink.setOnAction(e -> switchToSignUp());

        loginMessageLabel = new Label();

        container.getChildren().addAll(title, form, loginMessageLabel, signUpLink);
        centerWrapper.getChildren().add(container);
        root.setCenter(centerWrapper);
        return root;
    }

    // Builds the sign-up UI layout - CENTERED
    private BorderPane buildSignUpLayout() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // Header with dark mode toggle
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_RIGHT);
        header.setPadding(new Insets(0, 0, 10, 0));
        
        ToggleButton darkModeToggle = new ToggleButton("🌙 Dark");
        darkModeToggle.setSelected(isDarkMode());
        darkModeToggle.setText(isDarkMode() ? "🌙 Dark" : "☀️ Light");
        darkModeToggle.setOnAction(e -> {
            boolean dark = darkModeToggle.isSelected();
            setDarkMode(dark);
            darkModeToggle.setText(dark ? "🌙 Dark" : "☀️ Light");
            applyTheme(loginRoot);
            applyTheme(signUpRoot);
        });
        header.getChildren().add(darkModeToggle);
        root.setTop(header);

        // Center container using StackPane for true centering
        StackPane centerWrapper = new StackPane();
        
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setMaxWidth(400);
        container.getStyleClass().add("login-container");

        Label title = new Label("Bullet Club Sign Up");
        title.getStyleClass().add("login-title");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(12);
        form.setPadding(new Insets(20, 10, 10, 10));
        form.setAlignment(Pos.CENTER);

        // Fields
        signUpUsernameField = new TextField();
        signUpUsernameField.setPromptText("username");
        signUpUsernameField.setPrefWidth(220);

        signUpPasswordField = new PasswordField();
        signUpPasswordField.setPromptText("password");
        signUpPasswordField.setPrefWidth(220);

        signUpConfirmPasswordField = new PasswordField();
        signUpConfirmPasswordField.setPromptText("confirm password");
        signUpConfirmPasswordField.setPrefWidth(220);

        signUpEmailField = new TextField();
        signUpEmailField.setPromptText("email@example.com");
        signUpEmailField.setPrefWidth(220);

        signUpAddressField = new TextField();
        signUpAddressField.setPromptText("Address");
        signUpAddressField.setPrefWidth(220);

        signUpPhoneField = new TextField();
        signUpPhoneField.setPromptText("Phone number");
        signUpPhoneField.setPrefWidth(220);

        // Add components to form
        form.add(new Label("Username"), 0, 0);
        form.add(signUpUsernameField, 1, 0);
        form.add(new Label("Password"), 0, 1);
        form.add(signUpPasswordField, 1, 1);
        form.add(new Label("Confirm Password"), 0, 2);
        form.add(signUpConfirmPasswordField, 1, 2);
        form.add(new Label("Email"), 0, 3);
        form.add(signUpEmailField, 1, 3);
        form.add(new Label("Address"), 0, 4);
        form.add(signUpAddressField, 1, 4);
        form.add(new Label("Phone"), 0, 5);
        form.add(signUpPhoneField, 1, 5);

        // Sign-up button
        Button createAccountButton = new Button("Sign Up");
        createAccountButton.setMaxWidth(Double.MAX_VALUE);
        createAccountButton.setOnAction(e -> attemptSignUp());
        form.add(createAccountButton, 0, 6, 2, 1);

        // Link to login page
        Hyperlink backToLoginLink = new Hyperlink("Back to login");
        backToLoginLink.setOnAction(e -> switchToLogin());

        signUpMessageLabel = new Label();

        container.getChildren().addAll(title, form, signUpMessageLabel, backToLoginLink);
        centerWrapper.getChildren().add(container);
        root.setCenter(centerWrapper);
        return root;
    }

    // Apply theme to a root pane
    private void applyTheme(BorderPane root) {
        if (root == null) return;
        if (isDarkMode()) {
            if (!root.getStyleClass().contains("dark-theme")) {
                root.getStyleClass().add("dark-theme");
            }
        } else {
            root.getStyleClass().remove("dark-theme");
        }
    }
    
    // Static methods to check and update dark mode (for use by controllers)
    public static boolean isDarkMode() {
        return isDarkMode;
    }

    public static void setDarkMode(boolean value) {
        isDarkMode = value;
    }

    // Handles login logic
    private void attemptLogin() {
        loginMessageLabel.setStyle("-fx-text-fill: #c62828;");
        loginMessageLabel.setText("");

        String username = loginUsernameField.getText().trim();
        String password = loginPasswordField.getText();

        // Basic validation
        if (username.isEmpty() || password.isEmpty()) {
            loginMessageLabel.setText("Please enter username and password.");
            return;
        }

        // Create temp user object for authentication
        User credentials = new User(0, username, "", password, "", "", "");
        boolean success = User_Dol.login(credentials);

        // Check result
        if (success && User_Dol.isLoggedIn()) {
            User current = User_Dol.getLoggedInUser();
            loadHomePage(current.getRole());
        } else {
            loginMessageLabel.setText("Invalid username or password.");
        }
    }

    private void loadHomePage(String role) {

        String fxml;

        switch (role) {
            case "manager":
                fxml = "managerHome.fxml";
                break;
            case "employee":
                fxml = "employeeHome.fxml";
                break;
            case "admin":
                fxml = "adminHome.fxml";
                break;
            case "customer":
                fxml = "customerHome.fxml";
                break;
            default:
                loginMessageLabel.setText("Unknown role.");
                return;
        }

        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));

            Scene homeScene = new Scene(root, 960, 640);
            try {
                String css = getClass().getResource("bulletclub-theme.css").toExternalForm();
                homeScene.getStylesheets().add(css);
            } catch (Exception ignore) {
            }
            
            // Apply dark theme if enabled
            if (isDarkMode) {
                root.getStyleClass().add("dark-theme");
            }
            
            primaryStage.setScene(homeScene);
        } catch (IOException e) {
            e.printStackTrace();
            loginMessageLabel.setText("Unable to load homepage: " + fxml);
        }
    }

    // Handles sign-up logic
    private void attemptSignUp() {
        signUpMessageLabel.setStyle("-fx-text-fill: #c62828;");
        signUpMessageLabel.setText("");

        String username = signUpUsernameField.getText().trim();
        String password = signUpPasswordField.getText();
        String confirmPassword = signUpConfirmPasswordField.getText();
        String email = signUpEmailField.getText().trim();
        String address = signUpAddressField.getText().trim();
        String phone = signUpPhoneField.getText().trim();

        // Field validations
        if (username.length() < 3) {
            signUpMessageLabel.setText("Username must be at least 3 characters.");
            return;
        }
        if (password.length() < 6) {
            signUpMessageLabel.setText("Password must be at least 6 characters.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            signUpMessageLabel.setText("Passwords do not match.");
            return;
        }
        if (!email.contains("@")) {
            signUpMessageLabel.setText("Invalid email address.");
            return;
        }
        if (address.isEmpty()) {
            signUpMessageLabel.setText("Address is required.");
            return;
        }
        if (!phone.matches("\\d{7,15}")) {
            signUpMessageLabel.setText("Phone must contain 7-15 digits.");
            return;
        }

        // Create user object (default role: customer)
        User newUser = new User(0, username, "customer", password, address, email, phone);

        // Send request to database
        boolean created = User_Dol.signUp(newUser);

        if (created) {
            // Success message
            signUpMessageLabel.setStyle("-fx-text-fill: #2e7d32;");
            signUpMessageLabel.setText("Account created! You can login now.");
            clearSignUpForm();
            switchToLogin();
        } else {
            signUpMessageLabel.setText("Could not create account. Try a different username/email.");
        }
    }

    // Switch to login scene
    private void switchToLogin() {
        applyTheme(loginRoot);
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("Bullet Club Login");
    }

    // Switch to sign-up scene
    private void switchToSignUp() {
        applyTheme(signUpRoot);
        primaryStage.setScene(signUpScene);
        primaryStage.setTitle("Bullet Club Sign Up");
    }

    // Clear login form inputs
    private void clearLoginForm() {
        loginUsernameField.clear();
        loginPasswordField.clear();
    }

    // Clear sign-up form inputs
    private void clearSignUpForm() {
        signUpUsernameField.clear();
        signUpPasswordField.clear();
        signUpConfirmPasswordField.clear();
        signUpEmailField.clear();
        signUpAddressField.clear();
        signUpPhoneField.clear();
    }

    public static void main(String[] args) {
        launch(args);
    }
}