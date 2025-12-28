package javafxapplication1;

import bullet_club.entity.Purchase_Order;
import bullet_club.model.Dol.Purchase_Order_Dol;
import bullet_club.model.Dol.User_Dol;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;
import javafx.scene.control.ChoiceDialog;
import java.util.Arrays;
import java.util.List;

import bullet_club.entity.Supplier;
import bullet_club.entity.Product;
import bullet_club.entity.Purchase_Details;
import bullet_club.entity.Purchase_Order;
import bullet_club.entity.User;
import bullet_club.model.Dol.Supplier_Dol;
import bullet_club.model.Dol.Product_Dol;
import bullet_club.model.Dol.Purchase_Order_Dol;
import bullet_club.model.Dol.User_Dol;
import bullet_club.model.Dol.Order_Dol;
import bullet_club.model.Dol.Order_Dol.MonthlyProfit;
import bullet_club.model.Dol.Order_Dol.YearlyProfit;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import bullet_club.entity.Notfication;
import bullet_club.model.Dol.Notfication_Dol;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class ManagerHomeController implements Initializable {
@FXML
private Label welcomeLabel;
@FXML
private SplitPane rootSplit;
@FXML
private ToggleButton darkModeToggle;
    @FXML
    private TableView<Purchase_Order> purchaseTable;
    @FXML
    private TableColumn<Purchase_Order, Integer> colId;
    @FXML
    private TableColumn<Purchase_Order, java.util.Date> colDate;
    @FXML
    private TableColumn<Purchase_Order, String> colStatus;
    @FXML
    private TableColumn<Purchase_Order, Integer> colTotal;
    @FXML
    private TableColumn<Purchase_Order, String> colSupplier;
    @FXML
    private TableColumn<Purchase_Order, Void> colEdit;
    @FXML
    private TableColumn<Purchase_Order, Void> colDetails;
//
    @FXML
    private Tab ordersTab;
    @FXML
    private AnchorPane ordersContent;
    private Node savedOrdersContent; // to restore table view

    @FXML
    private void openOrderForm() {
        savedOrdersContent = ordersTab.getContent();
        ordersTab.setContent(buildOrderForm());
    }
//
// Products tab table + columns
    @FXML
    private TableView<Product> productsTable;
    @FXML
    private TableColumn<Product, Integer> pColId;
    @FXML
    private TableColumn<Product, String> pColName;
    @FXML
    private TableColumn<Product, String> pColCat;
    @FXML
    private TableColumn<Product, Integer> pColPrice;
    @FXML
    private TableColumn<Product, Integer> pColStock;
    @FXML
    private TableColumn<Product, Integer> pColReorder;
    @FXML
    private TableColumn<Product, Void> pColDesc;
    @FXML
    private TableColumn<Product, Void> pColAction;
    @FXML
    private Tab productsTab;
    private Node savedProductsContent;
    // Suppliers tab
    @FXML
    private TableView<Supplier> suppliersTable;
    @FXML
    private TableColumn<Supplier, Integer> sColId;
    @FXML
    private TableColumn<Supplier, String> sColName;
    @FXML
    private TableColumn<Supplier, String> sColEmail;
    @FXML
    private TableColumn<Supplier, String> sColPhone;
    @FXML
    private TableColumn<Supplier, Void> sColEdit;
    @FXML
    private TableColumn<Supplier, Void> sColDelete;
    @FXML
    private Tab suppliersTab;
    private Node savedSuppliersContent;

    // Reports tab
    @FXML
    private Tab reportsTab;
    @FXML
    private BarChart<String, Number> ordersChart;
    @FXML
    private CategoryAxis ordersChartXAxis;
    @FXML
    private NumberAxis ordersChartYAxis;
    @FXML
    private ComboBox<Integer> yearCombo;
    @FXML
    private Button btnMonthlyView;
    @FXML
    private Button btnYearlyView;

    private boolean yearlyMode = false;

// Notifications tab
    @FXML
    private TableView<Notfication> notifTable;
    @FXML
    private TableColumn<Notfication, Integer> nColId;
    @FXML
    private TableColumn<Notfication, java.time.LocalDate> nColDate;
    @FXML
    private TableColumn<Notfication, String> nColMsg;
    @FXML
    private TableColumn<Notfication, String> nColProduct;

    @FXML
    private TableColumn<Notfication, String> nColStatus;
    @FXML
    private TableColumn<Notfication, Void> nColDetails;
    @FXML
    private TableColumn<Notfication, Void> nColClose;
    private final Purchase_Order_Dol poDol = new Purchase_Order_Dol();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total_Amount"));
        if (colSupplier != null) {
            Supplier_Dol supplierDol = new Supplier_Dol();
            colSupplier.setCellValueFactory(cd -> {
                Purchase_Order po = cd.getValue();
                String name = "";
                if (po != null) {
                    int sid = po.getSupplierId();
                    if (sid > 0) {
                        String n = supplierDol.getSupplierNameById(sid);
                        name = n != null ? n : String.valueOf(sid);
                    }
                }
                return new ReadOnlyStringWrapper(name);
            });
        }

        var user = User_Dol.getLoggedInUser();
        if (welcomeLabel != null) {
    if (user != null) {
        welcomeLabel.setText("Welcome back " + user.getRole() + " " + user.getUsername());
    } else {
        welcomeLabel.setText("Welcome back");
    }
}
        var list = poDol.showAllPurchaseOrders(user);
        ObservableList<Purchase_Order> items = FXCollections.observableArrayList(list);
        setupEditColumn();
        setupDetailsColumn();
        purchaseTable.setItems(items);
        if (productsTable != null) {
            setupProductColumns();
            setupProductActionColumn();
            loadProducts();
        }
        if (pColDesc != null) {
            setupProductDescColumn();
        }
        if (suppliersTable != null) {
            setupSupplierColumns();
            setupSupplierEditColumn();
            setupSupplierDeleteColumn();
            loadSuppliers();
            setupNotifActionColumns();
        }
        if (notifTable != null) {
            setupNotifColumns();
            loadNotifs();
        }

        if (ordersChart != null) {
            loadReportsChart();
        }
        if (yearCombo != null) {
            yearCombo.setOnAction(e -> loadReportsChart());
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

    private void setupNotifActionColumns() {
        if (nColDetails != null) {
            nColDetails.setSortable(false);
            nColDetails.setCellFactory(col -> new TableCell<Notfication, Void>() {
                private final Button btn = new Button("Details");

                {
                    btn.setOnAction(e -> {
                        Notfication n = getTableView().getItems().get(getIndex());
                        Product p = n.getItem();
                        String text = (p == null)
                                ? "No product attached."
                                : "Name: " + p.getName()
                                + "\nCategory: " + p.getCategory()
                                + "\nPrice: " + p.getPrice()
                                + "\nStock: " + p.getQuantity()
                                + "\nReorder: " + p.getReorder_level()
                                + "\nDescription: " + p.getDescription();
                        Alert a = new Alert(Alert.AlertType.INFORMATION, text);
                        a.setTitle("Product Details");
                        a.setHeaderText("Notification #" + n.getId());
                        a.showAndWait();
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

        if (nColClose != null) {
            nColClose.setSortable(false);
            nColClose.setCellFactory(col -> new TableCell<Notfication, Void>() {
                private final Button btn = new Button("Close");

                {
                    btn.setOnAction(e -> {
                        Notfication n = getTableView().getItems().get(getIndex());
                        boolean ok = new Notfication_Dol().editNotificationStatus(n.getId(), "closed");
                        if (ok) {
                            loadNotifs();
                        } else {
                            new Alert(Alert.AlertType.ERROR, "Failed to close notification").showAndWait();
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        // Only show when status is 'opened'
                        Notfication n = getTableView().getItems().get(getIndex());
                        boolean opened = n.getStatus() != null && n.getStatus().equalsIgnoreCase("opened");
                        btn.setVisible(opened);
                        btn.setManaged(opened);
                        setGraphic(btn);
                        setText(null);
                    }
                }
            });
        }
    }

    private void setupEditColumn() {
        colEdit.setSortable(false);
        colEdit.setCellFactory(col -> new TableCell<Purchase_Order, Void>() {
            private final Button btn = new Button("Edit");

            {
                btn.setOnAction(e -> {
                    Purchase_Order po = getTableView().getItems().get(getIndex());
                    handleEdit(po);
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
                Purchase_Order po = getTableView().getItems().get(getIndex());
                boolean isDelivered = po.getStatus() != null && po.getStatus().equalsIgnoreCase("delivered");
                setGraphic(isDelivered ? null : btn);
                setText(null);
            }
        });
    }

    private void handleEdit(Purchase_Order po) {
        var user = User_Dol.getLoggedInUser();
        if (user == null) {
            new Alert(AlertType.WARNING, "Please login first.").showAndWait();
            return;
        }

        List<String> choices = Arrays.asList("Pending", "Approved", "Delivered");
        String current = choices.contains(po.getStatus()) ? po.getStatus() : "Pending";

        ChoiceDialog<String> dialog = new ChoiceDialog<>(current, choices);
        dialog.setTitle("Edit Purchase Order Status");
        dialog.setHeaderText("Change status for order #" + po.getId());
        dialog.setContentText("New status:");

        dialog.showAndWait().ifPresent(newStatus -> {
            if (!newStatus.equals(po.getStatus())) {
                boolean ok = poDol.editPurchaseOrderStatus(po.getId(), newStatus, user);
                if (ok) {
                    refreshTable();
                } else {
                    new Alert(AlertType.ERROR, "Failed to update status.").showAndWait();
                }
            }
        });
    }

    @FXML
    private void handleRefresh() {
        refreshTable();
    }

    private void refreshTable() {
        var user = User_Dol.getLoggedInUser();
        var list = poDol.showAllPurchaseOrders(user);
        purchaseTable.setItems(FXCollections.observableArrayList(list));
    }

    private void setupDetailsColumn() {
        colDetails.setSortable(false);
        colDetails.setCellFactory(col -> new TableCell<Purchase_Order, Void>() {
            private final Button btn = new Button("Details");

            {
                btn.setOnAction(e -> {
                    Purchase_Order po = getTableView().getItems().get(getIndex());
                    handleShowDetails(po);
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

    private void handleShowDetails(Purchase_Order po) {
        var list = poDol.getPurchaseDetailsByOrderId(po.getId());
        if (list == null || list.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "No details found for order #" + po.getId()).showAndWait();
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Items for order #").append(po.getId()).append(":\n");
        int total = 0;
        for (var d : list) {
            int line = d.getQuantity() * d.getPrice();
            total += line;
            sb.append("- ")
                    .append(d.getItem().getName())
                    .append("  x").append(d.getQuantity())
                    .append("  $ ").append(d.getPrice())
                    .append(" = ").append(line)
                    .append("\n");
        }
        sb.append("\nTotal (lines sum): ").append(total);

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Order #" + po.getId());
        a.setHeaderText("Purchase Order Details");
        a.setContentText(sb.toString());
        a.showAndWait();
    }

    private Node buildOrderForm() {
        BorderPane root = new BorderPane();

        // Top: Back + title
        Button btnBack = new Button("Back");
        Label title = new Label("New Purchase Order");
        HBox top = new HBox(10, btnBack, title);
        top.setPadding(new Insets(10));
        root.setTop(top);

        // Form content
        VBox form = new VBox(12);
        form.setPadding(new Insets(12));

        // Supplier selector
        ComboBox<Supplier> supplierBox = new ComboBox<>();
        supplierBox.setPrefWidth(320);
        Supplier_Dol supplierDol = new Supplier_Dol();
        supplierBox.getItems().setAll(supplierDol.getAllSuppliers());
        supplierBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Supplier s) {
                return s == null ? "" : s.getName();
            }

            @Override
            public Supplier fromString(String s) {
                return null;
            }
        });

        // Details list
        VBox detailsBox = new VBox(8);
        Button btnAddItem = new Button("Add item");
        btnAddItem.setOnAction(e -> detailsBox.getChildren().add(newDetailRow()));

        // Primary action
        Button btnCreate = new Button("Create order");
        btnCreate.setOnAction(e -> createOrder(supplierBox, detailsBox));

        form.getChildren().addAll(
                new HBox(10, new Label("Supplier:"), supplierBox),
                new Separator(),
                new Label("Items:"),
                detailsBox,
                btnAddItem,
                new Separator(),
                btnCreate
        );

        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true);
        root.setCenter(sp);

        btnBack.setOnAction(e -> {
            ordersTab.setContent(savedOrdersContent);
            handleRefresh();
        });
        return root;
    }

// Build one purchase detail row
    private HBox newDetailRow() {
        Product_Dol productDol = new Product_Dol();

        RadioButton rbExisting = new RadioButton("Existing");
        RadioButton rbNew = new RadioButton("New");
        ToggleGroup tg = new ToggleGroup();
        rbExisting.setToggleGroup(tg);
        rbNew.setToggleGroup(tg);
        rbExisting.setSelected(true);

        // Existing product pick
        ComboBox<Product> cbProduct = new ComboBox<>();
        cbProduct.setPrefWidth(250);
        cbProduct.getItems().setAll(productDol.showAllProducts());
        cbProduct.setConverter(new StringConverter<>() {
            @Override
            public String toString(Product p) {
                return p == null ? "" : p.getName();
            }

            @Override
            public Product fromString(String s) {
                return null;
            }
        });

        // New product fields (stacked vertically in a GridPane)
        TextField tfName = new TextField();
        tfName.setPromptText("name");
        TextField tfCat = new TextField();
        tfCat.setPromptText("category");
        TextField tfDesc = new TextField();
        tfDesc.setPromptText("description");
        TextField tfBasePrice = new TextField();
        tfBasePrice.setPromptText("base price");
        TextField tfReorder = new TextField();
        tfReorder.setPromptText("reorder");

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        int r = 0;
        grid.add(new Label("Name"), 0, r);
        grid.add(tfName, 1, r);
        r++;
        grid.add(new Label("Category"), 0, r);
        grid.add(tfCat, 1, r);
        grid.add(new Label("Base Price"), 2, r);
        grid.add(tfBasePrice, 3, r);
        r++;
        grid.add(new Label("Reorder"), 0, r);
        grid.add(tfReorder, 1, r);
        grid.add(new Label("Description"), 2, r);
        grid.add(tfDesc, 3, r);
        r++;

        Button btnApplyCollapse = new Button("Apply");

        grid.add(btnApplyCollapse, 3, r);

        TitledPane newProductPane = new TitledPane("New product details", grid);
        newProductPane.setExpanded(true);
        newProductPane.managedProperty().bind(newProductPane.visibleProperty());
        newProductPane.setVisible(false); // hidden unless "New" is selected

        btnApplyCollapse.setOnAction(e -> {
            // collapse after filling; values remain in the fields
            newProductPane.setExpanded(false);
        });

        // Detail quantity + line price
        TextField tfQty = new TextField();
        tfQty.setPromptText("qty");
        tfQty.setPrefWidth(60);
        TextField tfLinePrice = new TextField();
        tfLinePrice.setPromptText("line price");
        tfLinePrice.setPrefWidth(90);

        Button btnRemove = new Button("Remove");

        // Toggle visibility between existing product picker and new product form
        cbProduct.managedProperty().bind(cbProduct.visibleProperty());
        rbExisting.setOnAction(e -> {
            cbProduct.setVisible(true);
            newProductPane.setVisible(false);
        });
        rbNew.setOnAction(e -> {
            cbProduct.setVisible(false);
            newProductPane.setVisible(true);
            newProductPane.setExpanded(true);
        });

        // Product section stacked vertically (picker + collapsible form)
        VBox productSection = new VBox(6, new HBox(6, rbExisting, rbNew), cbProduct, newProductPane);

        HBox row = new HBox(10, productSection, new Label("Qty:"), tfQty, new Label("Price:"), tfLinePrice, btnRemove);
        row.setPadding(new Insets(6));

        // attach metadata to the row (no signature changes elsewhere)
        row.setUserData(new DetailMeta(
                rbExisting, cbProduct,
                tfName, tfCat, tfDesc,
                tfBasePrice, tfReorder,
                tfQty, tfLinePrice
        ));

        btnRemove.setOnAction(e -> ((VBox) row.getParent()).getChildren().remove(row));
        return row;
    }

// Holder for controls
    private static class DetailMeta {

        final RadioButton rbExisting;
        final ComboBox<Product> cbProduct;
        final TextField tfName, tfCat, tfDesc, tfBasePrice, tfReorder, tfQty, tfLinePrice;

        DetailMeta(RadioButton rbExisting, ComboBox<Product> cbProduct,
                TextField tfName, TextField tfCat, TextField tfDesc,
                TextField tfBasePrice, TextField tfReorder,
                TextField tfQty, TextField tfLinePrice) {
            this.rbExisting = rbExisting;
            this.cbProduct = cbProduct;
            this.tfName = tfName;
            this.tfCat = tfCat;
            this.tfDesc = tfDesc;
            this.tfBasePrice = tfBasePrice;
            this.tfReorder = tfReorder;
            this.tfQty = tfQty;
            this.tfLinePrice = tfLinePrice;
        }
    }

    private void createOrder(ComboBox<Supplier> supplierBox, VBox detailsBox) {
        Supplier supplier = supplierBox.getValue();
        if (supplier == null) {
            new Alert(Alert.AlertType.WARNING, "Choose a supplier").showAndWait();
            return;
        }

        ArrayList<Purchase_Details> details = new ArrayList<>();
        Product_Dol productDol = new Product_Dol();

        for (Node n : detailsBox.getChildren()) {
            if (!(n instanceof HBox)) {
                continue;
            }
            DetailMeta m = (DetailMeta) n.getUserData();
            int qty, linePrice;
            try {
                qty = Integer.parseInt(m.tfQty.getText().trim());
                linePrice = Integer.parseInt(m.tfLinePrice.getText().trim());
            } catch (Exception ex) {
                new Alert(Alert.AlertType.WARNING, "Qty/Price must be numbers").showAndWait();
                return;
            }

            if (m.rbExisting.isSelected()) {
                Product p = m.cbProduct.getValue();
                if (p == null) {
                    new Alert(Alert.AlertType.WARNING, "Pick a product").showAndWait();
                    return;
                }
                details.add(new Purchase_Details(qty, linePrice, p));
            } else {
                // create new product first
                int basePrice, reorder;
                try {
                    basePrice = Integer.parseInt(m.tfBasePrice.getText().trim());
                    reorder = Integer.parseInt(m.tfReorder.getText().trim());
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.WARNING, "New product price/reorder must be numbers").showAndWait();
                    return;
                }
                String name = m.tfName.getText().trim();
                String cat = m.tfCat.getText().trim();
                String desc = m.tfDesc.getText().trim();
                if (name.isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "New product name is required").showAndWait();
                    return;
                }

                Product newP = new Product(0, name, cat, desc, basePrice, qty /* initial stock */, reorder);
                String res = productDol.addProduct(newP);
                if (!"Product added".equals(res)) {
                    new Alert(Alert.AlertType.ERROR, "Add product failed: " + res).showAndWait();
                    return;
                }
                // ensure id is set via generated keys inside Product_Dol.addProduct
                details.add(new Purchase_Details(qty, linePrice, newP));
            }
        }

        if (details.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Add at least one item").showAndWait();
            return;
        }

        Purchase_Order po = new Purchase_Order(details);

        po.setStatus("Pending");

        User user = User_Dol.getLoggedInUser();
        int managerId = (user != null ? user.getId() : 0);
        String result = poDol.addPurchaseOrder(po, supplier.getId(), managerId, user);

        if (result.startsWith("Purchase order added")) {
            new Alert(Alert.AlertType.INFORMATION, "Order created").showAndWait();
            ordersTab.setContent(savedOrdersContent);
            handleRefresh();
        } else {
            new Alert(Alert.AlertType.ERROR, result).showAndWait();
        }
    }

    private void setupProductColumns() {
        pColId.setCellValueFactory(new PropertyValueFactory<>("id"));
        pColName.setCellValueFactory(new PropertyValueFactory<>("name"));
        pColCat.setCellValueFactory(new PropertyValueFactory<>("category"));
        pColPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        pColStock.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        pColReorder.setCellValueFactory(new PropertyValueFactory<>("reorder_level"));
    }

    private void loadProducts() {
        var list = new Product_Dol().showAllProducts();
        productsTable.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    private void handleRefreshProducts() {
        loadProducts();
    }

    private void setupProductActionColumn() {
        pColAction.setSortable(false);
        pColAction.setCellFactory(col -> new TableCell<Product, Void>() {
            private final Button btn = new Button("Adjust");

            {
                btn.setOnAction(e -> {
                    Product p = getTableView().getItems().get(getIndex());
                    openAdjustDialog(p);
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

// Dialog with Spinner and live delta text
    private void openAdjustDialog(Product p) {
        int currentQty = p.getQuantity();

        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Adjust Product - " + p.getName());
        ButtonType confirmType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(confirmType, ButtonType.CANCEL);

        // Quantity spinner + live delta label
        Spinner<Integer> qtySpinner = new Spinner<>(0, Integer.MAX_VALUE, currentQty, 1);
        qtySpinner.setEditable(false);
        Label qtyInfo = new Label();
        qtyInfo.setVisible(false);
        qtySpinner.valueProperty().addListener((obs, o, n) -> {
            int delta = n - currentQty;
            if (delta > 0) {
                qtyInfo.setText("You added " + delta + (delta == 1 ? " item" : " items"));
                qtyInfo.setVisible(true);
            } else if (delta < 0) {
                int k = -delta;
                qtyInfo.setText("You removed " + k + (k == 1 ? " item" : " items"));
                qtyInfo.setVisible(true);
            } else {
                qtyInfo.setVisible(false);
            }
        });

        // Editable product details (aside id)
        TextField nameField = new TextField(p.getName());
        TextField catField = new TextField(p.getCategory());
        TextArea descArea = new TextArea(p.getDescription());
        descArea.setPrefRowCount(3);
        TextField priceField = new TextField(String.valueOf(p.getPrice()));
        TextField reorderField = new TextField(String.valueOf(p.getReorder_level()));

        GridPane gp = new GridPane();
        gp.setHgap(10);
        gp.setVgap(8);
        gp.setPadding(new Insets(10));
        int r = 0;
        gp.add(new Label("Name"), 0, r);
        gp.add(nameField, 1, r);
        r++;
        gp.add(new Label("Category"), 0, r);
        gp.add(catField, 1, r);
        r++;
        gp.add(new Label("Price"), 0, r);
        gp.add(priceField, 1, r);
        r++;
        gp.add(new Label("Reorder"), 0, r);
        gp.add(reorderField, 1, r);
        r++;
        gp.add(new Label("Description"), 0, r);
        gp.add(descArea, 1, r);
        r++;
        gp.add(new Separator(), 0, r, 2, 1);
        r++;
        gp.add(new Label("Current stock: " + currentQty), 0, r);
        r++;
        gp.add(new HBox(10, new Label("New stock:"), qtySpinner), 0, r, 2, 1);
        r++;
        gp.add(qtyInfo, 0, r, 2, 1);

        dlg.getDialogPane().setContent(gp);

        dlg.showAndWait().ifPresent(bt -> {
            if (bt != confirmType) {
                return;
            }

            // Parse ints for details; validate
            int newPrice, newReorder;
            try {
                newPrice = Integer.parseInt(priceField.getText().trim());
                newReorder = Integer.parseInt(reorderField.getText().trim());
            } catch (Exception ex) {
                new Alert(Alert.AlertType.WARNING, "Price and Reorder must be numbers").showAndWait();
                return;
            }
            String newName = nameField.getText().trim();
            String newCat = catField.getText().trim();
            String newDesc = descArea.getText();

            boolean changedDetails
                    = !newName.equals(p.getName())
                    || !newCat.equals(p.getCategory())
                    || !String.valueOf(newPrice).equals(String.valueOf(p.getPrice()))
                    || !String.valueOf(newReorder).equals(String.valueOf(p.getReorder_level()))
                    || !String.valueOf(newDesc).equals(String.valueOf(p.getDescription()));

            int newQty = qtySpinner.getValue();
            boolean changedQty = newQty != currentQty;

            boolean ok = true;

            // Apply detail changes (aside id/quantity)
            if (changedDetails) {
                ok = new Product_Dol().editProduct(p.getId(), newName, newCat, newDesc, newPrice, newReorder);
                if (!ok) {
                    new Alert(Alert.AlertType.ERROR, "Failed to update product info").showAndWait();
                    return;
                }
            }

            // Apply quantity change (keep quantity logic as-is)
            if (changedQty) {
                ok = new Product_Dol().editQuantity(p.getId(), newQty);
                if (!ok) {
                    new Alert(Alert.AlertType.ERROR, "Failed to update quantity").showAndWait();
                    return;
                }
            }

            if (changedDetails || changedQty) {
                loadProducts();
            }
        });
    }

    private void setupProductDescColumn() {
        pColDesc.setSortable(false);
        pColDesc.setCellFactory(col -> new TableCell<Product, Void>() {
            private final Button btn = new Button("Desc");

            {
                btn.setOnAction(e -> {
                    Product p = getTableView().getItems().get(getIndex());
                    String desc = p.getDescription();
                    if (desc == null || desc.isBlank()) {
                        desc = "No description";
                    }
                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("Description - " + p.getName());
                    a.setHeaderText(null);
                    a.setContentText(desc);
                    a.showAndWait();
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

    @FXML
    private void openAddProductForm() {
        savedProductsContent = productsTab.getContent();
        productsTab.setContent(buildAddProductForm());
    }

    private Node buildAddProductForm() {
        BorderPane root = new BorderPane();

        Button btnBack = new Button("Back");
        Label title = new Label("Add Product");
        HBox top = new HBox(10, btnBack, title);
        top.setPadding(new Insets(10));
        root.setTop(top);

        TextField nameField = new TextField();
        TextField catField = new TextField();
        TextArea descArea = new TextArea();
        descArea.setPrefRowCount(3);
        TextField priceField = new TextField();
        TextField reorderField = new TextField();
        Spinner<Integer> qtySpinner = new Spinner<>(0, Integer.MAX_VALUE, 0, 1);

        GridPane gp = new GridPane();
        gp.setHgap(10);
        gp.setVgap(8);
        gp.setPadding(new Insets(12));
        int r = 0;
        gp.add(new Label("Name"), 0, r);
        gp.add(nameField, 1, r);
        r++;
        gp.add(new Label("Category"), 0, r);
        gp.add(catField, 1, r);
        r++;
        gp.add(new Label("Price"), 0, r);
        gp.add(priceField, 1, r);
        r++;
        gp.add(new Label("Reorder"), 0, r);
        gp.add(reorderField, 1, r);
        r++;
        gp.add(new Label("Description"), 0, r);
        gp.add(descArea, 1, r);
        r++;
        gp.add(new Label("Quantity"), 0, r);
        gp.add(qtySpinner, 1, r);
        r++;

        Button btnCreate = new Button("Create");
        VBox box = new VBox(12, gp, btnCreate);
        box.setPadding(new Insets(10));
        ScrollPane sp = new ScrollPane(box);
        sp.setFitToWidth(true);
        root.setCenter(sp);

        btnBack.setOnAction(e -> {
            productsTab.setContent(savedProductsContent);
            loadProducts();
        });

        btnCreate.setOnAction(e -> {
            String name = nameField.getText().trim();
            String cat = catField.getText().trim();
            String desc = descArea.getText();
            int price, reorder, qty = qtySpinner.getValue();
            try {
                price = Integer.parseInt(priceField.getText().trim());
                reorder = Integer.parseInt(reorderField.getText().trim());
            } catch (Exception ex) {
                new Alert(Alert.AlertType.WARNING, "Price/Reorder must be numbers").showAndWait();
                return;
            }
            if (name.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Name is required").showAndWait();
                return;
            }
            Product p = new Product(0, name, cat, desc, price, qty, reorder);
            String res = new Product_Dol().addProduct(p);
            if ("Product added".equals(res)) {
                new Alert(Alert.AlertType.INFORMATION, "Product created").showAndWait();
                productsTab.setContent(savedProductsContent);
                loadProducts();
            } else {
                new Alert(Alert.AlertType.ERROR, res).showAndWait();
            }
        });

        return root;
    }

    private void setupSupplierColumns() {
        sColId.setCellValueFactory(new PropertyValueFactory<>("id"));
        sColName.setCellValueFactory(new PropertyValueFactory<>("name"));
        sColEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        sColPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
    }

    private void loadSuppliers() {
        var list = new Supplier_Dol().getAllSuppliers();
        suppliersTable.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    private void handleRefreshSuppliers() {
        loadSuppliers();
    }

    private void setupSupplierEditColumn() {
        sColEdit.setSortable(false);
        sColEdit.setCellFactory(col -> new TableCell<Supplier, Void>() {
            private final Button btn = new Button("Edit");

            {
                btn.setOnAction(e -> {
                    Supplier s = getTableView().getItems().get(getIndex());
                    openEditSupplierDialog(s);
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

    private void setupSupplierDeleteColumn() {
        sColDelete.setSortable(false);
        sColDelete.setCellFactory(col -> new TableCell<Supplier, Void>() {
            private final Button btn = new Button("Delete");

            {
                btn.setOnAction(e -> {
                    Supplier s = getTableView().getItems().get(getIndex());
                    Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                            "Delete supplier \"" + s.getName() + "\"?");
                    a.showAndWait().ifPresent(bt -> {
                        if (bt == ButtonType.OK) {
                            boolean ok = new Supplier_Dol().deleteSupplier(s.getId());
                            if (ok) {
                                loadSuppliers();
                            } else {
                                new Alert(Alert.AlertType.ERROR, "Delete failed").showAndWait();
                            }
                        }
                    });
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

    private void openEditSupplierDialog(Supplier s) {
        TextField name = new TextField(s.getName());
        TextField email = new TextField(s.getEmail());
        TextField phone = new TextField(s.getPhone());
        GridPane gp = new GridPane();
        gp.setHgap(10);
        gp.setVgap(8);
        gp.setPadding(new Insets(10));
        gp.addRow(0, new Label("Name"), name);
        gp.addRow(1, new Label("Email"), email);
        gp.addRow(2, new Label("Phone"), phone);
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Edit Supplier #" + s.getId());
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.getDialogPane().setContent(gp);
        dlg.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                Supplier updated = new Supplier(s.getId(), phone.getText().trim(),
                        name.getText().trim(), email.getText().trim());
                boolean ok = new Supplier_Dol().updateSupplier(updated);
                if (ok) {
                    loadSuppliers();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Update failed").showAndWait();
                }
            }
        });
    }

    @FXML
    private void openAddSupplierForm() {
        savedSuppliersContent = suppliersTab.getContent();
        suppliersTab.setContent(buildAddSupplierForm());
    }

    private Node buildAddSupplierForm() {
        BorderPane root = new BorderPane();
        Button btnBack = new Button("Back");
        Label title = new Label("Add Supplier");
        HBox top = new HBox(10, btnBack, title);
        top.setPadding(new Insets(10));
        root.setTop(top);

        TextField name = new TextField();
        TextField email = new TextField();
        TextField phone = new TextField();
        name.setPromptText("name");
        email.setPromptText("email");
        phone.setPromptText("phone");

        GridPane gp = new GridPane();
        gp.setHgap(10);
        gp.setVgap(8);
        gp.setPadding(new Insets(12));
        gp.addRow(0, new Label("Name"), name);
        gp.addRow(1, new Label("Email"), email);
        gp.addRow(2, new Label("Phone"), phone);

        Button btnCreate = new Button("Create");
        VBox box = new VBox(12, gp, btnCreate);
        box.setPadding(new Insets(10));
        ScrollPane sp = new ScrollPane(box);
        sp.setFitToWidth(true);
        root.setCenter(sp);

        btnBack.setOnAction(e -> {
            suppliersTab.setContent(savedSuppliersContent);
            loadSuppliers();
        });

        btnCreate.setOnAction(e -> {
            if (name.getText().trim().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Name required").showAndWait();
                return;
            }
            Supplier s = new Supplier(0, phone.getText().trim(),
                    name.getText().trim(), email.getText().trim());
            String res = new Supplier_Dol().addSupplier(s);
            if (res.startsWith("Supplier added")) {
                new Alert(Alert.AlertType.INFORMATION, "Supplier created (id " + s.getId() + ")").showAndWait();
                suppliersTab.setContent(savedSuppliersContent);
                loadSuppliers();
            } else {
                new Alert(Alert.AlertType.ERROR, res).showAndWait();
            }
        });

        return root;
    }

    private void setupNotifColumns() {
        nColId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
        nColDate.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getDate()));
        nColMsg.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getMassage()));
        nColProduct.setCellValueFactory(cd -> {
            var item = cd.getValue().getItem();
            return new ReadOnlyStringWrapper(item == null ? "" : item.getName());
        });
        if (nColStatus != null) {
            nColStatus.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getStatus()));
        }
    }

    private void loadNotifs() {
        var list = new Notfication_Dol().showAll();
        notifTable.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    private void handleRefreshNotifs() {
        loadNotifs();
    }

    private void loadReportsChart() {
        if (yearlyMode) {
            loadYearlyChart();
        } else {
            loadMonthlyChart();
        }
    }

    private void loadMonthlyChart() {
        if (ordersChart == null) {
            return;
        }

        ordersChart.getData().clear();

        Order_Dol od = new Order_Dol();
        var data = od.getMonthlyProfit();

        if (data == null || data.isEmpty()) {
            if (yearCombo != null) {
                yearCombo.getItems().clear();
                yearCombo.setValue(null);
            }
            return;
        }

        ArrayList<Integer> years = new ArrayList<>();
        for (MonthlyProfit mp : data) {
            int y = mp.getYear();
            if (!years.contains(y)) {
                years.add(y);
            }
        }

        if (yearCombo != null) {
            yearCombo.getItems().setAll(years);
            yearCombo.setDisable(false);
        }

        if (ordersChartXAxis != null) {
            ordersChartXAxis.setLabel("Month");
        }

        Integer selectedYear = (yearCombo != null) ? yearCombo.getValue() : null;
        if (selectedYear == null || !years.contains(selectedYear)) {
            selectedYear = years.get(years.size() - 1); // default to last (likely latest)
            if (yearCombo != null) {
                yearCombo.setValue(selectedYear);
            }
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Total Amount (" + selectedYear + ")");

        for (MonthlyProfit mp : data) {
            if (mp.getYear() != selectedYear) {
                continue;
            }
            String label = (mp.getMonth() < 10
                    ? ("0" + mp.getMonth())
                    : String.valueOf(mp.getMonth()));
            series.getData().add(new XYChart.Data<>(label, mp.getTotalAmount()));
        }

        ordersChart.getData().add(series);
    }

    private void loadYearlyChart() {
        if (ordersChart == null) {
            return;
        }

        ordersChart.getData().clear();

        Order_Dol od = new Order_Dol();
        var data = od.getYearlyProfit();

        if (data == null || data.isEmpty()) {
            return;
        }

        if (yearCombo != null) {
            yearCombo.setDisable(true);
        }

        if (ordersChartXAxis != null) {
            ordersChartXAxis.setLabel("Year");
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Yearly Total Amount");

        for (YearlyProfit yp : data) {
            String label = String.valueOf(yp.getYear());
            series.getData().add(new XYChart.Data<>(label, yp.getTotalAmount()));
        }

        ordersChart.getData().add(series);
    }

    @FXML
    private void handleRefreshReports() {
        if (ordersChart != null) {
            loadReportsChart();
        }
    }

    @FXML
    private void handleMonthlyView() {
        yearlyMode = false;
        if (yearCombo != null) {
            yearCombo.setDisable(false);
        }
        loadReportsChart();
    }

    @FXML
    private void handleYearlyView() {
        yearlyMode = true;
        loadReportsChart();
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
