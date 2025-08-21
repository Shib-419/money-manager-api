package com.shiv.MoneyManager.service;

import com.shiv.MoneyManager.dataTransferObjects.ExpenseDTO;
import com.shiv.MoneyManager.entity.ProfileEntity;
import com.shiv.MoneyManager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private  final ExpenseService expenseService;

    @Value("${money.manager.frontend.url}")
    private String frontEndUrl;

    @Scheduled(cron = "0 0 22 * * *" ,zone = "IST")
    public void sendDailyIncomeExpenseRemainder(){
        log.info("Job started: sendDailyIncomeExpenseRemainder()");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for (ProfileEntity profile : profiles){
            String body = "Hii "+profile.getFullName()+",<br><br>"
                    +"This is a friendly remainder to add your income and expenses for today in Money Manager.<br><br>"
                    +"<a href="+frontEndUrl+" style='display:inline-block; padding:10px 20px;background-color:#4CAF50;color:#fff;text-decoration:none;border-radius:5px;font-weight:bold;'>Go to Money Manager</a>"
                    +"<br><br>Best Regards,<br>Money Manager Developer -> @Shiv";

            emailService.sendEmail(profile.getEmail(),"Daily Remainder: Add your incomes and expenses.",body);
        }
        log.info("Job Completed: sendDailyIncomeExpenseRemainder()");
    }

    /// =========================================================================================================================================================




    @Scheduled(cron = "0 0 23 * * *" ,zone = "IST")
    public void sendDailyExpenseSummary(){

        log.info("Job started: sendDailyExpenseSummary()");

        List<ProfileEntity> profiles = profileRepository.findAll();

        for (ProfileEntity profile : profiles){

            List<ExpenseDTO> todaysExpenses = expenseService.getExpensesForUserOnDate(profile.getId(), LocalDate.now(ZoneId.of("Asia/Kolkata")));
            if (!todaysExpenses.isEmpty()){
                StringBuilder table = new StringBuilder();
                table.append("<table style='border-collapse:collapse;width:100%;'>");
                table.append("<tr style='background-color:#f2f2f2;'><th style='border:1px solid #ddd;padding:8px;'>S.No</th><th style='border:1px solid #ddd;padding:8px;'>Name</th><th style='border : 1px solid #ddd;padding:8px;'>Amount</th><th style='border:1px solid #ddd;padding:8px;'>Category</th></tr>");
                int i = 1 ;

                for(ExpenseDTO expense : todaysExpenses){
                    table.append("<tr>");
                    table.append("<td style='border : 1px solid #ddd;padding:8px;'>").append(i++).append("</td>");
                    table.append("<td style='border : 1px solid #ddd;padding:8px;'>").append(expense.getName()).append("</td>");
                    table.append("<td style='border : 1px solid #ddd;padding:8px;'>").append(expense.getAmount()).append("</td>");
                    table.append("<td style='border : 1px solid #ddd;padding:8px;'>").append(expense.getCategoryId()!=null ? expense.getCategoryName() : "N/A").append("</td>");
                    table.append("</tr>");

                }
                table.append("</table>");
                String body = "Hii "+profile.getFullName()+",<br><br> Here is a summary of your expenses for today:<br><br>"+table+"<br><br>Best Regards,<br><br>Money Manager Developer -> @Shiv";
                emailService.sendEmail(profile.getEmail(),"Your Daily Expense Summary",body);


            }
        }
        log.info("Job Completed: sendDailyExpenseSummary()");
    }

}
