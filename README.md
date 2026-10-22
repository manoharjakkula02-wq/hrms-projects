# HRMS – Human Resource Management System (Java + MySQL)

Console application built with **core Java (JDBC)** and **MySQL**.

## Flow
```
Main Menu: 1. Create new Organization  2. Login to Organization  3. Exit
 └─ Login Menu: 1. Email + password  2. Forgot password  3. Back  4. Exit
     └─ Dashboard: 1. Employee  2. Attendance  3. Salary  4. Notice/Holiday  5. Logout
```

## Setup
1. Install JDK 17+, Maven, MySQL 8.
2. Create the DB:  `mysql -u root -p < sql/schema.sql`
3. `cp src/main/resources/db.properties.example src/main/resources/db.properties` and edit credentials
   (or set env vars `HRMS_DB_URL`, `HRMS_DB_USER`, `HRMS_DB_PASSWORD`).
4. Run: `mvn compile exec:java`   (use a real terminal so the password is hidden)

## Structure
```
sql/schema.sql                 tables (organizations, employees, attendance, salary, notices)
com.hrms.Main                  menus / navigation
com.hrms.db.DBConnection       JDBC connection
com.hrms.service.*             one class per module (SQL + console I/O)
com.hrms.util.Input            validated console input
com.hrms.util.PasswordUtil     PBKDF2 password hashing
```

## Notes
- Passwords are stored hashed (PBKDF2), never plain text.
- All data is scoped by `org_id`, so one organization can't see another's data.
- Forgot password verifies the organization's **PAN** before allowing a reset.
- Deleting an employee cascades to their attendance and salary rows.
