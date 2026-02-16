# Tsurugi の UDF サーバを Spring Boot で作成する

次世代 RDB [劔"Tsurugi"](https://www.tsurugidb.com/) は v1.8.0 で [UDF（User-Defined Function）](https://github.com/project-tsurugi/tsurugi-udf/blob/master/docs/udf-overview_ja.md) に対応しました。Tsurugi の UDF は gRPC サーバとして実装するだけで SQL から呼び出せるため、他 DBMS の UDF に比べて実装が容易です。本記事では、Tsurugi に不足している日付・時刻のコンポーネント取得関数（年・月・日・曜日・週番号・時・分・秒・ミリ秒など）を、Spring Boot と gRPC で実装した事例を紹介します。

## 背景

### UDFとは

一般に SQL の UDF は、標準の SQL 関数では対応できない計算やロジックを、ユーザーが独自に定義し SQL 文の中で再利用するための機能です。スカラー値やテーブル関数として定義でき、データクレンジングやビジネスロジックの共通化に役立ちます。

### PostgreSQL の UDF

例えば PostgreSQL の UDF を作成する場合、C で書いたコードを実行環境に合わせて共有ライブラリ（.so）にビルドし、`CREATE FUNCTION` で登録するとサーバがオンデマンドで動的ロードする形で利用します。同じプロセス内で動作するため、PostgreSQL の挙動に影響しないようメモリ管理（palloc/pfree）やスレッド安全性に配慮する必要があり、実装の難易度は高くなります。

### Tsurugi の UDF

Tsurugi 1.8.0 で利用可能になった UDF は、複数の値を返せないなどの制約はあるものの実行プロセスを gRPC で分離することで、従来の UDF の困難さを回避する設計になっています。SQL から呼び出す関数を gRPC で定義すれば、Tsurugi 側の呼び出し処理を組み込んだ共有ライブラリを自動生成できます。

開発者は定義通りに gRPC サーバ側を実装するだけで、Tsurugi からの安全な UDF 呼び出しを実現できます。

### Tsurugi UDF の作例

今回の作例では、Tsurugi 1.8.0 時点では提供されていない DATE / TIME / TIMESTAMP / TIMESTAMP WITH TIME ZONE のコンポーネント（年・月・曜日・週番号・時・分・秒・ミリ秒など）を取り出す関数群を、Spring Boot アプリとして gRPC サーバ化しました。

## 開発手順

### 全体の流れ

1. 要件を策定
2. gRPC定義(proto ファイル)を作成
3. proto ファイルを元に gRPC サーバを Spring boot で実装
4. 実装した gRPC サーバを起動
5. Tsurugi 側共有ライブラリを proto ファイルから生成
6. Tsurugi に共有ライブラリを組み込んで起動
7. UDF を呼び出す SQL を実行して動作確認

### 要件と設計

要件は [doc/requirements.md](https://github.com/septigram/tsurugi-udf-datetime1/blob/main/doc/requirements.md) にまとめ、設計は [doc/design.md](https://github.com/septigram/tsurugi-udf-datetime1/blob/main/doc/design.md) に記載しています。主なポイントは次のとおりです。

- **アーキテクチャ**: Tsurugi の UDF プラグインが gRPC クライアントとなり、本アプリ（gRPC サーバ）に Unary RPC でリクエストを送り、戻り値を UDF の結果として利用する。
- **Tsurugi UDF の制約**: 各 RPC のレスポンスは「1 フィールドのみの message」にすること。`optional` が使えないため、タイムゾーン省略版と指定版は別名の RPC（例: `TimestampYear` と `TimestampYearTz`）で分ける。
- **型**: 日付・時刻型は Tsurugi 公式の [tsurugi_types.proto](https://github.com/project-tsurugi/tsurugi-udf/blob/master/proto/tsurugidb/udf/tsurugi_types.proto) を import して利用する（`Date`, `LocalTime`, `LocalDatetime`, `OffsetDatetime` など）。

本プロジェクトの .proto では、TIMESTAMP（デフォルト TZ）用・TIMESTAMP（タイムゾーン指定）用・TIMESTAMP WITH TIME ZONE 用・DATE 用・TIME 用の 5 系統に分け、合計 37 個の RPC を定義しています。戻り値は `Int32Value` または `Int64Value`（EPOCH ミリ秒用）でラップします。

```proto
syntax = "proto3";

import "tsurugidb/udf/tsurugi_types.proto";

option java_package = "jp.septigram.tsurugi.udf.datetime1";

// Tsurugi UDF 日時コンポーネント取得サービス
service DateTimeService {
  // TIMESTAMP (LocalDatetime) - システムデフォルトタイムゾーン
  rpc TimestampYear (TimestampRequest) returns (Int32Value) {}
  rpc TimestampMonth (TimestampRequest) returns (Int32Value) {}
  rpc TimestampDay (TimestampRequest) returns (Int32Value) {}
  rpc TimestampDayOfWeek (TimestampRequest) returns (Int32Value) {}
  rpc TimestampWeekOfYear (TimestampRequest) returns (Int32Value) {}
  rpc TimestampHour (TimestampRequest) returns (Int32Value) {}
  rpc TimestampMinute (TimestampRequest) returns (Int32Value) {}
  rpc TimestampSecond (TimestampRequest) returns (Int32Value) {}
  rpc TimestampMillisecond (TimestampRequest) returns (Int32Value) {}
  rpc TimestampEpochMilli (TimestampRequest) returns (Int64Value) {}

  // TIMESTAMP (LocalDatetime) - タイムゾーン指定
  rpc TimestampYearTz (TimestampWithTzRequest) returns (Int32Value) {}
  rpc TimestampMonthTz (TimestampWithTzRequest) returns (Int32Value) {}
  rpc TimestampDayTz (TimestampWithTzRequest) returns (Int32Value) {}
  rpc TimestampDayOfWeekTz (TimestampWithTzRequest) returns (Int32Value) {}
  rpc TimestampWeekOfYearTz (TimestampWithTzRequest) returns (Int32Value) {}
  rpc TimestampHourTz (TimestampWithTzRequest) returns (Int32Value) {}
  rpc TimestampMinuteTz (TimestampWithTzRequest) returns (Int32Value) {}
  rpc TimestampSecondTz (TimestampWithTzRequest) returns (Int32Value) {}
  rpc TimestampMillisecondTz (TimestampWithTzRequest) returns (Int32Value) {}
  rpc TimestampEpochMilliTz (TimestampWithTzRequest) returns (Int64Value) {}

  // TIMESTAMP WITH TIME ZONE (OffsetDatetime)
  rpc OffsetTimestampYear (OffsetTimestampRequest) returns (Int32Value) {}
  rpc OffsetTimestampMonth (OffsetTimestampRequest) returns (Int32Value) {}
  rpc OffsetTimestampDay (OffsetTimestampRequest) returns (Int32Value) {}
  rpc OffsetTimestampDayOfWeek (OffsetTimestampRequest) returns (Int32Value) {}
  rpc OffsetTimestampWeekOfYear (OffsetTimestampRequest) returns (Int32Value) {}
  rpc OffsetTimestampHour (OffsetTimestampRequest) returns (Int32Value) {}
  rpc OffsetTimestampMinute (OffsetTimestampRequest) returns (Int32Value) {}
  rpc OffsetTimestampSecond (OffsetTimestampRequest) returns (Int32Value) {}
  rpc OffsetTimestampMillisecond (OffsetTimestampRequest) returns (Int32Value) {}
  rpc OffsetTimestampEpochMilli (OffsetTimestampRequest) returns (Int64Value) {}

  // DATE (Date)
  rpc DateYear (DateRequest) returns (Int32Value) {}
  rpc DateMonth (DateRequest) returns (Int32Value) {}
  rpc DateDay (DateRequest) returns (Int32Value) {}
  rpc DateDayOfWeek (DateRequest) returns (Int32Value) {}
  rpc DateWeekOfYear (DateRequest) returns (Int32Value) {}

  // TIME (LocalTime)
  rpc TimeHour (TimeRequest) returns (Int32Value) {}
  rpc TimeMinute (TimeRequest) returns (Int32Value) {}
  rpc TimeSecond (TimeRequest) returns (Int32Value) {}
  rpc TimeMillisecond (TimeRequest) returns (Int32Value) {}
}

// 共通レスポンス型
message Int32Value {
  int32 value = 1;
}
message Int64Value {
  int64 value = 1;
}

// リクエスト型
message TimestampRequest {
  tsurugidb.udf.LocalDatetime value = 1;
}

message TimestampWithTzRequest {
  tsurugidb.udf.LocalDatetime value = 1;
  string time_zone = 2;
}

message OffsetTimestampRequest {
  tsurugidb.udf.OffsetDatetime value = 1;
}

message DateRequest {
  tsurugidb.udf.Date value = 1;
}

message TimeRequest {
  tsurugidb.udf.LocalTime value = 1;
}

```


### 構成

構成は非常にシンプルです。

![構成図](https://qiita-image-store.s3.ap-northeast-1.amazonaws.com/0/542190/15e04b18-33c3-4c4d-837d-ee1a4f9c00b5.png)

モジュール|説明
-|-
Tsurugi DB|データベース
libdatetime-service.ini|UDF設定ファイル
libdatetime-service.so|UDF共有ライブラリ
tsurugi-udf-datetime1|UDFサーバ

### 実装

- **技術スタック**: Java 17、Spring Boot 3.x、Gradle 8.x、grpc-spring-boot-starter（または grpc-java）、Protocol Buffers。
- **プロジェクト構成**:
  - **config**: gRPC サーバのポート（デフォルト 50051）と Netty の起動・シャットダウン。
  - **converter**: `tsurugidb.udf.*` のメッセージと Java の `LocalDate` / `LocalTime` / `LocalDateTime` / `ZonedDateTime` / `OffsetDateTime` / `Instant` の相互変換。Tsurugi の `Date`（epoch からの日数）、`LocalTime`（ナノ秒）、`LocalDatetime`（offset_seconds + nano_adjustment）、`OffsetDatetime`（UTC 秒 + オフセット分）の仕様に合わせて変換する。
  - **service**: 生成された gRPC の Service 基底クラスを継承し、各 RPC で converter を使ってコンポーネントを取得し、`Int32Value` / `Int64Value` で返す。不正なタイムゾーン ID などは gRPC の `Status.INVALID_ARGUMENT` でエラーとして返却します。

ビルドは `./gradlew clean build`、起動は `./gradlew bootRun` または `java -jar build/libs/tsurugi-udf-datetime1-*.jar` で行います。

### UDF プラグインとの連携

Tsurugi 側で UDF を利用するには、本プロジェクトの .proto と `tsurugi_types.proto` を指定して `udf-plugin-builder` でプラグイン（`lib*.so` と `lib*.ini`）を生成します。`lib*.ini` の `endpoint` を本アプリの gRPC アドレス（例: `dns:///localhost:50051`）に合わせ、生成したプラグインを Tsurugi のプラグイン配置ディレクトリに置いて Tsurugi を起動すると、SQL から UDF を呼び出せます。Tsurugi の「tsurugidb.udf 利用時は 1 本にまとめてデプロイする」という制約に従い、本アプリ用の .proto と `tsurugi_types.proto` をまとめてプラグイン化します。

```bash
$ udf-plugin-builder --proto-file datetime-service.proto tsurugidb/udf/tsurugi_types.proto
```

udf-plugin-builder の詳細は [公式ドキュメント](https://github.com/project-tsurugi/tsurugi-udf/blob/master/docs/udf-plugin_ja.md) を参照してください。

### 結合テスト

gRPC サーバの起動確認に加え、udf-plugin を Tsurugi にデプロイし、tgsql から全 37 UDF を実行して期待値と一致することを [doc/test-result.md](https://github.com/septigram/tsurugi-udf-datetime1/blob/main/doc/test-result.md) のとおり確認しました。全呼び出し例は [doc/test-all.sql](https://github.com/septigram/tsurugi-udf-datetime1/blob/main/doc/test-all.sql) にあります。

## 利用例

tgsql で UDF を呼び出す例です。`FROM` 句にテーブル（ここでは `t1`）が必要です。

```
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

- `TimestampYear`: TIMESTAMP から年（システムデフォルト TZ で解釈）。
- `OffsetTimestampSecond`: TIMESTAMP WITH TIME ZONE から秒。
- `DateDayOfWeek`: DATE から曜日（0=日曜〜6=土曜。2026-01-01 は木曜で 4）。
- `TimeMillisecond`: TIME からミリ秒。

タイムゾーンを指定する場合は `TimestampYearTz(TIMESTAMP '2026-01-01 12:34:56', 'Asia/Tokyo')` のように第 2 引数で指定します。

## 成果物

- [リポジトリ](https://github.com/septigram/tsurugi-udf-datetime1)

## まとめ

Tsurugi の UDF は gRPC サーバを用意するだけで SQL から呼び出せるため、Spring Boot と gRPC で日付・時刻のコンポーネント取得関数を実装しました。.proto で Tsurugi の制約（レスポンス 1 フィールド、optional 非対応）に合わせ、`tsurugi_types.proto` の型を利用することで、Tsurugi と整合した UDF サーバを比較的少ない手間で構築できました。同様の手順で、他のドメインの UDF も追加しやすい構成になっています。

当初は週番号で `GROUP BY` したくて開発しましたが、Tsurugi v1.8.0 時点ではユーザー定義集計関数（UDAF）に未対応のため利用できませんでした。開発計画はあるようなので、実装を待ちたいと思います。

現在の UDF 呼び出しには、同じ筐体内のループバック接続でも1リクエストに4～6ミリ秒程度かかるので大量操作には向いていません。それでも現状機能に制約の多い Tsurugi を利用する上では強力なツールです。引き続き作例を重ねていきます。
