package com.parkit.parkingsystem.service;

import java.time.Duration;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.constants.Fare;

import com.parkit.parkingsystem.model.Ticket;

/**
 * The FareCalculatorService class is used to calculate the price of the ticket
 * according to the time spent in the car park and the type of vehicle.
 * 
 * @author Dave Discamps.
 */
public class FareCalculatorService {
    private static final Logger logger = LogManager.getLogger("FareCalculatorService");

    /**
     * 
     * @param ticket the information relative to the parked vehicle.
     */

    public void calculateFare(Ticket ticket) {
	logger.info(ticket.getInTime());
	logger.info(ticket.getOutTime());
	if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
	    throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
	}

	Date inHour = ticket.getInTime();
	Date outHour = ticket.getOutTime();

	Duration duration = Duration.between(inHour.toInstant(), outHour.toInstant());

	double reductionUserRecurring = 1;
	if (ticket.isUserRecurring()) {
	    reductionUserRecurring = 0.95;
	}

	switch (ticket.getParkingSpot().getParkingType()) {
	case CAR:

	    if (duration.getSeconds() <= 1800) {
		ticket.setPrice(0);
	    } else {
		ticket.setPrice(Fare.roundedFare(
			reductionUserRecurring * (duration.getSeconds() / 3600.0) * Fare.CAR_RATE_PER_HOUR));
	    }
	    break;
	case BIKE:

	    if (duration.getSeconds() <= 1800) {
		ticket.setPrice(0);
	    } else {
		ticket.setPrice(Fare.roundedFare(
			reductionUserRecurring * (duration.getSeconds() / 3600.0) * Fare.BIKE_RATE_PER_HOUR));
	    }

	    break;
	default:
	    throw new IllegalArgumentException("Unkown Parking Type");
	}

    }
}