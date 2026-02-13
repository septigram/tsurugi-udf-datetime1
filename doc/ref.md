# CursorによるVectorの劔"Tsurugi"シンク開発

オブザーバビリティデータパイプライン [Vector](https://github.com/vectordotdev/vector) の次世代RDB [劔"Tsurugi"](https://www.tsurugidb.com/) に向けた出力段のシンクをCursorによりAI支援開発しました。

筆者はRust初心者なので、ほとんどの作業をCursorにお願いしました。AIに支援される開発というよりも、AI様を人が支援する開発です。ちょっと挙動が怪しいですが、一応動くところまでできたので公開しています。CursorではProプランでAutoモデルを使っています。

## 背景

最近、VectorというRustで実装された高性能なオブザーバビリティデータパイプラインがあると知りました。高速なインメモリRDBである劔"Tsurugi"も数か月前にRust APIが実装されたので、これを接続したら最強です。

Vectorについて調べた内容は、以下の記事にまとめました。

https://qiita.com/septigram/items/9f5ddcebd8929691302f

https://qiita.com/septigram/items/bdcbbdd07813999501b3

## 開発手順

### 事前準備

Vectorのレポジトリをフォークします。そのディレクトリをCursorで開きます。

右も左も分からない状態からのスタートでしたが、LLMに **「シンクを新しく開発したいので、どこに置くのが適切か調べてください。」** と依頼すると、プロジェクトを読み込んで 「`src/sinks/` の下に配置するのが適切です。」と回答してくれました。

### 要件指定

まず、`src/sinks/tsurugidb/doc/plan.md` に以下の要件だけ書きます。

```markdown
## 要件

- [Tsurugi用Rustクライアント](https://github.com/project-tsurugi/tsubakuro-rust) を用いてシンクコンポーネントを開発する
- TsurugiはRDBMSなのでpostgres用シンクに準じる機能を実装する
```

作成したファイルを添付して **「要件に従って作業計画を立ててください。」** とLLMに依頼します。すると、以下を追記してくれました。

```markdown
## 作業計画

### 1. プロジェクト構造の確認と基本ファイルの作成

postgresシンクの構造を参考に、以下のファイルを作成します：

- `src/sinks/tsurugidb/mod.rs` - モジュールのエクスポート
- `src/sinks/tsurugidb/config.rs` - 設定構造体とSinkConfigトレイトの実装
- `src/sinks/tsurugidb/service.rs` - Serviceトレイトの実装、RetryLogic、Request/Response型
- `src/sinks/tsurugidb/sink.rs` - StreamSinkトレイトの実装

### 2. Cargo.tomlへの依存関係追加

- `tsubakuro-rust-core` の依存関係を追加
- `sinks-tsurugidb`フィーチャーを追加（`sinks-postgres`と同様のパターン）

### 3. config.rsの実装

postgresシンクの`PostgresConfig`を参考に、以下を実装：

- `TsurugiConfig`構造体
  - `endpoint`: Tsurugiへの接続文字列
  - `table`: データを挿入するテーブル名
  - `pool_size`: 接続プールサイズ（オプション、デフォルト値設定）
  - `transaction_type`: トランザクションタイプ
  - `batch`: イベントバッチ設定
  - `request`: TowerRequestConfig
  - `acknowledgements`: AcknowledgementsConfig
- `GenerateConfig`トレイトの実装
- `SinkConfig`トレイトの実装
  - `build()`: Tsurugiクライアントの初期化、サービスとシンクの構築
  - `input()`: 入力タイプの定義
  - `acknowledgements()`: 確認応答設定の返却
- `healthcheck()`関数: 接続確認用

### 4. service.rsの実装

postgresシンクの`PostgresService`を参考に、以下を実装：

- `TsurugiRetryLogic`: リトライロジックの実装
  - `is_retriable_error()`: リトライ可能なエラーの判定
- `TsurugiService`: メインのサービス実装
  - Tsurugiクライアントの保持
  - テーブル名とエンドポイントの保持
- `TsurugiRequest`: リクエスト型
  - `TryFrom<Vec<Event>>`の実装
  - `Finalizable`トレイトの実装
  - `MetaDescriptive`トレイトの実装
- `TsurugiResponse`: レスポンス型
  - `DriverResponse`トレイトの実装
- `TsurugiServiceError`: エラー型
  - `Snafu`を使用したエラー定義
- `Service<TsurugiRequest>`トレイトの実装
  - `call()`: イベントのシリアライズとTsurugiへの挿入処理
  - `jsonb_populate_recordset`相当の機能をTsurugiクライアントで実装

### 5. sink.rsの実装

postgresシンクの`PostgresSink`を参考に、以下を実装：

- `TsurugiSink`構造体
  - `service`: `Svc<TsurugiService, TsurugiRetryLogic>`
  - `batch_settings`: `BatcherSettings`
- `StreamSink<Event>`トレイトの実装
  - `run()`: イベントストリームの処理
  - バッチ処理とリクエスト変換の実装

### 6. mod.rsの実装

- モジュールの宣言とエクスポート
- `TsurugiConfig`の公開

### 7. sinks/mod.rsへの登録

- `#[cfg(feature = "sinks-tsurugidb")]`条件付きでモジュールを追加
- `pub mod tsurugidb;`の追加

### 8. テストの実装

- `config.rs`内のユニットテスト
  - `generate_config()`テスト
  - `parse_config()`テスト
- 統合テスト（必要に応じて）

### 実装上の注意点

1. **Tsurugiクライアントの使用方法**
   - tsubakuro-rustのAPIドキュメントとサンプルコードを参照
   - 接続プールの管理方法を確認
   - SQL実行方法を確認（postgresの`jsonb_populate_recordset`相当の機能）

2. **postgresシンクとの違い**
   - sqlxの代わりにtsubakuro-rustを使用
   - 接続文字列の形式が異なる可能性
   - SQL構文が異なる可能性（Tsurugi固有の構文）

3. **エラーハンドリング**
   - Tsurugi固有のエラータイプに対応
   - リトライ可能なエラーとそうでないエラーの適切な分類

4. **パフォーマンス**
   - バッチ処理の最適化
   - 接続プールの適切なサイズ設定

```

すばらしいです。この後の作業はこの計画書を元に進めます。

### 設計

LLMに仕様なしで作業依頼するのは無理があるので、予め tsubakuro-rust-core の仕様書を作成します。 tsubakuro-rust-core プロジェクトをクローンして、LLMにソースコードから仕様書を作成してもらいます。作成された仕様書をこの開発プロジェクトにコピーしておきました。

次にLLMに **「計画に沿って設計書を作成してください。tsubakuro-rust-coreの仕様は @src/sinks/tsurugidb/doc/tsubakuro-rust-core/specs.md および関連文書を参照してください。」** と依頼します。

すると設計書 `src/sinks/tsurugidb/doc/design.md` を作成してくれるので内容をレビューします。この時点の修正依頼は以下の2点だけでした。

- jsonb_populate_recordset関数を使う設計だったので、個別INSERT文を生成するアプローチにする
- Tsurugiの持つトランザクションタイプ OCC と LTX を設定で指定できるように追加する（初期値は OCC）

### 実装

実装もLLMに任せます。

**「設計書に基づいて工程の 1. プロジェクト構造の確認と基本ファイルの作成 を実施してください。」** というように、テスト工程まで次々に依頼します。

時々構文エラーなどを見落とすため、都度修正を依頼します。

設計時点で見落としていた型の問題など（例えばTsurugiはBoolean型やJson型が使えません）も修正します。

コンテキストが蓄積されてきたら、途中経過を作業ログに出力してもらいます。新しくチャットを開き、その作業ログをLLMに示して作業を継続してもらいます。

### 結合テスト

Tsurugi 1.7のDockerコンテナを用意して結合テストを実施します。

この工程が最も時間を要しました。Tsurugiと結合して使えない機能を避ける作業です。

この工程では以下のような対応を実施しました。

- `timestamp`は予約語のため、カラム名として使用しない
- `TEXT`型はサポートされていないため、`VARCHAR`を使用
- `TIMESTAMPTZ`型はサポートされていないため、`TIMESTAMP`を使用
- Long Transactionではwrite preserveが必要なため、テーブル作成にはShort Transactionを使用
- TIMESTAMP値は`'YYYY-MM-DD HH:MM:SS'`形式で指定する必要がある
- `SELECT 1`（VALUES演算子）はサポートされていない

最終的に結合テストが通るようになり、以下のコマンドでビルドすることができました。

```
cargo build --features sinks-tsurugidb
```

ログファイルを登録する試験を行います。まずTsurugiにテーブルを作成します。

```sql
create table logs (
    file varchar(255),
    host varchar(255),
    level varchar(255),
    message varchar(255),
    source_type varchar(255),
    "timestamp" varchar(255)
);
```

app.logを作成します。

```text
INFO User logged in: id=1 name=alice
ERROR Failed to save: id=2 name=bob
```

Vectorの設定ファイルを用意します。tmpフォルダも作成しておきます。

```toml
data_dir = "./tmp"

# テキストファイルを読む source
[sources.app_logs]
type    = "file"
include = ["./app.log"]
read_from = "beginning"

# VRL で行を JSON に整形する transform
[transforms.to_json]
type   = "remap"
inputs = ["app_logs"]
source = '''
.level = split!(.message, " ")[0]
.message = .message
'''

# Tsurugiに出力する sink
[sinks.tsurugidb]
type   = "tsurugidb"
inputs = ["to_json"]
endpoint = "tcp://localhost:12345"
table = "logs"
transaction_type = "occ"

[sinks.tsurugidb.credential]
type = "user_password"
user = "tsurugi"
password = "password"
```

Vectorを起動します。

```bash
$ ./target/debug/vector -c vector.toml
2025-12-16T08:25:59.017186Z  INFO vector::app: Log level is enabled. level="info"
2025-12-16T08:25:59.019571Z  INFO vector::app: Loading configs. paths=["vector_tsurugi.toml"]
2025-12-16T08:25:59.048211Z  INFO vector::topology::running: Running healthchecks.
2025-12-16T08:25:59.049401Z  INFO vector: Vector has started. debug="true" version="0.52.0" arch="x86_64" revision=""
2025-12-16T08:25:59.049561Z  INFO vector::app: API is disabled, enable by setting `api.enabled` to `true` and use commands like `vector top`.
2025-12-16T08:25:59.049665Z  INFO source{component_kind="source" component_id=app_logs component_type=file}: vector::sources::file: Starting file server. include=["./app.log"] exclude=[]
2025-12-16T08:25:59.052237Z  INFO vector::topology::builder: Healthcheck passed.
2025-12-16T08:25:59.052339Z  INFO source{component_kind="source" component_id=app_logs component_type=file}:file_server: vector::internal_events::file::source: Found new file to watch. file=app.log
```

うまく動作したようです。tgsqlコマンドでTsurugiにレコードが作成されていることを確認します。

```bash
tgsql> select * from logs;
start transaction implicitly. option=[
  type: OCC
  label: "tgsql-implicit-transaction2025-12-16 17:26:04.294+09:00"
]
Time: 1.331 ms
[file: VARCHAR(255), host: VARCHAR(255), level: VARCHAR(255), message: VARCHAR(255), source_type: VARCHAR(255), timestamp: VARCHAR(255)]
[app.log, kuromac2, INFO, INFO User logged in: id=1 name=alice, file, null]
[app.log, kuromac2, ERROR, ERROR Failed to save: id=2 name=bob, file, null]
(2 rows)
Time: 12.78 ms
transaction commit(DEFAULT) finished implicitly.
Time: 4.506 ms
```

## 成果物

- https://github.com/septigram/vector_tsurugi/tree/sinks_tsurugidb/src/sinks/tsurugidb

## 謝辞

tsubakuro の接続バージョンでトラブルがあり、tsubakuro-rust の開発者である hishidama さんにサポートしていただきました。ありがとうございます。

## まとめ

Cursorを使うと、Rust初心者でも丸一日でシンク・コンポーネントの開発ができました。という報告でした。
