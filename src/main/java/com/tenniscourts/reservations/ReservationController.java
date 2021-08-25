package com.tenniscourts.reservations;

import com.tenniscourts.config.BaseRestController;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/reservation")
public class ReservationController extends BaseRestController {

    private final ReservationService reservationService;

    @ApiOperation(value = "Book reservation")
    @PostMapping()
    public ResponseEntity<Void> bookReservation(@RequestBody CreateReservationRequestDTO createReservationRequestDTO) {
        return ResponseEntity.created(locationByEntity(reservationService.bookReservation(createReservationRequestDTO).getId())).build();
    }

    public ResponseEntity<ReservationDTO> findReservation(Long reservationId) {
        return ResponseEntity.ok(reservationService.findReservation(reservationId));
    }

    @ApiOperation(value = "Cancel a reservation")
    @PostMapping("{reservationId}/cancel")
    public ResponseEntity<ReservationDTO> cancelReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.cancelReservation(reservationId));
    }

    @ApiOperation(value = "Reschedule a reservation with different schedule")
    @PostMapping("{reservationId}/reschedule")
    public ResponseEntity<ReservationDTO> rescheduleReservation(@PathVariable Long reservationId, @RequestParam Long scheduleId) {
        return ResponseEntity.ok(reservationService.rescheduleReservation(reservationId, scheduleId));
    }
}
