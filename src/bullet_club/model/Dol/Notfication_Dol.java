package bullet_club.model.Dol;

import bullet_club.entity.Notfication;
import bullet_club.entity.Product;
import java.sql.ResultSet;
import java.sql.Date;
import java.util.ArrayList;

public class Notfication_Dol implements Data_Conn {

    //  Create notification for a product
    public String addNotification(int productId, String massage) {
        try {
            String insert = "INSERT INTO notifcation (massage, date, status, product_idproduct) VALUES (?, ?, ?, ?)";
            data_base.openConn();
            // Use generated keys if you want to retrieve the new id (optional)
            data_base.createpsWithGeneratedKeys(insert);

            data_base.getPs().setString(1, massage);
            data_base.getPs().setDate(2, new Date(System.currentTimeMillis()));
            data_base.getPs().setString(3, "opened"); // default status
            data_base.getPs().setInt(4, productId);

            int rows = data_base.getPs().executeUpdate();

            // Optionally read the new id:
            // try (ResultSet keys = data_base.getGeneratedKeys()) {
            //     if (keys.next()) {
            //         int newId = keys.getInt(1);
            //         // you can use newId if needed
            //     }
            // }
            data_base.closePs();
            data_base.closseConn();
            return rows > 0 ? "Notification added" : "Insert failed";
        } catch (Exception e) {
            System.out.println(e);
            try {
                data_base.closePs();
            } catch (Exception ignore) {
            }
            try {
                data_base.closeRs();
            } catch (Exception ignore) {
            }
            try {
                data_base.closseConn();
            } catch (Exception ignore) {
            }
            return "Error: " + e.getMessage();
        }
    }

    //  Show all notifications (joined with product for full object)
    public ArrayList<Notfication> showAll() {
        ArrayList<Notfication> list = new ArrayList<>();
        try {
            String q = "SELECT n.id, n.massage, n.date, n.status, "
                    + "p.idproduct AS pid, p.name, p.catagory, p.describtion, p.price, p.stocklevel, p.reoderlevel "
                    + "FROM notifcation n JOIN product p ON p.idproduct = n.product_idproduct "
                    + "ORDER BY n.date DESC, n.id DESC";
            data_base.openConn();
            data_base.createSt();
            data_base.createRs(q, data_base.getSt());
            ResultSet rs = data_base.getRs();
            while (rs.next()) {
                Product prod = new Product(
                        rs.getInt("pid"),
                        rs.getString("name"),
                        rs.getString("catagory"),
                        rs.getString("describtion"),
                        rs.getInt("price"),
                        rs.getInt("stocklevel"),
                        rs.getInt("reoderlevel")
                );
                Notfication n = new Notfication(
                        rs.getInt("id"),
                        rs.getString("massage"),
                        rs.getDate("date").toLocalDate(),
                        rs.getString("status"),
                        prod
                );
                list.add(n);
            }
            data_base.closeRs();
            data_base.closeSt();
            data_base.closseConn();
        } catch (Exception e) {
            System.out.println(e);
        }
        return list;
    }

    public boolean editNotificationStatus(int id, String newStatus) {
        try {
            String q = "UPDATE notifcation SET status = ? WHERE id = ?";
            data_base.openConn();
            data_base.createps(q);
            data_base.getPs().setString(1, newStatus);
            data_base.getPs().setInt(2, id);
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

}
