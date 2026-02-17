# Tsurugi UDF 日時コンポーネント取得サービス

## 概要

Tsurugi DB の User Defined Function (UDF) は gRPC サーバを呼び出して実行されます。本サービスは、DATE / TIME / TIMESTAMP / TIMESTAMP WITH TIME ZONE 型の値から年・月・日・曜日・週番号・時・分・秒・ミリ秒などのコンポーネントを返す UDF 群を提供する gRPC サーバです。Spring Boot で実装されています。

## 機能仕様

### 提供する UDF 一覧

| カテゴリ | 関数名 | 引数 | 戻り値 | 説明 |
|----------|--------|------|--------|------|
| **TIMESTAMP（デフォルト TZ）** | TimestampYear | TIMESTAMP | INT | 年 |
| | TimestampMonth | TIMESTAMP | INT | 月 (1–12) |
| | TimestampDay | TIMESTAMP | INT | 日 |
| | TimestampDayOfWeek | TIMESTAMP | INT | 曜日 (0–6) |
| | TimestampWeekOfYear | TIMESTAMP | INT | 週番号 (ISO 8601, 1–52/53) |
| | TimestampHour | TIMESTAMP | INT | 時 |
| | TimestampMinute | TIMESTAMP | INT | 分 |
| | TimestampSecond | TIMESTAMP | INT | 秒 |
| | TimestampMillisecond | TIMESTAMP | INT | ミリ秒 |
| | TimestampEpochMilli | TIMESTAMP | BIGINT | EPOCH からの経過ミリ秒 |
| **TIMESTAMP（タイムゾーン指定）** | TimestampYearTz | TIMESTAMP, VARCHAR | INT | 年（指定 TZ で解釈） |
| | … 同上 … | TIMESTAMP, VARCHAR | — | 月・日・曜日・週番号・時・分・秒・ミリ秒・EPOCH ミリ秒 |
| **TIMESTAMP WITH TIME ZONE** | OffsetTimestampYear | TIMESTAMP WITH TIME ZONE | INT | 年 |
| | … 同上 … | TIMESTAMP WITH TIME ZONE | — | 月・日・曜日・週番号・時・分・秒・ミリ秒・EPOCH ミリ秒 |
| **DATE** | DateYear | DATE | INT | 年 |
| | DateMonth | DATE | INT | 月 (1–12) |
| | DateDay | DATE | INT | 日 |
| | DateDayOfWeek | DATE | INT | 曜日 (0–6) |
| | DateWeekOfYear | DATE | INT | 週番号 (1–52/53) |
| **TIME** | TimeHour | TIME | INT | 時 |
| | TimeMinute | TIME | INT | 分 |
| | TimeSecond | TIME | INT | 秒 |
| | TimeMillisecond | TIME | INT | ミリ秒 |

### 値の定義

- **曜日**: 0 = 日曜, 1 = 月曜, …, 6 = 土曜
- **週番号**: ISO 8601 に準拠。1–52 または 53

### タイムゾーンの扱い

- **Timestamp*（Tz なし）**: JVM のデフォルトタイムゾーン（`ZoneId.systemDefault()`）で解釈
- **Timestamp*Tz**: 第 2 引数で指定したタイムゾーン（例: `'Asia/Tokyo'`）で解釈
- **OffsetTimestamp***: 値に含まれるタイムゾーンオフセットで解釈

---

## 使用方法

### 1. 本サービスの起動

```bash
./gradlew bootRun
# または
java -jar build/libs/tsurugi-udf-datetime1-0.0.1-SNAPSHOT.jar
```

gRPC サーバはデフォルトでポート **50051** でリッスンします。`src/main/resources/application.yml` の `grpc.server.port` で変更できます。

### 2. Tsurugi での UDF 呼び出し

1. **UDF プラグインの生成**: `udf-plugin-builder` で本プロジェクトの proto からプラグインを生成
   ```bash
   udf-plugin-builder --proto-path . /path/to/tsurugi-udf/proto \
     --proto-file src/main/proto/datetime-service.proto /path/to/tsurugi-udf/proto/tsurugidb/udf/tsurugi_types.proto
   ```

2. **プラグインのデプロイ**: 生成した `lib*.so` と `lib*.ini` を Tsurugi のプラグイン配置ディレクトリに配置。`lib*.ini` の `endpoint` を本サービスのアドレス（例: `dns:///localhost:50051`）に合わせる。

3. **SQL からの呼び出し**: Tsurugi を起動後、tgsql 等で UDF を呼び出し
   ```sql
   -- 例: テーブル t1 が必要（FROM 句は必須）
   SELECT TimestampYear(TIMESTAMP '2026-01-01 12:34:56') FROM t1;
   SELECT DateYear(DATE '2026-01-01') FROM t1;
   SELECT TimeHour(TIME '12:34:56') FROM t1;
   SELECT TimestampYearTz(TIMESTAMP '2026-01-01 12:34:56', 'Asia/Tokyo') FROM t1;
   ```

結合テスト用の全 UDF 呼び出し例は `doc/test-all.sql` を参照してください。

---

## ビルドと実行

- **ビルド**: `./gradlew clean build`  
  - 初回は Gradle ラッパーで Gradle を取得するため時間がかかります。`./gradlew` が動作しない場合は、Gradle をインストールしてプロジェクトで `gradle wrapper` を実行し、ラッパーを生成してください。
- **起動**: `./gradlew bootRun` または `java -jar build/libs/tsurugi-udf-datetime1-0.0.1-SNAPSHOT.jar`

---

## 参考

- **設計書**: [doc/design.md](doc/design.md)（アーキテクチャ、データ変換ロジック、プロジェクト構成）
- **結合テスト**: [doc/test-all.sql](doc/test-all.sql)、[doc/test-result.md](doc/test-result.md)

## ライセンス

本プロジェクトは Apache License 2.0 の下で提供されます。詳細は [LICENSE](LICENSE) を参照してください。

