package jp.septigram.tsurugi.udf.datetime1.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.tsurugidb.udf.TsurugiTypes;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * DateConverter の単体テスト。各型・境界値・往復変換を検証する。
 */
class DateConverterTest {

    @Test
    void toLocalDate_epoch_returns_1970_01_01() {
        var date = TsurugiTypes.Date.newBuilder().setDays(0).build();
        LocalDate result = DateConverter.toLocalDate(date);
        assertEquals(LocalDate.of(1970, 1, 1), result);
    }

    @Test
    void toLocalDate_2024_01_15() {
        // 2024-01-15 = epoch + 19737 days（19723=2024-01-01）
        var date = TsurugiTypes.Date.newBuilder().setDays(19737).build();
        LocalDate result = DateConverter.toLocalDate(date);
        assertEquals(LocalDate.of(2024, 1, 15), result);
    }

    @Test
    void toLocalDate_negative_days_before_epoch() {
        var date = TsurugiTypes.Date.newBuilder().setDays(-1).build();
        LocalDate result = DateConverter.toLocalDate(date);
        assertEquals(LocalDate.of(1969, 12, 31), result);
    }

    @Test
    void toLocalDate_null_returns_null() {
        assertNull(DateConverter.toLocalDate(null));
    }

    @Test
    void toTsurugiDate_roundtrip() {
        LocalDate original = LocalDate.of(2024, 6, 15);
        TsurugiTypes.Date tsurugi = DateConverter.toTsurugiDate(original);
        LocalDate back = DateConverter.toLocalDate(tsurugi);
        assertEquals(original, back);
    }

    @Test
    void toTsurugiDate_null_returns_null() {
        assertNull(DateConverter.toTsurugiDate(null));
    }

    @Test
    void getYear() {
        assertEquals(2024, DateConverter.getYear(LocalDate.of(2024, 3, 15)));
    }

    @Test
    void getMonth() {
        assertEquals(3, DateConverter.getMonth(LocalDate.of(2024, 3, 15)));
    }

    @Test
    void getDay() {
        assertEquals(15, DateConverter.getDay(LocalDate.of(2024, 3, 15)));
    }

    @Test
    void getDayOfWeek_sunday_is_0() {
        // 2024-01-07 は日曜日
        assertEquals(0, DateConverter.getDayOfWeek(LocalDate.of(2024, 1, 7)));
    }

    @Test
    void getDayOfWeek_monday_is_1() {
        assertEquals(1, DateConverter.getDayOfWeek(LocalDate.of(2024, 1, 8)));
    }

    @Test
    void getDayOfWeek_saturday_is_6() {
        assertEquals(6, DateConverter.getDayOfWeek(LocalDate.of(2024, 1, 6)));
    }

    @Test
    void getWeekOfYear_iso8601() {
        // 2024-01-01 は月曜日、週番号1
        assertEquals(1, DateConverter.getWeekOfYear(LocalDate.of(2024, 1, 1)));
    }

    @Test
    void getWeekOfYear_year_boundary() {
        // 2023-12-31 は日曜日、2024年の第1週の前日
        assertEquals(52, DateConverter.getWeekOfYear(LocalDate.of(2023, 12, 31)));
    }
}
