package com.co5.assignment;

import java.sql.*;
import java.util.*;

public class StudentDao implements Dao<Student, Integer> {
    private final Connection connection;
    public StudentDao(Connection connection) { this.connection = connection; }
    public void insert(Student s) throws SQLException {
        try (PreparedStatement p = connection.prepareStatement(
                "INSERT INTO student(student_id, register_no, name, email) VALUES(?,?,?,?)")) {
            p.setInt(1,s.studentId()); p.setString(2,s.registerNo()); p.setString(3,s.name()); p.setString(4,s.email()); p.executeUpdate();
        }
    }
    public Optional<Student> findById(Integer id) throws SQLException {
        try (PreparedStatement p=connection.prepareStatement("SELECT student_id,register_no,name,email FROM student WHERE student_id=?")) {
            p.setInt(1,id); try(ResultSet r=p.executeQuery()) {
                return r.next()?Optional.of(new Student(r.getInt(1),r.getString(2),r.getString(3),r.getString(4))):Optional.empty();
            }
        }
    }
    public List<Student> findAll() throws SQLException {
        List<Student> out=new ArrayList<>();
        try(Statement s=connection.createStatement(); ResultSet r=s.executeQuery("SELECT student_id,register_no,name,email FROM student")) {
            while(r.next()) out.add(new Student(r.getInt(1),r.getString(2),r.getString(3),r.getString(4)));
        } return out;
    }
    public void update(Student s) throws SQLException {
        try(PreparedStatement p=connection.prepareStatement("UPDATE student SET register_no=?,name=?,email=? WHERE student_id=?")) {
            p.setString(1,s.registerNo());p.setString(2,s.name());p.setString(3,s.email());p.setInt(4,s.studentId());p.executeUpdate();
        }
    }
    public void delete(Integer id) throws SQLException {
        try(PreparedStatement p=connection.prepareStatement("DELETE FROM student WHERE student_id=?")) {p.setInt(1,id);p.executeUpdate();}
    }
}
