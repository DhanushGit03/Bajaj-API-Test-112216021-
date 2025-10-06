package com.dhanush.bajajapitest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "api.base-url=https://bfhldevapigw.healthrx.co.in",
    "user.name=Gosala Dhanush",
    "user.regNo=112216021",
    "user.email=112216021@ece.iiitp.ac.in"
})
class BajajApiTestApplicationTests {

	@Test
	void contextLoads() {
	}

}
