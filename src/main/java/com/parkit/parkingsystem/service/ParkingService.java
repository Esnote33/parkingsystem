package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.Date;

/**
 * 
 * @author Dave Discamps this class processes both vehicle come in and come out
 *         to parking
 *
 */

public class ParkingService {
    /**
     * @see logger
     */

    private static final Logger logger = LogManager.getLogger("ParkingService");

    /**
     * @see FareCalculatorService
     */

    private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

    /**
     * @see InputReaderUtil
     */
    private InputReaderUtil inputReaderUtil;

    /**
     * @see ParkingSpotDAO
     */
    private ParkingSpotDAO parkingSpotDAO;

    /**
     * @see TicketDao
     */
    private TicketDAO ticketDAO;

    /**
     * Constructor with parameters.
     * 
     * @param inputReaderUtil
     * @param parkingSpotDAO
     * @param ticketDAO
     */
    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO) {
	this.inputReaderUtil = inputReaderUtil;
	this.parkingSpotDAO = parkingSpotDAO;
	this.ticketDAO = ticketDAO;
    }

    /**
     * Process when vehicle enter.
     */
    public void processIncomingVehicle() {
	try {
	    ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
	    if (parkingSpot != null && parkingSpot.getId() > 0) {
		String vehicleRegNumber = getVehicleRegNumber();

// ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)

		boolean isUserRecurring = ticketDAO.recurringUsers(vehicleRegNumber);
		if (isUserRecurring) {
		    logger.info("Welcome back!As a recurring user," + "of hour parking lot "
			    + "you'll benefit from a {} discount.", "5%");
		}
		parkingSpot.setAvailable(false);
		parkingSpotDAO.updateParking(parkingSpot);// allot this parking space and mark it's availability as
// false
		Date inTime = new Date();
		Ticket ticket = new Ticket();
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber(vehicleRegNumber);
		ticket.setPrice(0);
		ticket.setInTime(inTime);
		ticket.setOutTime(null);
		ticket.setUserRecurring(isUserRecurring);
		ticketDAO.saveTicket(ticket);

		logger.info("Generated Ticket and saved in DB");
		logger.info("Please park your vehicle in spot number:{}", parkingSpot.getId());
		logger.info("Recorded in-time for vehicle number:{} is:{} ", vehicleRegNumber, inTime);
	    }
	} catch (Exception e) {
	    logger.error("Unable to process incoming vehicle", e);
	}
    }

    /**
     * Get the vehicle registration number
     * 
     * @return vehicle registration number
     */
    private String getVehicleRegNumber() {
	logger.info("Please type the vehicle registration number and press enter key");
	return inputReaderUtil.readVehicleRegistrationNumber();
    }

    /**
     * Get the next parking number available
     * 
     * @return parking spot
     * @throws Exception if parking is full
     */
    public ParkingSpot getNextParkingNumberIfAvailable() {
	int parkingNumber = 0;
	ParkingSpot parkingSpot = null;
	try {
	    ParkingType parkingType = getVehicleType();
	    parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
	    if (parkingNumber > 0) {
		parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
	    } else {
		throw new SQLException("Error fetching parking number from DB. Parking slots might be full");
	    }
	} catch (IllegalArgumentException ie) {
	    logger.error("Error parsing user input for type of vehicle", ie);
	} catch (Exception e) {
	    logger.error("Error fetching next available parking slot", e);
	}
	return parkingSpot;
    }

    /**
     * Get the vehicle type
     * 
     * @return vehicle type
     */

    public ParkingType getVehicleType() {
	logger.info("Please select vehicle type from menu");
	logger.info("1 CAR");
	logger.info("2 BIKE");
	int input = inputReaderUtil.readSelection();
	switch (input) {
	case 1:
	    return ParkingType.CAR;

	case 2:
	    return ParkingType.BIKE;

	default:
	    logger.info("Incorrect input provided");
	    throw new IllegalArgumentException("Entered input is invalid");

	}
    }

    /**
     * Process when vehicle exit
     */
    public void processExitingVehicle() {
	try {
	    String vehicleRegNumber = getVehicleRegNumber();
	    Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
	    Date outTime = new Date();
	    ticket.setOutTime(outTime);
	    boolean isUserRecurring = ticketDAO.recurringUsers(vehicleRegNumber);
	    if (isUserRecurring) {
		ticket.setUserRecurring(isUserRecurring);
	    } else {
		fareCalculatorService.calculateFare(ticket);
	    }
	    if (ticketDAO.updateTicket(ticket)) {
		ParkingSpot parkingSpot = ticket.getParkingSpot();
		parkingSpot.setAvailable(true);
		parkingSpotDAO.updateParking(parkingSpot);
		logger.info("Please pay the parking fare:{}", ticket.getPrice());
		logger.info("Recorded out-time for vehicle number:{} is: {}", ticket.getVehicleRegNumber(), outTime);
	    } else {
		logger.error("Unable to update ticket information. Error occurred");
	    }
	} catch (Exception e) {
	    logger.error("Unable to process exiting vehicle", e);
	}
    }

}