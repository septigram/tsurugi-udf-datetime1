## 作例を使ったUDF呼び出しのパフォーマンス

作例のUDFを使うとTIMESPAN型同士を演算して間隔が一定時間以上、というような絞り込みもできるようになる。
 
```sql
tgsql> SELECT * FROM task_log WHERE TimestampEpochMilli(ended_at) - TimestampEpochMilli(started_at) > 499000;
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-18 12:40:39.048+09:00"
]
Time: 0.998 ms
[task_id: INT, started_at: TIMESTAMP, ended_at: TIMESTAMP, result_code: INT]
[142, 2026-02-19 12:18:46.645178000, 2026-02-19 12:27:06.645178000, 0]
[667, 2026-02-23 03:48:46.645178000, 2026-02-23 03:57:06.645178000, 0]
[879, 2026-02-24 15:08:46.645178000, 2026-02-24 15:17:06.645178000, 0]
(3 rows)
Time: 1,922.697 ms
transaction commit(DEFAULT) finished implicitly.
Time: 4.876 ms
```

task_logテーブルは1000レコード。UDFを呼び出すと1923ms、1呼び出しあたり2msのオーバーヘッド。
ちなみに予め計算してINSERT SELECTしたテーブルを使うと6ms。

```sql
tgsql> INSERT INTO task_log2 SELECT task_id, started_at, ended_at, result_code, TimestampEpochMilli(ended_at) - TimestampEpochMilli(started_at) FROM task_log;
...
tgsql> SELECT * FROM task_log2 WHERE interval_ms > 499000;
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2026-02-18 12:39:58.516+09:00"
]
Time: 0.876 ms
[task_id: INT, started_at: TIMESTAMP, ended_at: TIMESTAMP, result_code: INT, interval_ms: BIGINT]
[142, 2026-02-19 12:18:46.645178000, 2026-02-19 12:27:06.645178000, 0, 500000]
[667, 2026-02-23 03:48:46.645178000, 2026-02-23 03:57:06.645178000, 0, 500000]
[879, 2026-02-24 15:08:46.645178000, 2026-02-24 15:17:06.645178000, 0, 500000]
(3 rows)
Time: 6.202 ms
transaction commit(DEFAULT) finished implicitly.
Time: 2.86 ms
```

計測用のダミーデータは tsubakuro-rust-python で作成。

```python
"""10分毎に開始して作業時間が180秒から500秒の間にランダムで終了するタスクを1000個INSERTする

CREATE TABLE task_log (
    task_id INT,
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    result_code INT,
    PRIMARY KEY (task_id, started_at)
);
"""

import random
from datetime import datetime, timedelta

import tsubakuro_rust_python as tsurugi

TASK_COUNT = 1000
INTERVAL_MINUTES = 10
MIN_DURATION_SEC = 180
MAX_DURATION_SEC = 500

INSERT_SQL = """
    INSERT INTO task_log (task_id, started_at, ended_at, result_code)
    VALUES (?, ?, ?, ?)
"""


def create_config() -> tsurugi.Config:
    config = tsurugi.Config()
    config.endpoint = "tcp://localhost:12345"
    config.user = "tsurugi"
    config.password = "password"
    return config


def generate_tasks(count: int) -> list[tuple[int, datetime, datetime, int]]:
    base_time = datetime.now()
    return [
        (
            task_id,
            base_time + timedelta(minutes=task_id * INTERVAL_MINUTES),
            base_time + timedelta(minutes=task_id * INTERVAL_MINUTES)
            + timedelta(seconds=random.randint(MIN_DURATION_SEC, MAX_DURATION_SEC)),
            random.randint(0, 1),
        )
        for task_id in range(count)
    ]


def insert_dummy_data() -> None:
    tasks = generate_tasks(TASK_COUNT)
    config = create_config()

    with tsurugi.connect(config) as connection:
        with connection.cursor() as cursor:
            cursor.executemany(INSERT_SQL, tasks)
            connection.commit()


if __name__ == "__main__":
    insert_dummy_data()

```
