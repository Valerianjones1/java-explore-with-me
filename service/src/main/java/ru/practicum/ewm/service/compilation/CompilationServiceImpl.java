package ru.practicum.ewm.service.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.service.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.service.event.Event;
import ru.practicum.ewm.service.event.EventRepository;
import ru.practicum.ewm.service.exception.NotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        Set<Event> events = Collections.emptySet();
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events = newCompilationDto.getEvents()
                    .stream()
                    .map(eventId -> eventRepository.findById(eventId).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }


        Compilation compilation = CompilationMapper.mapToCompilation(newCompilationDto, events);


        return CompilationMapper.mapToCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    public void remove(long compId) {
        compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Подборка с идентификатором %s не найдена", compId)));

        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDto update(UpdateCompilationRequest updateCompilationRequest, long compId) {
        Compilation foundCompilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Подборка с идентификатором %s не найдена", compId)));

        Set<Event> events = foundCompilation.getEvents();
        if (updateCompilationRequest.getEvents() != null && !updateCompilationRequest.getEvents().isEmpty()) {
            events = updateCompilationRequest.getEvents()
                    .stream()
                    .map(eventId -> eventRepository.findById(eventId).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }

        Compilation newCompilation = CompilationMapper.mapToCompilation(updateCompilationRequest, events);

        return CompilationMapper.mapToCompilationDto(compilationRepository.save(fillCompilation(newCompilation, foundCompilation)));
    }

    @Override
    public List<CompilationDto> getAll(Boolean pinned, Pageable pageable) {
        if (pinned == null) {
            return compilationRepository.findAll(pageable)
                    .stream()
                    .map(CompilationMapper::mapToCompilationDto)
                    .collect(Collectors.toList());
        }
        return compilationRepository.findAllByPinned(pinned, pageable)
                .stream()
                .map(CompilationMapper::mapToCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getById(long compId) {
        Compilation foundCompilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Подборка с идентификатором %s не найдена", compId)));

        return CompilationMapper.mapToCompilationDto(foundCompilation);
    }

    private Compilation fillCompilation(Compilation newCompilation, Compilation oldCompilation) {
        if (newCompilation.getEvents() != null) {
            oldCompilation.setEvents(newCompilation.getEvents());
        }
        if (newCompilation.getPinned() != null) {
            oldCompilation.setPinned(newCompilation.getPinned());
        }
        if (newCompilation.getTitle() != null) {
            oldCompilation.setTitle(newCompilation.getTitle());
        }

        return oldCompilation;
    }
}
