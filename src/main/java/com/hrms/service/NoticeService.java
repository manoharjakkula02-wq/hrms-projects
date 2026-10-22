package com.hrms.service;

import com.hrms.db.DBConnection;
import com.hrms.util.Input;

import java.sql.*;
import java.time.LocalDate;

/** Notices and holidays (visible to all employees of the organization). */
public final class NoticeService {
    private NoticeService() {}

    public static void menu(int orgId) {
        while (true) {
            System.out.println("\n-- Notice / Holiday --\n 1. Create\n 2. View\n 3. Update\n 4. Delete\n 5. Back");
            int ch = Input.integer("Select: ");
            try {
                switch (ch) {
                    case 1 -> create(orgId);
                    case 2 -> view(orgId);
                    case 3 -> update(orgId);
                    case 4 -> delete(orgId);
                    case 5 -> { return; }
                    default -> Input.invalid();
                }
            } catch (SQLException e) { Input.dbError(e); }
        }
    }

    private static void create(int orgId) throws SQLException {
        String type;
        while (true) {
            int t = Input.integer("Type (1=Notice, 2=Holiday): ");
            if (t == 1) { type = "NOTICE"; break; }
            if (t == 2) { type = "HOLIDAY"; break; }
            Input.invalid();
        }
        String title = Input.required("Title       : ");
        String desc = Input.text("Description : ");
        LocalDate date = Input.date("Date (YYYY-MM-DD, blank=today): ", LocalDate.now());
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("INSERT INTO notices (org_id,type,title,description,event_date) VALUES (?,?,?,?,?)")) {
            ps.setInt(1, orgId); ps.setString(2, type); ps.setString(3, title); ps.setString(4, desc);
            ps.setDate(5, Date.valueOf(date));
            ps.executeUpdate();
            System.out.println("  " + type + " created for all employees.");
        }
    }

    private static void view(int orgId) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id,type,title,description,event_date FROM notices WHERE org_id=? ORDER BY event_date DESC")) {
            ps.setInt(1, orgId);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%n%-5s %-8s %-12s %-28s %s%n", "ID", "TYPE", "DATE", "TITLE", "DESCRIPTION");
                int n = 0;
                while (rs.next()) {
                    n++;
                    System.out.printf("%-5d %-8s %-12s %-28s %s%n", rs.getInt(1), rs.getString(2), rs.getDate(5),
                            rs.getString(3), rs.getString(4));
                }
                if (n == 0) System.out.println("  No notices or holidays.");
            }
        }
    }

    private static void update(int orgId) throws SQLException {
        int id = Input.integer("Notice ID: ");
        System.out.println("  (leave blank to keep current value)");
        String title = Input.text("New title       : ");
        String desc = Input.text("New description : ");
        LocalDate date = Input.optionalDate("New date (YYYY-MM-DD): ");
        String sql = "UPDATE notices SET title=COALESCE(NULLIF(?,''),title), description=COALESCE(NULLIF(?,''),description), "
                   + "event_date=COALESCE(?,event_date) WHERE id=? AND org_id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title); ps.setString(2, desc);
            ps.setDate(3, date == null ? null : Date.valueOf(date));
            ps.setInt(4, id); ps.setInt(5, orgId);
            System.out.println(ps.executeUpdate() > 0 ? "  Updated." : "  Record not found.");
        }
    }

    private static void delete(int orgId) throws SQLException {
        int id = Input.integer("Notice ID: ");
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM notices WHERE id=? AND org_id=?")) {
            ps.setInt(1, id); ps.setInt(2, orgId);
            System.out.println(ps.executeUpdate() > 0 ? "  Deleted." : "  Record not found.");
        }
    }
}
