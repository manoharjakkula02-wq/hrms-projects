package com.hrms.service;

import com.hrms.db.DBConnection;
import com.hrms.util.Input;

import java.math.BigDecimal;
import java.sql.*;

/** Salary for a particular employee: create / update / delete / view. */
public final class SalaryService {
    private SalaryService() {}

    public static void menu(int orgId) {
        while (true) {
            System.out.println("\n-- Salary (per employee) --\n 1. Create salary\n 2. Update salary\n 3. Delete salary\n 4. View salary\n 5. Back");
            int ch = Input.integer("Select: ");
            try {
                switch (ch) {
                    case 1 -> create(orgId);
                    case 2 -> update(orgId);
                    case 3 -> delete(orgId);
                    case 4 -> view(orgId);
                    case 5 -> { return; }
                    default -> Input.invalid();
                }
            } catch (SQLException e) { Input.dbError(e); }
        }
    }

    private static void create(int orgId) throws SQLException {
        int emp = EmployeeService.askExistingEmployee(orgId);
        if (emp < 0) return;
        String month = Input.month("Salary month (YYYY-MM): ");
        BigDecimal basic = Input.decimal("Basic      : ");
        BigDecimal hra = Input.decimal("HRA        : ");
        BigDecimal allow = Input.decimal("Allowances : ");
        BigDecimal ded = Input.decimal("Deductions : ");
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO salary (org_id,employee_id,salary_month,basic,hra,allowances,deductions) VALUES (?,?,?,?,?,?,?)")) {
            ps.setInt(1, orgId); ps.setInt(2, emp); ps.setString(3, month);
            ps.setBigDecimal(4, basic); ps.setBigDecimal(5, hra); ps.setBigDecimal(6, allow); ps.setBigDecimal(7, ded);
            ps.executeUpdate();
            System.out.println("  Salary created. Net = " + basic.add(hra).add(allow).subtract(ded));
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("  Salary for this employee and month already exists. Use Update instead.");
        }
    }

    private static void update(int orgId) throws SQLException {
        int id = Input.integer("Salary ID: ");
        System.out.println("  (leave blank to keep current value)");
        BigDecimal basic = Input.optionalDecimal("New basic      : ");
        BigDecimal hra = Input.optionalDecimal("New HRA        : ");
        BigDecimal allow = Input.optionalDecimal("New allowances : ");
        BigDecimal ded = Input.optionalDecimal("New deductions : ");
        String sql = "UPDATE salary SET basic=COALESCE(?,basic), hra=COALESCE(?,hra), allowances=COALESCE(?,allowances), "
                   + "deductions=COALESCE(?,deductions) WHERE id=? AND org_id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, basic); ps.setObject(2, hra); ps.setObject(3, allow); ps.setObject(4, ded);
            ps.setInt(5, id); ps.setInt(6, orgId);
            System.out.println(ps.executeUpdate() > 0 ? "  Salary updated." : "  Salary record not found.");
        }
    }

    private static void delete(int orgId) throws SQLException {
        int id = Input.integer("Salary ID: ");
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM salary WHERE id=? AND org_id=?")) {
            ps.setInt(1, id); ps.setInt(2, orgId);
            System.out.println(ps.executeUpdate() > 0 ? "  Salary deleted." : "  Salary record not found.");
        }
    }

    private static void view(int orgId) throws SQLException {
        int emp = EmployeeService.askExistingEmployee(orgId);
        if (emp < 0) return;
        String sql = "SELECT s.id, e.name, s.salary_month, s.basic, s.hra, s.allowances, s.deductions, s.net_salary "
                   + "FROM salary s JOIN employees e ON e.id=s.employee_id WHERE s.org_id=? AND s.employee_id=? "
                   + "ORDER BY s.salary_month DESC";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orgId); ps.setInt(2, emp);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%n%-6s %-20s %-8s %10s %10s %10s %10s %11s%n",
                        "ID", "NAME", "MONTH", "BASIC", "HRA", "ALLOW", "DEDUCT", "NET");
                int n = 0;
                while (rs.next()) {
                    n++;
                    System.out.printf("%-6d %-20s %-8s %10.2f %10.2f %10.2f %10.2f %11.2f%n", rs.getInt(1), rs.getString(2),
                            rs.getString(3), rs.getBigDecimal(4), rs.getBigDecimal(5), rs.getBigDecimal(6),
                            rs.getBigDecimal(7), rs.getBigDecimal(8));
                }
                if (n == 0) System.out.println("  No salary records for this employee.");
            }
        }
    }
}
