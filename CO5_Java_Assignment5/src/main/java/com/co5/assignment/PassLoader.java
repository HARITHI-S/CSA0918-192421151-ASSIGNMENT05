package com.co5.assignment;

import java.sql.*;
import java.util.List;

public final class PassLoader {
    private PassLoader() {}
    /** Loads in one transaction, sending batches of at most 1,000 rows. */
    public static int loadAll(Connection con, List<String[]> passes) throws SQLException {
        String sql = "INSERT INTO bus_pass(pass_id, reg_no, name, route_no, boarding_stop, validity_date, amount) VALUES (?, ?, ?, ?, ?, ?, ?)";
        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int n = 0;
            for (String[] p : passes) {
                if (p.length != 7) throw new SQLException("Pass must contain 7 fields");
                ps.setString(1, p[0]);
                ps.setString(2, p[1]);
                ps.setString(3, p[2]);
                ps.setInt(4, Integer.parseInt(p[3]));
                ps.setString(5, p[4]);
                ps.setDate(6, Date.valueOf(p[5]));
                ps.setDouble(7, Double.parseDouble(p[6]));
                ps.addBatch();
                n++;
                if (n % 1000 == 0) ps.executeBatch();
            }
            ps.executeBatch();
            con.commit();
            return n;
        } catch (SQLException | RuntimeException e) {
            try { con.rollback(); } catch (SQLException rollbackError) { e.addSuppressed(rollbackError); }
            throw e;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }
}
