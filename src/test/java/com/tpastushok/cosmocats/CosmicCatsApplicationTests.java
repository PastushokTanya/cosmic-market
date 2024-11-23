package com.tpastushok.cosmocats;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CosmicCatsApplicationTests {

	/**
	 * Test that ensures the Spring Boot application context loads
	 * and the application starts successfully by invoking the main method.
	 * This checks that the application initializes without exceptions
	 * and that all components, such as beans, are correctly set up.
	 */
	@Test
	void contextLoads() {
		CosmicCatsApplication.main(new String[] {});
	}

}
