package com.example.reactivetransactions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

@EnableTransactionManagement
@SpringBootApplication
public class ReactiveTransactionsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveTransactionsApplication.class, args);
	}

/*	@Bean
	ConnectionFactory connectionFactory(@Value("${spring.r2dbc.url}") String url) {
		return ConnectionFactories.get(url);
	}*/

	@Bean
	TransactionalOperator transactionalOperator(ReactiveTransactionManager transactionManager) {
		return TransactionalOperator.create(transactionManager);
	}

	@Bean
	ReactiveTransactionManager transactionManager(ReactiveMongoDatabaseFactory cf) {
		return new ReactiveMongoTransactionManager(cf);
//		return new R2dbcTransactionManager(connectionFactory);
	}
}

@Service
@RequiredArgsConstructor
class CustomerService {

	private final TransactionalOperator transactionalOperator;
	private final CustomerRepository customerRepository;

	@Transactional
	public Flux<Customer> saveAll(String... names) {

		var records = Flux.just(names)
			.map(n -> new Customer(null, n))
			.flatMap(this.customerRepository::save)
			.doOnNext(customer -> Assert.isTrue(customer.getEmail().contains("@"), "the email must contain a '@' in it!"));

		return records;
//		return this.transactionalOperator.execute(status -> records);
//		return records.as(this.transactionalOperator::transactional);
	}
}


interface CustomerRepository extends ReactiveCrudRepository<Customer, String> {
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
class Customer {

	@Id
	private String id;
	private String email;
}