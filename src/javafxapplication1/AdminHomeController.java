
package javafxapplication1;

import bullet_club.entity.User;
import bullet_club.model.Dol.Admin_Dol;
import bullet_club.model.Dol.User_Dol;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


public class AdminHomeController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private SplitPane rootSplit;

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, Integer> uColId;

    @FXML
    private TableColumn<User, String> uColUsername;

    @FXML
    private TableColumn<User, String> uColRole;

    @FXML
    private TableColumn<User, String> uColAddress;

    @FXML
    private TableColumn<User, String> uColEmail;

    @FXML
    private TableColumn<User, String> uColPhone;

    @FXML
    private TableColumn<User, Void> uColDelete;

    @FXML
    private TableColumn<User, Void> uColChangeRole;

    @FXML
    private ToggleButton darkModeToggle;

    private final Admin_Dol adminDol = new Admin_Dol();

    private Node savedUsersContent;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        var user = User_Dol.getLoggedInUser();
        if (welcomeLabel != null) {
            if (user != null) {
                welcomeLabel.setText("Welcome back " + user.getRole() + " " + user.getUsername());
            } else {
                welcomeLabel.setText("Welcome back");
            }
        }

        if (rootSplit != null && rootSplit.getItems().size() > 1) {
            savedUsersContent = rootSplit.getItems().get(1);
        }

        if (usersTable != null) {
            setupUserColumns();
            setupDeleteColumn();
            setupChangeRoleColumn();
            loadUsers();
        }

        if (darkModeToggle != null) {
            boolean dark = JavaFXApplication1.isDarkMode();
            darkModeToggle.setSelected(dark);
            darkModeToggle.setText(dark ? "🌙 Dark" : "☀️ Light");
        }
        applyTheme();
    }

    @FXML
    private void openProfile() {
        try {
            if (rootSplit != null && rootSplit.getItems().size() > 1) {
                rootSplit.getProperties().put("prevBottom", rootSplit.getItems().get(1));
                Node bottom = FXMLLoader.load(getClass().getResource("userProfile.fxml"));
                rootSplit.getItems().set(1, bottom);
            }
        } catch (Exception ex) {
            new Alert(AlertType.ERROR, "Failed to load profile: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML
    private void openAddUserForm() {
        if (rootSplit == null) {
            return;
        }
        if (savedUsersContent == null && rootSplit.getItems().size() > 1) {
            savedUsersContent = rootSplit.getItems().get(1);
        }
        rootSplit.getItems().set(1, buildAddUserForm());
    }

    private void setupUserColumns() {
        uColId.setCellValueFactory(new PropertyValueFactory<>("id"));
        uColUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        uColRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        uColAddress.setCellValueFactory(new PropertyValueFactory<>("add"));
        uColEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        uColPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
    }

    private void setupDeleteColumn() {
        if (uColDelete == null) {
            return;
        }
        uColDelete.setSortable(false);
        uColDelete.setCellFactory(col -> new TableCell<User, Void>() {
            private final Button btn = new Button("Delete");

            {
                btn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    if (u == null) {
                        return;
                    }
                    adminDol.remove_user(u);
                    loadUsers();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                User rowUser = getTableView().getItems().get(getIndex());
                User logged = User_Dol.getLoggedInUser();
                boolean isSelf = logged != null && rowUser.getId() == logged.getId();
                setGraphic(isSelf ? null : btn);
                setText(null);
            }
        });
    }

    private void setupChangeRoleColumn() {
        if (uColChangeRole == null) {
            return;
        }
        uColChangeRole.setSortable(false);
        uColChangeRole.setCellFactory(col -> new TableCell<User, Void>() {
            private final Button btn = new Button("Change Role");

            {
                btn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    if (u == null) {
                        return;
                    }
                    var roles = FXCollections.observableArrayList("employee", "manager");
                    String current = roles.contains(u.getRole()) ? u.getRole() : "employee";
                    ChoiceDialog<String> dialog = new ChoiceDialog<>(current, roles);
                    dialog.setTitle("Change Role");
                    dialog.setHeaderText("Change role for user #" + u.getId() + " (" + u.getUsername() + ")");
                    dialog.setContentText("New role:");

                    dialog.showAndWait().ifPresent(newRole -> {
                        if (newRole == null || newRole.equalsIgnoreCase(u.getRole())) {
                            return;
                        }
                        boolean ok = User_Dol.changeRole(u, newRole);
                        if (ok) {
                            loadUsers();
                        } else {
                            new Alert(AlertType.ERROR, "Failed to change role").showAndWait();
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                User u = getTableView().getItems().get(getIndex());
                String role = u.getRole();
                boolean isEmployee = role != null && role.equalsIgnoreCase("employee");
                setGraphic(isEmployee ? btn : null);
                setText(null);
            }
        });
    }

    private void loadUsers() {
        if (usersTable == null) {
            return;
        }
        var list = adminDol.show_all();
        ObservableList<User> items = FXCollections.observableArrayList(list);
        usersTable.setItems(items);
    }

    @FXML
    private void handleRefreshUsers() {
        loadUsers();
    }

    @FXML
    private void toggleDarkMode() {
        if (darkModeToggle == null) {
            return;
        }
        boolean dark = darkModeToggle.isSelected();
        JavaFXApplication1.setDarkMode(dark);
        darkModeToggle.setText(dark ? "🌙 Dark" : "☀️ Light");
        applyTheme();
    }

    private void applyTheme() {
        if (rootSplit == null) {
            return;
        }
        if (JavaFXApplication1.isDarkMode()) {
            if (!rootSplit.getStyleClass().contains("dark-theme")) {
                rootSplit.getStyleClass().add("dark-theme");
            }
        } else {
            rootSplit.getStyleClass().remove("dark-theme");
        }
    }

    private BorderPane buildAddUserForm() {
        BorderPane root = new BorderPane();

        Button btnBack = new Button("Back");
        Label title = new Label("Add User");
        HBox top = new HBox(10, btnBack, title);
        top.setPadding(new Insets(10));
        root.setTop(top);

        TextField tfUsername = new TextField();
        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("employee", "customer", "admin", "manager");
        roleBox.setPromptText("Select role");
        TextField tfPassword = new TextField();
        TextField tfAddress = new TextField();
        TextField tfEmail = new TextField();
        TextField tfPhone = new TextField();

        GridPane gp = new GridPane();
        gp.setHgap(10);
        gp.setVgap(8);
        gp.setPadding(new Insets(12));
        gp.addRow(0, new Label("Username"), tfUsername);
        gp.addRow(1, new Label("Role"), roleBox);
        gp.addRow(2, new Label("Password"), tfPassword);
        gp.addRow(3, new Label("Address"), tfAddress);
        gp.addRow(4, new Label("Email"), tfEmail);
        gp.addRow(5, new Label("Phone"), tfPhone);

        Button btnCreate = new Button("Create");
        VBox box = new VBox(12, gp, btnCreate);
        box.setPadding(new Insets(10));
        root.setCenter(box);

        btnBack.setOnAction(e -> {
            if (savedUsersContent != null) {
                rootSplit.getItems().set(1, savedUsersContent);
                loadUsers();
            }
        });

        btnCreate.setOnAction(e -> {
            String username = tfUsername.getText().trim();
            String role = roleBox.getValue() == null ? "" : roleBox.getValue().trim();
            String password = tfPassword.getText().trim();
            String address = tfAddress.getText().trim();
            String email = tfEmail.getText().trim();
            String phone = tfPhone.getText().trim();

            if (username.isEmpty() || role.isEmpty() || password.isEmpty()) {
                new Alert(AlertType.WARNING, "Username, role, and password are required").showAndWait();
                return;
            }

            User u = new User(0, username, role, password, address, email, phone);
            String res = adminDol.add_user(u);
            if ("accepted".equals(res)) {
                new Alert(AlertType.INFORMATION, "User added").showAndWait();
                if (savedUsersContent != null) {
                    rootSplit.getItems().set(1, savedUsersContent);
                }
                loadUsers();
            } else {
                new Alert(AlertType.ERROR, "Add user failed: " + res).showAndWait();
            }
        });

        return root;
    }
}
