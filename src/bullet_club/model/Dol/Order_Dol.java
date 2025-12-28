package bullet_club.model.Dol;

import bullet_club.entity.Order;
import bullet_club.entity.Order_Details;
import java.sql.Date;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.sql.PreparedStatement;
import bullet_club.entity.User;

public class Order_Dol implements Data_Conn {

    //  Add Order with many details 
    public String addOrder(Order order, User user) {
        try {
            var orderDetails = order.getItems();
            if (orderDetails == null || orderDetails.isEmpty()) {
                return "`order` must have at least one item";
            }

            Product_Dol productDol = new Product_Dol();
            // Decrease stock for each item
            for (Order_Details detail : orderDetails) {
                boolean decreased = productDol.decreaseQuantity(detail.getItem().getId(), detail.getQuantity());
                if (!decreased) {
                    return "Insufficient stock for product id=" + detail.getItem().getId();
                }
            }

            // Compute total
            int totalPrice = order.total_price();
            order.setTotal_Amount(totalPrice);

            // Insert order header with auto-generated id
            data_base.openConn();
            java.util.Date orderDate = order.getDate() != null ? order.getDate() : new java.util.Date();
            String insertOrder = "INSERT INTO `order` (status, totalamount, date, coustmer_user_id) VALUES (?, ?, ?, ?)";
            data_base.createpsWithGeneratedKeys(insertOrder);
            data_base.getPs().setString(1, order.getStatus());
            data_base.getPs().setInt(2, totalPrice);
            data_base.getPs().setDate(3, new Date(orderDate.getTime()));
            data_base.getPs().setInt(4, user.getId());
            int rowsHeader = data_base.getPs().executeUpdate();
            if (rowsHeader == 0) {
                data_base.closePs();
                data_base.closseConn();
                return "Insert `order` failed";
            }

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
            order.setId(generatedOrderId);

            // Insert details referencing generatedOrderId
            String insertDetail = "INSERT INTO orderdateli (order_idorder, product_idproduct, quantity, price) VALUES (?, ?, ?, ?)";
            for (Order_Details detail : orderDetails) {
                data_base.createps(insertDetail);
                data_base.getPs().setInt(1, generatedOrderId);
                data_base.getPs().setInt(2, detail.getItem().getId());
                data_base.getPs().setInt(3, detail.getQuantity());
                data_base.getPs().setInt(4, detail.getPrice());
                data_base.getPs().executeUpdate();
                data_base.closePs();
            }

            data_base.closseConn();
            return "`order` added";
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

    //  Helper: only employees are authorized
    private boolean isEmployee(User user) {
        return user != null && user.getRole() != null && user.getRole().equalsIgnoreCase("employee");
    }

    //  Edit order status (authorized to employee only)
    public boolean editStatus(int orderId, String newStatus, User user) {
        if (!isEmployee(user)) {
            return false;
        }
        try {
            String q = "UPDATE `order` SET status = ? WHERE idorder = ?";
            data_base.openConn();
            data_base.createps(q);
            data_base.getPs().setString(1, newStatus);
            data_base.getPs().setInt(2, orderId);
            int rows = data_base.getPs().executeUpdate();
            data_base.closePs();
            data_base.closseConn();
            return rows > 0;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    //  Show all orders (authorized to employee only)
    public ArrayList<Order> showAllOrders(User user) {
        ArrayList<Order> list = new ArrayList<>();
        // if (!isEmployee(user)) return list;
        try {
            String q = "SELECT idorder, status, totalamount, date,coustmer_user_id FROM `order`";
            data_base.openConn();
            data_base.createps(q);
            data_base.createRs();
            ResultSet rs = data_base.getRs();
            while (rs.next()) {
                Order o = new Order(
                        rs.getInt("idorder"),
                        rs.getString("status"),
                        rs.getInt("totalamount"),
                        new java.util.Date(rs.getDate("date").getTime()),
                        new ArrayList<>()
                );
                o.setCustomerUserId(rs.getInt("coustmer_user_id"));
                // load details
                ArrayList<Order_Details> details = listDetailsByOrderId(o.getId());
                o.setItems(details);
                list.add(o);
            }
            data_base.closeRs();
            data_base.closePs();
            data_base.closseConn();
        } catch (Exception e) {
            System.out.println(e);
        }
        return list;
    }

    //  Show all orders by customer id (authorized to employee only)
    public ArrayList<Order> showOrdersByCustomerId(int customerUserId, bullet_club.entity.User user) {
        ArrayList<Order> list = new ArrayList<>();
        //if (!isEmployee(user)) return list;
        try {
            String q = "SELECT idorder, status, totalamount, date FROM `order` WHERE coustmer_user_id = ?";
            data_base.openConn();
            data_base.createps(q);
            data_base.getPs().setInt(1, customerUserId);
            data_base.createRs();
            ResultSet rs = data_base.getRs();
            while (rs.next()) {
                Order o = new Order(
                        rs.getInt("idorder"),
                        rs.getString("status"),
                        rs.getInt("totalamount"),
                        new java.util.Date(rs.getDate("date").getTime()),
                        new ArrayList<>()
                );
                //o.setCustomerUserId(rs.getInt("coustmer_user_id"));
                ArrayList<Order_Details> details = listDetailsByOrderId(o.getId());
                o.setItems(details);
                list.add(o);
            }
            data_base.closeRs();
            data_base.closePs();
            data_base.closseConn();
        } catch (Exception e) {
            System.out.println(e);
        }
        return list;
    }

    //  Helper: list details for an order id from orderdateli table, joined with product
    private ArrayList<Order_Details> listDetailsByOrderId(int orderId) {
        ArrayList<Order_Details> list = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT od.product_idproduct, od.quantity, od.price, "
                    + "p.name, p.catagory, p.describtion, p.price AS product_price, p.stocklevel AS product_quantity, p.reoderlevel "
                    + "FROM orderdateli od JOIN product p ON p.idproduct = od.product_idproduct "
                    + "WHERE od.order_idorder = ?";
            ps = data_base.getConn().prepareStatement(sql);
            ps.setInt(1, orderId);
            rs = ps.executeQuery();
            while (rs.next()) {
                bullet_club.entity.Product product = new bullet_club.entity.Product(
                        rs.getInt("product_idproduct"),
                        rs.getString("name"),
                        rs.getString("catagory"),
                        rs.getString("describtion"),
                        rs.getInt("product_price"),
                        rs.getInt("product_quantity"),
                        rs.getInt("reoderlevel")
                );
                Order_Details d = new Order_Details(
                        rs.getInt("quantity"),
                        rs.getInt("price"),
                        product
                );
                list.add(d);
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ignore) {
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ignore) {
            }
        }
        return list;
    }
    public String getUsernameById(int userId) {
    try {
        String q = "SELECT username FROM user WHERE id = ?";
        data_base.openConn();
        data_base.createps(q);
        data_base.getPs().setInt(1, userId);
        data_base.createRs();
        ResultSet rs = data_base.getRs();
        String name = null;
        if (rs.next()) name = rs.getString("username");
        data_base.closeRs();
        data_base.closePs();
        data_base.closseConn();
        return name != null ? name : String.valueOf(userId);
    } catch (Exception e) {
        System.out.println(e);
        try { data_base.closeRs(); } catch (Exception ignore) {}
        try { data_base.closePs(); } catch (Exception ignore) {}
        try { data_base.closseConn(); } catch (Exception ignore) {}
        return String.valueOf(userId);
    }
    }

    public static class MonthlyProfit {
        private final int year;
        private final int month;
        private final int totalAmount;

        public MonthlyProfit(int year, int month, int totalAmount) {
            this.year = year;
            this.month = month;
            this.totalAmount = totalAmount;
        }

        public int getYear() {
            return year;
        }

        public int getMonth() {
            return month;
        }

        public int getTotalAmount() {
            return totalAmount;
        }
    }

    public ArrayList<MonthlyProfit> getMonthlyProfit() {
        ArrayList<MonthlyProfit> list = new ArrayList<>();
        ResultSet rs = null;
        try {
            String q =
                    "SELECT YEAR(`date`) AS y, MONTH(`date`) AS m, SUM(totalamount) AS total " +
                    "FROM `order` " +
                    "GROUP BY YEAR(`date`), MONTH(`date`) " +
                    "ORDER BY YEAR(`date`), MONTH(`date`)";
            data_base.openConn();
            data_base.createps(q);
            data_base.createRs();
            rs = data_base.getRs();
            while (rs.next()) {
                int year = rs.getInt("y");
                int month = rs.getInt("m");
                int total = rs.getInt("total");
                list.add(new MonthlyProfit(year, month, total));
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
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

    public static class YearlyProfit {
        private final int year;
        private final int totalAmount;

        public YearlyProfit(int year, int totalAmount) {
            this.year = year;
            this.totalAmount = totalAmount;
        }

        public int getYear() {
            return year;
        }

        public int getTotalAmount() {
            return totalAmount;
        }
    }

    public ArrayList<YearlyProfit> getYearlyProfit() {
        ArrayList<YearlyProfit> list = new ArrayList<>();
        ResultSet rs = null;
        try {
            String q =
                    "SELECT YEAR(`date`) AS y, SUM(totalamount) AS total " +
                    "FROM `order` " +
                    "GROUP BY YEAR(`date`) " +
                    "ORDER BY YEAR(`date`)";
            data_base.openConn();
            data_base.createps(q);
            data_base.createRs();
            rs = data_base.getRs();
            while (rs.next()) {
                int year = rs.getInt("y");
                int total = rs.getInt("total");
                list.add(new YearlyProfit(year, total));
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
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
