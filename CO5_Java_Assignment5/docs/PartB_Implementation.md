# PART B – Implementation & Experimental Investigation (25 Marks)

## B1 – File store and instrumentation (7)
`PassStore` provides add, find and listByRoute. The CSV parser supports quoted commas and doubled quotes. `find` ignores malformed records and continues. `LineCounter` implements byte-at-a-time and 64 KiB buffered reads. `Main` creates valid and damaged records and runs a small real timing demo.

Validation cases:
| Case | Input/action | Expected verification |
|---|---|---|
| Normal add/find | Add P1003, then find P1003 | Correct seven fields returned |
| Missing ID | Find P9999 | `null`, no crash |
| Damaged line | Insert row with wrong field count | Skipped; later valid row still found |
| Route list | List route 7 | Both route-7 valid rows returned |
| Quoted name | `Meena, Devi` | Comma remains part of name |

The provided `PassStore` record uses seven fields (the full assignment record), while the PDF's TODO starter uses five fields. This project extends the starter to include boarding stop and validity date required by A1.

## B2 – Adversarial inputs (5)
The database demo inserts a known test user then tries an injection-shaped ID with a wrong password. Unsafe SQL concatenation may accept it; PreparedStatement should reject it. The damaged CSV test includes a wrong field count, non-numeric route/amount and an empty/malformed row; `readValidRows` returns valid rows and a line-numbered failure list.

## B3 – Statement vs PreparedStatement vs batch (8)
`JdbcTasks.insertGenerated` supports `statement`, `prepared`, and `batch`. `PassLoader.loadAll` is the completed TODO implementation: binds all seven values, batches every 1,000 rows, executes the remainder, commits once, rolls back on failure, restores original auto-commit. `sql/schema.sql` creates the InnoDB table.

For the assignment's required tests, call `insertGenerated(connection, rowCount, mode, transaction)` for 1,000, 10,000 and 100,000 records, clearing the table between runs. Record elapsed milliseconds. Test both auto-commit and one transaction. To demonstrate rollback, create a deliberately invalid row (e.g. non-numeric route in `PassLoader` input); verify the table has no rows from that transaction after the exception. Do not use a duplicate primary key as a rollback test if previous test rows remain.

Expected trend, not a claimed measurement: Statement incurs repeated SQL parsing/round trips; PreparedStatement reuses parameterized SQL; batching reduces round trips and is typically fastest for large imports. A single counter issue is simplest with one PreparedStatement; nightly 100,000-row import is a strong batch + transaction use case.

## B4 – Final engineering decision (5)
`PassStore.readValidRows` validates each row and collects rejected line numbers/reasons. Pass its valid rows to `PassLoader.loadAll` to migrate. `JdbcTasks.metadata` reports database product/version, JDBC driver/version and table names. Database metadata helps detect environment differences during deployment.

**Decision:** use a relational database (MySQL/InnoDB) for 12 campuses, 100× data, concurrent counters, indexed pass-ID lookup, transactions and centralized access control. Text files are suitable for interchange/logging and small local stores; serialization is not a good shared multi-user persistence system. A database provides concurrent transactions, constraints, indexes and a single authoritative store.

### Alternative: one serialized file per campus
Each campus can access its local file, but a search across all campuses must query 12 files (O(12n), asymptotically O(n) when campus count is fixed) and aggregate results. Total storage remains O(n) plus per-file overhead. Concurrent counters require file locks and careful atomic updates; cross-campus consistency and global uniqueness are difficult. A shared database provides transaction isolation and indexes for global search.
