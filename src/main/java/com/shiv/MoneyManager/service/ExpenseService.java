package com.shiv.MoneyManager.service;

import com.shiv.MoneyManager.dataTransferObjects.ExpenseDTO;
import com.shiv.MoneyManager.entity.CategoryEntity;
import com.shiv.MoneyManager.entity.ExpenseEntity;
import com.shiv.MoneyManager.entity.ProfileEntity;
import com.shiv.MoneyManager.repository.CategoryRepository;
import com.shiv.MoneyManager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final ProfileService profileService;

    /// -----------------------------------------------------------------------------------------------------------------------------------------------------------------


    // Adding a new expense to the database
    public ExpenseDTO addExpense(ExpenseDTO expenseDTO) {

        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(expenseDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found."));
        ExpenseEntity newExpense = toEntity(expenseDTO, profile, category);
        newExpense = expenseRepository.save(newExpense);

        return toDTO(newExpense);
    }


    /// -----------------------------------------------------------------------------------------------------------------------------------------------------------------

    // Retrieve all the expenses for the current month/based on the start date and end date
    public List<ExpenseDTO> getCurrentMonthExpensesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return list.stream().map(this::toDTO).toList();

    }

    /// -----------------------------------------------------------------------------------------------------------------------------------------------------------------

    //Delete expense by id for current user
    public void deleteExpense(Long expenseId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        ExpenseEntity entity = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense Not found"));

        if (!entity.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorized to delete this expense");
        }
        expenseRepository.delete(entity);
    }

    /// -----------------------------------------------------------------------------------------------------------------------------------------------------------------

    // Get latest 5 expenses of teh current user
    public List<ExpenseDTO> getLatest5ExpensesOfCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> list = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return list.stream().map(this::toDTO).toList();
    }

    /// -----------------------------------------------------------------------------------------------------------------------------------------------------------------


    // Total Expense for the current user
    public BigDecimal getTotalExpenseForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total = expenseRepository.findTotalExpenseByProfileId(profile.getId());
        return total != null ? total : BigDecimal.ZERO;
    }


    /// -----------------------------------------------------------------------------------------------------------------------------------------------------------------

    //filter Expenses
    public List<ExpenseDTO> filterExpenses(LocalDate startDate , LocalDate endDate , String keyword , Sort sort){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate, endDate, keyword, sort);

        return list.stream().map(this::toDTO).toList();
    }

    /// -----------------------------------------------------------------------------------------------------------------------------------------------------------------

    //Notifications
    public List<ExpenseDTO> getExpensesForUserOnDate(Long profileId , LocalDate date){
        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDate(profileId, date);

        return list.stream().map(this::toDTO).toList();
    }

    /// -----------------------------------------------------------------------------------------------------------------------------------------------------------------



    // Helper methods
    private ExpenseEntity toEntity(ExpenseDTO expenseDTO, ProfileEntity profile, CategoryEntity category) {
        return ExpenseEntity.builder()
                .name(expenseDTO.getName())
                .icon(expenseDTO.getIcon())
                .amount(expenseDTO.getAmount())
                .date(expenseDTO.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private ExpenseDTO toDTO(ExpenseEntity entity) {
        return ExpenseDTO.builder()
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
