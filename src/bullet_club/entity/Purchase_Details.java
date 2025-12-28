package bullet_club.entity;

public class Purchase_Details {
    
    private int quantity;
    private int price;
    private  Product item;

    public Purchase_Details(int quantity, int price, Product item) {
        this.quantity = quantity;
        this.price = price;
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Purchase_Details{" + "quantity=" + quantity + ", price=" + price + ", item=" + item + '}';
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Product getItem() {
        return item;
    }

    public void setItem(Product item) {
        this.item = item;
    }
    public int total(){
        return this.price*this.quantity;
    }
    
}
