# Tsurugi UDFがGROUP BY句で使えるか実験

```sql
CREATE TABLE dt1(val INT, created_at TIMESTAMP);

INSERT INTO dt1 VALUES
 (1, '2026-02-13 12:00:00'),
 (2, '2026-02-13 13:00:00'),
 (3, '2026-02-13 14:00:00'),
 (4, '2026-02-14 15:00:00'),
 (5, '2026-02-14 16:00:00'),
 (6, '2026-02-14 17:00:00'),
 (7, '2026-02-14 18:00:00');
```

結論：使えない

```text
tgsql> SELECT SUM(val) FROM dt1 GROUP BY TimestampDay(created_at);
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 12:05:17.760+09:00"
]
Time: 0.96 ms
UNSUPPORTED_COMPILER_FEATURE_EXCEPTION (SQL-03010: compile failed with error:unsupported_feature message:"plain variable is required in GROUP BY clause" location:<input>:1:35+24)
Time: 1.641 ms
transaction rollback finished implicitly.
Time: 0.639 ms
```
