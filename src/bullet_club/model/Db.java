/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bullet_club.model;
import java.sql.*;
public class Db {
    private Connection conn;
private Statement st;
private ResultSet rs;
private PreparedStatement ps;

    public void closseConn() throws SQLException {
        conn.close();
    }

    public void openConn() throws SQLException {
       conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mydb", "root", "5091");
    }

    
    public void closeSt() throws SQLException {
       st.close();
    }

    public void closeRs() throws SQLException {
        rs.close();
    }

    public void closePs() throws SQLException {
     ps.close();
    }
public void createps( String k) throws SQLException {
ps=conn.prepareStatement(k) ;   }

    
    public void createSt() throws SQLException {
st=conn.createStatement() ;   }

    public void createRs(String quary,Statement st) throws SQLException {
    rs=st.executeQuery(quary);
    }

     public void createRs() throws SQLException {
    rs=ps.executeQuery();
    }

    public PreparedStatement getPs() {
        return ps;
    }
    public void setPs(PreparedStatement ps) {
        this.ps = ps;
    }

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    public Statement getSt() {
        return st;
    }

    public void setSt(Statement st) {
        this.st = st;
    }

    public ResultSet getRs() {
        return rs;
    }

    public void setRs(ResultSet rs) {
        this.rs = rs;
    }

    public void closeAll() throws SQLException {
        closeRs();
        closePs();
        closeSt();
        closseConn();
    }
    
   public void createpsWithGeneratedKeys(String sql) throws SQLException {
    ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
}

public ResultSet getGeneratedKeys() throws SQLException {
    return ps.getGeneratedKeys();
}

    
    
    
}
