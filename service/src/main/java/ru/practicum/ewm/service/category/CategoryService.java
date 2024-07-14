package ru.practicum.ewm.service.category;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto create(NewCategoryDto newCategoryDto);

    CategoryDto update(long catId, NewCategoryDto newCategoryDto);

    void remove(long catId);

    List<CategoryDto> getAll(Pageable pageable);

    CategoryDto get(long catId);
}
