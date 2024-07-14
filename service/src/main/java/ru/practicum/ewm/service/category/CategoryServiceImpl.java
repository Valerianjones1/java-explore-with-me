package ru.practicum.ewm.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;
import ru.practicum.ewm.service.category.mapper.CategoryMapper;
import ru.practicum.ewm.service.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repository;

    @Override
    public CategoryDto create(NewCategoryDto newCategoryDto) {
        Category category = CategoryMapper.mapToCategory(newCategoryDto);

        Category savedCategory = repository.save(category);

        return CategoryMapper.mapToCategoryDto(savedCategory);
    }

    @Override
    public CategoryDto update(long catId, NewCategoryDto newCategoryDto) {
        Category oldCategory = repository.findById(catId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Категория с идентификатором %s не найден", catId)));


        Category newCategory = CategoryMapper.mapToCategory(newCategoryDto);

        Category updatedCategory = fillCategory(newCategory, oldCategory);

        return CategoryMapper.mapToCategoryDto(repository.save(updatedCategory));
    }

    @Override
    public void remove(long catId) {
        repository.findById(catId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Категория с идентификатором %s не найден", catId)));

        repository.deleteById(catId);
    }

    @Override
    public List<CategoryDto> getAll(Pageable pageable) {
        return repository.findAll(pageable)
                .stream()
                .map(CategoryMapper::mapToCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto get(long catId) {
        Category category = repository.findById(catId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Категория с идентификатором %s не найден", catId)));
        return CategoryMapper.mapToCategoryDto(category);
    }

    private Category fillCategory(Category newCategory, Category oldCategory) {
        oldCategory.setName(newCategory.getName());
        return oldCategory;
    }
}
