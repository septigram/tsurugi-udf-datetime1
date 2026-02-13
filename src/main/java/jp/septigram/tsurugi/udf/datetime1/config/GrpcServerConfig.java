package jp.septigram.tsurugi.udf.datetime1.config;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.SmartLifecycle;
import jp.septigram.tsurugi.udf.datetime1.service.DateTimeUdfServiceImpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

/**
 * gRPC サーバの起動設定。
 * application.yml の grpc.server.port でポートを指定する。
 * <p>
 * spring-boot-starter のみ使用時は Web サーバがなくメインスレッドが即座に戻るため、
 * ApplicationRunner でシャットダウンシグナルまでブロックする。
 */
@Configuration
public class GrpcServerConfig {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerConfig.class);

    @Value("${grpc.server.port:50051}")
    private int grpcPort;

    @Bean
    public DateTimeUdfServiceImpl dateTimeUdfService() {
        return new DateTimeUdfServiceImpl();
    }

    @Bean
    public GrpcServerLifecycle grpcServerLifecycle(DateTimeUdfServiceImpl dateTimeUdfService) {
        return new GrpcServerLifecycle(dateTimeUdfService, grpcPort);
    }

    /**
     * シャットダウンシグナル（Ctrl+C 等）までメインスレッドをブロックし、
     * アプリケーションが即座に終了するのを防ぐ。
     */
    @Bean
    public ApplicationRunner grpcServerKeepAlive() {
        CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(latch::countDown));
        return (ApplicationArguments args) -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }

    /**
     * gRPC サーバを SmartLifecycle で管理。コンテキスト起動完了後にサーバを起動し、
     * 0.0.0.0 にバインドして確実にリッスンする。
     */
    public static class GrpcServerLifecycle implements SmartLifecycle {

        private final DateTimeUdfServiceImpl dateTimeUdfService;
        private final int port;
        private volatile Server server;
        private volatile boolean running;

        GrpcServerLifecycle(DateTimeUdfServiceImpl dateTimeUdfService, int port) {
            this.dateTimeUdfService = dateTimeUdfService;
            this.port = port;
        }

        @Override
        public void start() {
            try {
                server = NettyServerBuilder.forAddress(new InetSocketAddress("0.0.0.0", port))
                        .addService(dateTimeUdfService)
                        .build()
                        .start();
                running = true;
                log.info("gRPC server started, listening on 0.0.0.0:{}", port);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to start gRPC server on port " + port, e);
            }
        }

        @Override
        public void stop() {
            running = false;
            if (server != null && !server.isShutdown()) {
                server.shutdown();
                log.info("gRPC server stopped");
            }
        }

        @Override
        public boolean isRunning() {
            return running;
        }
    }
}
