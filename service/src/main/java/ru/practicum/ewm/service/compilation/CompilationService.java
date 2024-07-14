package ru.practicum.ewm.service.compilation;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto create(NewCompilationDto newCompilationDto);

    void remove(long compId);

    CompilationDto update(UpdateCompilationRequest updateCompilationRequest, long compId);

    List<CompilationDto> getAll(Boolean pinned, Pageable pageable);

    CompilationDto getById(long compId);
}
