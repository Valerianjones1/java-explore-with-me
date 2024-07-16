package ru.practicum.ewm.service.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.compilation.dto.CompilationDto;
import ru.practicum.ewm.service.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.service.compilation.dto.UpdateCompilationRequest;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/admin/compilations")
@RequiredArgsConstructor
@Slf4j
public class CompilationAdminController {
    private final CompilationService service;

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public CompilationDto createCompilation(@Valid @RequestBody NewCompilationDto compilationDto) {
        return service.create(compilationDto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeCompilation(@PathVariable long compId) {
        service.remove(compId);
    }

    @PatchMapping("/{compId}")
    @ResponseStatus(code = HttpStatus.OK)
    public CompilationDto updateCompilation(@Valid @RequestBody UpdateCompilationRequest updateCompilationRequest,
                                            @PathVariable long compId) {
        updateCompilationRequest.setCompId(compId);
        return service.update(updateCompilationRequest);
    }
}
