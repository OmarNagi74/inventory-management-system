package bullet_club.model.Dol;

import bullet_club.entity.Product;
import java.sql.ResultSet;
import java.util.ArrayList;

public class Product_Dol implements Data_Conn {

    //  Add Product
    public String addProduct(Product product) {
        try {
            String insert = "INSERT INTO product (name, catagory, describtion, price, stocklevel, reoderlevel) VALUES (?, ?, ?, ?, ?, ?)";
            data_base.openConn();
            data_base.createpsWithGeneratedKeys(insert);

            data_base.getPs().setString(1, product.getName());
            data_base.getPs().setString(2, product.getCategory());
            data_base.getPs().setString(3, product.getDescription());
            data_base.getPs().setInt(4, product.getPrice());
            data_base.getPs().setInt(5, product.getQuantity());
            data_base.getPs().setInt(6, product.getReorder_level());

            int rows = data_base.getPs().executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = data_base.getGeneratedKeys()) {
                    if (keys.next()) {
                        product.setId(keys.getInt(1));
                    }
                }
            }
            data_base.closePs();
            data_base.closseConn();
            return rows > 0 ? "Product added" : "Insert failed";
        } catch (Exception e) {
            System.out.println(e);
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

    public boolean editProduct(int id, String name, String category, String description, int price, int reorderLevel) {
        try {
            String q = "UPDATE product SET name = ?, catagory = ?, describtion = ?, price = ?, reoderlevel = ? WHERE idproduct = ?";
            data_base.openConn();
            data_base.createps(q);
            data_base.getPs().setString(1, name);
            data_base.getPs().setString(2, category);
            data_base.getPs().setString(3, description);
            data_base.getPs().setInt(4, price);
            data_base.getPs().setInt(5, reorderLevel);
            data_base.getPs().setInt(6, id);
            int rows = data_base.getPs().executeUpdate();
            data_base.closePs();
            data_base.closseConn();
            return rows > 0;
        } catch (Exception e) {
            System.out.println(e);
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

    //  Get product by name
    public Product getProductByName(String name) {
        try {
            String q = "SELECT idproduct, name, catagory, describtion, price, stocklevel, reoderlevel FROM product WHERE name = ?";
            data_base.openConn();
            data_base.createps(q);
            data_base.getPs().setString(1, name);
            data_base.createRs();
            ResultSet rs = data_base.getRs();

            if (rs.next()) {
                Product p = new Product(
                        rs.getInt("idproduct"),
                        rs.getString("name"),
                        rs.getString("catagory"),
                        rs.getString("describtion"),
                        rs.getInt("price"),
                        rs.getInt("stocklevel"),
                        rs.getInt("reoderlevel")
                );
                data_base.closeRs();
                data_base.closePs();
                data_base.closseConn();
                return p;
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
        return null;
    }

    //  Show all products
    public ArrayList<Product> showAllProducts() {
        ArrayList<Product> list = new ArrayList<>();
        try {
            String q = "SELECT idproduct, name, catagory, describtion, price, stocklevel, reoderlevel FROM product";
            data_base.openConn();
            data_base.createSt();
            data_base.createRs(q, data_base.getSt());
            ResultSet rs = data_base.getRs();
            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("idproduct"),
                        rs.getString("name"),
                        rs.getString("catagory"),
                        rs.getString("describtion"),
                        rs.getInt("price"),
                        rs.getInt("stocklevel"),
                        rs.getInt("reoderlevel")
                );
                list.add(p);
            }
            data_base.closeRs();
            data_base.closeSt();
            data_base.closseConn();
        } catch (Exception e) {
            System.out.println(e);
        }
        return list;
    }

    //  Increase product stocklevel by delta
    public boolean increaseQuantity(int productId, int delta) {
        try {
            String q = "UPDATE product SET stocklevel = stocklevel + ? WHERE idproduct = ?";
            data_base.openConn();
            data_base.createps(q);
            data_base.getPs().setInt(1, delta);
            data_base.getPs().setInt(2, productId);
            int rows = data_base.getPs().executeUpdate();
            data_base.closePs();
            data_base.closseConn();
            return rows > 0;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public boolean editQuantity(int productId, int newQuantity) {
        try {
            String q = "UPDATE product SET stocklevel = ? WHERE idproduct = ?";
            data_base.openConn();
            data_base.createps(q);
            data_base.getPs().setInt(1, newQuantity);
            data_base.getPs().setInt(2, productId);
            int rows = data_base.getPs().executeUpdate();
            data_base.closePs();
            data_base.closseConn();
            return rows > 0;
        } catch (Exception e) {
            System.out.println(e);
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

    //  Decrease product quantity by delta (no negatives)
    public boolean decreaseQuantity(int productId, int delta) {
        try {
            // Step 1: Open connection
            data_base.openConn();

            // Step 2: Check current stock
            String checkStock = "SELECT stocklevel, reoderlevel, name FROM product WHERE idproduct = ?";
            data_base.createps(checkStock);
            data_base.getPs().setInt(1, productId);
            data_base.createRs();
            ResultSet rs = data_base.getRs();

            if (!rs.next()) {
                // No product with this ID
                data_base.closeRs();
                data_base.closePs();
                data_base.closseConn();
                return false;
            }

            int currentStock = rs.getInt("stocklevel");
            int reorderLevel = rs.getInt("reoderlevel");
            String name = rs.getString("name");

            data_base.closeRs();
            data_base.closePs();

            // Step 3: Check if enough stock
            if (currentStock < delta) {
                System.out.println("Not enough stock for product ID: " + productId);
                data_base.closseConn();
                return false;
            }

            // Step 4: Decrease quantity
            String update = "UPDATE product SET stocklevel = stocklevel - ? WHERE idproduct = ?";
            data_base.createps(update);
            data_base.getPs().setInt(1, delta);
            data_base.getPs().setInt(2, productId);
            int rows = data_base.getPs().executeUpdate();

            data_base.closePs();

            // Step 5: If stock now <= reorder level, add notification
            int newStock = currentStock - delta;
            if (newStock <= reorderLevel) {
                Notfication_Dol notifDol = new Notfication_Dol();
                notifDol.addNotification(productId, "Reorder " + name);
            }

            // Step 6: Close connection
            data_base.closseConn();
            return rows > 0;

        } catch (Exception e) {
            System.out.println("Error decreasing quantity: " + e);
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

    public ArrayList<String> getAllCategories() {
        ArrayList<String> list = new ArrayList<>();
        try {
            String q = "SELECT DISTINCT catagory FROM product WHERE catagory IS NOT NULL AND catagory <> '' ORDER BY catagory";
            data_base.openConn();
            data_base.createSt();
            data_base.createRs(q, data_base.getSt());
            ResultSet rs = data_base.getRs();
            while (rs.next()) {
                list.add(rs.getString("catagory"));
            }
            data_base.closeRs();
            data_base.closeSt();
            data_base.closseConn();
        } catch (Exception e) {
            System.out.println(e);
            try {
                data_base.closeRs();
            } catch (Exception ignore) {
            }
            try {
                data_base.closeSt();
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
