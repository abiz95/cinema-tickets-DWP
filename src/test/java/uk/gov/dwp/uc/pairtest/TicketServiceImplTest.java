package uk.gov.dwp.uc.pairtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import uk.gov.dwp.uc.pairtest.constant.CustomErrorMessages;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

class TicketServiceImplTest {

	TicketServiceImpl ticketService = new TicketServiceImpl();

	@Test
	void testPurchaseTickets_NullAsAccountNumber() {
		InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class,
				() -> ticketService.purchaseTickets(null, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 11)));
		assertEquals(CustomErrorMessages.nullAccountId, exception.getMessage());
	}

	@Test
	void testPurchaseTickets_ZeroAsAccountNumber() {
		InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class,
				() -> ticketService.purchaseTickets(0L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 11)));
		assertEquals(CustomErrorMessages.invalidAccountId, exception.getMessage());
	}

	@Test
	void testPurchaseTickets_NegativeAccountNumber() {

		InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class,
				() -> ticketService.purchaseTickets(-2L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 11)));
		assertEquals(CustomErrorMessages.invalidAccountId, exception.getMessage());
	}

	@Test
	void testPurchaseTickets_PurchaseTicketsMoreThanTwenty() {
		InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class,
				() -> ticketService.purchaseTickets(6L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 21)));
		assertEquals(CustomErrorMessages.limitExceeded, exception.getMessage());
	}

	@Test
	void testPurchaseTickets_PaymentForMultipleAdult() {
		TicketServiceImpl mockTicketService = mock(TicketServiceImpl.class);
		TicketTypeRequest ticketTypeRequest1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
		TicketTypeRequest ticketTypeRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
		mockTicketService.purchaseTickets(6L, ticketTypeRequest1, ticketTypeRequest2);
		verify(mockTicketService).purchaseTickets(6L, ticketTypeRequest1, ticketTypeRequest2);
	}

	@Test
	void testPurchaseTickets_PaymentForOneAdultOneChildOneInfant() {
		TicketServiceImpl mockTicketService = mock(TicketServiceImpl.class);

		TicketTypeRequest ticketTypeRequest1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
		TicketTypeRequest ticketTypeRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
		TicketTypeRequest ticketTypeRequest3 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

		mockTicketService.purchaseTickets(-1L, ticketTypeRequest1, ticketTypeRequest2, ticketTypeRequest3);
		verify(mockTicketService).purchaseTickets(-1L, ticketTypeRequest1, ticketTypeRequest2, ticketTypeRequest3);
	}

	@Test
	void testPurchaseTickets_PaymentForOnlyOneChildOneInfant() {
		InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class,
				() -> ticketService.purchaseTickets(6L, new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
						new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)));
		assertEquals(CustomErrorMessages.atleastOneAdult, exception.getMessage());
	}

	@Test
	void testPurchaseTickets_PaymentForOnlyOneChild() {
		InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class,
				() -> ticketService.purchaseTickets(6L, new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1)));
		assertEquals(CustomErrorMessages.atleastOneAdult, exception.getMessage());
	}

	@Test
	void testPurchaseTickets_PaymentForOnlyOneInfant() {
		InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class,
				() -> ticketService.purchaseTickets(6L, new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)));
		assertEquals(CustomErrorMessages.atleastOneAdult, exception.getMessage());
	}
	
	@Test
	void testPurchaseTickets_PaymentWithNullValues() {
		InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class,
				() -> ticketService.purchaseTickets(6L, new TicketTypeRequest(null, 1)));
		assertEquals(CustomErrorMessages.invalidTicketDetails, exception.getMessage());
	}
	
	@Test
	void testPurchaseTickets_PaymentWithZeroTicket() {
		InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class,
				() -> ticketService.purchaseTickets(6L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 0)));
		assertEquals(CustomErrorMessages.invalidTicketDetails, exception.getMessage());
	}

}
