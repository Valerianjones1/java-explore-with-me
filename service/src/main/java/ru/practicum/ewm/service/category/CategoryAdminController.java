package ru.practicum.ewm.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.category.dto.CategoryDto;
import ru.practicum.ewm.service.category.dto.NewCategoryDto;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryAdminController {
    private final CategoryService service;

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("Создаем категорию {}", newCategoryDto);
        return service.create(newCategoryDto);
    }

    @PatchMapping("/{catId}")
    @ResponseStatus(code = HttpStatus.OK)
    public CategoryDto updateCategory(@PathVariable long catId,
                                      @Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("Обновляем категорию {}", newCategoryDto);
        return service.update(catId, newCategoryDto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeCategory(@PathVariable long catId) {
        log.info("Удаляем категорию с идентификатором{}", catId);
        service.remove(catId);
    }
}
