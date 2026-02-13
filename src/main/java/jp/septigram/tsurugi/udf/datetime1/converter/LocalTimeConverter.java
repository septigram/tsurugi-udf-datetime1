package jp.septigram.tsurugi.udf.datetime1.converter;

import com.tsurugidb.udf.TsurugiTypes;
import java.time.LocalTime;

/**
 * tsurugidb.udf.LocalTime と java.time.LocalTime の相互変換を行う。
 * <p>
 * 設計書 5.1 に基づく: LocalTime の {@code nanos} は 0 時からのナノ秒（sint64）。
 * ナノ秒から時・分・秒・ミリ秒は除算と剰余で算出する。
 */
public final class LocalTimeConverter {

    private static final long NANOS_PER_SECOND = 1_000_000_000L;
    private static final long NANOS_PER_DAY = 24 * 60 * 60 * NANOS_PER_SECOND;

    private LocalTimeConverter() {
    }

    /**
     * Tsurugi LocalTime を java.time.LocalTime に変換する。
     *
     * @param localTime Tsurugi の LocalTime（null の場合は null を返す）
     * @return 対応する java.time.LocalTime
     */
    public static LocalTime toJavaLocalTime(TsurugiTypes.LocalTime localTime) {
        if (localTime == null) {
            return null;
        }
        long nanos = localTime.getNanos();
        // 0-86399999999999 の範囲に正規化（24時間未満）
        if (nanos < 0 || nanos >= NANOS_PER_DAY) {
            nanos = Math.floorMod(nanos, NANOS_PER_DAY);
        }
        return LocalTime.ofNanoOfDay(nanos);
    }

    /**
     * java.time.LocalTime を Tsurugi LocalTime に変換する。
     *
     * @param localTime java.time.LocalTime（null の場合は null を返す）
     * @return 対応する Tsurugi LocalTime
     */
    public static TsurugiTypes.LocalTime toTsurugiLocalTime(LocalTime localTime) {
        if (localTime == null) {
            return null;
        }
        return TsurugiTypes.LocalTime.newBuilder()
                .setNanos(localTime.toNanoOfDay())
                .build();
    }

    /**
     * LocalTime から時（0-23）を取得する。
     */
    public static int getHour(LocalTime localTime) {
        return localTime.getHour();
    }

    /**
     * LocalTime から分（0-59）を取得する。
     */
    public static int getMinute(LocalTime localTime) {
        return localTime.getMinute();
    }

    /**
     * LocalTime から秒（0-59）を取得する。
     */
    public static int getSecond(LocalTime localTime) {
        return localTime.getSecond();
    }

    /**
     * LocalTime からミリ秒（0-999）を取得する。
     */
    public static int getMillisecond(LocalTime localTime) {
        return localTime.getNano() / 1_000_000;
    }
}
