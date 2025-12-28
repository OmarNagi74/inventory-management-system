
package bullet_club.entity;


public class Product {
    private int id;
    private String name;
    private String category;
    private String description;
    private int price;
    private int quantity;// الكميه كلها
    private int reorder_level;

    public Product(int id, String name, String category, String description, int price, int quantity, int reorder_level) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.reorder_level = reorder_level;
    }

    @Override
    public String toString() {
        return "Product{" + "id=" + id + ", name=" + name + ", category=" + category + ", description=" + description + ", price=" + price + ", quantity=" + quantity + ", reorder_level=" + reorder_level + '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getReorder_level() {
        return reorder_level;
    }

    public void setReorder_level(int reorder_level) {
        this.reorder_level = reorder_level;
    }
    
    
    public boolean is_reorder_needed( int q)
    { 
        
        
        return q<reorder_level;
    
    }
    
}
