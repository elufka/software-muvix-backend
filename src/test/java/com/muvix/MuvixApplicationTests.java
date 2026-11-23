package com.muvix;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionTemplate;
import com.muvix.domain.model.event.OrderConfirmedEvent;
import com.muvix.infrastructure.adapters.out.persistence.mysql.repository.OrderConfirmedEventJpaRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class MuvixApplicationTests {

	@Autowired private ApplicationEventPublisher eventPublisher;
	@Autowired private TransactionTemplate transactionTemplate;
	@Autowired private OrderConfirmedEventJpaRepository orderConfirmedEvents;

	@Test
	void contextLoads() {
	}

	@Test
	void persistsOrderHistoryNamesBeforeTransactionCommit() {
		long orderId = System.nanoTime();
		transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent(
				new OrderConfirmedEvent(orderId, "Historical buyer", "Historical seller", null,
						new java.math.BigDecimal("42.00"))));

		var saved = orderConfirmedEvents.findById(orderId);
		assertTrue(saved.isPresent());
		assertEquals("Historical buyer", saved.orElseThrow().getBuyerName());
		assertEquals("Historical seller", saved.orElseThrow().getSellerName());
	}

}
