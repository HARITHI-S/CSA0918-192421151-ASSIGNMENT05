package com.co5.assignment;

import java.sql.*;
import java.util.*;

public final class JdbcTasks {
    private JdbcTasks() {}
    public static void unsafeLoginDemo(Connection c, String id, String pwd) throws SQLException {
        String sql = "SELECT * FROM app_user WHERE user_id='" + id + "' AND pwd='" + pwd + "'";
        try (Statement st=c.createStatement(); ResultSet rs=st.executeQuery(sql)) {
            System.out.println("Unsafe login accepted: " + rs.next());
        }
    }
    public static void safeLoginDemo(Connection c, String id, String pwd) throws SQLException {
        String sql = "SELECT * FROM app_user WHERE user_id=? AND pwd=?";
        try (PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1,id); ps.setString(2,pwd);
            try(ResultSet rs=ps.executeQuery()) { System.out.println("PreparedStatement login accepted: " + rs.next()); }
        }
    }
    public static void verifyTable(Connection c) throws SQLException {
        try (Statement st=c.createStatement(); ResultSet rs=st.executeQuery("SELECT * FROM bus_pass LIMIT 10")) {
            ResultSetMetaData md=rs.getMetaData();
            System.out.println("\nResultSetMetaData:");
            for(int i=1;i<=md.getColumnCount();i++)
                System.out.println(md.getColumnName(i)+" | "+md.getColumnTypeName(i)+" | nullable="+(md.isNullable(i)!=ResultSetMetaData.columnNoNulls));
            int rows=0; while(rs.next()) rows++;
            System.out.println("Rows returned by verification SELECT: "+rows);
        }
    }

    public static void metadata(Connection c) throws SQLException {
        DatabaseMetaData m=c.getMetaData();
        System.out.println("Database: "+m.getDatabaseProductName()+" "+m.getDatabaseProductVersion());
        System.out.println("JDBC driver: "+m.getDriverName()+" "+m.getDriverVersion());
        try(ResultSet r=m.getTables(c.getCatalog(),null,"%",new String[]{"TABLE"})) {
            System.out.println("Tables:");
            while(r.next()) System.out.println(" - "+r.getString("TABLE_NAME"));
        }
    }
    public static long insertGenerated(Connection c, int count, String mode, boolean transaction) throws SQLException {
        String sql="INSERT INTO bus_pass(pass_id,reg_no,name,route_no,boarding_stop,validity_date,amount) VALUES(?,?,?,?,?,?,?)";
        boolean old=c.getAutoCommit();
        if(transaction) c.setAutoCommit(false); else c.setAutoCommit(true);
        long start=System.nanoTime();
        try {
            if ("statement".equals(mode)) {
                try(Statement st=c.createStatement()) {
                    for(int i=0;i<count;i++) st.executeUpdate("INSERT INTO bus_pass VALUES('"+id(i)+"','REG"+i+"','Student "+i+"',"+(i%20)+",'Stop "+(i%10)+"','2027-12-31',4500.0)");
                }
            } else try(PreparedStatement ps=c.prepareStatement(sql)) {
                for(int i=0;i<count;i++) {
                    ps.setString(1,id(i)); ps.setString(2,"REG"+i); ps.setString(3,"Student "+i);
                    ps.setInt(4,i%20); ps.setString(5,"Stop "+(i%10)); ps.setDate(6,java.sql.Date.valueOf("2027-12-31")); ps.setDouble(7,4500);
                    if ("batch".equals(mode)) { ps.addBatch(); if((i+1)%1000==0) ps.executeBatch(); }
                    else ps.executeUpdate();
                }
                if("batch".equals(mode)) ps.executeBatch();
            }
            if(transaction) c.commit();
        } catch(SQLException e) { if(transaction) c.rollback(); throw e; }
        finally { c.setAutoCommit(old); }
        return (System.nanoTime()-start)/1_000_000;
    }
    private static String id(int i) { return String.format("P%09d",i); }
}
