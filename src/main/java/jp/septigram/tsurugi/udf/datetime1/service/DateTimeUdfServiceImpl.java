package jp.septigram.tsurugi.udf.datetime1.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.zone.ZoneRulesException;
import jp.septigram.tsurugi.udf.datetime1.DatetimeService;
import jp.septigram.tsurugi.udf.datetime1.DateTimeServiceGrpc;
import jp.septigram.tsurugi.udf.datetime1.converter.DateConverter;
import jp.septigram.tsurugi.udf.datetime1.converter.LocalDatetimeConverter;
import jp.septigram.tsurugi.udf.datetime1.converter.LocalTimeConverter;
import jp.septigram.tsurugi.udf.datetime1.converter.OffsetDatetimeConverter;

/**
 * DateTimeService の実装。コンバータを用いて Tsurugi 型を Java 日時型に変換し、
 * 各コンポーネント（年・月・日・曜日・週番号・時・分・秒・ミリ秒・EPOCH ミリ秒）を返す。
 */
public class DateTimeUdfServiceImpl extends DateTimeServiceGrpc.DateTimeServiceImplBase {

    private static DatetimeService.Int32Value int32(int value) {
        return DatetimeService.Int32Value.newBuilder().setValue(value).build();
    }

    private static DatetimeService.Int64Value int64(long value) {
        return DatetimeService.Int64Value.newBuilder().setValue(value).build();
    }

    private static void handleError(Throwable t, StreamObserver<?> observer) {
        if (t instanceof DateTimeException || t instanceof ZoneRulesException
                || t instanceof IllegalArgumentException || t instanceof ArithmeticException) {
            observer.onError(Status.INVALID_ARGUMENT
                    .withDescription(t.getMessage())
                    .withCause(t)
                    .asRuntimeException());
        } else {
            observer.onError(Status.INTERNAL
                    .withDescription(t.getMessage())
                    .withCause(t)
                    .asRuntimeException());
        }
    }

    private static ZoneId parseZoneId(String timeZone) {
        if (timeZone == null || timeZone.isBlank()) {
            throw new IllegalArgumentException("time_zone must not be empty");
        }
        return ZoneId.of(timeZone.trim());
    }

    // === TIMESTAMP (LocalDatetime) - システムデフォルトタイムゾーン ===

    @Override
    public void timestampYear(DatetimeService.TimestampRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(request.getValue());
            responseObserver.onNext(int32(LocalDatetimeConverter.getYear(zdt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void timestampMonth(DatetimeService.TimestampRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(request.getValue());
            responseObserver.onNext(int32(LocalDatetimeConverter.getMonth(zdt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void timestampDay(DatetimeService.TimestampRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(request.getValue());
            responseObserver.onNext(int32(LocalDatetimeConverter.getDay(zdt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void timestampDayOfWeek(DatetimeService.TimestampRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(request.getValue());
            responseObserver.onNext(int32(LocalDatetimeConverter.getDayOfWeek(zdt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void timestampWeekOfYear(DatetimeService.TimestampRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(request.getValue());
            responseObserver.onNext(int32(LocalDatetimeConverter.getWeekOfYear(zdt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void timestampHour(DatetimeService.TimestampRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(request.getValue());
            responseObserver.onNext(int32(LocalDatetimeConverter.getHour(zdt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void timestampMinute(DatetimeService.TimestampRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(request.getValue());
            responseObserver.onNext(int32(LocalDatetimeConverter.getMinute(zdt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void timestampSecond(DatetimeService.TimestampRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(request.getValue());
            responseObserver.onNext(int32(LocalDatetimeConverter.getSecond(zdt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void timestampMillisecond(DatetimeService.TimestampRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(request.getValue());
            responseObserver.onNext(int32(LocalDatetimeConverter.getMillisecond(zdt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void timestampEpochMilli(DatetimeService.TimestampRequest request,
            StreamObserver<DatetimeService.Int64Value> responseObserver) {
        try {
            ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(request.getValue());
            responseObserver.onNext(int64(LocalDatetimeConverter.getEpochMilli(zdt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    // === TIMESTAMP (LocalDatetime) - タイムゾーン指定 ===

    @Override
    public void timestampYearTz(DatetimeService.TimestampWithTzRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            ZoneId zoneId = parseZoneId(request.getTimeZone());
            ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(
                    request.getValue(), zoneId);
            responseObserver.onNext(int32(LocalDatetimeConverter.getYear(zdt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void timestampMonthTz(DatetimeService.TimestampWithTzRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            ZoneId zoneId = parseZoneId(request.getTimeZone());
            ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(
                    request.getValue(), zoneId);
            responseObserver.onNext(int32(LocalDatetimeConverter.getMonth(zdt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void timestampDayTz(DatetimeService.TimestampWithTzRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            ZoneId zoneId = parseZoneId(request.getTimeZone());
            ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(
                    request.getValue(), zoneId);
            responseObserver.onNext(int32(LocalDatetimeConverter.getDay(zdt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void timestampDayOfWeekTz(DatetimeService.TimestampWithTzRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            ZoneId zoneId = parseZoneId(request.getTimeZone());
            ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(
                    request.getValue(), zoneId);
            responseObserver.onNext(int32(LocalDatetimeConverter.getDayOfWeek(zdt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void timestampWeekOfYearTz(DatetimeService.TimestampWithTzRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            ZoneId zoneId = parseZoneId(request.getTimeZone());
            ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(
                    request.getValue(), zoneId);
            responseObserver.onNext(int32(LocalDatetimeConverter.getWeekOfYear(zdt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void timestampHourTz(DatetimeService.TimestampWithTzRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            ZoneId zoneId = parseZoneId(request.getTimeZone());
            ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(
                    request.getValue(), zoneId);
            responseObserver.onNext(int32(LocalDatetimeConverter.getHour(zdt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void timestampMinuteTz(DatetimeService.TimestampWithTzRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            ZoneId zoneId = parseZoneId(request.getTimeZone());
            ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(
                    request.getValue(), zoneId);
            responseObserver.onNext(int32(LocalDatetimeConverter.getMinute(zdt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void timestampSecondTz(DatetimeService.TimestampWithTzRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            ZoneId zoneId = parseZoneId(request.getTimeZone());
            ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(
                    request.getValue(), zoneId);
            responseObserver.onNext(int32(LocalDatetimeConverter.getSecond(zdt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void timestampMillisecondTz(DatetimeService.TimestampWithTzRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            ZoneId zoneId = parseZoneId(request.getTimeZone());
            ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(
                    request.getValue(), zoneId);
            responseObserver.onNext(int32(LocalDatetimeConverter.getMillisecond(zdt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void timestampEpochMilliTz(DatetimeService.TimestampWithTzRequest request,
            StreamObserver<DatetimeService.Int64Value> responseObserver) {
        try {
            ZoneId zoneId = parseZoneId(request.getTimeZone());
            ZonedDateTime zdt = LocalDatetimeConverter.toZonedDateTime(
                    request.getValue(), zoneId);
            responseObserver.onNext(int64(LocalDatetimeConverter.getEpochMilli(zdt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    // === TIMESTAMP WITH TIME ZONE (OffsetDatetime) ===

    @Override
    public void offsetTimestampYear(DatetimeService.OffsetTimestampRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            var odt = OffsetDatetimeConverter.toOffsetDateTime(request.getValue());
            responseObserver.onNext(int32(OffsetDatetimeConverter.getYear(odt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void offsetTimestampMonth(DatetimeService.OffsetTimestampRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            var odt = OffsetDatetimeConverter.toOffsetDateTime(request.getValue());
            responseObserver.onNext(int32(OffsetDatetimeConverter.getMonth(odt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void offsetTimestampDay(DatetimeService.OffsetTimestampRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            var odt = OffsetDatetimeConverter.toOffsetDateTime(request.getValue());
            responseObserver.onNext(int32(OffsetDatetimeConverter.getDay(odt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void offsetTimestampDayOfWeek(DatetimeService.OffsetTimestampRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            var odt = OffsetDatetimeConverter.toOffsetDateTime(request.getValue());
            responseObserver.onNext(int32(OffsetDatetimeConverter.getDayOfWeek(odt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void offsetTimestampWeekOfYear(DatetimeService.OffsetTimestampRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            var odt = OffsetDatetimeConverter.toOffsetDateTime(request.getValue());
            responseObserver.onNext(int32(OffsetDatetimeConverter.getWeekOfYear(odt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void offsetTimestampHour(DatetimeService.OffsetTimestampRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            var odt = OffsetDatetimeConverter.toOffsetDateTime(request.getValue());
            responseObserver.onNext(int32(OffsetDatetimeConverter.getHour(odt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void offsetTimestampMinute(DatetimeService.OffsetTimestampRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            var odt = OffsetDatetimeConverter.toOffsetDateTime(request.getValue());
            responseObserver.onNext(int32(OffsetDatetimeConverter.getMinute(odt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void offsetTimestampSecond(DatetimeService.OffsetTimestampRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            var odt = OffsetDatetimeConverter.toOffsetDateTime(request.getValue());
            responseObserver.onNext(int32(OffsetDatetimeConverter.getSecond(odt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void offsetTimestampMillisecond(DatetimeService.OffsetTimestampRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            var odt = OffsetDatetimeConverter.toOffsetDateTime(request.getValue());
            responseObserver.onNext(int32(OffsetDatetimeConverter.getMillisecond(odt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void offsetTimestampEpochMilli(DatetimeService.OffsetTimestampRequest request,
            StreamObserver<DatetimeService.Int64Value> responseObserver) {
        try {
            var odt = OffsetDatetimeConverter.toOffsetDateTime(request.getValue());
            responseObserver.onNext(int64(OffsetDatetimeConverter.getEpochMilli(odt)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    // === DATE (Date) ===

    @Override
    public void dateYear(DatetimeService.DateRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            var localDate = DateConverter.toLocalDate(request.getValue());
            responseObserver.onNext(int32(DateConverter.getYear(localDate)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void dateMonth(DatetimeService.DateRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            var localDate = DateConverter.toLocalDate(request.getValue());
            responseObserver.onNext(int32(DateConverter.getMonth(localDate)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void dateDay(DatetimeService.DateRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            var localDate = DateConverter.toLocalDate(request.getValue());
            responseObserver.onNext(int32(DateConverter.getDay(localDate)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void dateDayOfWeek(DatetimeService.DateRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            var localDate = DateConverter.toLocalDate(request.getValue());
            responseObserver.onNext(int32(DateConverter.getDayOfWeek(localDate)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void dateWeekOfYear(DatetimeService.DateRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            var localDate = DateConverter.toLocalDate(request.getValue());
            responseObserver.onNext(int32(DateConverter.getWeekOfYear(localDate)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    // === TIME (LocalTime) ===

    @Override
    public void timeHour(DatetimeService.TimeRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            var localTime = LocalTimeConverter.toJavaLocalTime(request.getValue());
            responseObserver.onNext(int32(LocalTimeConverter.getHour(localTime)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void timeMinute(DatetimeService.TimeRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            var localTime = LocalTimeConverter.toJavaLocalTime(request.getValue());
            responseObserver.onNext(int32(LocalTimeConverter.getMinute(localTime)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void timeSecond(DatetimeService.TimeRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            var localTime = LocalTimeConverter.toJavaLocalTime(request.getValue());
            responseObserver.onNext(int32(LocalTimeConverter.getSecond(localTime)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }

    @Override
    public void timeMillisecond(DatetimeService.TimeRequest request,
            StreamObserver<DatetimeService.Int32Value> responseObserver) {
        try {
            var localTime = LocalTimeConverter.toJavaLocalTime(request.getValue());
            responseObserver.onNext(int32(LocalTimeConverter.getMillisecond(localTime)));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            handleError(t, responseObserver);
        }
    }
}
