package ru.practicum.ewm.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.category.dto.CategoryDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryPublicController {
    private final CategoryService service;

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public List<CategoryDto> getCategories(@PositiveOrZero @RequestParam(required = false, defaultValue = "0") int from,
                                           @Positive @RequestParam(required = false, defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        return service.getAll(pageable);
    }

    @GetMapping("/{catId}")
    @ResponseStatus(code = HttpStatus.OK)
    public CategoryDto getCategory(@PathVariable long catId) {
        return service.get(catId);
    }

}
