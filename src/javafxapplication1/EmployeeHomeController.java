package javafxapplication1;

import bullet_club.entity.Product;
import bullet_club.entity.Order;
import bullet_club.entity.Order_Details;
import bullet_club.model.Dol.Product_Dol;
import bullet_club.model.Dol.Order_Dol;
import bullet_club.model.Dol.User_Dol;
import javafx.fxml.FXMLLoader;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;

public class EmployeeHomeController implements Initializable {
    @FXML
    private SplitPane rootSplit;
    @FXML
    private Label welcomeLabel;
    @FXML
    private ToggleButton darkModeToggle;
    // Products tab
    @FXML
    private Tab tabProducts;
    @FXML
    private TableView<Product> empProductsTable;
    @FXML
    private TableColumn<Product, String> pColNameE, pColCatE;
    @FXML
    private TableColumn<Product, Integer> pColPriceE, pColQtyE;
    @FXML
    private TableColumn<Product, Void> pColDescE;

    // Orders tab
    @FXML
    private Tab tabOrders;
    @FXML
    private TableView<Order> empOrdersTable;
    @FXML
    private TableColumn<Order, Integer> oColIdE, oColTotalE;
    @FXML
    private TableColumn<Order, String> oColStatusE;
    @FXML
    private TableColumn<Order, java.util.Date> oColDateE;
    @FXML
    private TableColumn<Order, Void> oColDetailsE;
    @FXML
    private TableColumn<Order, String> oColCustomerE;
    @FXML
    private TableColumn<Order, Void> oColEditStatus;
    private final java.util.Map<Integer, String> usernameCache = new java.util.HashMap<>();
    @FXML
    private TextField searchFieldE;
    @FXML
    private ComboBox<String> categoryFilterE;
    @FXML
    private TextField orderSearchFieldE;

    private FilteredList<Product> filteredProductsE;
    private FilteredList<Order> filteredOrdersE;

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
        setupProductsTable();
        setupCategoryFilterE();
        setupSearchE();

        setupOrdersTable();
        setupOrderSearchE();
        loadProducts();
        loadOrders();
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
            new Alert(Alert.AlertType.ERROR, "Failed to load profile: " + ex.getMessage()).showAndWait();
        }
    }

    private void setupProductsTable() {
        pColNameE.setCellValueFactory(new PropertyValueFactory<>("name"));
        pColCatE.setCellValueFactory(new PropertyValueFactory<>("category"));
        pColPriceE.setCellValueFactory(new PropertyValueFactory<>("price"));
        pColQtyE.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // Show description button
        pColDescE.setSortable(false);
        pColDescE.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Desc");

            {
                btn.setOnAction(e -> {
                    Product p = getTableView().getItems().get(getIndex());
                    String text = (p.getDescription() == null || p.getDescription().isBlank())
                            ? "No description" : p.getDescription();
                    Alert a = new Alert(Alert.AlertType.INFORMATION, text);
                    a.setTitle("Description - " + p.getName());
                    a.setHeaderText(null);
                    a.showAndWait();
                });
            }

            @Override
            protected void updateItem(Void it, boolean empty) {
                super.updateItem(it, empty);
                setGraphic(empty ? null : btn);
                setText(null);
            }
        });
    }

    private void loadProducts() {
        List<Product> list = new Product_Dol().showAllProducts();
        filteredProductsE = new FilteredList<>(FXCollections.observableArrayList(list), p -> true);
        empProductsTable.setItems(filteredProductsE);
        applyProductFilterE(searchFieldE != null ? searchFieldE.getText() : "");
    }

    private void setupSearchE() {
        if (searchFieldE != null) {
            searchFieldE.textProperty().addListener((obs, oldV, newV) -> applyProductFilterE(newV));
        }
    }

    private void setupCategoryFilterE() {
        if (categoryFilterE == null) {
            return;
        }
        var cats = new Product_Dol().getAllCategories();
        categoryFilterE.getItems().clear();
        categoryFilterE.getItems().add("All");
        categoryFilterE.getItems().addAll(cats);
        categoryFilterE.getSelectionModel().selectFirst();
        categoryFilterE.valueProperty().addListener((obs, ov, nv)
                -> applyProductFilterE(searchFieldE != null ? searchFieldE.getText() : "")
        );
    }

    private void applyProductFilterE(String text) {
        if (filteredProductsE == null) {
            return;
        }
        final String q = text == null ? "" : text.toLowerCase().trim();
        final String selected = (categoryFilterE == null || categoryFilterE.getValue() == null)
                ? "All" : categoryFilterE.getValue();

        filteredProductsE.setPredicate(p -> {
            // category filter
            boolean categoryOk = "All".equals(selected)
                    || (p.getCategory() != null && p.getCategory().equalsIgnoreCase(selected));
            if (!categoryOk) {
                return false;
            }

            // text filter
            if (q.isEmpty()) {
                return true;
            }
            String name = p.getName() == null ? "" : p.getName().toLowerCase();
            String desc = p.getDescription() == null ? "" : p.getDescription().toLowerCase();
            return name.contains(q) || desc.contains(q);
        });
    }

    private void setupOrderSearchE() {
        if (orderSearchFieldE != null) {
            orderSearchFieldE.textProperty().addListener((obs, oldV, newV) -> applyOrderFilterE(newV));
        }
    }

    private void applyOrderFilterE(String text) {
        if (filteredOrdersE == null) {
            return;
        }
        final String q = text == null ? "" : text.toLowerCase().trim();

        filteredOrdersE.setPredicate(o -> {
            if (o == null) {
                return false;
            }
            if (q.isEmpty()) {
                return true;
            }
            // match by order id
            String idStr = String.valueOf(o.getId());
            if (idStr.contains(q)) {
                return true;
            }
            // match by customer username
            int uid = o.getCustomerUserId();
            String name = usernameCache.get(uid);
            if (name == null) {
                name = new Order_Dol().getUsernameById(uid);
                usernameCache.put(uid, name);
            }
            String nm = name == null ? "" : name.toLowerCase();
            return nm.contains(q);
        });
    }

    @FXML
    private void handleRefreshProducts() {
        loadProducts();
    }

    private void setupOrdersTable() {
        oColIdE.setCellValueFactory(new PropertyValueFactory<>("id"));
        oColDateE.setCellValueFactory(new PropertyValueFactory<>("date"));
        oColStatusE.setCellValueFactory(new PropertyValueFactory<>("status"));
        oColTotalE.setCellValueFactory(new PropertyValueFactory<>("total_Amount"));
        oColCustomerE.setCellValueFactory(cd -> {
            int uid = cd.getValue().getCustomerUserId();
            String name = usernameCache.get(uid);
            if (name == null) {
                name = new Order_Dol().getUsernameById(uid);
                usernameCache.put(uid, name);
            }
            return new ReadOnlyStringWrapper(name == null ? "" : name);
        });
        oColDetailsE.setSortable(false);
        oColDetailsE.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Details");

            {
                btn.setOnAction(e -> {
                    Order o = getTableView().getItems().get(getIndex());
                    showOrderDetails(o);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
                setText(null);
            }
        });
        oColEditStatus.setSortable(false);
        oColEditStatus.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> combo = new ComboBox<>(
                    FXCollections.observableArrayList("Pending", "onTheWay", "delivered")
            );

            {
                combo.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx < 0 || idx >= getTableView().getItems().size()) {
                        return;
                    }
                    Order o = getTableView().getItems().get(idx);
                    String newStatus = combo.getValue();
                    var user = User_Dol.getLoggedInUser();

                    boolean ok = new Order_Dol().editStatus(o.getId(), newStatus, user);
                    if (ok) {
                        o.setStatus(newStatus);
                        // If delivered, hide the drop-down
                        if ("delivered".equalsIgnoreCase(newStatus)) {
                            setGraphic(null);
                        }
                        getTableView().refresh();
                    } else {
                        // Revert selection on failure
                        combo.setValue(o.getStatus());
                        new Alert(Alert.AlertType.ERROR, "Failed to update status").showAndWait();
                    }
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
                Order o = getTableView().getItems().get(getIndex());
                if (o == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                // Show ComboBox only if not delivered; otherwise remove it
                if ("delivered".equalsIgnoreCase(o.getStatus())) {
                    setGraphic(null);
                    setText(null);
                } else {
                    combo.setValue(o.getStatus());
                    setGraphic(combo);
                    setText(null);
                }
            }
        });
    }

    private void showOrderDetails(Order o) {
        if (o == null || o.getItems() == null || o.getItems().isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "No details for order #" + (o == null ? "?" : o.getId())).showAndWait();
            return;
        }
        int total = 0;
        StringBuilder sb = new StringBuilder("Items for order #").append(o.getId()).append(":\n");
        for (Order_Details d : o.getItems()) {
            int line = d.getQuantity() * d.getPrice();
            total += line;
            sb.append("- ").append(d.getItem().getName())
                    .append("  x").append(d.getQuantity())
                    .append("  $ ").append(d.getPrice())
                    .append(" = ").append(line).append("\n");
        }
        sb.append("\nTotal (lines sum): ").append(total);
        new Alert(Alert.AlertType.INFORMATION, sb.toString()).showAndWait();
    }

    private void loadOrders() {
        var user = User_Dol.getLoggedInUser();
        // If showAllOrders() requires employee role, make sure the logged-in user is an employee.
        var list = new Order_Dol().showAllOrders(user);
        filteredOrdersE = new FilteredList<>(FXCollections.observableArrayList(list), o -> true);
        empOrdersTable.setItems(filteredOrdersE);
        applyOrderFilterE(orderSearchFieldE != null ? orderSearchFieldE.getText() : "");
    }

    @FXML
    private void handleRefreshOrders() {
        loadOrders();
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

}
