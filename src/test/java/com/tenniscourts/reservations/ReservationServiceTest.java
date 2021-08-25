package com.tenniscourts.reservations;

import com.tenniscourts.exceptions.EntityNotFoundException;
import com.tenniscourts.guests.Guest;
import com.tenniscourts.guests.GuestRepository;
import com.tenniscourts.schedules.Schedule;
import com.tenniscourts.schedules.ScheduleDTO;
import com.tenniscourts.schedules.ScheduleRepository;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = ReservationService.class)
public class ReservationServiceTest {

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    ScheduleRepository scheduleRepository;

    @Mock
    GuestRepository guestRepository;

    @InjectMocks
    ReservationService reservationService;

    @Spy
    ReservationMapper reservationMapper = Mappers.getMapper(ReservationMapper.class);

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private Long validGuestId = 1L;
    private Long validScheduleId = 3L;
    private Long secondValidScheduleId = 4L;
    private Long validReservationId = 10L;

    @Test
    public void getRefundValueFullRefund() {
        Schedule schedule = new Schedule();

        LocalDateTime startDateTime = LocalDateTime.now().plusDays(2);

        schedule.setStartDateTime(startDateTime);

        Assert.assertEquals(reservationService.getRefundValue(Reservation.builder().schedule(schedule).value(new BigDecimal(10L)).build()), new BigDecimal(10));
    }

    @Test
    public void bookReservationShouldRThrowErrorIfWrongScheduleId() {
        CreateReservationRequestDTO createReservationRequestDTO = CreateReservationRequestDTO.builder()
                .guestId(validGuestId)
                .scheduleId(12L)
                .build();
        exceptionRule.expect(EntityNotFoundException.class);
        exceptionRule.expectMessage("Schedule not found.");
        reservationService.bookReservation(createReservationRequestDTO);
    }

    @Test
    public void bookReservationShouldThrowErrorIfWrongGuestId() {
        CreateReservationRequestDTO createReservationRequestDTO = CreateReservationRequestDTO.builder()
                .guestId(12L)
                .scheduleId(validScheduleId)
                .build();
        when(scheduleRepository.findById(validScheduleId)).thenReturn(this.createValidSchedule());
        exceptionRule.expect(EntityNotFoundException.class);
        exceptionRule.expectMessage("Guest not found.");
        reservationService.bookReservation(createReservationRequestDTO);
    }

    @Test
    public void bookReservationShouldReturnReservationDto() {
        CreateReservationRequestDTO createReservationRequestDTO = CreateReservationRequestDTO.builder()
                .guestId(validGuestId)
                .scheduleId(validScheduleId)
                .build();
        when(scheduleRepository.findById(validScheduleId)).thenReturn(this.createValidSchedule());
        when(guestRepository.findById(validGuestId)).thenReturn(this.createValidGuest());
        Reservation reservationMock = this.createValidReservation().get();
        when(reservationRepository.save(any())).thenReturn(reservationMock);
        var reservationDTO = new ReservationDTO();
        reservationDTO.setSchedule(new ScheduleDTO());
        var response = reservationService.bookReservation(createReservationRequestDTO);

        assertTrue(response instanceof ReservationDTO);
        assertEquals(reservationMock.getId(), response.getId());
    }

    @Test
    public void rescheduleReservationShouldThrowErrorIfWrongScheduleId() {
        when(reservationRepository.findById(validReservationId)).thenReturn(this.createValidReservation());
        exceptionRule.expect(EntityNotFoundException.class);
        exceptionRule.expectMessage("Schedule not found.");
        reservationService.rescheduleReservation(validReservationId, 1L);
    }

    @Test
    public void rescheduleReservationShouldThrowErrorIfWrongReservationId() {
        exceptionRule.expect(EntityNotFoundException.class);
        exceptionRule.expectMessage("Reservation not found.");
        reservationService.rescheduleReservation(7L, validScheduleId);
    }

    @Test
    public void rescheduleReservationShouldThrowErrorIfSameScheduleId() {
        when(reservationRepository.findById(validReservationId)).thenReturn(this.createValidReservation());
        when(scheduleRepository.findById(validScheduleId)).thenReturn(this.createValidSchedule());
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Cannot reschedule to the same slot.");
        reservationService.rescheduleReservation(validReservationId, validScheduleId);
    }

    @Test
    public void rescheduleReservationShouldReturnRescheduleReservation() {
        when(reservationRepository.findById(validReservationId)).thenReturn(this.createValidReservation());
        when(scheduleRepository.findById(secondValidScheduleId)).thenReturn(this.createValidSchedule());
        when(guestRepository.findById(validGuestId)).thenReturn(this.createValidGuest());

        Reservation newReservation = this.createValidReservation().get();
        var reservationMock = newReservation;
        reservationMock.setValue(BigDecimal.ZERO);
        reservationMock.setRefundValue(ReservationConstants.DEFAULT_VALUE);
        var previousReservation = reservationMock;
        previousReservation.setReservationStatus(ReservationStatus.RESCHEDULED);
        newReservation.setId(validReservationId + 1);


        when(reservationRepository.save(any())).thenReturn(reservationMock).thenReturn(previousReservation).thenReturn(this.createValidReservation().get());
        var response = reservationService.rescheduleReservation(validReservationId, secondValidScheduleId);

        assertTrue(response instanceof ReservationDTO);
        assertEquals(validReservationId + 1, response.getPreviousReservation().getId());
    }


    private Optional<Schedule> createValidSchedule() {
        var schedule = new Schedule();
        schedule.setId(validScheduleId);
        schedule.setStartDateTime(LocalDateTime.of(2021, 9, 5, 10, 0, 0));
        return Optional.of(schedule);
    }

    private Optional<Guest> createValidGuest() {
        var guest = new Guest();
        guest.setId(validGuestId);
        return Optional.of(guest);
    }

    private Optional<Reservation> createValidReservation() {
        var reservation = new Reservation();
        reservation.setId(validReservationId);
        reservation.setValue(ReservationConstants.DEFAULT_VALUE);
        reservation.setSchedule(this.createValidSchedule().get());
        reservation.setGuest(this.createValidGuest().get());
        return Optional.of(reservation);
    }
}