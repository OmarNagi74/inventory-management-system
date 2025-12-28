package bullet_club;

import bullet_club.entity.User;
import bullet_club.entity.Product;
import bullet_club.entity.Order;
import bullet_club.entity.Order_Details;
import bullet_club.entity.Supplier;
import bullet_club.entity.Purchase_Details;
import bullet_club.entity.Purchase_Order;
import bullet_club.model.Dol.Admin_Dol;
import bullet_club.model.Dol.Order_Dol;
import bullet_club.model.Dol.Product_Dol;
import bullet_club.model.Dol.Purchase_Order_Dol;

import java.util.ArrayList;
import java.util.Date;

public class Bullet_club {
    public static void main(String[] args) {

        // 1. إنشاء Admin Dol
        Admin_Dol adminDol = new Admin_Dol();

        // 2. إنشاء Admin user
        User admin = new User(1, "admin1", "admin", "adminpass", "Cairo", "admin@example.com", "01000000001");
        System.out.println("Adding admin...");
        adminDol.add_user(admin);

        // 3. إنشاء 4 مستخدمين
        User user1 = new User(2, "user1", "customer", "pass1", "Cairo", "user1@example.com", "01000000002");
        User user2 = new User(3, "user2", "customer", "pass2", "Cairo", "user2@example.com", "01000000003");
        User user3 = new User(4, "user3", "customer", "pass3", "Cairo", "user3@example.com", "01000000004");
        User user4 = new User(5, "user4", "customer", "pass4", "Cairo", "user4@example.com", "01000000005");

        System.out.println("\nAdding 4 users...");
        adminDol.add_user(user1);
        adminDol.add_user(user2);
        adminDol.add_user(user3);
        adminDol.add_user(user4);

        // إزالة user2
        System.out.println("\nRemoving user2...");
        adminDol.remove_user(user2);

        // عرض كل المستخدمين
        System.out.println("\nAll users in DB:");
        ArrayList<User> allUsers = adminDol.show_all();
        for (User u : allUsers) {
            System.out.println("ID: " + u.getId() + ", Username: " + u.getUsername() + ", Role: " + u.getRole());
        }

        // 4. إنشاء Product Dol
        Product_Dol productDol = new Product_Dol();

        // إنشاء 5 منتجات بواسطة المدير
        Product prod1 = new Product(1, "Laptop", "Electronics", "High-end laptop", 1500, 20, admin.getId());
        Product prod2 = new Product(2, "Smartphone", "Electronics", "Latest phone", 800, 20, admin.getId());
        Product prod3 = new Product(3, "Headphones", "Electronics", "Noise-cancelling", 200, 15, admin.getId());
        Product prod4 = new Product(4, "Keyboard", "Electronics", "Mechanical keyboard", 100, 25, admin.getId());
        Product prod5 = new Product(5, "Mouse", "Electronics", "Wireless mouse", 50, 30, admin.getId());

        System.out.println("\nAdmin adding products...");
        productDol.addProduct(prod1);
        productDol.addProduct(prod2);
        productDol.addProduct(prod3);
        productDol.addProduct(prod4);
        productDol.addProduct(prod5);

        // عرض كل المنتجات
        System.out.println("\nAll products in DB:");
        ArrayList<Product> allProducts = productDol.showAllProducts();
        for (Product p : allProducts) {
            System.out.println("ID: " + p.getId() + ", Name: " + p.getName() + ", Quantity: " + p.getQuantity());
        }

        // 5. إنشاء Order بواسطة user1
       ArrayList<Order_Details> itemsOrder1 = new ArrayList<>();
        itemsOrder1.add(new Order_Details(1, 2, prod1)); // 2 Laptop
        itemsOrder1.add(new Order_Details(2, 1, prod3)); // 1 Headphones
//
//        int totalAmount = 0;
//        for (Order_Details item : itemsOrder1) {
//            totalAmount += item.total(); // مجموع سعر المنتج * الكمية
//        }
//
        Order order1 = new Order(1, "Processing", 0, new Date(), itemsOrder1);
//
//        System.out.println("\nOrder created by " + user1.getUsername() + ":");
//        System.out.println(order1.order_info());


           Order_Dol od=new Order_Dol();
           od.addOrder(order1, user1);

        // ============================================================
        // 🟢  المدير يعمل طلب شراء من المورد ويزود المخزون
        // ============================================================

        System.out.println("\n===== Manager creates purchase order from supplier =====");

        // 1. إنشاء Supplier
        Supplier supplier = new Supplier(1, "0123456789", "TechSupplier", "supplier@tech.com");

        // 2. تحديد المنتج اللي محتاج زيادة في المخزون (مثلاً Laptop)
        Product productToRestock = prod1;

        System.out.println("Current stock of " + productToRestock.getName() + ": " + productToRestock.getQuantity());

        // 3. المدير يقرر يشتري 5 أجهزة Laptop إضافية من المورد
        ArrayList<Purchase_Details> purchaseItems = new ArrayList<>();
        purchaseItems.add(new Purchase_Details(6, productToRestock.getPrice(), productToRestock));

        // 4. إنشاء Purchase_Order
        Purchase_Order purchaseOrder = new Purchase_Order(purchaseItems);
        purchaseOrder.setId(1002);
        purchaseOrder.setStatus("Approved");
        purchaseOrder.setTotal_Amount(purchaseOrder.total_price());
        
        
        System.out.println("\nPurchase Order created by admin:");
        System.out.println(purchaseOrder.order_info());

        // 5. تحديث المخزون بعد الشراء
        int newStock = productToRestock.getQuantity() + purchaseItems.get(0).getQuantity();
        productToRestock.setQuantity(newStock);
        
        Purchase_Order_Dol x =new Purchase_Order_Dol();
        x.addPurchaseOrder(purchaseOrder, newStock, newStock, user4);
        
        
        System.out.println("\nUpdated stock for " + productToRestock.getName() + ": " + productToRestock.getQuantity());
        System.out.println("Purchase completed successfully!");
    }
}
