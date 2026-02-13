package jp.septigram.tsurugi.udf.datetime1.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.tsurugidb.udf.TsurugiTypes;
import java.time.Instant;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

/**
 * OffsetDatetimeConverter の単体テスト。各型・境界値・週番号を検証する。
 */
class OffsetDatetimeConverterTest {

    @Test
    void toInstant_epoch() {
        var od = TsurugiTypes.OffsetDatetime.newBuilder()
                .setOffsetSeconds(0)
                .setNanoAdjustment(0)
                .setTimeZoneOffset(0)
                .build();
        Instant result = OffsetDatetimeConverter.toInstant(od);
        assertEquals(Instant.EPOCH, result);
    }

    @Test
    void toOffsetDateTime_epoch_utc() {
        var od = TsurugiTypes.OffsetDatetime.newBuilder()
                .setOffsetSeconds(0)
                .setNanoAdjustment(0)
                .setTimeZoneOffset(0)  // UTC
                .build();
        OffsetDateTime result = OffsetDatetimeConverter.toOffsetDateTime(od);
        assertEquals(1970, result.getYear());
        assertEquals(1, result.getMonthValue());
        assertEquals(1, result.getDayOfMonth());
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
        assertEquals(Instant.EPOCH, result.toInstant());
    }

    @Test
    void toOffsetDateTime_jst_offset() {
        // 1970-01-01 00:00:00 UTC、JST(+540分) で表示すると 1970-01-01 09:00:00+09:00
        var od = TsurugiTypes.OffsetDatetime.newBuilder()
                .setOffsetSeconds(0)
                .setNanoAdjustment(0)
                .setTimeZoneOffset(540)  // +09:00
                .build();
        OffsetDateTime result = OffsetDatetimeConverter.toOffsetDateTime(od);
        assertEquals(1970, result.getYear());
        assertEquals(1, result.getMonthValue());
        assertEquals(1, result.getDayOfMonth());
        assertEquals(9, result.getHour());
        assertEquals(0, result.getMinute());
    }

    @Test
    void toOffsetDateTime_with_nano_adjustment() {
        // 1秒 + 500ミリ秒
        var od = TsurugiTypes.OffsetDatetime.newBuilder()
                .setOffsetSeconds(1)
                .setNanoAdjustment(500_000_000)
                .setTimeZoneOffset(0)
                .build();
        OffsetDateTime result = OffsetDatetimeConverter.toOffsetDateTime(od);
        assertEquals(1, result.getSecond());
        assertEquals(500, OffsetDatetimeConverter.getMillisecond(result));
    }

    @Test
    void toOffsetDateTime_null_returns_null() {
        assertNull(OffsetDatetimeConverter.toOffsetDateTime(null));
    }

    @Test
    void toInstant_null_returns_null() {
        assertNull(OffsetDatetimeConverter.toInstant(null));
    }

    @Test
    void getDayOfWeek() {
        // 1970-01-01 00:00:00 UTC は木曜日 → 4
        var od = TsurugiTypes.OffsetDatetime.newBuilder()
                .setOffsetSeconds(0)
                .setNanoAdjustment(0)
                .setTimeZoneOffset(0)
                .build();
        OffsetDateTime odt = OffsetDatetimeConverter.toOffsetDateTime(od);
        assertEquals(4, OffsetDatetimeConverter.getDayOfWeek(odt));
    }

    @Test
    void getWeekOfYear() {
        // 1970-01-01 木曜日、週番号1（その週の木曜日が含まれる週）
        var od = TsurugiTypes.OffsetDatetime.newBuilder()
                .setOffsetSeconds(0)
                .setNanoAdjustment(0)
                .setTimeZoneOffset(0)
                .build();
        OffsetDateTime odt = OffsetDatetimeConverter.toOffsetDateTime(od);
        assertEquals(1, OffsetDatetimeConverter.getWeekOfYear(odt));
    }

    @Test
    void getEpochMilli() {
        var od = TsurugiTypes.OffsetDatetime.newBuilder()
                .setOffsetSeconds(0)
                .setNanoAdjustment(0)
                .setTimeZoneOffset(0)
                .build();
        OffsetDateTime odt = OffsetDatetimeConverter.toOffsetDateTime(od);
        assertEquals(0L, OffsetDatetimeConverter.getEpochMilli(odt));
    }
}
