# 要件

## 機能要件

- TsurugiのUDF向けにgRPCサーバとして動作する
- TsurugiのTIMESTAMP型およびTIMESTAMP WITH TIMEZONE型の値を引数に指定し、以下の値を返す関数を提供する:
    - 年
    - 月(1:JANUARY, ..., 12:DECENBER)
    - 日
    - 曜日(0:SUNDAY, 1:MONDAY, ..., 6:SATURDAY)
    - 週番号(1から採番)
    - 時
    - 分
    - 秒
    - ミリ秒
    - EPOCからの経過ミリ秒
    - TIMESTANP型の場合はタイムゾーンを引数に指定することで任意のタイムゾーンの日時に変換してから値を返す
    - タイムゾーンを省略した場合はシステムのデフォルト・タイムゾーンで変換してから値を返す
        - Tsurugi UDFではOptionalが指定できないので別名の関数にする
- TsurugiのDATE型を引数に指定し、以下の値を返す関数を提供する:
    - 年
    - 月(1:JANUARY, ..., 12:DECENBER)
    - 日
    - 曜日(0:SUNDAY, 1:MONDAY, ..., 6:SATURDAY)
    - 週番号(1から採番)
- TsurugiのTIME型を引数に指定し、以下の値を返す関数を提供する:
    - 時
    - 分
    - 秒
    - ミリ秒

## 非機能要件

- Spring bootで実装する
- Gradleでビルドする
- fat jarを生成する
