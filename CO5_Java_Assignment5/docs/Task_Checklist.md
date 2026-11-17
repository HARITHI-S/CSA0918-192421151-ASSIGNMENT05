# Submission Checklist

- [ ] Add student name and register number to README.
- [ ] Compile with `mvn clean compile`.
- [ ] Run `mvn exec:java` and save screenshots of actual output.
- [ ] Run 1MB/10MB/50MB line-count experiments and fill Results_Template.md.
- [ ] Run lookup growth tests through 100,000 records and fill results.
- [ ] Install/start MySQL and execute `sql/schema.sql`.
- [ ] Set DB_URL, DB_USER, DB_PASSWORD in terminal (never commit password).
- [ ] Run `mvn clean compile exec:java "-Dexec.args=--db"`.
- [ ] Perform 1k/10k/100k comparison and transaction failure tests.
- [ ] Capture metadata, SELECT results, ResultSetMetaData and failure evidence.
- [ ] Add output screenshots under `screenshots/`.
- [ ] Create GitHub repository CSA09-<RegisterNumber>-Assignment5.
- [ ] Commit each task progressively and add repository URL to README/report.
