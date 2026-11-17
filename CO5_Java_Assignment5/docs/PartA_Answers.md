# PART A – Persistence Design & Pseudocode (25 Marks)

## A1 – Design a File Format (7)

### A1.1 Record layouts
**Delimited text (CSV):** `passId,regNo,name,routeNo,boardingStop,validityDate,amount`. Use UTF-8; quote a field containing comma, quote or newline and double embedded quote characters (RFC-4180-style escaping). Example: `P1002,192311002,"Meena, Devi",7,Library,2027-12-31,3800.00`. Do not split naïvely on comma when reading quoted fields.

**Binary DataOutputStream layout:** write fields in the same logical order: `writeUTF(passId)`, `writeUTF(regNo)`, `writeUTF(name)`, `writeInt(routeNo)`, `writeUTF(boardingStop)`, `writeUTF(validityDate)` (ISO-8601), `writeDouble(amount)`. Each UTF field is length-prefixed by DataOutputStream's modified-UTF representation; int is 4 bytes and double is 8 bytes. This is variable length, not a fixed-size record. A fixed-size alternative must define maximum UTF-8 byte lengths and pad/truncate safely, with a version/header.

### A1.2 Operations and stream choices
Use `BufferedReader` over `FileReader` (or `Files.newBufferedReader`) for sequential text reads; use `BufferedWriter` over `FileWriter` (or `Files.newBufferedWriter`) for append. Buffering reduces expensive lower-level read/write calls. `FileInputStream`/`FileOutputStream` are appropriate for raw bytes, not convenient delimited text parsing.

**ADD(pass):** validate fields; open `BufferedWriter` in CREATE+APPEND; encode CSV fields; write one record and newline; close using try-with-resources.

**FIND(passId):** if file absent return NOT_FOUND; open `BufferedReader`; for each line parse CSV; if malformed/wrong field count/invalid numeric field record an error and continue; compare field 1 with passId; return matching record; close and return NOT_FOUND if no match.

**LIST(route):** open `BufferedReader`; parse each line; skip/report malformed lines; append records whose routeNo equals requested route; return list.

### A1.3 Damaged data
Detect wrong field count, invalid integer/double/date, malformed quoting and empty required fields. Isolate a bad line, log line number and reason, then continue. Missing file: distinguish first-time empty store from unexpected missing store; create only for ADD and report empty result for FIND/LIST. Incomplete append after power loss: a trailing partial line will fail validation and be quarantined/skipped. For stronger durability write to a temporary file then atomic rename for whole-file rewrites, flush and optionally `FileChannel.force(true)`; maintain backups/checksums. Append-only files cannot guarantee a complete record merely because `close()` was called.

### Alternative: RandomAccessFile
A fixed-size record allows `seek(i * recordSize)` and direct access by record index in O(1). However, finding by pass ID still needs O(n) scanning unless a separate ID-to-offset index is maintained; the assignment's O(1) seek applies when record index i is known. Fixed-width names/stops waste padding space, and updates may overflow field capacity. Text scan is O(n), with O(1) extra memory per line; fixed record seek is O(1) by index and O(1) working memory.

## A2 – Don't Read What You Don't Need (5)

### A2.1 Predict cost
A 50 MiB file contains 52,428,800 bytes. A loop calling `FileInputStream.read()` once per byte makes approximately 52.4 million Java read calls. The stream implementation may involve buffering in the OS, but the Java-level per-byte method/loop overhead is still large; without a buffer the implementation can also trigger many native reads.

### A2.2 Buffered redesign
Wrap the stream in `BufferedInputStream` with e.g. a 64 KiB buffer, or read byte arrays in a loop. The application processes each byte but underlying I/O is performed in blocks, roughly fileSize/bufferSize (about 800 full-buffer reads for 50 MiB at 64 KiB, plus a remainder). Complexity remains O(n) time; auxiliary memory is O(buffer size).

### A2.3 When buffering helps little
If data is already in memory, is delivered in large blocks by an upstream API, or the program performs one large sequential read, another buffer may add copying without reducing I/O calls. It may also add little benefit for tiny files.

### Alternative: Files.readAllLines
Time O(n) to read/parse all bytes; memory O(n) for all lines and String objects (50 MiB file may require substantially more than 50 MiB heap). Buffered streaming is O(n) time and O(buffer size + longest line) memory. Use `readAllLines` for small files needing repeated random access to all lines; use streaming for large logs and bounded-memory processing.

## A3 – Discover the Data-Access Design Pattern (5)

### A3.1 Common and variable parts
Repeated steps: obtain connection, create SQL, bind values, execute, iterate ResultSet, map rows to objects, handle exceptions and close resources. Varying parts: table/column names, entity type, primary key, SQL statements and row-to-object mapping.

### A3.2 Generic DAO
```java
interface Dao<T,K> {
  void insert(T value);
  Optional<T> findById(K id);
  List<T> findAll();
  void update(T value);
  void delete(K id);
}
```
`StudentDao implements Dao<Student,Integer>` and owns Student-specific SQL and mapping. The included `StudentDao.java` is a compilable JDBC implementation using PreparedStatement and try-with-resources.

### A3.3 Object-table mapping
`Student.studentId` → `student.student_id` (PRIMARY KEY); `registerNo` → `register_no` (UNIQUE); `name` → `name`; `email` → `email`. A DAO centralizes persistence logic, so a renamed column is changed in the DAO rather than every GUI/service class. Runtime and asymptotic time/space per query are effectively the same; DAO adds a small abstraction/dispatch cost, not a different database algorithm.

### Alternative: SQL in service/GUI
Every service/GUI that references the renamed column must be located and changed, increasing maintenance risk. DAO centralization reduces change locations. Query execution still has the same database complexity and result memory; DAO itself does not make SQL faster.

## A4 – What Should Be Serialized? (4)

### A4.1 transient and object graph
Mark the live `Connection` field `transient`: it is a runtime resource, not durable application state, and cannot meaningfully be restored from a serialized stream. Password should not be serialized in plaintext; preferably remove it from the persisted object or store a salted password hash separately. If it must remain as a field, mark it transient. The Hostel → Rooms → Residents object graph is traversed recursively; every reachable non-transient object must implement Serializable or serialization throws `NotSerializableException`. Shared references are preserved through object handles; cycles are supported.

### A4.2 Version change
Adding `phoneNumber` while keeping a compatible explicit `serialVersionUID` generally allows an older stream to be read; the new field receives its default value (`null` for String). If computed/default serialVersionUID changes incompatibly, deserialization can throw `InvalidClassException`. Define and manage `serialVersionUID`, and implement migration/defaulting logic where required.

### Alternative: CSV/JSON
Text formats are often larger than compact binary serialization but are human-readable and language-independent. Saving/loading n residents is O(n) time; memory is O(n) if fully materialized, or O(buffer) with streaming parsers. New optional fields can be added compatibly if readers tolerate missing properties; explicit schema/versioning is still needed. Java serialization is convenient for Java object graphs but tightly coupled to class definitions and requires deserialization security precautions.

## A5 – Design for Correctness and Safety (4)

### A5.1 Transaction pseudocode
```text
ALGORITHM Transfer500(sourceWallet, targetWallet)
  OPEN JDBC CONNECTION
  SET AUTO-COMMIT TO FALSE
  TRY
    UPDATE wallet SET balance = balance - 500 WHERE wallet_id = sourceWallet
    IF affected rows != 1 THEN RAISE ERROR
    UPDATE wallet SET balance = balance + 500 WHERE wallet_id = targetWallet
    IF affected rows != 1 THEN RAISE ERROR
    COMMIT
  CATCH ANY ERROR
    ROLLBACK
    REPORT FAILURE
  FINALLY
    RESTORE AUTO-COMMIT
    CLOSE CONNECTION
END ALGORITHM
```
Check sufficient balance and use database constraints/locking as appropriate. Both updates commit together or neither does.

### A5.2 SQL injection
With concatenation, input such as username `' OR '1'='1' -- ` can change the WHERE clause so the predicate is always true and comments out the password comparison (exact comment syntax can depend on SQL dialect/spacing). Use `SELECT ... WHERE id=? AND pwd=?` with PreparedStatement and bind both values; user input is treated as data, not SQL syntax. Passwords should be stored as salted hashes and checked with a password-hashing library, not plaintext.

### Alternative: escaping/regex
Escaping and validation are fragile because SQL quoting rules vary by encoding, database and context; a missed character can reopen injection. PreparedStatement separates SQL structure from parameter data. Validation adds O(length of input) work per request; PreparedStatement also has binding/parse costs, but is the robust baseline and can reuse a compiled statement. The transaction prevents partial wallet updates; two auto-commit updates can leave money deducted without credit if failure occurs between them. Transaction overhead is usually modest compared with the correctness guarantee; each approach performs two updates, O(1) in row count when indexed, but transaction adds log/commit coordination.
