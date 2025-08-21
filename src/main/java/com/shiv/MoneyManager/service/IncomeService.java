package com.shiv.MoneyManager.service;

import com.shiv.MoneyManager.dataTransferObjects.ExpenseDTO;
import com.shiv.MoneyManager.dataTransferObjects.IncomeDTO;
import com.shiv.MoneyManager.entity.CategoryEntity;
import com.shiv.MoneyManager.entity.ExpenseEntity;
import com.shiv.MoneyManager.entity.IncomeEntity;
import com.shiv.MoneyManager.entity.ProfileEntity;
import com.shiv.MoneyManager.repository.CategoryRepository;
import com.shiv.MoneyManager.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final ProfileService profileService;


    /// -----------------------------------------------------------------------------------------------------------------------------------------------------------------


    // Adding a new expense to the database
    public IncomeDTO addIncome(IncomeDTO incomeDTO) {

        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(incomeDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found."));
        IncomeEntity newIncome = toEntity(incomeDTO, profile, category);
        newIncome = incomeRepository.save(newIncome);

        return toDTO(newIncome);
    }

    /// -----------------------------------------------------------------------------------------------------------------------------------------------------------------

    // Retrieve all the incomes for the current month/based on the start date and end date
    public List<IncomeDTO> getCurrentMonthIncomesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<IncomeEntity> list = incomeRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return list.stream().map(this::toDTO).toList();

    }

    /// -----------------------------------------------------------------------------------------------------------------------------------------------------------------

    //Delete expense by id for current user
    public void deleteIncome(Long incomeId){
        ProfileEntity profile = profileService.getCurrentProfile();
        IncomeEntity entity = incomeRepository.findById(incomeId)
                .orElseThrow(()-> new RuntimeException("Income Not found"));

        if (!entity.getProfile().getId().equals(profile.getId())){
            throw new RuntimeException("Unauthorized to delete this income");
        }
        incomeRepository.delete(entity);
    }


    /// -----------------------------------------------------------------------------------------------------------------------------------------------------------------

    // Get latest 5 incomes of teh current user
    public List<IncomeDTO> getLatest5IncomesOfCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> list = incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return list.stream().map(this::toDTO).toList();
    }

    /// -----------------------------------------------------------------------------------------------------------------------------------------------------------------


    // Total income for the current user
    public BigDecimal getTotalIncomeForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total = incomeRepository.findTotalIncomeByProfileId(profile.getId());
        return total != null ? total : BigDecimal.ZERO;
    }


    /// -----------------------------------------------------------------------------------------------------------------------------------------------------------------

    //filter Incomes
    public List<IncomeDTO> filterIncomes(LocalDate startDate , LocalDate endDate , String keyword , Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> list = incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate, endDate, keyword, sort);

        return list.stream().map(this::toDTO).toList();

    }

    /// -----------------------------------------------------------------------------------------------------------------------------------------------------------------



    // Helper methods
    private IncomeEntity toEntity(IncomeDTO incomeDTO, ProfileEntity profile, CategoryEntity category) {
        return IncomeEntity.builder()
                .name(incomeDTO.getName())
                .icon(incomeDTO.getIcon())
                .amount(incomeDTO.getAmount())
                .date(incomeDTO.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private IncomeDTO toDTO(IncomeEntity entity) {
        return IncomeDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : "N/A")
                .amount(entity.getAmount())
                .date(entity.getDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
