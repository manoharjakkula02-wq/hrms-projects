package com.hrms.service;

import com.hrms.db.DBConnection;
import com.hrms.util.Input;

import java.sql.*;
import java.time.LocalDate;

/** Employee CRUD (scoped to the logged-in organization). */
public final class EmployeeService {
    private EmployeeService() {}

    public static void menu(int orgId) {
        while (true) {
            System.out.println("\n-- Employee --\n 1. Add\n 2. View all\n 3. View one\n 4. Update\n 5. Delete\n 6. Back");
            int ch = Input.integer("Select: ");
            try {
                switch (ch) {
                    case 1 -> add(orgId);
                    case 2 -> viewAll(orgId);
                    case 3 -> viewOne(orgId);
                    case 4 -> update(orgId);
                    case 5 -> delete(orgId);
                    case 6 -> { return; }
                    default -> Input.invalid();
                }
            } catch (SQLException e) { Input.dbError(e); }
        }
    }

    /** True if the employee exists in this organization. */
    public static boolean exists(int orgId, int empId) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM employees WHERE id=? AND org_id=?")) {
            ps.setInt(1, empId); ps.setInt(2, orgId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public static int askExistingEmployee(int orgId) throws SQLException {
        int id = Input.integer("Employee ID: ");
        if (!exists(orgId, id)) { System.out.println("  No such employee in your organization."); return -1; }
        return id;
    }

    private static void add(int orgId) throws SQLException {
        try (Connection c = DBConnection.get()) {
            int declared = 0, current = 0;
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT o.employee_count, (SELECT COUNT(*) FROM employees WHERE org_id=o.id) FROM organizations o WHERE o.id=?")) {
                ps.setInt(1, orgId);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) { declared = rs.getInt(1); current = rs.getInt(2); } }
            }
            if (current >= declared)
                System.out.println("  Note: you have registered " + declared + " employees; this exceeds that count.");

            String name = Input.required("Name        : ");
            String email = Input.email("Email       : ");
            String phone = Input.text("Phone       : ");
            String desig = Input.text("Designation : ");
            String dept = Input.text("Department  : ");
            LocalDate join = Input.date("Joining date (YYYY-MM-DD, blank=today): ", LocalDate.now());

            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO employees (org_id,name,email,phone,designation,department,joining_date) VALUES (?,?,?,?,?,?,?)")) {
                ps.setInt(1, orgId); ps.setString(2, name); ps.setString(3, email); ps.setString(4, phone);
                ps.setString(5, desig); ps.setString(6, dept); ps.setDate(7, Date.valueOf(join));
                ps.executeUpdate();
                System.out.println("  Employee added.");
            }
        }
    }

    private static void viewAll(int orgId) throws SQLException {
        String sql = "SELECT id,name,email,phone,designation,department,joining_date FROM employees WHERE org_id=? ORDER BY id";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orgId);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%n%-5s %-20s %-28s %-13s %-16s %-14s %-10s%n",
                        "ID", "NAME", "EMAIL", "PHONE", "DESIGNATION", "DEPARTMENT", "JOINED");
                int n = 0;
                while (rs.next()) {
                    n++;
                    System.out.printf("%-5d %-20s %-28s %-13s %-16s %-14s %-10s%n", rs.getInt(1), rs.getString(2),
                            rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getDate(7));
                }
                if (n == 0) System.out.println("  No employees found.");
            }
        }
    }

    private static void viewOne(int orgId) throws SQLException {
        int id = Input.integer("Employee ID: ");
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM employees WHERE id=? AND org_id=?")) {
            ps.setInt(1, id); ps.setInt(2, orgId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) { System.out.println("  Not found."); return; }
                System.out.println("  ID          : " + rs.getInt("id"));
                System.out.println("  Name        : " + rs.getString("name"));
                System.out.println("  Email       : " + rs.getString("email"));
                System.out.println("  Phone       : " + rs.getString("phone"));
                System.out.println("  Designation : " + rs.getString("designation"));
                System.out.println("  Department  : " + rs.getString("department"));
                System.out.println("  Joined      : " + rs.getDate("joining_date"));
            }
        }
    }

    private static void update(int orgId) throws SQLException {
        int id = askExistingEmployee(orgId);
        if (id < 0) return;
        System.out.println("  (leave blank to keep current value)");
        String name = Input.text("New name        : ");
        String phone = Input.text("New phone       : ");
        String desig = Input.text("New designation : ");
        String dept = Input.text("New department  : ");
        String sql = "UPDATE employees SET name=COALESCE(NULLIF(?,''),name), phone=COALESCE(NULLIF(?,''),phone), "
                   + "designation=COALESCE(NULLIF(?,''),designation), department=COALESCE(NULLIF(?,''),department) "
                   + "WHERE id=? AND org_id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name); ps.setString(2, phone); ps.setString(3, desig); ps.setString(4, dept);
            ps.setInt(5, id); ps.setInt(6, orgId);
            ps.executeUpdate();
            System.out.println("  Employee updated.");
        }
    }

    private static void delete(int orgId) throws SQLException {
        int id = askExistingEmployee(orgId);
        if (id < 0) return;
        if (!Input.text("Delete employee and all their attendance/salary records? (y/n): ").equalsIgnoreCase("y")) return;
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM employees WHERE id=? AND org_id=?")) {
            ps.setInt(1, id); ps.setInt(2, orgId);
            ps.executeUpdate();
            System.out.println("  Employee deleted.");
        }
    }
}
