package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * 
 * @author Dave Discamps this class contains ParkingDataBase integration tests
 *
 */

@ExtendWith(MockitoExtension.class)
class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception {
	parkingSpotDAO = new ParkingSpotDAO();
	parkingSpotDAO.setDataBaseConfig(dataBaseTestConfig);
	ticketDAO = new TicketDAO();
	ticketDAO.setDataBaseConfig(dataBaseTestConfig);
	dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
	when(inputReaderUtil.readSelection()).thenReturn(1);
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

	dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown() {

    }

    @Test
    @DisplayName("Parking system save ticket to DB and Update parkingSpot with availability")
    void testParkingACar() {
	// GIVEN:
	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
	int next = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
	// WHEN
	parkingService.processIncomingVehicle();
	// THEN:
	Ticket ticket = ticketDAO.getTicket("ABCDEF");
	ParkingSpot parkingSpot = ticket.getParkingSpot();
	assertNotNull(parkingSpot);
	assertNotNull(ticket);
	assertFalse(parkingSpot.isAvailable());

	assertEquals(next, parkingSpot.getId());

    }

    @Test
    @DisplayName("Parking system generated fare and out tim saving to DB")
    void testParkingLotExitACar() throws InterruptedException {
	// GIVEN:
	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
	int numberOfNextAvailableSlot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
	parkingService.processIncomingVehicle();
	Thread.sleep(500);
	// WHEN:
	parkingService.processExitingVehicle();
	// THEN:
	Ticket ticket = ticketDAO.getTicket("ABCDEF");

	assertEquals(1, numberOfNextAvailableSlot);
	assertNotNull(ticket);
	assertNotNull(ticket.getOutTime());
	assertEquals(0, ticket.getPrice());

    }

}