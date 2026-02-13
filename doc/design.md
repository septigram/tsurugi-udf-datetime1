# 設計書: Tsurugi UDF 日時コンポーネント取得サービス

## 1. 概要

本アプリケーションは、Tsurugi の UDF 向けに gRPC サーバとして動作し、DATE / TIME / TIMESTAMP / TIMESTAMP WITH TIME ZONE 型の値から年・月・日・曜日・週番号・時・分・秒・ミリ秒などのコンポーネントを返す関数群を提供する。

- **実装**: Spring Boot（Java）
- **ビルド**: Gradle
- **成果物**: fat jar（実行可能 JAR）

---

## 2. 要件との対応

### 2.1 機能要件

| 要件 | 対応 |
|------|------|
| Tsurugi の UDF 向けに gRPC サーバとして動作 | gRPC サーバを Spring Boot アプリ内で起動し、Unary RPC のみ提供する |
| TIMESTAMP / TIMESTAMP WITH TIME ZONE から年・月・日・曜日・週番号・時・分・秒・ミリ秒・EPOCH ミリ秒を返す | 各コンポーネントごとに 1 RPC を定義（Tsurugi UDF はレスポンス 1 フィールドの制約あり） |
| TIMESTAMP でタイムゾーン省略時はシステムデフォルト TZ、指定時は任意 TZ で変換 | 「省略用」と「タイムゾーン引数あり」で別名の RPC を用意（UDF では optional 非対応のため） |
| DATE から年・月・日・曜日・週番号を返す | DATE 用の 5 種類の RPC を定義 |
| TIME から時・分・秒・ミリ秒を返す | TIME 用の 4 種類の RPC を定義 |

### 2.2 非機能要件

| 要件 | 対応 |
|------|------|
| Spring Boot で実装 | Spring Boot 3.x + spring-boot-starter 系を使用 |
| Gradle でビルド | Gradle 8.x、Java 17 以上を想定 |
| fat jar を生成 | `bootJar` で 1 つの実行可能 JAR を生成 |

---

## 3. アーキテクチャ

### 3.1 全体構成

```
┌─────────────────┐     gRPC (Unary)      ┌──────────────────────────┐
│  Tsurugi        │ ◄──────────────────► │  本アプリ (gRPC Server)   │
│  (UDF プラグイン) │   Request / Response  │  Spring Boot + gRPC       │
└─────────────────┘                       └──────────────────────────┘
```

- Tsurugi が UDF 呼び出し時に gRPC クライアントとして本サーバにリクエストを送り、レスポンスを UDF の戻り値として利用する。
- 通信は Unary RPC のみ（Tsurugi UDF の制約）。
- 現状の Tsurugi UDF では Insecure gRPC のみサポートのため、TLS は使用しない。

### 3.2 技術スタック

| 項目 | 選定 |
|------|------|
| 言語 | Java 17+ |
| フレームワーク | Spring Boot 3.x |
| gRPC | grpc-spring-boot-starter（または grpc-java + 手動登録） |
| ビルド | Gradle 8.x |
| Protocol Buffers | protobuf-gradle-plugin でコンパイル、tsurugi_types.proto は import して利用 |

---

## 4. インターフェース定義（.proto）

### 4.1 方針

- Tsurugi の [udf-proto 制約](doc/tsurugi-manual/udf-proto_ja.md) に従う。
  - 単一の `service` ブロックのみ。
  - 各 RPC は Unary、リクエストは `message`、レスポンスは **フィールド 1 つの message**。
  - `optional` / `repeated` は使用しない。TIMESTAMP の「タイムゾーン省略」と「指定」は **別名の RPC** で分ける。
- 日時型は [tsurugi_types.proto](doc/tsurugi-manual/tsurugi_types.proto) の型を使用する。
  - DATE → `tsurugidb.udf.Date`
  - TIME → `tsurugidb.udf.LocalTime`
  - TIMESTAMP → `tsurugidb.udf.LocalDatetime`
  - TIMESTAMP WITH TIME ZONE → `tsurugidb.udf.OffsetDatetime`
- 戻り値はスカラーに合わせて `int32` / `int64` のラップ用 message を定義する（レスポンスは message 必須のため）。

### 4.2 型・値の定義

- **月**: 1 = JANUARY ～ 12 = DECEMBER（`int32`）
- **曜日**: 0 = SUNDAY, 1 = MONDAY, …, 6 = SATURDAY（`int32`）
- **週番号**: 1 から採番（ISO 8601 の週番号。1–52 または 53）（`int32`）
- **EPOCH からの経過ミリ秒**: `int64`

### 4.3 サービス・メッセージ一覧

#### 4.3.1 TIMESTAMP（LocalDatetime）— システムデフォルトタイムゾーン

入力: `LocalDatetime` のみ。

| RPC 名 | リクエスト | レスポンス（1 フィールド） | 説明 |
|--------|------------|----------------------------|------|
| TimestampYear | TimestampRequest | Int32Value | 年 |
| TimestampMonth | TimestampRequest | Int32Value | 月 (1–12) |
| TimestampDay | TimestampRequest | Int32Value | 日 |
| TimestampDayOfWeek | TimestampRequest | Int32Value | 曜日 (0–6) |
| TimestampWeekOfYear | TimestampRequest | Int32Value | 週番号 (1–52/53) |
| TimestampHour | TimestampRequest | Int32Value | 時 |
| TimestampMinute | TimestampRequest | Int32Value | 分 |
| TimestampSecond | TimestampRequest | Int32Value | 秒 |
| TimestampMillisecond | TimestampRequest | Int32Value | ミリ秒 |
| TimestampEpochMilli | TimestampRequest | Int64Value | EPOCH からの経過ミリ秒 |

- `TimestampRequest`: フィールド 1 つ `tsurugidb.udf.LocalDatetime value = 1;`
- 変換には JVM のデフォルト `ZoneId`（`ZoneId.systemDefault()`）を使用する。

#### 4.3.2 TIMESTAMP（LocalDatetime）— タイムゾーン指定

入力: `LocalDatetime` + タイムゾーン（例: `"Asia/Tokyo"`）。optional が使えないため、上記と別名の RPC とする。

| RPC 名 | リクエスト | レスポンス | 説明 |
|--------|------------|------------|------|
| TimestampYearTz | TimestampWithTzRequest | Int32Value | 年 |
| TimestampMonthTz | TimestampWithTzRequest | Int32Value | 月 (1–12) |
| TimestampDayTz | TimestampWithTzRequest | Int32Value | 日 |
| TimestampDayOfWeekTz | TimestampWithTzRequest | Int32Value | 曜日 (0–6) |
| TimestampWeekOfYearTz | TimestampWithTzRequest | Int32Value | 週番号 (1–52/53) |
| TimestampHourTz | TimestampWithTzRequest | Int32Value | 時 |
| TimestampMinuteTz | TimestampWithTzRequest | Int32Value | 分 |
| TimestampSecondTz | TimestampWithTzRequest | Int32Value | 秒 |
| TimestampMillisecondTz | TimestampWithTzRequest | Int32Value | ミリ秒 |
| TimestampEpochMilliTz | TimestampWithTzRequest | Int64Value | EPOCH からの経過ミリ秒 |

- `TimestampWithTzRequest`: `LocalDatetime value = 1;`, `string time_zone = 2;`（順序どおり引数に対応）

#### 4.3.3 TIMESTAMP WITH TIME ZONE（OffsetDatetime）

入力: `OffsetDatetime` のみ。すでにタイムゾーン情報を持つため、そのオフセットで解釈する。

| RPC 名 | リクエスト | レスポンス | 説明 |
|--------|------------|------------|------|
| OffsetTimestampYear | OffsetTimestampRequest | Int32Value | 年 |
| OffsetTimestampMonth | OffsetTimestampRequest | Int32Value | 月 (1–12) |
| OffsetTimestampDay | OffsetTimestampRequest | Int32Value | 日 |
| OffsetTimestampDayOfWeek | OffsetTimestampRequest | Int32Value | 曜日 (0–6) |
| OffsetTimestampWeekOfYear | OffsetTimestampRequest | Int32Value | 週番号 (1–52/53) |
| OffsetTimestampHour | OffsetTimestampRequest | Int32Value | 時 |
| OffsetTimestampMinute | OffsetTimestampRequest | Int32Value | 分 |
| OffsetTimestampSecond | OffsetTimestampRequest | Int32Value | 秒 |
| OffsetTimestampMillisecond | OffsetTimestampRequest | Int32Value | ミリ秒 |
| OffsetTimestampEpochMilli | OffsetTimestampRequest | Int64Value | EPOCH からの経過ミリ秒 |

- `OffsetTimestampRequest`: フィールド 1 つ `tsurugidb.udf.OffsetDatetime value = 1;`

#### 4.3.4 DATE（Date）

| RPC 名 | リクエスト | レスポンス | 説明 |
|--------|------------|------------|------|
| DateYear | DateRequest | Int32Value | 年 |
| DateMonth | DateRequest | Int32Value | 月 (1–12) |
| DateDay | DateRequest | Int32Value | 日 |
| DateDayOfWeek | DateRequest | Int32Value | 曜日 (0–6) |
| DateWeekOfYear | DateRequest | Int32Value | 週番号 (1–52/53) |

- `DateRequest`: フィールド 1 つ `tsurugidb.udf.Date value = 1;`

#### 4.3.5 TIME（LocalTime）

| RPC 名 | リクエスト | レスポンス | 説明 |
|--------|------------|------------|------|
| TimeHour | TimeRequest | Int32Value | 時 |
| TimeMinute | TimeRequest | Int32Value | 分 |
| TimeSecond | TimeRequest | Int32Value | 秒 |
| TimeMillisecond | TimeRequest | Int32Value | ミリ秒 |

- `TimeRequest`: フィールド 1 つ `tsurugidb.udf.LocalTime value = 1;`

#### 4.3.6 共通レスポンス型

- `message Int32Value { int32 value = 1; }`
- `message Int64Value { int64 value = 1; }`

### 4.4 .proto ファイル配置と import

- プロジェクト内に `src/main/proto/` を用意し、ここにサービス定義用の `.proto` を置く。
- `tsurugi_types.proto` は `doc/tsurugi-manual/` または `proto/tsurugidb/udf/` に配置し、`import "tsurugidb/udf/tsurugi_types.proto";` で参照する。
- udf-plugin-builder でプラグイン生成する際は、[udf-plugin の tsurugidb.udf 利用](doc/tsurugi-manual/udf-plugin_ja.md#tsurugidbudf-メッセージ型-の利用) に従い、`--proto-path` と `--proto-file` で本プロジェクトの proto と `tsurugi_types.proto` の両方を指定する。

---

## 5. データ変換ロジック

### 5.1 Tsurugi 型の解釈（tsurugi_types.proto より）

- **Date**: `days` = 1970-01-01 からの日数（sint32）。Java では `LocalDate.ofEpochDay(days)` で変換。
- **LocalTime**: `nanos` = 0 時からのナノ秒（sint64）。ナノ秒 → 時・分・秒・ミリ秒は除算と剰余で算出。
- **LocalDatetime**: `offset_seconds` は「ローカルタイムゾーンでの EPOCH からのオフセット秒」、`nano_adjustment` は 0～10^9-1。  
  → 秒＋ナノ秒から `Instant` を構成するには、**ローカルタイムゾーン**（デフォルト TZ または指定 TZ）を決め、その TZ で `LocalDateTime` を組み立ててから `ZonedDateTime` → `Instant` に変換する。逆に、指定 TZ で解釈する場合は、同じ秒・ナノ秒を「その TZ の 1970-01-01 00:00:00 からのオフセット」として解釈する。
- **OffsetDatetime**: `offset_seconds` は UTC の EPOCH からの秒、`nano_adjustment`、`time_zone_offset`（分）。  
  → UTC で `Instant` を組み立て、表示用には `time_zone_offset` で `ZoneOffset` を組み、`OffsetDateTime` としてコンポーネント抽出する。

### 5.2 コンポーネント取得

- **年・月・日・曜日・週番号**: `LocalDate` / `ZonedDateTime` / `OffsetDateTime` の `getYear()`, `getMonthValue()`, `getDayOfMonth()`, `getDayOfWeek().getValue() % 7`（Tsurugi は 0=Sunday のため、`getValue()` は 1=Monday…7=Sunday → 0–6 に変換）、週番号は ISO 8601 に従い `IsoFields.WEEK_OF_WEEK_BASED_YEAR` で取得（1–52 または 53）。
- **時・分・秒・ミリ秒**: `LocalTime` または `ZonedDateTime`/`OffsetDateTime` の `toLocalTime()` から取得。ミリ秒はナノ秒を 1_000_000 で割る。
- **EPOCH ミリ秒**: `Instant` の `toEpochMilli()`。

### 5.3 エラー扱い

- 不正なタイムゾーン ID やオーバーフローは、gRPC の `Status.INVALID_ARGUMENT` 等で返す。
- 必要に応じてログ出力し、クライアント（Tsurugi）には gRPC ステータスで理由を返す。

---

## 6. プロジェクト構成（案）

- **Java パッケージ**: `jp.septigram.tsurugi.udf.datetime1`（サブパッケージとして `config` / `converter` / `service` を置く）。

```
tsurugi-udf-datetime1/
├── build.gradle
├── settings.gradle
├── doc/
│   ├── requirements.md
│   ├── design.md
│   └── tsurugi-manual/
├── src/
│   ├── main/
│   │   ├── resources/
│   │   │   └── application.yml   # Spring の設定ファイル
│   │   ├── java/
│   │   │   └── jp/septigram/tsurugi/udf/datetime1/
│   │   │       ├── Application.java
│   │   │       ├── config/
│   │   │       │   └── GrpcServerConfig.java   # gRPC サーバポート・ライフサイクル
│   │   │       ├── converter/
│   │   │       │   ├── DateConverter.java     # Date ↔ LocalDate
│   │   │       │   ├── LocalTimeConverter.java
│   │   │       │   ├── LocalDatetimeConverter.java
│   │   │       │   └── OffsetDatetimeConverter.java
│   │   │       └── service/
│   │   │           └── DateTimeUdfServiceImpl.java  # 全 RPC の実装
│   │   └── proto/
│   │       └── (proto ファイルおよび tsurugidb/udf/tsurugi_types.proto の配置)
│   └── test/
│       └── java/
│           └── jp/septigram/tsurugi/udf/datetime1/  # 同パッケージの単体・統合テスト
└── README.md
```

- **Application**: Spring Boot のエントリポイント（`jp.septigram.tsurugi.udf.datetime1.Application`）。gRPC サーバは `@Bean` または auto-configuration で起動。
- **application.yml**: Spring の設定ファイル。gRPC のポートなどは本ファイルで指定する。
- **config**: gRPC のポート（例: 50051）、Netty サーバの設定。
- **converter**: `tsurugidb.udf.*` のメッセージと Java の `LocalDate` / `LocalTime` / `LocalDateTime` / `ZonedDateTime` / `OffsetDateTime` / `Instant` の相互変換。
- **service**: 生成された gRPC の Service 基底クラスを継承し、上記 RPC を実装。converter を利用してコンポーネントを取得し、`Int32Value` / `Int64Value` で返す。

---

## 7. ビルド・実行

### 7.1 ビルド

- `./gradlew clean build` でコンパイル・テスト・fat jar 生成。
- fat jar は `build/libs/*-boot.jar` など（Spring Boot の `bootJar` タスクの出力）。

### 7.2 実行

- `java -jar build/libs/<artifact>-boot.jar` で起動。
- 起動時に gRPC サーバがリッスンするポート（例: 50051）を、`application.yml`（Spring の設定ファイル）および環境変数で指定できるようにする。

### 7.3 UDF プラグインとの対応

- 本サービス用の `.proto` と `tsurugi_types.proto` を指定して `udf-plugin-builder` を実行し、`lib*.so` と `lib*.ini` を生成。
- `lib*.ini` の `endpoint` を、本アプリの gRPC のアドレス（例: `localhost:50051`）に合わせる。
- 生成したプラグインを Tsurugi のプラグイン配置ディレクトリに配置し、Tsurugi を起動すると、SQL から上記 UDF を呼び出せる。

---

## 8. 制約・注意事項（Tsurugi UDF 既知の制約）

- **optional 非対応**: タイムゾーン省略版と指定版は別名 RPC で実現する（本設計で対応済み）。
- **レスポンス 1 フィールド**: 各 RPC の戻り値は 1 つのスカラーをラップする message のみ（Int32Value / Int64Value）。
- **Unary RPC のみ**: Streaming は使用しない。
- **Secure gRPC 未対応**: Insecure でリッスンする。
- **tsurugidb.udf 利用時**: `tsurugi_types.proto` を import する UDF プラグインは、既知の制約により 1 本にまとめてデプロイする必要がある（[udf-known-issues](doc/tsurugi-manual/udf-known-issues_ja.md)）。

---

## 9. 今後の拡張案

- 設定ファイルや環境変数でデフォルトタイムゾーンを上書き可能にする。
- ヘルスチェック用の簡単な RPC（例: Echo）を追加し、Tsurugi 側や運用ツールから疎通確認に利用する。
