package com.pxd.telemetry;

import co.elastic.apm.attach.ElasticApmAttacher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OpenTelemetryApplication {

    public static void main(String[] args) {
        ElasticApmAttacher.attach();
        SpringApplication.run(OpenTelemetryApplication.class, args);
    }

}
