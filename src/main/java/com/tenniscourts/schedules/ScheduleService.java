package com.tenniscourts.schedules;

import com.tenniscourts.tenniscourts.TennisCourtService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduleService {
    private ScheduleRepository scheduleRepository;
    @Autowired
    public void setScheduleRepository(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    private TennisCourtService tennisCourtService;
    @Autowired
    public void setTennisCourtService(TennisCourtService tennisCourtService) {
        this.tennisCourtService = tennisCourtService;
    }

    private ScheduleMapper scheduleMapper;
    @Autowired
    public void setScheduleMapper(ScheduleMapper scheduleMapper) {
        this.scheduleMapper = scheduleMapper;
    }

    public ScheduleDTO addSchedule(Long tennisCourtId, CreateScheduleRequestDTO createScheduleRequestDTO) {
        var tennisCourt = tennisCourtService.findTennisCourtById(tennisCourtId);
        var scheduleDTO = new ScheduleDTO();
        scheduleDTO.setTennisCourt(tennisCourt);
        scheduleDTO.setStartDateTime(createScheduleRequestDTO.getStartDateTime());
        scheduleDTO.setEndDateTime(createScheduleRequestDTO.getStartDateTime().plusHours(1));
        return scheduleMapper.map(scheduleRepository.saveAndFlush(scheduleMapper.map(scheduleDTO)));
    }

    public List<ScheduleDTO> findSchedulesByDates(LocalDateTime startDate, LocalDateTime endDate) {
        //TODO: implement
        return null;
    }

    public ScheduleDTO findSchedule(Long scheduleId) {
        //TODO: implement
        return null;
    }

    public List<ScheduleDTO> findFreeSchedulesByTennisCourtId(Long tennisCourtId) {
        return scheduleMapper.map(scheduleRepository.findByTennisCourt_IdAndWithoutReservationOrderByStartDateTime(tennisCourtId));
    }
}
