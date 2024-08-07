package ru.practicum.ewm.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.service.category.dto.CategoryDto;
import ru.practicum.ewm.service.category.dto.NewCategoryDto;
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
    @Transactional
    public CategoryDto create(NewCategoryDto newCategoryDto) {
        Category category = CategoryMapper.mapToCategory(newCategoryDto);

        Category savedCategory = repository.save(category);

        log.info("Категория была успешна сохранена {}", category);
        return CategoryMapper.mapToCategoryDto(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDto update(long catId, NewCategoryDto newCategoryDto) {
        Category oldCategory = repository.findById(catId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Категория с идентификатором %s не найден", catId)));


        Category newCategory = CategoryMapper.mapToCategory(newCategoryDto);

        Category updatedCategory = repository.save(fillCategory(newCategory, oldCategory));

        log.info("Категория была успешна обновлена {}", updatedCategory);
        return CategoryMapper.mapToCategoryDto(updatedCategory);
    }

    @Override
    @Transactional
    public void remove(long catId) {
        repository.findById(catId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Категория с идентификатором %s не найден", catId)));
        repository.deleteById(catId);
        log.info("Категория с идентификатором {} была успешна удалена", catId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAll(Pageable pageable) {
        log.info("Успешно получили категории");
        return repository.findAll(pageable)
                .stream()
                .map(CategoryMapper::mapToCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto get(long catId) {
        Category category = repository.findById(catId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Категория с идентификатором %s не найден", catId)));
        log.info("Получили событие {}", category);
        return CategoryMapper.mapToCategoryDto(category);
    }

    private Category fillCategory(Category newCategory, Category oldCategory) {
        oldCategory.setName(newCategory.getName());
        return oldCategory;
    }
}
