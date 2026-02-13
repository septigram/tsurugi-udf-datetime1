package jp.septigram.tsurugi.udf.datetime1.converter;

import com.tsurugidb.udf.TsurugiTypes;
import java.time.LocalDate;
import java.time.temporal.IsoFields;

/**
 * tsurugidb.udf.Date と LocalDate の相互変換を行う。
 * <p>
 * 設計書 5.1 に基づく: Date の {@code days} は 1970-01-01 からの日数（sint32）。
 * LocalDate.ofEpochDay(days) で変換する。
 */
public final class DateConverter {

    private DateConverter() {
    }

    /**
     * Tsurugi Date を LocalDate に変換する。
     *
     * @param date Tsurugi の Date（null の場合は null を返す）
     * @return 対応する LocalDate
     */
    public static LocalDate toLocalDate(TsurugiTypes.Date date) {
        if (date == null) {
            return null;
        }
        return LocalDate.ofEpochDay(date.getDays());
    }

    /**
     * LocalDate を Tsurugi Date に変換する。
     *
     * @param localDate LocalDate（null の場合は null を返す）
     * @return 対応する Tsurugi Date
     */
    public static TsurugiTypes.Date toTsurugiDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return TsurugiTypes.Date.newBuilder()
                .setDays((int) localDate.toEpochDay())
                .build();
    }

    /**
     * LocalDate から年を取得する。
     */
    public static int getYear(LocalDate localDate) {
        return localDate.getYear();
    }

    /**
     * LocalDate から月（1-12）を取得する。
     */
    public static int getMonth(LocalDate localDate) {
        return localDate.getMonthValue();
    }

    /**
     * LocalDate から日を取得する。
     */
    public static int getDay(LocalDate localDate) {
        return localDate.getDayOfMonth();
    }

    /**
     * LocalDate から曜日（0=日曜, 1=月曜, ..., 6=土曜）を取得する。
     */
    public static int getDayOfWeek(LocalDate localDate) {
        return localDate.getDayOfWeek().getValue() % 7;
    }

    /**
     * LocalDate から週番号（ISO 8601、1-52 または 53）を取得する。
     */
    public static int getWeekOfYear(LocalDate localDate) {
        return localDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }
}
