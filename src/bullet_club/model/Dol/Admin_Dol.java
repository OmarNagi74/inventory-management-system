package bullet_club.model.Dol;

import bullet_club.entity.User;
import java.util.ArrayList;

public class Admin_Dol extends User_Dol {

public String add_user(User user1) {
    try {
        data_base.openConn();

        // check duplicates by username+email (same logic as current code)
        String s = "SELECT id FROM user WHERE username=? AND email=?";
        data_base.createps(s);
        data_base.getPs().setString(1, user1.getUsername());
        data_base.getPs().setString(2, user1.getEmail());
        data_base.createRs();

        boolean exists = data_base.getRs().next();

        // close the SELECT resources before INSERT
        try { data_base.closeRs(); } catch (Exception ignore) {}
        try { data_base.closePs(); } catch (Exception ignore) {}

        if (exists) {
            return "rejected";
        }

        String insert = "INSERT INTO user (username, role, password, adderess, email, phone) VALUES (?, ?, ?, ?, ?, ?)";
        data_base.createpsWithGeneratedKeys(insert);
        data_base.getPs().setString(1, user1.getUsername());
        data_base.getPs().setString(2, user1.getRole());
        data_base.getPs().setString(3, user1.getPassword());
        data_base.getPs().setString(4, user1.getAdd());
        data_base.getPs().setString(5, user1.getEmail());
        data_base.getPs().setString(6, user1.getPhone());

        int rows = data_base.getPs().executeUpdate();
        if (rows > 0) {
            try (java.sql.ResultSet keys = data_base.getGeneratedKeys()) {
                if (keys.next()) {
                    user1.setId(keys.getInt(1));
                }
            }
            return "accepted";
        } else {
            return "rejected";
        }
    } catch (Exception e) {
        System.out.println(e);
        return "rejected";
    } finally {
        try { data_base.closeRs(); } catch (Exception ignore) {}
        try { data_base.closePs(); } catch (Exception ignore) {}
        try { data_base.closseConn(); } catch (Exception ignore) {}
    }
}

    public void remove_user(User user1) {
        try {
            data_base.openConn();

            String check = "SELECT * FROM user WHERE username=? AND email=? AND password=? AND adderess=? AND phone=? AND id=? AND role=?";
            data_base.createps(check);
            data_base.getPs().setString(1, user1.getUsername());
            data_base.getPs().setString(2, user1.getEmail());
            data_base.getPs().setString(3, user1.getPassword());
            data_base.getPs().setString(4, user1.getAdd());
            data_base.getPs().setString(5, user1.getPhone());
            data_base.getPs().setInt(6, user1.getId());
            data_base.getPs().setString(7, user1.getRole());
            data_base.createRs();

            if (data_base.getRs().next()) {
                String delete = "DELETE FROM user WHERE id=?";
                data_base.createps(delete);
                data_base.getPs().setInt(1, user1.getId());
                int rows = data_base.getPs().executeUpdate();

                if (rows > 0) {
                    System.out.println("User removed successfully.");
                } else {
                    System.out.println("️ Failed to remove user.");
                }
            } else {
                System.out.println(" No access — user not found.");
            }
            data_base.closseConn();
        } catch (Exception e) {
            System.out.println("Error in remove_user: " + e);
        }
    }

    public ArrayList<User> show_all() {
        ArrayList<User> users = new ArrayList<>();

        try {
            data_base.openConn();

            String S = "SELECT * FROM user ";
            data_base.createSt();

            data_base.createRs(S, data_base.getSt());
            while (data_base.getRs().next()) {
                users.add(new User(
                        data_base.getRs().getInt("id"),
                        data_base.getRs().getString("username"),
                        data_base.getRs().getString("role"),
                        data_base.getRs().getString("password"),
                        data_base.getRs().getString("adderess"),
                        data_base.getRs().getString("email"),
                        data_base.getRs().getString("phone")
                ));

            }
            data_base.closseConn();
        } catch (Exception e) {
            System.out.println(e);

        }
        return users;

    }
}
