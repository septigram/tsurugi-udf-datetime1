# 開発計画

本ドキュメントは Tsurugi UDF 日時コンポーネント取得サービスの開発計画である。進捗・結果は [doc/progress.md](progress.md) に記録する。

---

## 実行環境

- **OS**: Ubuntu 24.04
- **Java**: 21（開発に使用する JDK／コンパイラのバージョン。設計書の成果物は Java 17+ のクラスバージョンのままとする）

---

## 1. フェーズ一覧

| # | フェーズ名 | 概要 | 状態 |
|---|------------|------|------|
| 1 | 要件定義 | 機能・非機能要件の整理 | 済 |
| 2 | 設計 | アーキテクチャ・インターフェース・データ変換の設計 | 済 |
| 3 | プロジェクト準備 | Gradle・Spring Boot・proto 配置とコード生成 | 未 |
| 4 | コンバータ実装 | Tsurugi 型 ↔ Java 日時型の変換 | 済 |
| 5 | gRPC サービス実装 | 全 RPC の実装とサーバ起動 | 済 |
| 6 | 単体テスト | コンバータ・サービスロジックの単体テスト | 済 |
| 7 | 結合テスト | gRPC クライアントおよび Tsurugi からの呼び出し確認 | 済 |

---

## 2. フェーズ詳細

### 2.1 フェーズ1: 要件定義（済）

- **成果物**: [doc/requirements.md](requirements.md)
- **内容**:
  - 機能要件（gRPC サーバ、TIMESTAMP/DATE/TIME の各コンポーネント取得、タイムゾーン扱い、週番号など）
  - 非機能要件（Spring Boot、Gradle、fat jar）
- **完了条件**: 要件が文書化され、設計の入力となること。

---

### 2.2 フェーズ2: 設計（済）

- **成果物**: [doc/design.md](design.md), [doc/datetime-service.proto](datetime-service.proto)
- **内容**:
  - アーキテクチャ（Tsurugi ↔ 本アプリの gRPC 構成）
  - インターフェース定義（.proto のサービス・メッセージ一覧）
  - データ変換ロジック（Date / LocalTime / LocalDatetime / OffsetDatetime の解釈とコンポーネント取得）
  - プロジェクト構成案
- **完了条件**: 設計書と proto が揃い、実装に着手可能であること。

---

### 2.3 フェーズ3: プロジェクト準備

- **成果物**:
  - `build.gradle`, `settings.gradle`（Gradle Groovy DSL）
  - `src/main/proto/` 配下の proto ファイル配置（`datetime-service.proto` および `tsurugi_types.proto` の import 解決）
  - Protocol Buffers / gRPC のコード生成設定と生成コード
  - Spring Boot エントリポイント（`Application.java`）
  - `src/main/resources/application.yml`（Spring の設定ファイル）
  - gRPC サーバの起動設定（ポート 50051 等）
- **作業内容**:
  - Gradle プロジェクトの作成（Java 17+ をターゲット、Spring Boot 3.x、grpc-spring-boot-starter または grpc-java）
  - Spring の設定は `application.yml` を使用する。
  - protobuf-gradle-plugin 等で proto から Java コード生成（tsurugi_types の import パス対応）
  - `bootJar` で fat jar が生成できることの確認
- **完了条件**:
  - `./gradlew clean build` が成功する。
  - 起動時に gRPC サーバが指定ポートでリッスンする（実装が空でもよい）。

---

### 2.4 フェーズ4: コンバータ実装

- **成果物**:
  - `converter/DateConverter.java`（tsurugidb.udf.Date ↔ LocalDate）
  - `converter/LocalTimeConverter.java`（LocalTime ↔ 時・分・秒・ミリ秒）
  - `converter/LocalDatetimeConverter.java`（LocalDatetime ↔ ZonedDateTime / Instant、デフォルト TZ・指定 TZ）
  - `converter/OffsetDatetimeConverter.java`（OffsetDatetime ↔ OffsetDateTime / Instant）
- **作業内容**:
  - 設計書 5.1 に基づく各型の解釈（days, nanos, offset_seconds, nano_adjustment, time_zone_offset）
  - 曜日 0–6（日曜=0）、週番号（ISO 8601）、EPOCH ミリ秒の算出
- **完了条件**:
  - 各 Tsurugi 型から正しい Java 日時型へ変換できること。
  - 境界値・タイムゾーン指定時も設計どおりに動作すること（単体テストで検証可能な状態）。

---

### 2.5 フェーズ5: gRPC サービス実装

- **成果物**:
  - `service/DateTimeUdfServiceImpl.java`（生成された Service の実装）
  - 全 RPC の実装（Timestamp* / OffsetTimestamp* / Date* / Time*、各コンポーネント・週番号含む）
- **作業内容**:
  - 各 RPC でリクエストをコンバータで Java 型に変換し、年・月・日・曜日・週番号・時・分・秒・ミリ秒・EPOCH ミリ秒のいずれかを取得して Int32Value / Int64Value で返す。
  - 不正なタイムゾーン ID 等は gRPC の Status でエラー返却。
- **完了条件**:
  - 全 RPC が定義どおりに応答する。
  - fat jar で起動し、外部 gRPC クライアントから呼び出し可能であること。

---

### 2.6 フェーズ6: 単体テスト

- **成果物**:
  - コンバータの単体テスト（各型・境界値・タイムゾーン）
  - サービス層の単体テスト（モックまたは in-memory チャネルで各 RPC の戻り値検証）
- **作業内容**:
  - JUnit 5 等でコンバータの入出力を検証。
  - gRPC の Service 実装を直接呼び出し、リクエスト／レスポンスをアサート。
- **完了条件**:
  - `./gradlew test` で単体テストがすべて成功する。
  - 主要な日付・時刻・タイムゾーン・週番号のパターンをカバーしていること。

---

### 2.7 フェーズ7: 結合テスト

- **成果物**:
  - 結合テスト用コードまたは手順（テストクラス／スクリプト／手順書）
  - 必要に応じてサンプル gRPC クライアント
- **作業内容**:
  - **7a. gRPC 結合**: 本アプリを起動し、別プロセスまたはテスト内の gRPC クライアントから各 RPC を呼び出し、期待値と一致することを確認する。
  - **7b. Tsurugi 結合（可能な環境の場合）**: udf-plugin-builder でプラグインを生成し、Tsurugi にデプロイ。SQL から UDF を呼び出し、期待するコンポーネント（年・月・日・曜日・週番号・時・分・秒・ミリ秒・EPOCH ミリ秒）が返ることを確認する。
- **完了条件**:
  - 7a: 主要 RPC について、gRPC 経由で正しい値が返ることを確認したこと。
  - 7b: Tsurugi 環境が利用できる場合、少なくとも 1 種類以上の UDF を SQL から実行し、結果が期待と一致したこと（環境がなければ 7a までで完了とし、7b は「Tsurugi 環境準備後に実施」と記録する）。

---

## 3. 成果物一覧（参照）

| 成果物 | ドキュメント・パス |
|--------|---------------------|
| 要件定義 | doc/requirements.md |
| 設計書 | doc/design.md |
| Proto 定義 | doc/datetime-service.proto |
| 開発計画 | doc/plan.md（本ドキュメント） |
| 進捗・結果 | doc/progress.md |

---

## 4. 注意事項

- 結合テストの Tsurugi 連携（7b）は、Tsurugi および udf-plugin-builder の利用可能な環境に依存する。環境がなくても 7a まで完了していれば、計画上の結合テストは「gRPC 結合まで完了」とみなす。
- 不具合の修正は、該当フェーズの「完了条件」を満たすまで行い、必要に応じて doc/progress.md に事象と対応を記録する。
