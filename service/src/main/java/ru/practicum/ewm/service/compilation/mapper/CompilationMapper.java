package ru.practicum.ewm.service.compilation.mapper;

import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.service.compilation.Compilation;
import ru.practicum.ewm.service.event.Event;
import ru.practicum.ewm.service.event.mapper.EventMapper;

import java.util.Set;
import java.util.stream.Collectors;

public class CompilationMapper {
    public static Compilation mapToCompilation(NewCompilationDto newCompilationDto, Set<Event> events) {
        Compilation compilation = new Compilation();
        compilation.setEvents(events);
        compilation.setPinned(newCompilationDto.getPinned());
        compilation.setTitle(newCompilationDto.getTitle());
        return compilation;
    }

    public static Compilation mapToCompilation(UpdateCompilationRequest updateCompilationRequest, Set<Event> events) {
        Compilation compilation = new Compilation();
        compilation.setEvents(events);
        compilation.setPinned(updateCompilationRequest.getPinned());
        compilation.setTitle(updateCompilationRequest.getTitle());
        return compilation;
    }

    public static CompilationDto mapToCompilationDto(Compilation compilation) {
        CompilationDto compilationDto = new CompilationDto();
        compilationDto.setEvents(compilation.getEvents()
                .stream()
                .map(EventMapper::mapToEventShortDto)
                .collect(Collectors.toList()));
        compilationDto.setPinned(compilation.getPinned());
        compilationDto.setTitle(compilation.getTitle());
        compilationDto.setId(compilation.getId());
        return compilationDto;
    }
}
