package ru.practicum.ewm.service.category.mapper;

import ru.practicum.ewm.service.category.Category;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;

public class CategoryMapper {
    public static Category mapToCategory(NewCategoryDto newCategoryDto) {
        Category category = new Category();
        category.setName(newCategoryDto.getName());
        return category;
    }

    public static CategoryDto mapToCategoryDto(Category category) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId().intValue());
        categoryDto.setName(category.getName());

        return categoryDto;
    }
}
