package com.example.reactivetransactions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.test.StepVerifier;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CustomerServiceTest {

	@Autowired
	private CustomerService customerService;

	@Autowired
	private CustomerRepository customerRepository;

	@Test
	public void saveAll() throws Exception {

		StepVerifier.create(this.customerRepository.deleteAll()).verifyComplete();

		StepVerifier
			.create(this.customerRepository.findAll())
			.expectNextCount(0)
			.verifyComplete();

		StepVerifier
			.create(this.customerService.saveAll("Jane@jane.com", "Jorge@gmail.com", "Jeff@gmail.com", "Janet@gmail.com"))
			.expectNextCount(4)
			.verifyComplete();

		StepVerifier
			.create(this.customerRepository.findAll())
			.expectNextCount(4)
			.verifyComplete();

		StepVerifier
			.create(this.customerService.saveAll("foo", "bar"))
			.expectError()
			.verify();

		StepVerifier.create(this.customerRepository.findAll()).expectNextCount(4).verifyComplete();
	}

}