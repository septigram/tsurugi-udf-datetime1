package jp.septigram.tsurugi.udf.datetime1.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.tsurugidb.udf.TsurugiTypes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

/**
 * LocalDatetimeConverter の単体テスト。各型・境界値・タイムゾーン・週番号を検証する。
 */
class LocalDatetimeConverterTest {

    @Test
    void toZonedDateTime_epoch_utc() {
        var ld = TsurugiTypes.LocalDatetime.newBuilder()
                .setOffsetSeconds(0)
                .setNanoAdjustment(0)
                .build();
        ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(ld, ZoneId.of("UTC"));
        assertEquals(1970, zdt.getYear());
        assertEquals(1, zdt.getMonthValue());
        assertEquals(1, zdt.getDayOfMonth());
        assertEquals(0, zdt.getHour());
        assertEquals(0, zdt.getMinute());
        assertEquals(0, zdt.getSecond());
    }

    @Test
    void toZonedDateTime_epoch_asia_tokyo() {
        // offset_seconds=0 はそのTZの 1970-01-01 00:00:00
        // JST の 1970-01-01 00:00 = 1969-12-31 15:00 UTC
        var ld = TsurugiTypes.LocalDatetime.newBuilder()
                .setOffsetSeconds(0)
                .setNanoAdjustment(0)
                .build();
        ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(ld, ZoneId.of("Asia/Tokyo"));
        assertEquals(1970, zdt.getYear());
        assertEquals(1, zdt.getMonthValue());
        assertEquals(1, zdt.getDayOfMonth());
        assertEquals(0, zdt.getHour());
        assertEquals(Instant.parse("1969-12-31T15:00:00Z"), zdt.toInstant());
    }

    @Test
    void toZonedDateTime_with_offset_and_nanos() {
        // 1時間30分45秒 + 123ミリ秒
        long offsetSeconds = 3600 + 30 * 60 + 45;
        int nanoAdjustment = 123_000_000;
        var ld = TsurugiTypes.LocalDatetime.newBuilder()
                .setOffsetSeconds(offsetSeconds)
                .setNanoAdjustment(nanoAdjustment)
                .build();
        ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(ld, ZoneId.of("UTC"));
        assertEquals(1970, zdt.getYear());
        assertEquals(1, zdt.getMonthValue());
        assertEquals(1, zdt.getDayOfMonth());
        assertEquals(1, zdt.getHour());
        assertEquals(30, zdt.getMinute());
        assertEquals(45, zdt.getSecond());
        assertEquals(123, LocalDatetimeConverter.getMillisecond(zdt));
    }

    @Test
    void toZonedDateTime_null_returns_null() {
        assertNull(LocalDatetimeConverter.toZonedDateTime(null, ZoneId.of("UTC")));
    }

    @Test
    void toInstant_returns_utc_instant() {
        var ld = TsurugiTypes.LocalDatetime.newBuilder()
                .setOffsetSeconds(0)
                .setNanoAdjustment(0)
                .build();
        Instant instant = LocalDatetimeConverter.toInstant(ld, ZoneId.of("UTC"));
        assertEquals(Instant.EPOCH, instant);
    }

    @Test
    void getDayOfWeek_sunday_is_0() {
        // 1970-01-04 00:00:00 UTC は日曜日（epoch+3日）
        long offsetSeconds = 3 * 24 * 3600;
        var ld = TsurugiTypes.LocalDatetime.newBuilder()
                .setOffsetSeconds(offsetSeconds)
                .setNanoAdjustment(0)
                .build();
        ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(ld, ZoneId.of("UTC"));
        assertEquals(0, LocalDatetimeConverter.getDayOfWeek(zdt));
    }

    @Test
    void getWeekOfYear() {
        // 2024-01-01 00:00:00 UTC（月曜日）週番号1
        long offsetSeconds = (365 + 366 + 365 + 365) * 24 * 3600L; // 1970→1974 は閏年含め
        // 簡易: 1974-01-01 を計算。1970+4年で days = 365+366+365+365 = 1461
        long days = 365 + 366 + 365 + 365;
        offsetSeconds = days * 24 * 3600;
        var ld = TsurugiTypes.LocalDatetime.newBuilder()
                .setOffsetSeconds(offsetSeconds)
                .setNanoAdjustment(0)
                .build();
        ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(ld, ZoneId.of("UTC"));
        assertEquals(1974, zdt.getYear());
        assertEquals(1, zdt.getMonthValue());
        int week = LocalDatetimeConverter.getWeekOfYear(zdt);
        assertEquals(1, week);
    }

    @Test
    void getEpochMilli() {
        var ld = TsurugiTypes.LocalDatetime.newBuilder()
                .setOffsetSeconds(0)
                .setNanoAdjustment(0)
                .build();
        ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(ld, ZoneId.of("UTC"));
        assertEquals(0L, LocalDatetimeConverter.getEpochMilli(zdt));
    }
}
