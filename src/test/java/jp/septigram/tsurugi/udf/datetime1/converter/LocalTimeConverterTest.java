package jp.septigram.tsurugi.udf.datetime1.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.tsurugidb.udf.TsurugiTypes;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

/**
 * LocalTimeConverter の単体テスト。各型・境界値・24時間超の正規化を検証する。
 */
class LocalTimeConverterTest {

    private static final long NANOS_PER_SECOND = 1_000_000_000L;
    private static final long NANOS_PER_DAY = 24 * 60 * 60 * NANOS_PER_SECOND;

    @Test
    void toJavaLocalTime_midnight() {
        var lt = TsurugiTypes.LocalTime.newBuilder().setNanos(0).build();
        LocalTime result = LocalTimeConverter.toJavaLocalTime(lt);
        assertEquals(LocalTime.MIDNIGHT, result);
    }

    @Test
    void toJavaLocalTime_noon() {
        long nanos = 12L * 3600 * NANOS_PER_SECOND;
        var lt = TsurugiTypes.LocalTime.newBuilder().setNanos(nanos).build();
        LocalTime result = LocalTimeConverter.toJavaLocalTime(lt);
        assertEquals(LocalTime.NOON, result);
    }

    @Test
    void toJavaLocalTime_with_milliseconds() {
        // 12:30:45.123
        long nanos = 12L * 3600 * NANOS_PER_SECOND
                + 30 * 60 * NANOS_PER_SECOND
                + 45 * NANOS_PER_SECOND
                + 123_000_000;
        var lt = TsurugiTypes.LocalTime.newBuilder().setNanos(nanos).build();
        LocalTime result = LocalTimeConverter.toJavaLocalTime(lt);
        assertEquals(LocalTime.of(12, 30, 45, 123_000_000), result);
    }

    @Test
    void toJavaLocalTime_23_59_59_999() {
        long nanos = NANOS_PER_DAY - 1; // 23:59:59.999999999
        var lt = TsurugiTypes.LocalTime.newBuilder().setNanos(nanos).build();
        LocalTime result = LocalTimeConverter.toJavaLocalTime(lt);
        assertEquals(23, result.getHour());
        assertEquals(59, result.getMinute());
        assertEquals(59, result.getSecond());
        assertEquals(999, result.getNano() / 1_000_000);
    }

    @Test
    void toJavaLocalTime_overflow_normalized() {
        // 25:00:00 = 1日 + 1時間 → 01:00:00 に正規化
        long nanos = NANOS_PER_DAY + 3600 * NANOS_PER_SECOND;
        var lt = TsurugiTypes.LocalTime.newBuilder().setNanos(nanos).build();
        LocalTime result = LocalTimeConverter.toJavaLocalTime(lt);
        assertEquals(LocalTime.of(1, 0, 0), result);
    }

    @Test
    void toJavaLocalTime_negative_normalized() {
        // -1時間 = 23:00:00 に正規化
        long nanos = -3600L * NANOS_PER_SECOND;
        var lt = TsurugiTypes.LocalTime.newBuilder().setNanos(nanos).build();
        LocalTime result = LocalTimeConverter.toJavaLocalTime(lt);
        assertEquals(LocalTime.of(23, 0, 0), result);
    }

    @Test
    void toJavaLocalTime_null_returns_null() {
        assertNull(LocalTimeConverter.toJavaLocalTime(null));
    }

    @Test
    void toTsurugiLocalTime_roundtrip() {
        LocalTime original = LocalTime.of(14, 30, 45, 123_456_789);
        TsurugiTypes.LocalTime tsurugi = LocalTimeConverter.toTsurugiLocalTime(original);
        LocalTime back = LocalTimeConverter.toJavaLocalTime(tsurugi);
        assertEquals(original, back);
    }

    @Test
    void toTsurugiLocalTime_null_returns_null() {
        assertNull(LocalTimeConverter.toTsurugiLocalTime(null));
    }

    @Test
    void getHour_minute_second_millisecond() {
        LocalTime lt = LocalTime.of(9, 5, 7, 500_000_000);
        assertEquals(9, LocalTimeConverter.getHour(lt));
        assertEquals(5, LocalTimeConverter.getMinute(lt));
        assertEquals(7, LocalTimeConverter.getSecond(lt));
        assertEquals(500, LocalTimeConverter.getMillisecond(lt));
    }
}
