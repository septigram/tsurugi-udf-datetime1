# 進捗記録

開発計画 [doc/plan.md](plan.md) に基づく進捗・作業結果を記録する。

---

## フェーズ別状態

| # | フェーズ名 | 状態 | 備考 |
|---|------------|------|------|
| 1 | 要件定義 | 済 | 本チャット開始前より doc/requirements.md が存在 |
| 2 | 設計 | 済 | 本チャットで設計書・proto を作成・更新 |
| 3 | プロジェクト準備 | 済 | ビルド・起動確認まで完了 |
| 4 | コンバータ実装 | 済 | — |
| 5 | gRPC サービス実装 | 済 | — |
| 6 | 単体テスト | 済 | — |
| 7 | 結合テスト | 済 | 7a・7b 完了 |

---

## 本チャットで実施した作業

### 1. 設計書の作成

- **成果物**: [doc/design.md](design.md)
- **内容**:
  - 要件（doc/requirements.md）と Tsurugi マニュアル（doc/tsurugi-manual/）を参照し、設計書を新規作成した。
  - 概要、要件対応、アーキテクチャ、技術スタック、インターフェース定義（.proto 方針・メッセージ一覧）、データ変換ロジック、プロジェクト構成、ビルド・実行、制約・注意事項を記載した。

### 2. gRPC Proto ファイルの作成

- **成果物**: [doc/datetime-service.proto](datetime-service.proto)
- **内容**:
  - 設計書に沿い、`DateTimeService` を定義した。
  - TIMESTAMP（デフォルト TZ）9 RPC、TIMESTAMP（タイムゾーン指定）9 RPC、TIMESTAMP WITH TIME ZONE 9 RPC、DATE 4 RPC、TIME 4 RPC および各リクエスト/レスポンス message を定義した。
  - `tsurugidb/udf/tsurugi_types.proto` を import して Tsurugi の日付・時刻型を利用している。

### 3. 週番号機能の追加

- **対象**: doc/design.md, doc/datetime-service.proto
- **内容**:
  - 要件にあった「週番号(1から採番)」が設計・proto に含まれていなかったため、両方に追加した。
  - **design.md**: 概要・要件対応表・型定義・各 RPC 一覧に週番号を追加。週番号は ISO 8601（1–52/53）とし、取得方法として `IsoFields.WEEK_OF_WEEK_BASED_YEAR` を記載。DATE の RPC 数を 4 → 5 に変更。
  - **datetime-service.proto**: 次の 4 RPC を追加。
    - `TimestampWeekOfYear`, `TimestampWeekOfYearTz`, `OffsetTimestampWeekOfYear`, `DateWeekOfYear`（いずれも `Int32Value` を返す）。

### 4. 曜日仕様の確認（参考）

- 曜日を「日曜=0」とするか「日曜=1」とするかの一般的な仕様について回答した。
- 現行設計（0=SUNDAY, 1=MONDAY, …, 6=SATURDAY）は C/JavaScript/PostgreSQL 等と同様の慣習に沿っている旨を説明。ファイルの変更は行っていない。

### 5. 開発計画の作成

- **成果物**: [doc/plan.md](plan.md)
- **内容**:
  - 要件定義・設計を「済」、以降をプロジェクト準備〜結合テストまで 7 フェーズに分けた開発計画を作成した。
  - 各フェーズの成果物・作業内容・完了条件を記載。結合テストは gRPC 結合（7a）と Tsurugi 結合（7b）に分け、7b は環境依存である旨を記載した。
  - 進捗・結果は本ドキュメント（progress.md）にまとめる前提で計画のみを記述した。

---

## フェーズ3: プロジェクト準備（完了）

- **成果物**: build.gradle / settings.gradle（Gradle Groovy DSL）、src/main/proto/ 配下の proto、生成コード、Application.java、application.yml、GrpcServerConfig、DateTimeUdfServiceImpl（スタブ）、Gradle ラッパー、README のビルド・実行説明
- **実施内容**:
  - Gradle プロジェクト作成（Java 17 ターゲット、Spring Boot 3.2.5、protobuf プラグイン、gRPC）
  - proto を src/main/proto/ に配置（datetime-service.proto、tsurugidb/udf/tsurugi_types.proto）。generateProto で Java/gRPC コード生成
  - Spring Boot エントリポイント、application.yml（grpc.server.port: 50051）、gRPC サーバ起動（Netty）と終了時シャットダウン
  - DateTimeUdfServiceImpl で全 RPC をスタブ実装（0 返却）。フェーズ5 で実装に置き換え予定
  - README.md は既存の概要を残しつつ「ビルドと実行」を追記
- **完了条件**: `./gradlew clean build` 成功、起動時に gRPC がポート 50051 でリッスンすることを確認済み。

---

## パッケージ変更（jp.septigram.tsurugi.udf.datetime1）

- **対象**: Java ソース、build.gradle、design.md
- **実施内容**:
  - アプリケーションのパッケージを `tsurugi.udf` から `jp.septigram.tsurugi.udf.datetime1` に変更。
  - Application → `jp.septigram.tsurugi.udf.datetime1`、config → `jp.septigram.tsurugi.udf.datetime1.config`、service → `jp.septigram.tsurugi.udf.datetime1.service` に配置。
  - build.gradle の group と bootRun/bootJar の mainClass を新パッケージに合わせて更新。archiveBaseName は `tsurugi-udf-datetime1` のままに設定。
  - design.md の「6. プロジェクト構成」に Java パッケージ `jp.septigram.tsurugi.udf.datetime1` を明記し、ツリーのパスを更新。

---

## datetime-service.proto の変更確認と同期（追記）

- **確認内容**: `src/main/proto/datetime-service.proto` に `option java_package = "jp.septigram.tsurugi.udf.datetime1";` が追加されていることを確認した。サービス定義・メッセージ・RPC は設計どおりで問題なし。
- **対応**: 上記に合わせて `doc/datetime-service.proto` にも同じ `option java_package` を追記し、両ファイルを一致させた。generateProto 再実行後、生成コードは `jp.septigram.tsurugi.udf.datetime1` に出力される。DateTimeUdfServiceImpl の import は当該パッケージを参照するようになっており、`./gradlew clean build` でビルド成功を確認済み。

---

---

## フェーズ4: コンバータ実装（完了）

- **成果物**: DateConverter.java, LocalTimeConverter.java, LocalDatetimeConverter.java, OffsetDatetimeConverter.java
- **実施内容**:
  - **DateConverter**: tsurugidb.udf.Date ↔ LocalDate。days（1970-01-01 からの日数）を LocalDate.ofEpochDay() で変換。年・月・日・曜日（0–6）・週番号（ISO 8601）取得メソッドを提供。
  - **LocalTimeConverter**: tsurugidb.udf.LocalTime ↔ java.time.LocalTime。nanos（0 時からのナノ秒）を LocalTime.ofNanoOfDay() で変換。時・分・秒・ミリ秒取得メソッドを提供。24 時間超の nanos は Math.floorMod で正規化。
  - **LocalDatetimeConverter**: tsurugidb.udf.LocalDatetime ↔ ZonedDateTime / Instant。offset_seconds（ローカル TZ での EPOCH オフセット秒）と nano_adjustment から LocalDateTime を組み立て、指定 ZoneId（または systemDefault）で ZonedDateTime に変換。全コンポーネント取得メソッドを提供。
  - **OffsetDatetimeConverter**: tsurugidb.udf.OffsetDatetime ↔ OffsetDateTime / Instant。offset_seconds（UTC EPOCH 秒）と nano_adjustment で Instant を構成し、time_zone_offset（分）で ZoneOffset を組み OffsetDateTime に変換。全コンポーネント取得メソッドを提供。
- **完了条件**: 各 Tsurugi 型から正しい Java 日時型へ変換できること。`./gradlew clean build` 成功を確認済み。単体テストはフェーズ6で実施予定。

---

## フェーズ5: gRPC サービス実装（完了）

- **成果物**: DateTimeUdfServiceImpl.java（実装完了）、DateTimeUdfServiceImplTest.java（主要 RPC 検証用テスト）
- **実施内容**:
  - スタブ実装をコンバータ利用の実装に置き換え。全 37 RPC（Timestamp* 10、Timestamp*Tz 10、OffsetTimestamp* 10、Date* 5、Time* 4）を実装。
  - 各 RPC でリクエストをコンバータで Java 型に変換し、コンポーネント（年・月・日・曜日・週番号・時・分・秒・ミリ秒・EPOCH ミリ秒）を Int32Value / Int64Value で返却。
  - 不正なタイムゾーン ID や空の time_zone に対して Status.INVALID_ARGUMENT を返却。DateTimeException、ZoneRulesException、IllegalArgumentException、ArithmeticException を INVALID_ARGUMENT にマッピング。
- **テスト**: DateYear、DateDayOfWeek、TimeHour、TimestampYear、OffsetTimestampEpochMilli、TimestampYearTz（不正 TZ でエラー）を検証。`./gradlew test` 成功。
- **完了条件**: 全 RPC が定義どおりに応答すること。fat jar で起動し、gRPC サーバがポート 50051 でリッスンすることを確認済み。

---

## フェーズ6: 単体テスト（完了）

- **成果物**: DateConverterTest, LocalTimeConverterTest, LocalDatetimeConverterTest, OffsetDatetimeConverterTest, DateTimeUdfServiceImplTest（拡充）
- **実施内容**:
  - **コンバータテスト**: 各型の往復変換、境界値（epoch、負の日数、24時間超の nanos）、null 処理、曜日（0–6）、週番号（ISO 8601）、タイムゾーン（UTC、Asia/Tokyo）を検証。
  - **サービス層テスト**: Date（年・月・日・曜日・週番号）、Time（時・分・秒・ミリ秒）、Timestamp（デフォルト TZ、指定 TZ）、OffsetTimestamp（月・日・時、JST オフセット）、不正タイムゾーン時の INVALID_ARGUMENT を検証。
- **完了条件**: `./gradlew test` で全 55 テストが成功。主要な日付・時刻・タイムゾーン・週番号のパターンをカバー済み。

---

## フェーズ7: 結合テスト（完了）

- **成果物**: doc/test-all.sql、doc/test-result.md
- **実施内容**:
  - **7a. gRPC 結合**: 本アプリを fat jar で起動し、gRPC サーバがポート 50051 でリッスンすることを確認。主要 RPC の動作は単体テストで検証済み。
  - **7b. Tsurugi 結合**: udf-plugin-builder でプラグインを生成し、Tsurugi にデプロイ。doc/test-all.sql に定義した全 37 UDF を tgsql から実行し、期待する値が返ることを確認。
- **テスト結果**: doc/test-result.md に記載。全 37 件の UDF 呼び出しが成功し、ログにエラーや異常は認められない。
- **完了条件**: 7a・7b ともに完了。Tsurugi 環境で SQL から UDF を実行し、Timestamp* / OffsetTimestamp* / Date* / Time* の各コンポーネントが正しく返ることを確認済み。

---

## 今後の作業

- 全フェーズ完了。必要に応じて不具合対応時に、この progress.md に結果や事象を追記する。
