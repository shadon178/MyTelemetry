package com.pxd.telemetry;

import com.pxd.telemetry.entity.DemoResponse;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RestController
public class DemoController {

    private final Meter meter;

    /**
     * 计数器 - 记录API调用次数
     */
    private final LongCounter apiCallCounter;

    /**
     * 直方图 - 记录API响应时间
     */
    private final DoubleHistogram apiResponseTimeHistogram;

    /**
     * gauge - 记录当前活跃用户数
     */
    private final AtomicLong activeUsers;

    public DemoController(OpenTelemetry openTelemetry) {
        this.meter = openTelemetry.getMeter(DemoController.class.getName());

        apiCallCounter = meter.counterBuilder("api.calls.count")
            .setDescription("API接口访问次数")
            .setUnit("ts")
            .build();

        apiResponseTimeHistogram = meter.histogramBuilder("api.response.time")
            .setDescription("API接口响应毫秒数")
            .setUnit("ms")
            .build();

        activeUsers = new AtomicLong(0);

        meter.gaugeBuilder("active.users.count")
            .setDescription("当前活跃用户数")
            .setUnit("users")
            .buildWithCallback(measurement -> measurement.record(activeUsers.get()));
    }

    @GetMapping("/hello")
    public DemoResponse hello() {
        long start = System.currentTimeMillis();
        DemoResponse demoResponse = new DemoResponse();
        demoResponse.setName("Hello World");
        demoResponse.setAge(100);
        log.info("hello: response = {}", demoResponse);
        long end = System.currentTimeMillis();
        long durationMs = end - start;

        apiCallCounter.add(1,
            io.opentelemetry.api.common.Attributes.of(
                io.opentelemetry.api.common.AttributeKey.stringKey("endpoint"), "/hello"
            ));

        apiResponseTimeHistogram.record(durationMs,
            io.opentelemetry.api.common.Attributes.of(
                io.opentelemetry.api.common.AttributeKey.stringKey("endpoint"), "/hello"
            ));
        return demoResponse;
    }

    @GetMapping("/login")
    public void userLogin() {
        activeUsers.incrementAndGet();
    }

    @GetMapping("/logout")
    public void userLogout() {
        activeUsers.decrementAndGet();
    }

}
