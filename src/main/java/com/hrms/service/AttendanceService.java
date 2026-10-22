package com.hrms.service;

import com.hrms.db.DBConnection;
import com.hrms.util.Input;

import java.sql.*;
import java.time.LocalDate;

/** Attendance create / view / update / delete. */
public final class AttendanceService {
    private static final String[] STATUS = {"PRESENT", "ABSENT", "LEAVE", "HALF_DAY"};

    private AttendanceService() {}

    public static void menu(int orgId) {
        while (true) {
            System.out.println("\n-- Attendance --\n 1. Create\n 2. View\n 3. Update\n 4. Delete\n 5. Back");
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

    private static String askStatus() {
        while (true) {
            System.out.println("  Status: 1.PRESENT  2.ABSENT  3.LEAVE  4.HALF_DAY");
            int s = Input.integer("Select status: ");
            if (s >= 1 && s <= 4) return STATUS[s - 1];
            Input.invalid();
        }
    }

    private static void create(int orgId) throws SQLException {
        int emp = EmployeeService.askExistingEmployee(orgId);
        if (emp < 0) return;
        LocalDate d = Input.date("Date (YYYY-MM-DD, blank=today): ", LocalDate.now());
        String status = askStatus();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("INSERT INTO attendance (org_id,employee_id,att_date,status) VALUES (?,?,?,?)")) {
            ps.setInt(1, orgId); ps.setInt(2, emp); ps.setDate(3, Date.valueOf(d)); ps.setString(4, status);
            ps.executeUpdate();
            System.out.println("  Attendance recorded.");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("  Attendance for this employee/date already exists. Use Update instead.");
        }
    }

    private static void view(int orgId) throws SQLException {
        System.out.println(" 1. By date (all employees)\n 2. By employee (all dates)");
        int ch = Input.integer("Select: ");
        String sql = "SELECT a.id, e.id, e.name, a.att_date, a.status FROM attendance a JOIN employees e ON e.id=a.employee_id "
                   + "WHERE a.org_id=? AND ";
        try (Connection c = DBConnection.get()) {
            PreparedStatement ps;
            if (ch == 1) {
                LocalDate d = Input.date("Date (blank=today): ", LocalDate.now());
                ps = c.prepareStatement(sql + "a.att_date=? ORDER BY e.name");
                ps.setInt(1, orgId); ps.setDate(2, Date.valueOf(d));
            } else if (ch == 2) {
                int emp = EmployeeService.askExistingEmployee(orgId);
                if (emp < 0) return;
                ps = c.prepareStatement(sql + "e.id=? ORDER BY a.att_date DESC");
                ps.setInt(1, orgId); ps.setInt(2, emp);
            } else { Input.invalid(); return; }

            try (ps; ResultSet rs = ps.executeQuery()) {
                System.out.printf("%n%-7s %-6s %-22s %-12s %-10s%n", "ATT_ID", "EMP", "NAME", "DATE", "STATUS");
                int n = 0;
                while (rs.next()) {
                    n++;
                    System.out.printf("%-7d %-6d %-22s %-12s %-10s%n",
                            rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getDate(4), rs.getString(5));
                }
                if (n == 0) System.out.println("  No records found.");
            }
        }
    }

    private static void update(int orgId) throws SQLException {
        int id = Input.integer("Attendance ID: ");
        String status = askStatus();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("UPDATE attendance SET status=? WHERE id=? AND org_id=?")) {
            ps.setString(1, status); ps.setInt(2, id); ps.setInt(3, orgId);
            System.out.println(ps.executeUpdate() > 0 ? "  Attendance updated." : "  Record not found.");
        }
    }

    private static void delete(int orgId) throws SQLException {
        int id = Input.integer("Attendance ID: ");
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM attendance WHERE id=? AND org_id=?")) {
            ps.setInt(1, id); ps.setInt(2, orgId);
            System.out.println(ps.executeUpdate() > 0 ? "  Attendance deleted." : "  Record not found.");
        }
    }
}
