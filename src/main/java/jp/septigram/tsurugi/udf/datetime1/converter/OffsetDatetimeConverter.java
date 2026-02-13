package jp.septigram.tsurugi.udf.datetime1.converter;

import com.tsurugidb.udf.TsurugiTypes;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.IsoFields;

/**
 * tsurugidb.udf.OffsetDatetime と OffsetDateTime / Instant の相互変換を行う。
 * <p>
 * 設計書 5.1 に基づく: OffsetDatetime の {@code offset_seconds} は UTC の EPOCH からの秒、
 * {@code nano_adjustment} は [0, 10^9-1]、{@code time_zone_offset} は分単位のオフセット。
 * UTC で Instant を組み立て、time_zone_offset で ZoneOffset を組み、OffsetDateTime として
 * コンポーネント抽出する。
 */
public final class OffsetDatetimeConverter {

    private OffsetDatetimeConverter() {
    }

    /**
     * Tsurugi OffsetDatetime を OffsetDateTime に変換する。
     *
     * @param offsetDatetime Tsurugi の OffsetDatetime（null の場合は null を返す）
     * @return 対応する OffsetDateTime
     */
    public static OffsetDateTime toOffsetDateTime(TsurugiTypes.OffsetDatetime offsetDatetime) {
        if (offsetDatetime == null) {
            return null;
        }
        Instant instant = toInstant(offsetDatetime);
        ZoneOffset offset = ZoneOffset.ofTotalSeconds(
                offsetDatetime.getTimeZoneOffset() * 60);
        return instant.atOffset(offset);
    }

    /**
     * Tsurugi OffsetDatetime を Instant に変換する。
     *
     * @param offsetDatetime Tsurugi の OffsetDatetime（null の場合は null を返す）
     * @return 対応する Instant（UTC）
     */
    public static Instant toInstant(TsurugiTypes.OffsetDatetime offsetDatetime) {
        if (offsetDatetime == null) {
            return null;
        }
        long epochSecond = offsetDatetime.getOffsetSeconds();
        int nanoAdjustment = offsetDatetime.getNanoAdjustment();
        return Instant.ofEpochSecond(epochSecond, nanoAdjustment);
    }

    /**
     * OffsetDateTime から年を取得する。
     */
    public static int getYear(OffsetDateTime offsetDateTime) {
        return offsetDateTime.getYear();
    }

    /**
     * OffsetDateTime から月（1-12）を取得する。
     */
    public static int getMonth(OffsetDateTime offsetDateTime) {
        return offsetDateTime.getMonthValue();
    }

    /**
     * OffsetDateTime から日を取得する。
     */
    public static int getDay(OffsetDateTime offsetDateTime) {
        return offsetDateTime.getDayOfMonth();
    }

    /**
     * OffsetDateTime から曜日（0=日曜, 1=月曜, ..., 6=土曜）を取得する。
     */
    public static int getDayOfWeek(OffsetDateTime offsetDateTime) {
        return offsetDateTime.getDayOfWeek().getValue() % 7;
    }

    /**
     * OffsetDateTime から週番号（ISO 8601、1-52 または 53）を取得する。
     */
    public static int getWeekOfYear(OffsetDateTime offsetDateTime) {
        return offsetDateTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }

    /**
     * OffsetDateTime から時（0-23）を取得する。
     */
    public static int getHour(OffsetDateTime offsetDateTime) {
        return offsetDateTime.getHour();
    }

    /**
     * OffsetDateTime から分（0-59）を取得する。
     */
    public static int getMinute(OffsetDateTime offsetDateTime) {
        return offsetDateTime.getMinute();
    }

    /**
     * OffsetDateTime から秒（0-59）を取得する。
     */
    public static int getSecond(OffsetDateTime offsetDateTime) {
        return offsetDateTime.getSecond();
    }

    /**
     * OffsetDateTime からミリ秒（0-999）を取得する。
     */
    public static int getMillisecond(OffsetDateTime offsetDateTime) {
        return offsetDateTime.getNano() / 1_000_000;
    }

    /**
     * OffsetDateTime から EPOCH ミリ秒を取得する。
     */
    public static long getEpochMilli(OffsetDateTime offsetDateTime) {
        return offsetDateTime.toInstant().toEpochMilli();
    }
}
