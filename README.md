# Plant-Grow (PRJ301 Final Project - FPT University)

Smart seedling tray and web management system designed to measure actual water retention via load cells, prevent overwatering, and monitor plant growth environmental parameters.

## Key Features
- **Hardware Integration:** Uses ESP32, load cells (HX711), soil moisture, and light sensors to track pot weight changes and environmental data.
- **Rule Engine & Classification:** Automatically labels operating sessions into `OK`, `OVERWATER`, `NO_UPTAKE`, `DRY_STRESS`, and `LEAK`.
- **Web Management (MVC2):** Built with Java Servlet, JSP, JDBC, and SQL Server.
- **Role-Based Access Control:** Strict authorization across 5 roles (`ADMIN`, `GROWER`, `TECHNICIAN`, `ANALYST`, `VIEWER`).
- **Data Visualization & Export:** Interactive canvas charts and CSV data export.

## Tech Stack
- **Backend:** Java 8, Servlets, JDBC
- **Database:** SQL Server
- **Server:** Apache Tomcat 9
- **Hardware:** ESP32, Arduino C++
