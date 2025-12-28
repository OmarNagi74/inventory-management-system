package bullet_club.model.Dol;

import bullet_club.entity.Purchase_Details;
import bullet_club.entity.User;
import bullet_club.entity.Product;
import bullet_club.entity.Purchase_Order;

import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import bullet_club.model.Dol.*;

public class Purchase_Order_Dol implements Data_Conn {

    private boolean isAuthorized(User user) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        String role = user.getRole().toLowerCase();
        return role.equals("admin") || role.equals("manager");
    }

    //  Add Purchase Order
    public String addPurchaseOrder(Purchase_Order purchaseOrder, int supplierId, int managerUserId, User user) {
//        if (!isAuthorized(user)) {
//            return "Unauthorized: Only admin or manager can add purchase orders";
//        }
        try {
            ArrayList<Purchase_Details> purchaseDetails = purchaseOrder.getItems();
            if (purchaseDetails == null || purchaseDetails.isEmpty()) {
                return "Purchase order must have at least one item";
            }

            Product_Dol productDol = new Product_Dol();

            // Process each product in purchase details
            for (Purchase_Details detail : purchaseDetails) {
                Product product = detail.getItem();
                String productName = product.getName();

                // Check if product exists by name
                Product existingProduct = productDol.getProductByName(productName);

                if (existingProduct == null) {
                    // Product doesn't exist - add it using addProduct
                    // Set initial quantity from purchase detail quantity

                    product.setQuantity(detail.getQuantity());
                    String result = productDol.addProduct(product);
                    if (!result.equals("Product added")) {
                        return "Failed to add product: " + productName + " - " + result;
                    }
                } else {
                    // Update the product id in detail to match existing product
                    product.setId(existingProduct.getId());
                }
            }

            // Calculate total_price using total_price() method
            int totalPrice = purchaseOrder.total_price();
            purchaseOrder.setTotal_Amount(totalPrice);

            // Insert Purchase_Order_Dol into database
            data_base.openConn();
            String insertOrder = "INSERT INTO Purchase_order (date, status, total_price, supplier_idsupplier) VALUES (?, ?, ?, ?)";
            data_base.createpsWithGeneratedKeys(insertOrder);
            data_base.getPs().setDate(1, new java.sql.Date(purchaseOrder.getDate().getTime()));
            data_base.getPs().setString(2, purchaseOrder.getStatus());
            data_base.getPs().setInt(3, totalPrice);
            data_base.getPs().setInt(4, supplierId);

            int rowsHeader = data_base.getPs().executeUpdate();
            if (rowsHeader == 0) {
                data_base.closePs();
                data_base.closseConn();
                return "Insert purchase order failed";
            }

// Get generated id from JDBC
            int generatedOrderId = 0;
            try (ResultSet keys = data_base.getGeneratedKeys()) {
                if (keys.next()) {
                    generatedOrderId = keys.getInt(1);
                }
            }
            data_base.closePs();
            if (generatedOrderId == 0) {
                data_base.closseConn();
                return "Could not obtain new order id";
            }

// Insert details with generated id
            String insertDetail = "INSERT INTO purchasedetails (Purchase_order_idPurchase_order, product_idproduct, quantity, price) VALUES (?, ?, ?, ?)";
            for (Purchase_Details detail : purchaseDetails) {
                data_base.createps(insertDetail);
                data_base.getPs().setInt(1, generatedOrderId);
                data_base.getPs().setInt(2, detail.getItem().getId());
                data_base.getPs().setInt(3, detail.getQuantity());
                data_base.getPs().setInt(4, detail.getPrice());
                data_base.getPs().executeUpdate();
                data_base.closePs();
            }

            data_base.closseConn();
            return "Purchase order added";
        } catch (Exception e) {
            System.out.println(e);
            try {
                data_base.closeRs();
            } catch (Exception ignore) {
            }
            try {
                data_base.closePs();
            } catch (Exception ignore) {
            }
            try {
                data_base.closseConn();
            } catch (Exception ignore) {
            }
            return "Error: " + e.getMessage();
        }
    }

    //  Show all purchase orders (authorized to admin or manager only)
    public ArrayList<Purchase_Order> showAllPurchaseOrders(User user) {
        ArrayList<Purchase_Order> list = new ArrayList<>();
        if (!isAuthorized(user)) {
            return list;
        }
        try {
            String q = "SELECT idPurchase_order, date, status, total_price, supplier_idsupplier FROM Purchase_order";
            data_base.openConn();
            data_base.createps(q);
            data_base.createRs();
            ResultSet rs = data_base.getRs();

            while (rs.next()) {
                Purchase_Order po = new Purchase_Order(new ArrayList<>());
                po.setId(rs.getInt("idPurchase_order"));
                po.setDate(new java.util.Date(rs.getDate("date").getTime()));
                po.setStatus(rs.getString("status"));
                po.setTotal_Amount(rs.getInt("total_price"));
                po.setSupplierId(rs.getInt("supplier_idsupplier"));
                // Load details if needed (can be added later)
                list.add(po);
            }
            data_base.closeRs();
            data_base.closePs();
            data_base.closseConn();
        } catch (Exception e) {
            System.out.println(e);
            try {
                data_base.closeRs();
            } catch (Exception ignore) {
            }
            try {
                data_base.closePs();
            } catch (Exception ignore) {
            }
            try {
                data_base.closseConn();
            } catch (Exception ignore) {
            }
        }
        return list;
    }

    //  Edit purchase order status (authorized to admin or manager only)
    public boolean editPurchaseOrderStatus(int purchaseOrderId, String newStatus, User user) {
        if (!isAuthorized(user)) {
            return false;
        }
        String oldStatus = null;
        try {
            // Read current status
            data_base.openConn();
            String qOld = "SELECT status FROM Purchase_order WHERE idPurchase_order = ?";
            data_base.createps(qOld);
            data_base.getPs().setInt(1, purchaseOrderId);
            data_base.createRs();
            ResultSet rs = data_base.getRs();
            if (rs.next()) {
                oldStatus = rs.getString("status");
            }
            data_base.closeRs();
            data_base.closePs();

            // Update status
            String q = "UPDATE Purchase_order SET status = ? WHERE idPurchase_order = ?";
            data_base.createps(q);
            data_base.getPs().setString(1, newStatus);
            data_base.getPs().setInt(2, purchaseOrderId);
            int rows = data_base.getPs().executeUpdate();
            data_base.closePs();

            // If newly delivered, increase product quantities once
            if (rows > 0
                    && "delivered".equalsIgnoreCase(newStatus)
                    && (oldStatus == null || !"delivered".equalsIgnoreCase(oldStatus))) {

                Product_Dol productDol = new Product_Dol();
                var details = getPurchaseDetailsByOrderId(purchaseOrderId); // already implemented
                for (Purchase_Details d : details) {
                    // Use the product id from the details result
                    productDol.increaseQuantity(d.getItem().getId(), d.getQuantity());
                }
            }

            data_base.closseConn();
            return rows > 0;
        } catch (Exception e) {
            System.out.println(e);
            try {
                data_base.closeRs();
            } catch (Exception ignore) {
            }
            try {
                data_base.closePs();
            } catch (Exception ignore) {
            }
            try {
                data_base.closseConn();
            } catch (Exception ignore) {
            }
            return false;
        }
    }

    // Fetch purchase details for a given order (joins product to get product name)
    public ArrayList<Purchase_Details> getPurchaseDetailsByOrderId(int orderId) {
        ArrayList<Purchase_Details> list = new ArrayList<>();
        try {
            String q
                    = "SELECT pd.product_idproduct, pd.quantity, pd.price, p.name "
                    + "FROM purchasedetails pd "
                    + "JOIN product p ON p.idproduct = pd.product_idproduct "
                    + "WHERE pd.Purchase_order_idPurchase_order = ?";
            data_base.openConn();
            data_base.createps(q);
            data_base.getPs().setInt(1, orderId);
            data_base.createRs();
            ResultSet rs = data_base.getRs();

            while (rs.next()) {
                int pid = rs.getInt("product_idproduct");
                String name = rs.getString("name");
                int qty = rs.getInt("quantity");
                int price = rs.getInt("price");

                // Product needs full-arg constructor; fill missing fields with defaults
                Product item = new Product(pid, name, "", "", 0, 0, 0);
                Purchase_Details detail = new Purchase_Details(qty, price, item);
                list.add(detail);
            }
            data_base.closeRs();
            data_base.closePs();
            data_base.closseConn();
        } catch (Exception e) {
            System.out.println(e);
            try {
                data_base.closeRs();
            } catch (Exception ignore) {
            }
            try {
                data_base.closePs();
            } catch (Exception ignore) {
            }
            try {
                data_base.closseConn();
            } catch (Exception ignore) {
            }
        }
        return list;
    }

}
