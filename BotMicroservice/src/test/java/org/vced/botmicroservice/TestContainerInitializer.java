//package org.vced.botmicroservice;
//
//import org.junit.ClassRule;
//import org.springframework.boot.test.util.TestPropertyValues;
//import org.springframework.context.ApplicationContextInitializer;
//import org.springframework.context.ConfigurableApplicationContext;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.utility.DockerImageName;
//
//public class TestContainerInitializer {
//    @ClassRule //from JUnit to set up a database container before executing test methods
//    // public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:11.1")
//    public static PostgreSQLContainer postgreSQLContainer = (PostgreSQLContainer) new PostgreSQLContainer(DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres"))
//            .withDatabaseName("integration-tests-db")
//            .withUsername("sa")
//            .withPassword("sa")
//            .withInitScript("init-vector.sql") // Указываем скрипт инициализации
//            .withLabel("container.name", "my-pgvector");
//    static {
//        postgreSQLContainer.start();
//    }
//
//    static class Initializer
//            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
//        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
//            TestPropertyValues.of(
//                    // wtf?? : https://stackoverflow.com/questions/61362092/spring-testcontainers-driver-org-testcontainers-jdbc-containerdatabasedriver-cla
//                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
//                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
//                    "spring.datasource.password=" + postgreSQLContainer.getPassword()
//            ).applyTo(configurableApplicationContext.getEnvironment());
//        }
//    }
//}
