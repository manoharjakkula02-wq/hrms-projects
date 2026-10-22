package com.hrms.service;

import com.hrms.db.DBConnection;
import com.hrms.model.Organization;
import com.hrms.util.Input;
import com.hrms.util.PasswordUtil;

import java.sql.*;

/** Create organization, login, forgot password. */
public final class OrganizationService {
    private OrganizationService() {}

    public static void create() throws SQLException {
        System.out.println("\n-- Create New Organization --");
        String name = Input.required("Organization name : ");
        String address = Input.required("Address           : ");
        String dept = Input.required("Department        : ");
        String email = Input.email("Email (login id)  : ");
        String pwd = Input.password("Password          : ", true);
        int count = Input.positiveInt("Employees count   : ");

        String sql = "INSERT INTO organizations (name,address,department,email,password_hash,employee_count) "
                + "VALUES (?,?,?,?,?,?)";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name); ps.setString(2, address); ps.setString(3, dept);
            ps.setString(4, email); ps.setString(5, PasswordUtil.hash(pwd)); ps.setInt(6, count);
            ps.executeUpdate();
            System.out.println("  Organization created successfully. You can now login with " + email);
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("  An organization with this email already exists.");
        }
    }

    /** @return logged-in organization, or null when credentials are wrong. */
    public static Organization login() throws SQLException {
        String email = Input.email("Enter email    : ");
        String pwd = Input.password("Enter password : ", false);
        String sql = "SELECT id, name, password_hash FROM organizations WHERE email = ?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && PasswordUtil.verify(pwd, rs.getString("password_hash")))
                    return new Organization(rs.getInt("id"), rs.getString("name"));
            }
        }
        System.out.println("  Invalid email or password.");
        return null;
    }

    /** Reset password: email + PAN (identity check) + new password. */
    public static void forgotPassword() throws SQLException {
        System.out.println("\n-- Forgot Password --");
        String email = Input.email("Enter email                : ");
        String pwd = Input.password("Enter new password for login : ", true);
        String sql = "UPDATE organizations SET password_hash = ? WHERE email = ?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, PasswordUtil.hash(pwd)); ps.setString(2, email);
            System.out.println(ps.executeUpdate() > 0
                    ? "  Password updated. Please login with your new password."
                    : "  No organization found with this email.");
        }
    }
}