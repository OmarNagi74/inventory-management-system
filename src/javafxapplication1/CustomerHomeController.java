package javafxapplication1;

import bullet_club.entity.Product;
import bullet_club.entity.Order_Details;
import bullet_club.model.Dol.Product_Dol;
import bullet_club.model.Dol.User_Dol;
import javafx.fxml.FXMLLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.transformation.FilteredList;
import bullet_club.entity.Order;
import bullet_club.model.Dol.Order_Dol;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;

public class CustomerHomeController implements Initializable {
    @FXML
    private SplitPane rootSplit;
    @FXML
    private Label welcomeLabel;
    @FXML
    private ToggleButton darkModeToggle;
    @FXML
    private TabPane customerTabs;
    @FXML
    private Tab tabProducts, tabCart, tabOrders;

    // Products table
    @FXML
    private TableView<Product> productsTable;
    @FXML
    private TableColumn<Product, String> pColName, pColCat;
    @FXML
    private TableColumn<Product, Integer> pColPrice, pColQty;
    @FXML
    private TableColumn<Product, Void> pColDesc, pColAdd;

    // Cart table
    @FXML
    private TableView<Order_Details> cartTable;
    @FXML
    private TableColumn<Order_Details, String> cColName;
    @FXML
    private TableColumn<Order_Details, Integer> cColPrice, cColQty, cColTotal;
    @FXML
    private Label cartTotalLabel;
    @FXML
    private Button orderBtn;
    // Search
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> categoryFilter;

    // Orders table
    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private TableColumn<Order, Integer> oColId, oColTotal;
    @FXML
    private TableColumn<Order, String> oColStatus;
    @FXML
    private TableColumn<Order, java.util.Date> oColDate;
    @FXML
    private TableColumn<Order, Void> oColDetails;

    // Cart desc column
    @FXML
    private TableColumn<Order_Details, Void> cColDesc;

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
        setupCategoryFilter();
        setupCartTable();
        setupOrdersTable();      // new
        setupCartDescColumn();   // new
        setupSearch();           // new

        customerTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == tabProducts) {
                loadProducts();
            } else if (newTab == tabCart) {
                loadCart();
            } else if (newTab == tabOrders) {
                loadOrders();
            }
        });
        
        loadProducts();
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
                // save previous bottom to restore on Back
                rootSplit.getProperties().put("prevBottom", rootSplit.getItems().get(1));
                Node bottom = FXMLLoader.load(getClass().getResource("userProfile.fxml"));
                rootSplit.getItems().set(1, bottom);
            }
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Failed to load profile: " + ex.getMessage()).showAndWait();
        }
    }

    private void setupProductsTable() {
        pColName.setCellValueFactory(new PropertyValueFactory<>("name"));
        pColCat.setCellValueFactory(new PropertyValueFactory<>("category"));
        pColPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        pColQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        pColDesc.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("show description");

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

        pColAdd.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("add to cart");

            {
                btn.setOnAction(e -> {
                    Product p = getTableView().getItems().get(getIndex());
                    int max = Math.max(0, p.getQuantity());
                    if (max == 0) {
                        new Alert(Alert.AlertType.INFORMATION, "Out of stock").showAndWait();
                        return;
                    }
                    Spinner<Integer> sp = new Spinner<>(1, max, 1, 1);
                    sp.setEditable(false);
                    Dialog<ButtonType> dlg = new Dialog<>();
                    dlg.setTitle("Add to cart - " + p.getName());
                    dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                    dlg.getDialogPane().setContent(new HBox(10, new Label("Quantity:"), sp));
                    dlg.showAndWait().ifPresent(bt -> {
                        if (bt == ButtonType.OK) {
                            int qty = sp.getValue();
                            User_Dol.addToCart(p, qty);
                            new Alert(Alert.AlertType.INFORMATION, "Added " + qty + " to cart").showAndWait();
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void it, boolean empty) {
                super.updateItem(it, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Product p = getTableView().getItems().get(getIndex());
                btn.setDisable(p.getQuantity() <= 0);
                setGraphic(btn);
                setText(null);
            }
        });
    }

    private void setupCartTable() {
        cColName.setCellValueFactory(cd
                -> new ReadOnlyStringWrapper(cd.getValue().getItem().getName()));
        cColPrice.setCellValueFactory(cd
                -> new ReadOnlyObjectWrapper<>(cd.getValue().getPrice()));
        cColQty.setCellValueFactory(cd
                -> new ReadOnlyObjectWrapper<>(cd.getValue().getQuantity()));
        cColTotal.setCellValueFactory(cd
                -> new ReadOnlyObjectWrapper<>((int) cd.getValue().total()));
    }

    private void loadProducts() {
        var list = new Product_Dol().showAllProducts();
        filteredProducts = new FilteredList<>(FXCollections.observableArrayList(list), p -> true);
        productsTable.setItems(filteredProducts);
        applyProductFilter(searchField.getText());
    }

    private void loadCart() {
        List<Order_Details> list = User_Dol.getCart();
        ObservableList<Order_Details> items = FXCollections.observableArrayList(list);
        cartTable.setItems(items);
        int sum = list.stream().mapToInt(od -> (int) od.total()).sum();
        cartTotalLabel.setText("Total: " + sum);
    }

    @FXML
    private void placeOrder() {
        var list = User_Dol.getCart();
        if (list.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Cart is empty").showAndWait();
            return;
        }
        var user = User_Dol.getLoggedInUser();
        if (user == null) {
            new Alert(Alert.AlertType.WARNING, "Please login first").showAndWait();
            return;
        }
        // Build order
        Order order = new Order(0, "Pending", 0, new java.util.Date(), new java.util.ArrayList<>(list));
        String res = new Order_Dol().addOrder(order, user);
        if (res.startsWith("`order` added")) {
            new Alert(Alert.AlertType.INFORMATION, "Order placed. Total: " + order.getTotal_Amount()).showAndWait();
            User_Dol.clearCart();
            loadCart();
            loadOrders();
            customerTabs.getSelectionModel().select(tabOrders);
        } else {
            new Alert(Alert.AlertType.ERROR, res).showAndWait();
        }
    }
    private FilteredList<Product> filteredProducts;

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldV, newV) -> applyProductFilter(newV));
    }

    private void setupCartDescColumn() {
        if (cColDesc == null) {
            return;
        }
        cColDesc.setSortable(false);
        cColDesc.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Desc");

            {
                btn.setOnAction(e -> {
                    Order_Details od = getTableView().getItems().get(getIndex());
                    var p = od.getItem();
                    String text = (p.getDescription() == null || p.getDescription().isBlank())
                            ? "No description" : p.getDescription();
                    new Alert(Alert.AlertType.INFORMATION, text).showAndWait();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
                setText(null);
            }
        });
    }

    private void setupOrdersTable() {
        if (ordersTable == null) {
            return;
        }
        oColId.setCellValueFactory(new PropertyValueFactory<>("id"));
        oColDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        oColStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        oColTotal.setCellValueFactory(new PropertyValueFactory<>("total_Amount"));
        
        if (oColDetails != null) {
            oColDetails.setSortable(false);
            oColDetails.setCellFactory(col -> new TableCell<>() {
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
        }
        
    }

    private void showOrderDetails(Order o) {
        if (o == null || o.getItems() == null || o.getItems().isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "No details for order #" + (o == null ? "?" : o.getId())).showAndWait();
            return;
        }
        int total = 0;
        StringBuilder sb = new StringBuilder("Items for order #").append(o.getId()).append(":\n");
        for (var d : o.getItems()) {
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
        if (user == null) {
            ordersTable.setItems(FXCollections.observableArrayList());
            return;
        }
        var list = new Order_Dol().showOrdersByCustomerId(user.getId(), user);
        ordersTable.setItems(FXCollections.observableArrayList(list));
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

    private void setupCategoryFilter() {
        var cats = new Product_Dol().getAllCategories();
        categoryFilter.getItems().clear();
        categoryFilter.getItems().add("All");
        categoryFilter.getItems().addAll(cats);
        categoryFilter.getSelectionModel().selectFirst();
        categoryFilter.valueProperty().addListener((obs, ov, nv) -> applyProductFilter(searchField.getText()));
    }

    private void applyProductFilter(String text) {
        if (filteredProducts == null) {
            return;
        }
        final String q = text == null ? "" : text.toLowerCase().trim();
        final String selected = (categoryFilter == null || categoryFilter.getValue() == null)
                ? "All" : categoryFilter.getValue();

        filteredProducts.setPredicate(p -> {
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
}
