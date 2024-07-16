package ru.practicum.ewm.service.compilation;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.service.compilation.dto.CompilationDto;
import ru.practicum.ewm.service.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.service.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto create(NewCompilationDto newCompilationDto);

    void remove(long compId);

    CompilationDto update(UpdateCompilationRequest updateCompilationRequest);

    List<CompilationDto> getAll(Boolean pinned, Pageable pageable);

    CompilationDto getById(long compId);
}
