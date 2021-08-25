package com.tenniscourts.reservations;

import com.tenniscourts.schedules.*;
import com.tenniscourts.tenniscourts.TennisCourtDTO;
import com.tenniscourts.tenniscourts.TennisCourtService;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = ReservationService.class)
public class ScheduleServiceTest {

    @Mock
    ScheduleRepository scheduleRepository;

    @Mock
    TennisCourtService tennisCourtService;

    @Spy
    ScheduleMapper scheduleMapper = Mappers.getMapper(ScheduleMapper.class);


    @InjectMocks
    ScheduleService scheduleService;

    private Long validTennisCourtId = 1L;
    private Long validScheduleId = 3L;

    @Test
    public void findFreeSchedulesByTennisCourtIdShouldCallRepositoryWithCorrectParam() {
        when(scheduleRepository.findByTennisCourt_IdAndWithoutReservationOrderByStartDateTime(validTennisCourtId)).thenReturn(List.of(this.createValidSchedule().get()));
        scheduleService.findFreeSchedulesByTennisCourtId(validTennisCourtId);
        verify(scheduleRepository).findByTennisCourt_IdAndWithoutReservationOrderByStartDateTime(validTennisCourtId);
    }

    @Test
    public void addScheduleTennisCourtShouldReturnValidSchedule() {
        var createScheduleDto = new CreateScheduleRequestDTO();
        createScheduleDto.setTennisCourtId(validTennisCourtId);
        createScheduleDto.setStartDateTime(LocalDateTime.now());
        when(tennisCourtService.findTennisCourtById(validTennisCourtId)).thenReturn(this.createValidTennisCourt());
        when(scheduleRepository.saveAndFlush(any())).thenReturn(this.createValidSchedule().get());
        var response = scheduleService.addSchedule(validTennisCourtId, createScheduleDto);
        assertEquals(validScheduleId, response.getId());
    }

    private Optional<Schedule> createValidSchedule() {
        var schedule = new Schedule();
        schedule.setId(validScheduleId);
        return Optional.of(schedule);
    }

    private TennisCourtDTO createValidTennisCourt() {
        var tennisCourtDTO = new TennisCourtDTO();
        tennisCourtDTO.setId(validTennisCourtId);
        return tennisCourtDTO;
    }
}
