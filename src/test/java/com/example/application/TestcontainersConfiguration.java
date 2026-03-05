package com.example.application;

import com.example.application.data.SampleBookRepository;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	private SampleBookRepository sampleBookRepository;

	@Bean
	public DatabaseInitializationSettings databaseInitializationSettings() {
		return new DatabaseInitializationSettings();

	};

	@Bean
	@ServiceConnection
	PostgreSQLContainer<?> postgresContainer() {
		return new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
				.withReuse(true); // this makes DB container reused between tests
		// Now need to add "testcontainers.reuse.enable=true" to .testcontainers.properties in Home.
	}

	@Bean
	public DataSourceScriptDatabaseInitializer dataSourceInitializer(DataSource dataSource, DatabaseInitializationSettings settings, SampleBookRepository repository) {


		DataSourceScriptDatabaseInitializer initializer = new DataSourceScriptDatabaseInitializer(dataSource, settings);
		if (repository.count() == 0L) {
			initializer.initializeDatabase();
		}
		return initializer;
	}

}