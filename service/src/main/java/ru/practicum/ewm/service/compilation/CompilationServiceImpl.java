package ru.practicum.ewm.service.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.service.compilation.dto.CompilationDto;
import ru.practicum.ewm.service.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.service.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.service.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.service.event.Event;
import ru.practicum.ewm.service.event.EventRepository;
import ru.practicum.ewm.service.exception.NotFoundException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        Set<Event> compilationEvents = Collections.emptySet();

        List<Long> eventIds = newCompilationDto.getEvents();

        if (eventIds != null && !eventIds.isEmpty()) {
            Map<Long, Event> events = eventRepository.findAllByIdIn(eventIds)
                    .stream()
                    .collect(Collectors.toMap(Event::getId, event -> event));

            compilationEvents = eventIds
                    .stream()
                    .map(events::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (compilationEvents.size() != eventIds.size()) {
                throw new DataIntegrityViolationException("Ожидается, что полученные события должны совпадать");
            }
        }

        Compilation compilation = CompilationMapper.mapToCompilation(newCompilationDto, compilationEvents);

        return CompilationMapper.mapToCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public void remove(long compId) {
        compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Подборка с идентификатором %s не найдена", compId)));

        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto update(UpdateCompilationRequest updateCompilationRequest) {
        long compId = updateCompilationRequest.getCompId();
        Compilation foundCompilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Подборка с идентификатором %s не найдена", compId)));

        Set<Event> compilationEvents = foundCompilation.getEvents();

        List<Long> eventIds = updateCompilationRequest.getEvents();

        if (eventIds != null && !eventIds.isEmpty()) {
            Map<Long, Event> events = eventRepository.findAllByIdIn(eventIds)
                    .stream()
                    .collect(Collectors.toMap(Event::getId, event -> event));

            compilationEvents = eventIds
                    .stream()
                    .map(events::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (compilationEvents.size() != eventIds.size()) {
                throw new DataIntegrityViolationException("Ожидается, что полученные события должны совпадать");
            }
        }

        Compilation newCompilation = CompilationMapper.mapToCompilation(updateCompilationRequest, compilationEvents);

        return CompilationMapper.mapToCompilationDto(compilationRepository.save(fillCompilation(newCompilation, foundCompilation)));
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
