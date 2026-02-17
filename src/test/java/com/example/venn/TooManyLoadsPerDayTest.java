package com.example.venn;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;

/**
 * This test exercises the path where too many load events per day are attempted to be added.
 * The system will not process any load beyond the limit of 3 loads per day.
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("testcontainertest")
@ContextConfiguration(initializers = {TooManyLoadsPerDayTest.Initializer.class})
public class TooManyLoadsPerDayTest {

    static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            var username = "postgres";
            var password = "mysecretpassword";
            Network network = Network.newNetwork();
            PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:15")
                    .withDatabaseName("postgres")
                    .withUsername(username)
                    .withPassword(password);
            postgreSQLContainer.withNetwork(network);
            postgreSQLContainer.start();
            String jdbcUrl = postgreSQLContainer.getJdbcUrl();

            TestPropertyValues testPropertyValues = TestPropertyValues.of(
                    "spring.datasource.url=" + jdbcUrl,
                    "spring.datasource.username=" + username,
                    "spring.datasource.password=" + password,
                    "loadevent.input.file=" + "${user.dir}/testInput/tooManyLoadsPerDay.txt"
            );
            testPropertyValues.applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Test
    public void tooManyLoadsPerDayTest() throws InterruptedException, IOException {
        System.out.println("Running test");
        Thread.sleep(20 * 1000L);
        var userDir = System.getProperty("user.dir");
        var outputFile = new File(userDir).toPath().resolve("output/output.txt").toFile();
        var referenceOutputFile = new File(userDir).toPath().resolve("referenceOutput/tooManyLoadsPerDay.txt").toFile();
        Reader r1 = Files.newBufferedReader(outputFile.toPath());
        Reader r2 = Files.newBufferedReader(referenceOutputFile.toPath());
        var result = IOUtils.contentEqualsIgnoreEOL(r1, r2);
        Assertions.assertEquals(true, result);
        System.out.println("Finished test");
    }
}
