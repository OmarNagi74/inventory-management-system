package javafxapplication1;

import bullet_club.entity.User;
import bullet_club.model.Dol.User_Dol;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class UserProfileController implements Initializable {
    @FXML private javafx.scene.layout.AnchorPane root;
    @FXML private ToggleButton darkModeToggle;
    @FXML private TextField txtId;
    @FXML private TextField txtUsername;
    @FXML private TextField txtRole;
    @FXML private TextField txtAddress;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private PasswordField txtCurrentPwd;
    @FXML private PasswordField txtNewPwd;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User u = User_Dol.getLoggedInUser();
        if (u != null) {
            txtId.setText(String.valueOf(u.getId()));
            txtUsername.setText(u.getUsername());
            txtRole.setText(u.getRole());
            txtAddress.setText(u.getAdd());
            txtEmail.setText(u.getEmail());
            txtPhone.setText(u.getPhone());
        } else {
            txtId.setText("");
            txtUsername.setText("Not logged in");
            txtRole.setText("");
            txtAddress.setText("");
            txtEmail.setText("");
            txtPhone.setText("");
        }

        
    }

    @FXML
    private void goBack() {
        try {
            if (root != null && root.getScene() != null) {
                Node sceneRoot = root.getScene().getRoot();
                if (sceneRoot instanceof SplitPane) {
                    SplitPane sp = (SplitPane) sceneRoot;
                    Object prev = sp.getProperties().get("prevBottom");
                    if (prev instanceof Node) {
                        sp.getItems().set(1, (Node) prev);
                    }
                }
            }
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Failed to go back: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML
    private void saveProfile() {
        User u = User_Dol.getLoggedInUser();
        if (u == null) {
            new Alert(Alert.AlertType.WARNING, "Please login first").showAndWait();
            return;
        }
        String newUsername = txtUsername.getText().trim();
        String newEmail = txtEmail.getText().trim();
        String newPhone = txtPhone.getText().trim();
        String newAddress = txtAddress.getText().trim();

        if (newUsername.isEmpty() || newEmail.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Username and Email are required").showAndWait();
            return;
        }

        boolean ok = User_Dol.edit_user(u, newUsername, newEmail, newPhone, newAddress);
        if (ok) {
            new Alert(Alert.AlertType.INFORMATION, "Profile updated").showAndWait();
        } else {
            new Alert(Alert.AlertType.ERROR, "Update failed. Username or Email may already exist.").showAndWait();
        }
    }

    @FXML
    private void changePassword() {
        User u = User_Dol.getLoggedInUser();
        if (u == null) {
            new Alert(Alert.AlertType.WARNING, "Please login first").showAndWait();
            return;
        }
        String cur = txtCurrentPwd.getText();
        String nxt = txtNewPwd.getText();
        if (cur == null || cur.isEmpty() || nxt == null || nxt.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Enter both current and new password").showAndWait();
            return;
        }
        boolean ok = User_Dol.change_password(u, cur, nxt);
        if (ok) {
            txtCurrentPwd.clear();
            txtNewPwd.clear();
            new Alert(Alert.AlertType.INFORMATION, "Password changed").showAndWait();
        } else {
            new Alert(Alert.AlertType.ERROR, "Current password is incorrect").showAndWait();
        }
    }

    @FXML
    private void logout() {
        try {
            // Clear logged-in user in the data layer
            User_Dol.logout();

            if (root != null && root.getScene() != null) {
                Stage current = (Stage) root.getScene().getWindow();
                // Close current home window
                current.close();

                // Start a fresh login window
                JavaFXApplication1 app = new JavaFXApplication1();
                Stage newStage = new Stage();
                app.start(newStage);
            }
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Failed to logout: " + ex.getMessage()).showAndWait();
        }
    }


}
