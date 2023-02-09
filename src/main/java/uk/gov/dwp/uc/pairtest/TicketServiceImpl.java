package uk.gov.dwp.uc.pairtest;

import java.util.Arrays;
import java.util.Optional;
import java.util.Objects;

import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.constant.CustomErrorMessages;
import uk.gov.dwp.uc.pairtest.constant.TicketPrice;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
	/**
	 * Should only have private methods other than the one below.
	 */

	final static int maxTicketCount = 20;

	TicketPaymentServiceImpl TicketPaymentService = new TicketPaymentServiceImpl();
	SeatReservationServiceImpl SeatReservationService = new SeatReservationServiceImpl();

	@Override
	public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests)
			throws InvalidPurchaseException {
		final Optional<Long> accountIdValue = Optional.ofNullable(accountId);
		//Checking whether the account Id is null.
		if (accountIdValue.isEmpty()) {
			throw new InvalidPurchaseException(CustomErrorMessages.nullAccountId);
			//Checking whether the account Id is equal to or less that zero.
		} else if (accountIdValue.get() <= 0) {
			throw new InvalidPurchaseException(CustomErrorMessages.invalidAccountId);
		} else {
			//Validating the business rules.
			validatePurchase(accountIdValue.get(), ticketTypeRequests);
		}
	}

	private int getTicketPrice(TicketTypeRequest.Type TicketType) {

		switch (TicketType) {
		case ADULT:
			return TicketPrice.adultTicketPrice;
		case CHILD:
			return TicketPrice.childTicketPrice;
		case INFANT:
			return TicketPrice.infantTicketPrice;
		default:
			return 0;
		}

	}

	//Calculating the total amount of the tickets.
	private int calculateTotalTicketPrice(TicketTypeRequest... ticketTypeRequests) {
		int totalAmount = 0;
		for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
			totalAmount = totalAmount
					+ (getTicketPrice(ticketTypeRequest.getTicketType()) * ticketTypeRequest.getNoOfTickets());
		}
		return totalAmount;
	}

	private void validatePurchase(Long accountId, TicketTypeRequest... ticketTypeRequests) {
		// Checking if the TicketTypeRequest is null or number of tickets are less than 1.
		checkNullObjects(ticketTypeRequests);
		// Validating if the tickets are above 20.
		if (isTicketExceeded(ticketTypeRequests)) {
			//Checking if adults are present in the purchase.
			if (isAdultPresent(ticketTypeRequests)) {
				// Making the payment through the third party payment gateway.
				TicketPaymentService.makePayment(accountId, calculateTotalTicketPrice(ticketTypeRequests));
				// Reserving the seats and checking if the purchase has infant tickets.
				reserveCustomerSeats(accountId, ticketTypeRequests);
			} else {
				throw new InvalidPurchaseException(CustomErrorMessages.atleastOneAdult);
			}

		} else {
			throw new InvalidPurchaseException(CustomErrorMessages.limitExceeded);
		}
	}

	private void checkNullObjects(TicketTypeRequest... ticketTypeRequests) {
		if (!Arrays.stream(ticketTypeRequests)
				.allMatch(t -> t.getNoOfTickets() > 0 && Objects.nonNull(t.getTicketType()))) {
			throw new InvalidPurchaseException(CustomErrorMessages.invalidTicketDetails);
		}
	}

	private void reserveCustomerSeats(Long accountId, TicketTypeRequest... ticketTypeRequests) {
		for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
			if (!(ticketTypeRequest.getTicketType().INFANT.equals(TicketTypeRequest.Type.INFANT)) == false) {
				SeatReservationService.reserveSeat(accountId, getNumberOfTickets(ticketTypeRequests));
			}
		}
	}

	private boolean isAdultPresent(TicketTypeRequest... ticketTypeRequests) {
		int count = 0;
		for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
			if (ticketTypeRequest.getTicketType().equals(Type.ADULT)) {
				count++;
			}
		}
		return count > 0;
	}

	private boolean isTicketExceeded(TicketTypeRequest... ticketTypeRequests) {
		return getNumberOfTickets(ticketTypeRequests) <= 20;
	}

	private int getNumberOfTickets(TicketTypeRequest... ticketTypeRequests) {
		return Arrays.stream(ticketTypeRequests).mapToInt(TicketTypeRequest::getNoOfTickets).sum();
	}

}
