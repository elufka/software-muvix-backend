package com.muvix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.cassandra.autoconfigure.CassandraAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(excludeName = {
        "org.springframework.boot.cassandra.autoconfigure.CassandraAutoConfiguration",
        "org.springframework.boot.data.cassandra.autoconfigure.DataCassandraAutoConfiguration",
        "org.springframework.boot.data.cassandra.autoconfigure.DataCassandraReactiveAutoConfiguration",
        "org.springframework.boot.data.cassandra.autoconfigure.DataCassandraRepositoriesAutoConfiguration"
})
@ComponentScan(
        basePackages = "com.muvix",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.muvix\\.infrastructure\\.adapters\\.out\\.persistence\\.cassandra\\..*"
        )
)
@EnableScheduling
public class MuvixApplication {

    public static void main(String[] args) {
        SpringApplication.run(MuvixApplication.class, args);
    }

}
