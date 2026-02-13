package jp.septigram.tsurugi.udf.datetime1.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tsurugidb.udf.TsurugiTypes;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import jp.septigram.tsurugi.udf.datetime1.DatetimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * DateTimeUdfServiceImpl の単体テスト。主要 RPC の戻り値を検証する。
 */
class DateTimeUdfServiceImplTest {

    private DateTimeUdfServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DateTimeUdfServiceImpl();
    }

    private static <T> StreamObserver<T> capturingObserver(AtomicReference<T> resultRef,
            AtomicReference<Throwable> errorRef, CountDownLatch latch) {
        return new StreamObserver<>() {
            @Override
            public void onNext(T value) {
                resultRef.set(value);
            }

            @Override
            public void onError(Throwable t) {
                errorRef.set(t);
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };
    }

    @Test
    void dateYear_1970_01_01_returns_1970() throws Exception {
        var request = DatetimeService.DateRequest.newBuilder()
                .setValue(TsurugiTypes.Date.newBuilder().setDays(0).build())
                .build();
        var resultRef = new AtomicReference<DatetimeService.Int32Value>();
        var errorRef = new AtomicReference<Throwable>();
        var latch = new CountDownLatch(1);

        service.dateYear(request, capturingObserver(resultRef, errorRef, latch));
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertNull(errorRef.get(), () -> "Unexpected error: " + errorRef.get());

        assertEquals(1970, resultRef.get().getValue());
    }

    @Test
    void dateYear_2024_01_15_returns_2024() throws Exception {
        // 2024-01-15 = 1970-01-01 + 19737 days
        var request = DatetimeService.DateRequest.newBuilder()
                .setValue(TsurugiTypes.Date.newBuilder().setDays(19737).build())
                .build();
        var resultRef = new AtomicReference<DatetimeService.Int32Value>();
        var errorRef = new AtomicReference<Throwable>();
        var latch = new CountDownLatch(1);

        service.dateYear(request, capturingObserver(resultRef, errorRef, latch));
        assertTrue(latch.await(2, TimeUnit.SECONDS));

        assertEquals(2024, resultRef.get().getValue());
    }

    @Test
    void dateDayOfWeek_1970_01_01_returns_4_thursday() throws Exception {
        // 1970-01-01 は木曜日。Tsurugi: 0=Sun, 1=Mon, 2=Tue, 3=Wed, 4=Thu
        var request = DatetimeService.DateRequest.newBuilder()
                .setValue(TsurugiTypes.Date.newBuilder().setDays(0).build())
                .build();
        var resultRef = new AtomicReference<DatetimeService.Int32Value>();
        var errorRef = new AtomicReference<Throwable>();
        var latch = new CountDownLatch(1);

        service.dateDayOfWeek(request, capturingObserver(resultRef, errorRef, latch));
        assertTrue(latch.await(2, TimeUnit.SECONDS));

        assertEquals(4, resultRef.get().getValue()); // Thursday
    }

    @Test
    void timeHour_noon_returns_12() throws Exception {
        // 12:00:00 = 12 * 3600 * 1_000_000_000 nanos
        long nanos = 12L * 3600 * 1_000_000_000;
        var request = DatetimeService.TimeRequest.newBuilder()
                .setValue(TsurugiTypes.LocalTime.newBuilder().setNanos(nanos).build())
                .build();
        var resultRef = new AtomicReference<DatetimeService.Int32Value>();
        var errorRef = new AtomicReference<Throwable>();
        var latch = new CountDownLatch(1);

        service.timeHour(request, capturingObserver(resultRef, errorRef, latch));
        assertTrue(latch.await(2, TimeUnit.SECONDS));

        assertEquals(12, resultRef.get().getValue());
    }

    @Test
    void timestampYear_epoch_in_jst() throws Exception {
        // offset_seconds=0, nano_adjustment=0 in JST = 1969-12-31 15:00:00 JST → 年は1969
        // ただしシステム TZ が JST の場合。環境依存なので、0,0 で 1970-01-01 00:00:00 local になる TZ なら年は1970
        // offset_seconds は「ローカルTZでの epoch からのオフセット」なので、
        // 0 = そのTZの 1970-01-01 00:00:00 → 年は 1970
        var request = DatetimeService.TimestampRequest.newBuilder()
                .setValue(TsurugiTypes.LocalDatetime.newBuilder()
                        .setOffsetSeconds(0)
                        .setNanoAdjustment(0)
                        .build())
                .build();
        var resultRef = new AtomicReference<DatetimeService.Int32Value>();
        var errorRef = new AtomicReference<Throwable>();
        var latch = new CountDownLatch(1);

        service.timestampYear(request, capturingObserver(resultRef, errorRef, latch));
        assertTrue(latch.await(2, TimeUnit.SECONDS));

        assertEquals(1970, resultRef.get().getValue());
    }

    @Test
    void offsetTimestampEpochMilli_returns_0() throws Exception {
        var request = DatetimeService.OffsetTimestampRequest.newBuilder()
                .setValue(TsurugiTypes.OffsetDatetime.newBuilder()
                        .setOffsetSeconds(0)
                        .setNanoAdjustment(0)
                        .setTimeZoneOffset(0)  // UTC
                        .build())
                .build();
        var resultRef = new AtomicReference<DatetimeService.Int64Value>();
        var errorRef = new AtomicReference<Throwable>();
        var latch = new CountDownLatch(1);

        service.offsetTimestampEpochMilli(request, capturingObserver(resultRef, errorRef, latch));
        assertTrue(latch.await(2, TimeUnit.SECONDS));

        assertEquals(0L, resultRef.get().getValue());
    }

    @Test
    void timestampYearTz_invalid_timezone_returns_error() throws Exception {
        var request = DatetimeService.TimestampWithTzRequest.newBuilder()
                .setValue(TsurugiTypes.LocalDatetime.newBuilder()
                        .setOffsetSeconds(0)
                        .setNanoAdjustment(0)
                        .build())
                .setTimeZone("Invalid/Timezone_X")
                .build();
        var resultRef = new AtomicReference<DatetimeService.Int32Value>();
        var errorRef = new AtomicReference<Throwable>();
        var latch = new CountDownLatch(1);

        service.timestampYearTz(request, capturingObserver(resultRef, errorRef, latch));
        assertTrue(latch.await(2, TimeUnit.SECONDS));

        assertNotNull(errorRef.get());
        assertTrue(errorRef.get() instanceof StatusRuntimeException);
        assertEquals(Status.Code.INVALID_ARGUMENT,
                ((StatusRuntimeException) errorRef.get()).getStatus().getCode());
    }

    @Test
    void dateMonth_and_dateDay() throws Exception {
        // 2024-06-15（epoch day = 19889）
        var request = DatetimeService.DateRequest.newBuilder()
                .setValue(TsurugiTypes.Date.newBuilder().setDays(19889).build())
                .build();
        var monthRef = new AtomicReference<DatetimeService.Int32Value>();
        var dayRef = new AtomicReference<DatetimeService.Int32Value>();
        var latch = new CountDownLatch(2);

        service.dateMonth(request, capturingObserver(monthRef, new AtomicReference<>(), latch));
        service.dateDay(request, capturingObserver(dayRef, new AtomicReference<>(), latch));
        assertTrue(latch.await(2, TimeUnit.SECONDS));

        assertEquals(6, monthRef.get().getValue());
        assertEquals(15, dayRef.get().getValue());
    }

    @Test
    void dateWeekOfYear() throws Exception {
        // 2024-01-15 は第3週（2024-01-01 が月曜なので週番号1）
        var request = DatetimeService.DateRequest.newBuilder()
                .setValue(TsurugiTypes.Date.newBuilder().setDays(19737).build())
                .build();
        var resultRef = new AtomicReference<DatetimeService.Int32Value>();
        var errorRef = new AtomicReference<Throwable>();
        var latch = new CountDownLatch(1);

        service.dateWeekOfYear(request, capturingObserver(resultRef, errorRef, latch));
        assertTrue(latch.await(2, TimeUnit.SECONDS));

        assertNull(errorRef.get());
        assertEquals(3, resultRef.get().getValue()); // 2024-01-15 は第3週
    }

    @Test
    void timeMinute() throws Exception {
        // 12:34:56.789
        long nanos = 12L * 3600 * 1_000_000_000
                + 34L * 60 * 1_000_000_000
                + 56L * 1_000_000_000
                + 789_000_000;
        var request = DatetimeService.TimeRequest.newBuilder()
                .setValue(TsurugiTypes.LocalTime.newBuilder().setNanos(nanos).build())
                .build();
        var resultRef = new AtomicReference<DatetimeService.Int32Value>();
        var errorRef = new AtomicReference<Throwable>();
        var latch = new CountDownLatch(1);
        service.timeMinute(request, capturingObserver(resultRef, errorRef, latch));
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertNull(errorRef.get());
        assertEquals(34, resultRef.get().getValue());
    }

    @Test
    void timeSecond() throws Exception {
        long nanos = 12L * 3600 * 1_000_000_000
                + 34L * 60 * 1_000_000_000
                + 56L * 1_000_000_000
                + 789_000_000;
        var request = DatetimeService.TimeRequest.newBuilder()
                .setValue(TsurugiTypes.LocalTime.newBuilder().setNanos(nanos).build())
                .build();
        var resultRef = new AtomicReference<DatetimeService.Int32Value>();
        var latch = new CountDownLatch(1);
        service.timeSecond(request, capturingObserver(resultRef, new AtomicReference<>(), latch));
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(56, resultRef.get().getValue());
    }

    @Test
    void timeMillisecond() throws Exception {
        long nanos = 12L * 3600 * 1_000_000_000
                + 34L * 60 * 1_000_000_000
                + 56L * 1_000_000_000
                + 789_000_000;
        var request = DatetimeService.TimeRequest.newBuilder()
                .setValue(TsurugiTypes.LocalTime.newBuilder().setNanos(nanos).build())
                .build();
        var resultRef = new AtomicReference<DatetimeService.Int32Value>();
        var latch = new CountDownLatch(1);
        service.timeMillisecond(request, capturingObserver(resultRef, new AtomicReference<>(), latch));
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(789, resultRef.get().getValue());
    }

    @Test
    void timestampYearTz_with_asia_tokyo() throws Exception {
        // offset_seconds=0, nano=0 を Asia/Tokyo で解釈 → 1970-01-01 00:00 JST
        var request = DatetimeService.TimestampWithTzRequest.newBuilder()
                .setValue(TsurugiTypes.LocalDatetime.newBuilder()
                        .setOffsetSeconds(0)
                        .setNanoAdjustment(0)
                        .build())
                .setTimeZone("Asia/Tokyo")
                .build();
        var resultRef = new AtomicReference<DatetimeService.Int32Value>();
        var errorRef = new AtomicReference<Throwable>();
        var latch = new CountDownLatch(1);

        service.timestampYearTz(request, capturingObserver(resultRef, errorRef, latch));
        assertTrue(latch.await(2, TimeUnit.SECONDS));

        assertNull(errorRef.get());
        assertEquals(1970, resultRef.get().getValue());
    }

    @Test
    void offsetTimestampMonth_day_hour() throws Exception {
        // 2024-06-15 12:00:00 UTC, JST(+540) で表示
        // Epoch: 2024-06-15 12:00:00 UTC（epoch day 19889）
        long epochSeconds = (19889L * 24 + 12) * 3600;
        var request = DatetimeService.OffsetTimestampRequest.newBuilder()
                .setValue(TsurugiTypes.OffsetDatetime.newBuilder()
                        .setOffsetSeconds(epochSeconds)
                        .setNanoAdjustment(0)
                        .setTimeZoneOffset(540)
                        .build())
                .build();
        var monthRef = new AtomicReference<DatetimeService.Int32Value>();
        var dayRef = new AtomicReference<DatetimeService.Int32Value>();
        var hourRef = new AtomicReference<DatetimeService.Int32Value>();
        var latch = new CountDownLatch(3);

        service.offsetTimestampMonth(request, capturingObserver(monthRef, new AtomicReference<>(), latch));
        service.offsetTimestampDay(request, capturingObserver(dayRef, new AtomicReference<>(), latch));
        service.offsetTimestampHour(request, capturingObserver(hourRef, new AtomicReference<>(), latch));
        assertTrue(latch.await(2, TimeUnit.SECONDS));

        assertEquals(6, monthRef.get().getValue());
        assertEquals(15, dayRef.get().getValue());
        assertEquals(21, hourRef.get().getValue()); // 12:00 UTC + 9h = 21:00 JST
    }
}
