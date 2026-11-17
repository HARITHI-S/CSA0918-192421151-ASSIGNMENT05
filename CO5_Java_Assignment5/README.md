# CO5 Assignment 5 – Data Persistence Engineering

This is a runnable Java 17 + Maven project based on the uploaded Assignment 5 PDF. It includes Part A design answers in `docs/PartA_Answers.md`, Part B implementation code, SQL setup, and run instructions.

## Requirements
- JDK 17 or newer (`java -version`, `javac -version`)
- VS Code with the **Extension Pack for Java**
- Maven 3.8+ (`mvn -version`)
- MySQL 8.x only for the optional JDBC tasks in B2/B3/B4

## Open and run in VS Code
1. Extract the ZIP.
2. In VS Code choose **File → Open Folder** and select `CO5_Java_Assignment5`.
3. Install the Extension Pack for Java if prompted.
4. Open the terminal (**Terminal → New Terminal**).
5. Run `mvn clean compile`.
6. Run the self-contained file tasks: `mvn exec:java`.
7. Outputs and sample files are created in the `data/` folder. Timings are measured on your computer; they are not fabricated.

You can also open `src/main/java/com/co5/assignment/Main.java` and press **Run** above `main`.

## Optional MySQL setup (B2, B3, B4)
1. Start MySQL Server.
2. Execute `sql/schema.sql` in MySQL Workbench or with:
   `mysql -u root -p < sql/schema.sql`
3. Set environment variables in the VS Code terminal (PowerShell example):
   ```powershell
   $env:DB_URL="jdbc:mysql://localhost:3306/co5_assignment5?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
   $env:DB_USER="root"
   $env:DB_PASSWORD="YOUR_PASSWORD"
   ```
4. Run `mvn clean compile exec:java "-Dexec.args=--db"`.
5. Do not put your actual database password in GitHub.

The Maven build downloads MySQL Connector/J automatically. The ordinary file tasks work without MySQL. For the required 1/10/50 MB and 1k/10k/50k/100k file measurements, run `mvn exec:java "-Dexec.args=--bench"`; this generates the test data and prints actual local timings.

## Main classes
- `PassStore`: CSV-safe add/find/list, skips malformed rows.
- `LineCounter`: unbuffered and buffered line counting.
- `PassLoader`: completed transactional JDBC batch loader (1000 rows per batch).
- `JdbcTasks`: unsafe vs prepared login, metadata, ResultSetMetaData and insert timing helper.
- `Benchmarks`: generates 1/10/50 MB logs and 1k–100k pass files for actual timing.
- `Migration`: validates a source file, reports rejected rows and transactionally migrates valid records.
- `Dao<T,K>` / `StudentDao`: generic DAO pattern.
- `Main`: executable demonstrations.

## Required experimental evidence
The assignment explicitly asks for 1MB, 10MB and 50MB line-count timings; pass lookup at 1,000 to 100,000 rows; and MySQL insert tests at 1,000, 10,000 and 100,000 rows for Statement, PreparedStatement and batch. These measurements depend on your CPU, disk, JVM, MySQL server and network. Run them on your own machine and enter the actual values in `docs/Results_Template.md`. The included starter demo prints real timings for its small sample and a 1,000-row DB run. Never submit invented timings as experimental results.

## GitHub submission
Create repository `CSA09-<RegisterNumber>-Assignment5`. Keep this project structure, add your name/register number and Java version to the README, capture screenshots of actual execution, and commit each task progressively. Include your repository link in the report/cover page.

## Troubleshooting
- `mvn is not recognized`: install Maven and reopen VS Code, or use the VS Code Java extension's Maven support.
- `mvn clean compile` fails: check `java -version` and `mvn -version`; use JDK 17+.
- MySQL connection refused: ensure MySQL service is running and DB_URL/port are correct.
- Access denied: set DB_USER and DB_PASSWORD correctly.
- Unknown database/table: run `sql/schema.sql` first.
