# Experimental Results – Fill with your actual run data

**Machine:** ____________________  **OS:** ____________________  
**JDK:** ____________________  **Maven:** ____________________  
**MySQL version:** ____________________  **Date:** ____________________

## B1.3 – Line counting
Run each test at least 3 times; report median. Generate files of exact sizes or record actual byte sizes.

| File size | Actual bytes | Unbuffered time (ms) | Buffered time (ms) | Speed-up |
|---|---:|---:|---:|---:|
| 1 MB | | | | |
| 10 MB | | | | |
| 50 MB | | | | |

## B1.4 – Pass lookup growth
| Records | Lookup target | Time (ms) | Notes |
|---:|---|---:|---|
| 1,000 | last record | | |
| 10,000 | last record | | |
| 50,000 | last record | | |
| 100,000 | last record | | |

## B3.2 – JDBC insert comparison
| Rows | Statement (ms) | PreparedStatement (ms) | PreparedStatement batch (ms) |
|---:|---:|---:|---:|
| 1,000 | | | |
| 10,000 | | | |
| 100,000 | | | |

## B3.3 – Transaction test
| Mode | Failure injected at row | Rows remaining after failure | Expected |
|---|---:|---:|---|
| Auto-commit | halfway | | Earlier inserts may remain |
| One transaction | halfway | | 0 rows from failed transaction |

## Interpretation
Explain trends using the measured values. Mention warm-up, disk cache, JVM, database location/network, indexes, and repeated trials. Do not report expected trends as measured results.
