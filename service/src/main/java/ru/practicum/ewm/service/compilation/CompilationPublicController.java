package ru.practicum.ewm.service.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/compilations")
@RequiredArgsConstructor
@Slf4j
public class CompilationPublicController {
    private final CompilationService service;

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(required = false, defaultValue = "0") @PositiveOrZero int from,
                                                @RequestParam(required = false, defaultValue = "10") @Positive int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        return service.getAll(pinned, pageable);
    }

    @GetMapping("/{compId}")
    @ResponseStatus(code = HttpStatus.OK)
    public CompilationDto getCompilation(@PathVariable long compId) {
        return service.getById(compId);
    }
}
