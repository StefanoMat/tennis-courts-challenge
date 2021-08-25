package com.tenniscourts.schedules;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("select s from Schedule s left join Reservation r on (r.schedule.id = s.id) where s.tennisCourt.id = ?1 and r.id IS NULL order by s.startDateTime")
    List<Schedule> findByTennisCourt_IdAndWithoutReservationOrderByStartDateTime(Long id);
}