# 結合テスト結果報告書

## テスト結果

### 確認結果（2026-02-13 実施）

- **実行環境**: Tsurugi + gRPC サーバ（本アプリ）の結合
- **テスト内容**: doc/test-all.sql に定義した全 37 UDF の呼び出し
- **結果**: **全件成功**

実行ログを確認したところ、全 SELECT 文がエラーなく完了し、期待する値が返却されていることを確認した。

| カテゴリ | 件数 | 確認内容 |
|----------|------|----------|
| Timestamp*（デフォルト TZ） | 10 | 年・月・日・曜日(4=木)・週番号・時・分・秒・ミリ秒・EPOCH ミリ秒 |
| Timestamp*Tz（Asia/Tokyo） | 10 | 上記と同様の値が返却 |
| OffsetTimestamp* | 10 | TIMESTAMP WITH TIME ZONE から同様のコンポーネント取得 |
| Date* | 5 | 年(2026)・月(1)・日(1)・曜日(4)・週番号(1) |
| Time* | 4 | 時(12)・分(34)・秒(56)・ミリ秒(789) |

各クエリは `(1 row)` で完了し、トランザクションのコミットも正常に実施されている。ログにエラーや異常は認められない。

---

## 実行ログ

```text
tgsql> -- 結合テスト用 SQL: 全 UDF メソッドの呼び出し
     | -- 前提: テーブル t1 が存在すること（例: CREATE TABLE t1 (c1 INT); INSERT INTO t1 VALUES (1);）
     | -- Tsurugi は FROM 句が省略できないため FROM t1 を指定
     |
     | -- === TIMESTAMP (LocalDatetime) - システムデフォルトタイムゾ��ン ===
     | SELECT TimestampYear(TIMESTAMP '2026-01-01 12:34:56') FROM t1;
     | SELECT TimestampMonth(TIMESTAMP '2026-01-01 12:34:56') FROM t1;
     | SELECT TimestampDay(TIMESTAMP '2026-01-01 12:34:56') FROM t1;
     | SELECT TimestampDayOfWeek(TIMESTAMP '2026-01-01 12:34:56') FROM t1;
     | SELECT TimestampWeekOfYear(TIMESTAMP '2026-01-01 12:34:56') FROM t1;
     | SELECT TimestampHour(TIMESTAMP '2026-01-01 12:34:56') FROM t1;
     | SELECT TimestampMinute(TIMESTAMP '2026-01-01 12:34:56') FROM t1;
     | SELECT TimestampSecond(TIMESTAMP '2026-01-01 12:34:56') FROM t1;
     | SELECT TimestampMillisecond(TIMESTAMP '2026-01-01 12:34:56') FROM t1;
     | SELECT TimestampEpochMilli(TIMESTAMP '2026-01-01 12:34:56') FROM t1;
     |
     | -- === TIMESTAMP (LocalDatetime) - タイムゾーン指定 ===
     | SELECT TimestampYearTz(TIMESTAMP '2026-01-01 12:34:56', 'Asia/Tokyo') FROM t1;
     | SELECT TimestampMonthTz(TIMESTAMP '2026-01-01 12:34:56', 'Asia/Tokyo') FROM t1;
     | SELECT TimestampDayTz(TIMESTAMP '2026-01-01 12:34:56', 'Asia/Tokyo') FROM t1;
     | SELECT TimestampDayOfWeekTz(TIMESTAMP '2026-01-01 12:34:56', 'Asia/Tokyo') FROM t1;
     | SELECT TimestampWeekOfYearTz(TIMESTAMP '2026-01-01 12:34:56', 'Asia/Tokyo') FROM t1;
     | SELECT TimestampHourTz(TIMESTAMP '2026-01-01 12:34:56', 'Asia/Tokyo') FROM t1;
     | SELECT TimestampMinuteTz(TIMESTAMP '2026-01-01 12:34:56', 'Asia/Tokyo') FROM t1;
     | SELECT TimestampSecondTz(TIMESTAMP '2026-01-01 12:34:56', 'Asia/Tokyo') FROM t1;
     | SELECT TimestampMillisecondTz(TIMESTAMP '2026-01-01 12:34:56', 'Asia/Tokyo') FROM t1;
     | SELECT TimestampEpochMilliTz(TIMESTAMP '2026-01-01 12:34:56', 'Asia/Tokyo') FROM t1;
     |
     | -- === TIMESTAMP WITH TIME ZONE (OffsetDatetime) ===
     | SELECT OffsetTimestampYear(TIMESTAMP WITH TIME ZONE '2026-01-01 12:34:56+09:00') FROM t1;
     | SELECT OffsetTimestampMonth(TIMESTAMP WITH TIME ZONE '2026-01-01 12:34:56+09:00') FROM t1;
     | SELECT OffsetTimestampDay(TIMESTAMP WITH TIME ZONE '2026-01-01 12:34:56+09:00') FROM t1;
     | SELECT OffsetTimestampDayOfWeek(TIMESTAMP WITH TIME ZONE '2026-01-01 12:34:56+09:00') FROM t1;
     | SELECT OffsetTimestampWeekOfYear(TIMESTAMP WITH TIME ZONE '2026-01-01 12:34:56+09:00') FROM t1;
     | SELECT OffsetTimestampHour(TIMESTAMP WITH TIME ZONE '2026-01-01 12:34:56+09:00') FROM t1;
     | SELECT OffsetTimestampMinute(TIMESTAMP WITH TIME ZONE '2026-01-01 12:34:56+09:00') FROM t1;
     | SELECT OffsetTimestampSecond(TIMESTAMP WITH TIME ZONE '2026-01-01 12:34:56+09:00') FROM t1;
     | SELECT OffsetTimestampMillisecond(TIMESTAMP WITH TIME ZONE '2026-01-01 12:34:56+09:00') FROM t1;
     | SELECT OffsetTimestampEpochMilli(TIMESTAMP WITH TIME ZONE '2026-01-01 12:34:56+09:00') FROM t1;
     |
     | -- === DATE (Date) ===
     | SELECT DateYear(DATE '2026-01-01') FROM t1;
     | SELECT DateMonth(DATE '2026-01-01') FROM t1;
     | SELECT DateDay(DATE '2026-01-01') FROM t1;
     | SELECT DateDayOfWeek(DATE '2026-01-01') FROM t1;
     | SELECT DateWeekOfYear(DATE '2026-01-01') FROM t1;
     |
     | -- === TIME (LocalTime) ===
     | SELECT TimeHour(TIME '12:34:56') FROM t1;
     | SELECT TimeMinute(TIME '12:34:56') FROM t1;
     | SELECT TimeSecond(TIME '12:34:56') FROM t1;
     | SELECT TimeMillisecond(TIME '12:34:56.789') FROM t1;
     |
SELECT TimestampYear(TIMESTAMP '2026-01-01 12:34:56') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.261+09:00"
]
Time: 1.068 ms
[@#0: INT]
[2026]
(1 row)
Time: 6.507 ms
transaction commit(DEFAULT) finished implicitly.
Time: 3.361 ms
SELECT TimestampMonth(TIMESTAMP '2026-01-01 12:34:56') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.276+09:00"
]
Time: 1.101 ms
[@#0: INT]
[1]
(1 row)
Time: 7.777 ms
transaction commit(DEFAULT) finished implicitly.
Time: 2.93 ms
SELECT TimestampDay(TIMESTAMP '2026-01-01 12:34:56') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.293+09:00"
]
Time: 1.174 ms
[@#0: INT]
[1]
(1 row)
Time: 7.158 ms
transaction commit(DEFAULT) finished implicitly.
Time: 1.661 ms
SELECT TimestampDayOfWeek(TIMESTAMP '2026-01-01 12:34:56') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.308+09:00"
]
Time: 0.688 ms
[@#0: INT]
[4]
(1 row)
Time: 8.227 ms
transaction commit(DEFAULT) finished implicitly.
Time: 2.197 ms
SELECT TimestampWeekOfYear(TIMESTAMP '2026-01-01 12:34:56') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.324+09:00"
]
Time: 1.063 ms
[@#0: INT]
[1]
(1 row)
Time: 7.905 ms
transaction commit(DEFAULT) finished implicitly.
Time: 2.229 ms
SELECT TimestampHour(TIMESTAMP '2026-01-01 12:34:56') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.339+09:00"
]
Time: 0.943 ms
[@#0: INT]
[12]
(1 row)
Time: 8.102 ms
transaction commit(DEFAULT) finished implicitly.
Time: 1.616 ms
SELECT TimestampMinute(TIMESTAMP '2026-01-01 12:34:56') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.355+09:00"
]
Time: 0.909 ms
[@#0: INT]
[34]
(1 row)
Time: 8.434 ms
transaction commit(DEFAULT) finished implicitly.
Time: 4.316 ms
SELECT TimestampSecond(TIMESTAMP '2026-01-01 12:34:56') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.373+09:00"
]
Time: 0.796 ms
[@#0: INT]
[56]
(1 row)
Time: 4.933 ms
transaction commit(DEFAULT) finished implicitly.
Time: 2.356 ms
SELECT TimestampMillisecond(TIMESTAMP '2026-01-01 12:34:56') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.385+09:00"
]
Time: 0.536 ms
[@#0: INT]
[0]
(1 row)
Time: 5.849 ms
transaction commit(DEFAULT) finished implicitly.
Time: 3.409 ms
SELECT TimestampEpochMilli(TIMESTAMP '2026-01-01 12:34:56') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.397+09:00"
]
Time: 0.911 ms
[@#0: BIGINT]
[1767238496000]
(1 row)
Time: 8.199 ms
transaction commit(DEFAULT) finished implicitly.
Time: 3.557 ms
SELECT TimestampYearTz(TIMESTAMP '2026-01-01 12:34:56', 'Asia/Tokyo') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.414+09:00"
]
Time: 0.757 ms
[@#0: INT]
[2026]
(1 row)
Time: 13.17 ms
transaction commit(DEFAULT) finished implicitly.
Time: 2.826 ms
SELECT TimestampMonthTz(TIMESTAMP '2026-01-01 12:34:56', 'Asia/Tokyo') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.436+09:00"
]
Time: 0.837 ms
[@#0: INT]
[1]
(1 row)
Time: 6.742 ms
transaction commit(DEFAULT) finished implicitly.
Time: 3.804 ms
SELECT TimestampDayTz(TIMESTAMP '2026-01-01 12:34:56', 'Asia/Tokyo') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.452+09:00"
]
Time: 0.846 ms
[@#0: INT]
[1]
(1 row)
Time: 7.061 ms
transaction commit(DEFAULT) finished implicitly.
Time: 2.721 ms
SELECT TimestampDayOfWeekTz(TIMESTAMP '2026-01-01 12:34:56', 'Asia/Tokyo') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.467+09:00"
]
Time: 0.918 ms
[@#0: INT]
[4]
(1 row)
Time: 7.499 ms
transaction commit(DEFAULT) finished implicitly.
Time: 2.793 ms
SELECT TimestampWeekOfYearTz(TIMESTAMP '2026-01-01 12:34:56', 'Asia/Tokyo') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.483+09:00"
]
Time: 0.893 ms
[@#0: INT]
[1]
(1 row)
Time: 6.892 ms
transaction commit(DEFAULT) finished implicitly.
Time: 3.235 ms
SELECT TimestampHourTz(TIMESTAMP '2026-01-01 12:34:56', 'Asia/Tokyo') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.498+09:00"
]
Time: 0.828 ms
[@#0: INT]
[12]
(1 row)
Time: 6.843 ms
transaction commit(DEFAULT) finished implicitly.
Time: 3.761 ms
SELECT TimestampMinuteTz(TIMESTAMP '2026-01-01 12:34:56', 'Asia/Tokyo') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.514+09:00"
]
Time: 0.83 ms
[@#0: INT]
[34]
(1 row)
Time: 9.54 ms
transaction commit(DEFAULT) finished implicitly.
Time: 3.281 ms
SELECT TimestampSecondTz(TIMESTAMP '2026-01-01 12:34:56', 'Asia/Tokyo') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.532+09:00"
]
Time: 0.831 ms
[@#0: INT]
[56]
(1 row)
Time: 6.358 ms
transaction commit(DEFAULT) finished implicitly.
Time: 3.912 ms
SELECT TimestampMillisecondTz(TIMESTAMP '2026-01-01 12:34:56', 'Asia/Tokyo') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.548+09:00"
]
Time: 0.806 ms
[@#0: INT]
[0]
(1 row)
Time: 6.746 ms
transaction commit(DEFAULT) finished implicitly.
Time: 3.243 ms
SELECT TimestampEpochMilliTz(TIMESTAMP '2026-01-01 12:34:56', 'Asia/Tokyo') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.564+09:00"
]
Time: 0.793 ms
[@#0: BIGINT]
[1767238496000]
(1 row)
Time: 7.191 ms
transaction commit(DEFAULT) finished implicitly.
Time: 3.226 ms
SELECT OffsetTimestampYear(TIMESTAMP WITH TIME ZONE '2026-01-01 12:34:56+09:00') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.579+09:00"
]
Time: 0.771 ms
[@#0: INT]
[2026]
(1 row)
Time: 12.549 ms
transaction commit(DEFAULT) finished implicitly.
Time: 4.041 ms
SELECT OffsetTimestampMonth(TIMESTAMP WITH TIME ZONE '2026-01-01 12:34:56+09:00') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.601+09:00"
]
Time: 0.848 ms
[@#0: INT]
[1]
(1 row)
Time: 6.661 ms
transaction commit(DEFAULT) finished implicitly.
Time: 3.819 ms
SELECT OffsetTimestampDay(TIMESTAMP WITH TIME ZONE '2026-01-01 12:34:56+09:00') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.617+09:00"
]
Time: 0.846 ms
[@#0: INT]
[1]
(1 row)
Time: 6.282 ms
transaction commit(DEFAULT) finished implicitly.
Time: 4.498 ms
SELECT OffsetTimestampDayOfWeek(TIMESTAMP WITH TIME ZONE '2026-01-01 12:34:56+09:00') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.633+09:00"
]
Time: 0.913 ms
[@#0: INT]
[4]
(1 row)
Time: 6.99 ms
transaction commit(DEFAULT) finished implicitly.
Time: 1.838 ms
SELECT OffsetTimestampWeekOfYear(TIMESTAMP WITH TIME ZONE '2026-01-01 12:34:56+09:00') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.648+09:00"
]
Time: 0.923 ms
[@#0: INT]
[1]
(1 row)
Time: 6.928 ms
transaction commit(DEFAULT) finished implicitly.
Time: 2.091 ms
SELECT OffsetTimestampHour(TIMESTAMP WITH TIME ZONE '2026-01-01 12:34:56+09:00') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.664+09:00"
]
Time: 0.868 ms
[@#0: INT]
[12]
(1 row)
Time: 6.149 ms
transaction commit(DEFAULT) finished implicitly.
Time: 2.842 ms
SELECT OffsetTimestampMinute(TIMESTAMP WITH TIME ZONE '2026-01-01 12:34:56+09:00') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.678+09:00"
]
Time: 0.586 ms
[@#0: INT]
[34]
(1 row)
Time: 3.933 ms
transaction commit(DEFAULT) finished implicitly.
Time: 1.995 ms
SELECT OffsetTimestampSecond(TIMESTAMP WITH TIME ZONE '2026-01-01 12:34:56+09:00') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.687+09:00"
]
Time: 0.501 ms
[@#0: INT]
[56]
(1 row)
Time: 3.944 ms
transaction commit(DEFAULT) finished implicitly.
Time: 2.44 ms
SELECT OffsetTimestampMillisecond(TIMESTAMP WITH TIME ZONE '2026-01-01 12:34:56+09:00') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.696+09:00"
]
Time: 0.415 ms
[@#0: INT]
[0]
(1 row)
Time: 3.519 ms
transaction commit(DEFAULT) finished implicitly.
Time: 3.078 ms
SELECT OffsetTimestampEpochMilli(TIMESTAMP WITH TIME ZONE '2026-01-01 12:34:56+09:00') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.706+09:00"
]
Time: 0.463 ms
[@#0: BIGINT]
[1767238496000]
(1 row)
Time: 3.746 ms
transaction commit(DEFAULT) finished implicitly.
Time: 2.807 ms
SELECT DateYear(DATE '2026-01-01') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.715+09:00"
]
Time: 0.462 ms
[@#0: INT]
[2026]
(1 row)
Time: 6.438 ms
transaction commit(DEFAULT) finished implicitly.
Time: 3.292 ms
SELECT DateMonth(DATE '2026-01-01') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.727+09:00"
]
Time: 0.334 ms
[@#0: INT]
[1]
(1 row)
Time: 4.168 ms
transaction commit(DEFAULT) finished implicitly.
Time: 2.82 ms
SELECT DateDay(DATE '2026-01-01') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.736+09:00"
]
Time: 0.391 ms
[@#0: INT]
[1]
(1 row)
Time: 3.584 ms
transaction commit(DEFAULT) finished implicitly.
Time: 3.372 ms
SELECT DateDayOfWeek(DATE '2026-01-01') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.746+09:00"
]
Time: 0.528 ms
[@#0: INT]
[4]
(1 row)
Time: 4.345 ms
transaction commit(DEFAULT) finished implicitly.
Time: 2.264 ms
SELECT DateWeekOfYear(DATE '2026-01-01') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.755+09:00"
]
Time: 0.56 ms
[@#0: INT]
[1]
(1 row)
Time: 3.86 ms
transaction commit(DEFAULT) finished implicitly.
Time: 2.632 ms
SELECT TimeHour(TIME '12:34:56') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.764+09:00"
]
Time: 0.568 ms
[@#0: INT]
[12]
(1 row)
Time: 7.155 ms
transaction commit(DEFAULT) finished implicitly.
Time: 2.447 ms
SELECT TimeMinute(TIME '12:34:56') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.777+09:00"
]
Time: 0.604 ms
[@#0: INT]
[34]
(1 row)
Time: 5.286 ms
transaction commit(DEFAULT) finished implicitly.
Time: 3.562 ms
SELECT TimeSecond(TIME '12:34:56') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.790+09:00"
]
Time: 0.606 ms
[@#0: INT]
[56]
(1 row)
Time: 5.545 ms
transaction commit(DEFAULT) finished implicitly.
Time: 2.958 ms
SELECT TimeMillisecond(TIME '12:34:56.789') FROM t1
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-13 11:54:51.803+09:00"
]
Time: 0.685 ms
[@#0: INT]
[789]
(1 row)
Time: 5.806 ms
transaction commit(DEFAULT) finished implicitly.
Time: 1.888 ms
```