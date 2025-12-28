
package bullet_club.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

public class Purchase_Order {
    private int id;
    private String status;
    private int total_Amount;
    private Date date;
    private ArrayList<Purchase_Details> items;
    private int supplierId;
    
    public Purchase_Order(ArrayList<Purchase_Details> items) {
        this.status = "Pending";
        this.date = new Date(System.currentTimeMillis());
        this.items = items;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotal_Amount() {
        return total_Amount;
    }

    public void setTotal_Amount(int total_Amount) {
        this.total_Amount = total_Amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public ArrayList<Purchase_Details> getItems() {
        return items;
    }

    public void setItems(ArrayList<Purchase_Details> items) {
        this.items = items;
    }
   public int total_price(){
        int sum=0;
        for(  Purchase_Details item:items  ){
            sum+=item.total(); // total() returns price * quantity
        }
        return sum;
   } 
    public String order_info() {
        return "Order{" + "id=" + id + ", status=" + status + ", total_Amount=" + total_Amount + ", date=" + date + ", items=" + items + '}';
    }

}
