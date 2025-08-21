package com.shiv.MoneyManager.service;

import com.shiv.MoneyManager.dataTransferObjects.CategoryDTO;
import com.shiv.MoneyManager.entity.CategoryEntity;
import com.shiv.MoneyManager.entity.ProfileEntity;
import com.shiv.MoneyManager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;


    // Save Category
    public CategoryDTO saveCategory(CategoryDTO categoryDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();
        if (categoryRepository.existsByNameAndProfileId(categoryDTO.getName(), profile.getId())) {
            throw new RuntimeException("Category with this name already exists");
        }
        CategoryEntity newCategory = toEntity(categoryDTO, profile);
        newCategory = categoryRepository.save(newCategory);
        return toDTO(newCategory);
    }

    /// --------------------------------------------------------------------------------------------------------------------------------------------------------------------



    //Get categories for current User
    public List<CategoryDTO> getCategoriesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> categories = categoryRepository.findByProfileId(profile.getId());
        return categories.stream().map(this::toDTO).toList();
    }


    /// --------------------------------------------------------------------------------------------------------------------------------------------------------------------


    // Get Categories by type for current User..
    public List<CategoryDTO> getCategoriesByTypeForCurrentUSer(String type){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> entities = categoryRepository.findByTypeAndProfileId(type, profile.getId());

        return entities.stream().map(this::toDTO).toList();
    }



    /// --------------------------------------------------------------------------------------------------------------------------------------------------------------------


    //
    public CategoryDTO updateCategory(Long categoryId , CategoryDTO categoryDTO){
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity existingCategory = categoryRepository.findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(() -> new RuntimeException("Category Not Found Or Not Accessible."));

        existingCategory.setName(categoryDTO.getName());
        existingCategory.setIcon(categoryDTO.getIcon());
        existingCategory.setType(categoryDTO.getType());
        existingCategory = categoryRepository.save(existingCategory);

        return toDTO(existingCategory);
    }


    /// --------------------------------------------------------------------------------------------------------------------------------------------------------------------


    // Helper Methods
    private CategoryEntity toEntity(CategoryDTO categoryDTO, ProfileEntity profile) {
        return CategoryEntity.builder()
                .name(categoryDTO.getName())
                .icon(categoryDTO.getIcon())
                .profile(profile)
                .type(categoryDTO.getType())
                .build();
    }


    /// --------------------------------------------------------------------------------------------------------------------------------------------------------------------


    private CategoryDTO toDTO(CategoryEntity entity) {
        return CategoryDTO.builder()
                .id(entity.getId())
                .profileID(entity.getProfile() != null ? entity.getProfile().getId() : null)
                .name(entity.getName())
                .icon(entity.getIcon())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .type(entity.getType())
                .build();
    }
}
