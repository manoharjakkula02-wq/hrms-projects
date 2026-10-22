package com.hrms.util;

import java.io.Console;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.regex.Pattern;

/** Console input helpers with validation. */
public final class Input {
    private static final Scanner SC = new Scanner(System.in);
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern PAN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]$");
    private static final Pattern GST = Pattern.compile("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$");

    private Input() {}

    public static String text(String prompt) {
        System.out.print(prompt);
        return SC.hasNextLine() ? SC.nextLine().trim() : "";
    }

    public static String required(String prompt) {
        while (true) {
            String s = text(prompt);
            if (!s.isEmpty()) return s;
            System.out.println("  Value cannot be empty.");
        }
    }

    public static int integer(String prompt) {
        while (true) {
            try { return Integer.parseInt(text(prompt)); }
            catch (NumberFormatException e) { System.out.println("  Please enter a valid number."); }
        }
    }

    public static int positiveInt(String prompt) {
        while (true) {
            int n = integer(prompt);
            if (n > 0) return n;
            System.out.println("  Must be greater than 0.");
        }
    }

    public static BigDecimal decimal(String prompt) {
        while (true) {
            try {
                BigDecimal d = new BigDecimal(text(prompt));
                if (d.signum() >= 0) return d;
                System.out.println("  Cannot be negative.");
            } catch (NumberFormatException e) { System.out.println("  Please enter a valid amount."); }
        }
    }

    /** Blank = keep existing (returns null). */
    public static BigDecimal optionalDecimal(String prompt) {
        while (true) {
            String s = text(prompt);
            if (s.isEmpty()) return null;
            try { return new BigDecimal(s); }
            catch (NumberFormatException e) { System.out.println("  Please enter a valid amount."); }
        }
    }

    public static LocalDate date(String prompt, LocalDate defaultValue) {
        while (true) {
            String s = text(prompt);
            if (s.isEmpty() && defaultValue != null) return defaultValue;
            try { return LocalDate.parse(s); }
            catch (Exception e) { System.out.println("  Use format YYYY-MM-DD."); }
        }
    }

    /** Blank = keep existing (returns null). */
    public static LocalDate optionalDate(String prompt) {
        while (true) {
            String s = text(prompt);
            if (s.isEmpty()) return null;
            try { return LocalDate.parse(s); }
            catch (Exception e) { System.out.println("  Use format YYYY-MM-DD."); }
        }
    }

    public static String month(String prompt) {
        while (true) {
            String s = text(prompt);
            if (s.matches("^\\d{4}-(0[1-9]|1[0-2])$")) return s;
            System.out.println("  Use format YYYY-MM (e.g. 2026-10).");
        }
    }

    public static String email(String prompt) {
        while (true) {
            String s = text(prompt);
            if (EMAIL.matcher(s).matches()) return s.toLowerCase();
            System.out.println("  Invalid email address.");
        }
    }

    public static String pan(String prompt) {
        while (true) {
            String s = text(prompt).toUpperCase();
            if (PAN.matcher(s).matches()) return s;
            System.out.println("  Invalid PAN (format: ABCDE1234F).");
        }
    }

    public static String gst(String prompt) {
        while (true) {
            String s = text(prompt).toUpperCase();
            if (GST.matcher(s).matches()) return s;
            System.out.println("  Invalid GST (format: 22ABCDE1234F1Z5).");
        }
    }

    /** Reads a password (hidden when a real console is available). */
    public static String password(String prompt, boolean checkStrength) {
        while (true) {
            Console console = System.console();
            String p = console != null ? new String(console.readPassword(prompt)) : text(prompt);
            if (!checkStrength || p.length() >= 6) return p;
            System.out.println("  Password must be at least 6 characters.");
        }
    }

    public static void dbError(SQLException e) {
        if (e instanceof SQLIntegrityConstraintViolationException)
            System.out.println("  Failed: duplicate entry or invalid reference. (" + e.getMessage() + ")");
        else
            System.out.println("  Database error: " + e.getMessage());
    }

    public static void invalid() { System.out.println("  Invalid option, try again."); }
}
