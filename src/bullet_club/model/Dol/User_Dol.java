package bullet_club.model.Dol;

import bullet_club.entity.User;
import java.sql.SQLException;
import java.util.ArrayList;
import bullet_club.entity.Product;
import bullet_club.entity.Order_Details;

public class User_Dol implements Data_Conn {

    // Store the currently logged-in user 
    private static User loggedInUser = null;

    private static ArrayList<Order_Details> cart = new ArrayList<>();

    // Getter for the logged-in user
    public static User getLoggedInUser() {
        return loggedInUser;
    }

    // Check if a user is currently logged in
    public static boolean isLoggedIn() {
        return loggedInUser != null;
    }

    public static boolean login(User user1) {
        boolean success = false;
        try {
            data_base.openConn();
            String s = "select * from user where username=? and password=?";
            data_base.createps(s);
            data_base.getPs().setString(1, user1.getUsername());
            data_base.getPs().setString(2, user1.getPassword());
            data_base.createRs();

            if (data_base.getRs().next()) {
                // Authentication successful - store the complete user object
                loggedInUser = new User(
                        data_base.getRs().getInt("id"),
                        data_base.getRs().getString("username"),
                        data_base.getRs().getString("role"),
                        data_base.getRs().getString("password"),
                        data_base.getRs().getString("adderess"),
                        data_base.getRs().getString("email"),
                        data_base.getRs().getString("phone")
                );
                System.out.println("Login successful: " + loggedInUser.getUsername());
                success = true;
            } else {
                // Authentication failed
                loggedInUser = null;
                System.out.println("Login failed: Invalid username or password");
            }
        } catch (Exception e) {
            System.out.println("Error while logging in: " + e.getMessage());
        } finally {
            try {
                data_base.closseConn();
            } catch (Exception closeEx) {
                System.out.println("Error closing resources after login: " + closeEx.getMessage());
            }
        }
        return success;
    }

    public static void logout() {
        if (loggedInUser != null) {
            System.out.println("Logging out user: " + loggedInUser.getUsername());
            loggedInUser = null;
            System.out.println("Logout successful");
        } else {
            System.out.println("No user is currently logged in");
        }
    }

    public static boolean change_password(User user1, String currentPass, String newPass) {
        try {
            data_base.openConn();

            // verify current password by user id
            String check = "SELECT id FROM user WHERE id=? AND password=?";
            data_base.createps(check);
            data_base.getPs().setInt(1, user1.getId());
            data_base.getPs().setString(2, currentPass);
            data_base.createRs();
            if (!data_base.getRs().next()) {
                return false; // wrong current password
            }
            data_base.closeRs();
            data_base.closePs();

            String s = "UPDATE user SET password = ? WHERE id = ?";
            data_base.createps(s);
            data_base.getPs().setString(1, newPass);
            data_base.getPs().setInt(2, user1.getId());

            int rows = data_base.getPs().executeUpdate();

            if (rows > 0) {
                user1.setPassword(newPass);
                if (loggedInUser != null && loggedInUser.getId() == user1.getId()) {
                    loggedInUser.setPassword(newPass);
                }
                return true;
            }
            return false;

        } catch (Exception e) {
            System.out.println("Error in change_password: " + e.getMessage());
            return false;
        } finally {
            try { data_base.closseConn(); } catch (Exception ignore) {}
        }
    }

    public static boolean edit_user(User user1, String newUsername, String newEmail, String newPhone, String newAddress) {
        boolean updated = false;
        try {
            data_base.openConn();

            // enforce unique username/email (for other users)
            String dup = "SELECT id FROM user WHERE (username=? OR email=?) AND id<>?";
            data_base.createps(dup);
            data_base.getPs().setString(1, newUsername);
            data_base.getPs().setString(2, newEmail);
            data_base.getPs().setInt(3, user1.getId());
            data_base.createRs();
            if (data_base.getRs().next()) {
                return false;
            }
            data_base.closeRs();
            data_base.closePs();

            String upd = "UPDATE user SET username=?, adderess=?, email=?, phone=? WHERE id=?";
            data_base.createps(upd);
            data_base.getPs().setString(1, newUsername);
            data_base.getPs().setString(2, newAddress);
            data_base.getPs().setString(3, newEmail);
            data_base.getPs().setString(4, newPhone);
            data_base.getPs().setInt(5, user1.getId());
            int rows = data_base.getPs().executeUpdate();
            updated = rows > 0;
            if (updated) {
                user1.setUsername(newUsername);
                user1.setAdd(newAddress);
                user1.setEmail(newEmail);
                user1.setPhone(newPhone);
                if (loggedInUser != null && loggedInUser.getId() == user1.getId()) {
                    loggedInUser.setUsername(newUsername);
                    loggedInUser.setAdd(newAddress);
                    loggedInUser.setEmail(newEmail);
                    loggedInUser.setPhone(newPhone);
                }
            }
        } catch (Exception e) {
            System.out.println("Error in edit_user: " + e.getMessage());
        } finally {
            try { data_base.closseConn(); } catch (Exception ignore) {}
        }
        return updated;
    }

    //  Change user role by id
    public static boolean changeRole(User user1, String newRole) {
        if (user1 == null || newRole == null || newRole.isBlank()) {
            return false;
        }
        boolean updated = false;
        try {
            data_base.openConn();
            String upd = "UPDATE user SET role=? WHERE id=?";
            data_base.createps(upd);
            data_base.getPs().setString(1, newRole);
            data_base.getPs().setInt(2, user1.getId());
            int rows = data_base.getPs().executeUpdate();
            updated = rows > 0;
            if (updated) {
                user1.setRole(newRole);
                if (loggedInUser != null && loggedInUser.getId() == user1.getId()) {
                    loggedInUser.setRole(newRole);
                }
            }
        } catch (Exception e) {
            System.out.println("Error in changeRole: " + e.getMessage());
        } finally {
            try { data_base.closseConn(); } catch (Exception ignore) {}
        }
        return updated;
    }

    public static boolean signUp(User user1) {
        boolean created = false;
        try {
            data_base.openConn();
            String check = "SELECT id FROM user WHERE username=? OR email=?";
            data_base.createps(check);
            data_base.getPs().setString(1, user1.getUsername());
            data_base.getPs().setString(2, user1.getEmail());
            data_base.createRs();
            if (data_base.getRs().next()) {
                System.out.println("Sign up rejected: username or email already exists");
                return false;
            }
            data_base.closeRs();
            data_base.closePs();

            String insert = "INSERT INTO user (username, role, password, adderess, email, phone) VALUES (?, ?, ?, ?, ?, ?)";
            data_base.createpsWithGeneratedKeys(insert);
            data_base.getPs().setString(1, user1.getUsername());
            data_base.getPs().setString(2, user1.getRole());
            data_base.getPs().setString(3, user1.getPassword());
            data_base.getPs().setString(4, user1.getAdd());
            data_base.getPs().setString(5, user1.getEmail());
            data_base.getPs().setString(6, user1.getPhone());

            int rows = data_base.getPs().executeUpdate();
            created = rows > 0;
            if (created) {
                try (java.sql.ResultSet keys = data_base.getGeneratedKeys()) {
                    if (keys.next()) {
                        user1.setId(keys.getInt(1));
                    }
                }
                System.out.println("Sign up successful for user: " + user1.getUsername());
            } else {
                System.out.println("Sign up failed for user: " + user1.getUsername());
            }
        } catch (Exception e) {
            System.out.println("Error while signing up: " + e.getMessage());
        } finally {
            try {
                data_base.closseConn();
            } catch (Exception closeEx) {
                System.out.println("Error closing resources after sign up: " + closeEx.getMessage());
            }
        }
        return created;
    }


    public static synchronized void addToCart(Product p, int qty) {
        if (qty <= 0 || p == null) {
            return;
        }
        for (Order_Details od : cart) {
            if (od.getItem().getId() == p.getId()) {
                int allowed = Math.min(p.getQuantity(), od.getQuantity() + qty);
                od.setQuantity(allowed);
                return;
            }
        }
        int allowed = Math.min(p.getQuantity(), qty);
        cart.add(new Order_Details(allowed, p.getPrice(), p));
    }

    public static synchronized ArrayList<Order_Details> getCart() {
        return new ArrayList<>(cart);
    }

    public static synchronized void clearCart() {
        cart.clear();
    }

    public static synchronized int cartTotal() {
        return cart.stream().mapToInt(od -> (int) od.total()).sum();
    }
}
