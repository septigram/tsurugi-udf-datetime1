package jp.septigram.tsurugi.udf.datetime1.converter;

import com.tsurugidb.udf.TsurugiTypes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.IsoFields;

/**
 * tsurugidb.udf.LocalDatetime と ZonedDateTime / Instant の相互変換を行う。
 * <p>
 * 設計書 5.1 に基づく: LocalDatetime の {@code offset_seconds} はローカルタイムゾーンでの
 * EPOCH（1970-01-01 00:00:00）からのオフセット秒。{@code nano_adjustment} は [0, 10^9-1]。
 * 指定された ZoneId で LocalDateTime を組み立て、ZonedDateTime → Instant に変換する。
 */
public final class LocalDatetimeConverter {

    private LocalDatetimeConverter() {
    }

    /**
     * Tsurugi LocalDatetime を ZonedDateTime に変換する（デフォルトタイムゾーン）。
     *
     * @param localDatetime Tsurugi の LocalDatetime（null の場合は null を返す）
     * @return 対応する ZonedDateTime（システムデフォルト TZ）
     */
    public static ZonedDateTime toZonedDateTime(TsurugiTypes.LocalDatetime localDatetime) {
        return toZonedDateTime(localDatetime, ZoneId.systemDefault());
    }

    /**
     * Tsurugi LocalDatetime を指定タイムゾーンの ZonedDateTime に変換する。
     *
     * @param localDatetime Tsurugi の LocalDatetime（null の場合は null を返す）
     * @param zoneId        解釈に使うタイムゾーン
     * @return 対応する ZonedDateTime
     */
    public static ZonedDateTime toZonedDateTime(TsurugiTypes.LocalDatetime localDatetime,
            ZoneId zoneId) {
        if (localDatetime == null) {
            return null;
        }
        long offsetSeconds = localDatetime.getOffsetSeconds();
        int nanoAdjustment = localDatetime.getNanoAdjustment();

        LocalDateTime ldt = LocalDateTime.of(1970, 1, 1, 0, 0, 0, 0)
                .plusSeconds(offsetSeconds)
                .plusNanos(nanoAdjustment);
        return ldt.atZone(zoneId);
    }

    /**
     * Tsurugi LocalDatetime を Instant に変換する（デフォルトタイムゾーンで解釈）。
     */
    public static Instant toInstant(TsurugiTypes.LocalDatetime localDatetime) {
        ZonedDateTime zdt = toZonedDateTime(localDatetime);
        return zdt != null ? zdt.toInstant() : null;
    }

    /**
     * Tsurugi LocalDatetime を Instant に変換する（指定タイムゾーンで解釈）。
     */
    public static Instant toInstant(TsurugiTypes.LocalDatetime localDatetime, ZoneId zoneId) {
        ZonedDateTime zdt = toZonedDateTime(localDatetime, zoneId);
        return zdt != null ? zdt.toInstant() : null;
    }

    /**
     * ZonedDateTime から年を取得する。
     */
    public static int getYear(ZonedDateTime zonedDateTime) {
        return zonedDateTime.getYear();
    }

    /**
     * ZonedDateTime から月（1-12）を取得する。
     */
    public static int getMonth(ZonedDateTime zonedDateTime) {
        return zonedDateTime.getMonthValue();
    }

    /**
     * ZonedDateTime から日を取得する。
     */
    public static int getDay(ZonedDateTime zonedDateTime) {
        return zonedDateTime.getDayOfMonth();
    }

    /**
     * ZonedDateTime から曜日（0=日曜, 1=月曜, ..., 6=土曜）を取得する。
     */
    public static int getDayOfWeek(ZonedDateTime zonedDateTime) {
        return zonedDateTime.getDayOfWeek().getValue() % 7;
    }

    /**
     * ZonedDateTime から週番号（ISO 8601、1-52 または 53）を取得する。
     */
    public static int getWeekOfYear(ZonedDateTime zonedDateTime) {
        return zonedDateTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }

    /**
     * ZonedDateTime から時（0-23）を取得する。
     */
    public static int getHour(ZonedDateTime zonedDateTime) {
        return zonedDateTime.getHour();
    }

    /**
     * ZonedDateTime から分（0-59）を取得する。
     */
    public static int getMinute(ZonedDateTime zonedDateTime) {
        return zonedDateTime.getMinute();
    }

    /**
     * ZonedDateTime から秒（0-59）を取得する。
     */
    public static int getSecond(ZonedDateTime zonedDateTime) {
        return zonedDateTime.getSecond();
    }

    /**
     * ZonedDateTime からミリ秒（0-999）を取得する。
     */
    public static int getMillisecond(ZonedDateTime zonedDateTime) {
        return zonedDateTime.getNano() / 1_000_000;
    }

    /**
     * ZonedDateTime から EPOCH ミリ秒を取得する。
     */
    public static long getEpochMilli(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toInstant().toEpochMilli();
    }
}
