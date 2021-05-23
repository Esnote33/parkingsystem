package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import static org.mockito.Mockito.*;

/**
 * 
 * @author Dave Discamps this class contains unit test of ParkingSerice class
 *
 */

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {

	parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

    }

    @Test
    void processIncomingVehicleCarTest() throws Exception {
	// GIVEN
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
	when(inputReaderUtil.readSelection()).thenReturn(1);
	when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(8);
	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenThrow(IllegalArgumentException.class);
	// WHEN
	parkingService.processIncomingVehicle();
	// THEN
	verify(ticketDAO, Mockito.times(0)).saveTicket(any(Ticket.class));
	verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    void processIncomingVehicleBikeTest() throws Exception {
	// GIVEN
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
	when(inputReaderUtil.readSelection()).thenReturn(2);
	when(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).thenReturn(9);
	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenThrow(IllegalArgumentException.class);
	// WHEN
	parkingService.processIncomingVehicle();
	// THEN
	verify(ticketDAO, Mockito.times(0)).saveTicket(any(Ticket.class));
	verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    void processExitingVehicleCarTest() throws Exception {
	// GIVEN
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
	Ticket ticket = new Ticket();
	ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("ABCDEF");
	when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
	// WHEN
	parkingService.processExitingVehicle();
	// THEN
	verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    void processExitingVehicleBikeTest() throws Exception {
	// GIVEN
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
	Ticket ticket = new Ticket();
	ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("ABCDEF");
	when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
	// WHEN
	parkingService.processExitingVehicle();
	// THEN
	verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    @DisplayName("Vehicle entry with space available")
    void getNextParkingNumberIfAvailableTest() {
	// GIVEN
	when(inputReaderUtil.readSelection()).thenReturn(1);
	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
	ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
	// WHEN

	// THEN
	assertNotNull(parkingSpot);

    }

    @Test
    @DisplayName("Error fetching next available parking slot")
    void getNextParkingNumberIfNotAvailableTest() {
	// GIVEN
	when(inputReaderUtil.readSelection()).thenReturn(1);
	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);
	// WHEN
	parkingService.getNextParkingNumberIfAvailable();
	// THEN
	verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));
    }

    @Test

    @DisplayName("Incorrect input provided for vehicle")
    void getVehicleTypeDefaultTest() {
	// GIVEN
	when(inputReaderUtil.readSelection()).thenReturn(3);
	ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
	// WHEN

	// THEN
	assertNull(parkingSpot);
    }

    @Test
    void processIncomingReccuringUserTest() throws Exception {
	// GIVEN
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
	when(inputReaderUtil.readSelection()).thenReturn(1);
	when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
	when(ticketDAO.recurringUsers(anyString())).thenReturn(true);
	// WHEN
	parkingService.processIncomingVehicle();
	// THEN
	verify(ticketDAO, Mockito.times(1)).recurringUsers(anyString());
	verify(parkingSpotDAO).updateParking(any(ParkingSpot.class));
    }

    @Test
    @DisplayName("Unable to update ticket information. Error occurred")
    void processExitingCarNoTicketTest() {
	// GIVEN
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
	Ticket ticket = new Ticket();
	ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("ABCDEF");
	when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
	// WHEN
	parkingService.processExitingVehicle();
	// THEN
	verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
    }

    @Test
    @DisplayName("Unable to update ticket information. Error occurred")
    void processExitingBikeNoTicketTest() {
	// GIVEN
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
	Ticket ticket = new Ticket();
	ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("ABCDEF");
	when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
	// WHEN
	parkingService.processExitingVehicle();
	// THEN
	verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
    }

    @Test
    @DisplayName("Unable to process exiting vehicle")
    void processExitingVehicleNoCarTest() throws Exception {
	// GIVEN
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
	Ticket ticket = new Ticket();
	ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("ABCDEF");
	when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenThrow(IllegalArgumentException.class);
	// WHEN
	parkingService.processExitingVehicle();
	// THEN
	verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    @DisplayName("Unable to process exiting vehicle")
    void processExitingVehicleNoBikeTest() throws Exception {
	// GIVEN
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
	Ticket ticket = new Ticket();
	ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("ABCDEF");
	when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenThrow(IllegalArgumentException.class);
	// WHEN
	parkingService.processExitingVehicle();
	// THEN
	verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }

}