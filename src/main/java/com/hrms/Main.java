package com.hrms;

import com.hrms.model.Organization;
import com.hrms.service.*;
import com.hrms.util.Input;

import java.sql.SQLException;

/** HRMS console application entry point. */
public class Main {

    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("   HRMS - Human Resource Management");
        System.out.println("=====================================");
        while (true) {
            System.out.println("\nMain Menu\n 1. Create new Organization\n 2. Login to Organization\n 3. Exit");
            switch (Input.integer("Select an option: ")) {
                case 1 -> {
                    try { OrganizationService.create(); } catch (SQLException e) { Input.dbError(e); }
                }
                case 2 -> loginMenu();
                case 3 -> { System.out.println("Goodbye!"); return; }
                default -> Input.invalid();
            }
        }
    }

    private static void loginMenu() {
        while (true) {
            System.out.println("\nLogin Menu\n 1. Enter email and password for login\n 2. Forgot password\n 3. Back to main menu\n 4. Exit");
            int ch = Input.integer("Select an option: ");
            try {
                switch (ch) {
                    case 1 -> {
                        Organization org = OrganizationService.login();
                        if (org != null) { dashboard(org); return; }   // after logout -> main menu
                    }
                    case 2 -> OrganizationService.forgotPassword();
                    case 3 -> { return; }
                    case 4 -> { System.out.println("Goodbye!"); System.exit(0); }
                    default -> Input.invalid();
                }
            } catch (SQLException e) { Input.dbError(e); }
        }
    }

    private static void dashboard(Organization org) {
        System.out.println("\nWelcome to " + org.name() + "!");
        while (true) {
            System.out.println("\nSelect the option\n 1. Employee   (add, view, update, delete)"
                    + "\n 2. Attendance (create, view, update, delete)"
                    + "\n 3. Salary     (for a particular employee: create, update, delete, view)"
                    + "\n 4. Notice or Holiday (create, view, update, delete - for all employees)"
                    + "\n 5. Logout");
            switch (Input.integer("Select an option: ")) {
                case 1 -> EmployeeService.menu(org.id());
                case 2 -> AttendanceService.menu(org.id());
                case 3 -> SalaryService.menu(org.id());
                case 4 -> NoticeService.menu(org.id());
                case 5 -> { System.out.println("Logged out."); return; }
                default -> Input.invalid();
            }
        }
    }
}
