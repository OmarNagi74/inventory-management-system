/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bullet_club.entity;

import java.time.LocalDate;

/**
 *
 * @author ENG abdelrahman nagi
 */
public class Notfication {
private int id;
private String massage;
private LocalDate date;
private Product item;

private String status;

public Notfication(int id, String massage, LocalDate date, String status, Product item) {
    this.id = id;
    this.massage = massage;
    this.date = date;
    this.status = status;
    this.item = item;
}

public String getStatus() { return status; }

   public int getId() { return id; }
public String getMassage() { return massage; }
public java.time.LocalDate getDate() { return date; }
public Product getItem() { return item; }
    public String genarete() {
        return "Notfication{" + "id=" + id + ", massage=" + massage + ", date=" + date + ", item=" + item + '}';
    }


}
