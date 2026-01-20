package in.dipak.money_manager.service;

import in.dipak.money_manager.dto.ExpenseDTO;
import in.dipak.money_manager.entity.ProfileEntity;
import in.dipak.money_manager.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final ProfileRepository profileRepository;
    private final ExpenseService expenseService;
    private final EmailService emailService;

    @Value("${money.manager.frontend.url}")
    private String frontendUrl;

//    @Scheduled(cron = "0 * * * * *", zone = "Asia/Kolkata")  // every minutes.
    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Kolkata")  // at 10 pm
    public void sendDailyIncomeExpenseReminder() {
        log.info("Job started: sendDailyIncomeExpenseReminder()");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for (ProfileEntity profile: profiles) {
            String body = "Hi " + profile.getFullname() + ",<br><br>"
                    + "This is a friendly reminder to add your income and expenses for today in Money Manager.<br><br>"
                    + "<a href=" + frontendUrl + " style='display:inline-block;padding:10px 20px;background-color:#4CAF50; color:#fff;text-decoration:none;border-radius:5px;font-weight:bold;'> Go to Money Manager</a>"
                    + "<br><br>Best regards,<br>Money Manager Team";
            emailService.sendEmail(profile.getEmail(),"Daily reminder: Add your income and expenses",body);
        }
        log.info("Job completed: sendDailyIncomeExpenseReminder()");
    }

//    @Scheduled(cron = "0 * * * * *", zone = "IST")
    @Scheduled(cron = "0 0 23 * * *", zone = "Asia/Kolkata")
    public void sendDailyExpenseSummary() {
        log.info("\nJob started: sendDailyExpenseSummary()");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for (ProfileEntity profile: profiles) {
            List<ExpenseDTO> today_sExpenses = expenseService.getExpensesForUserOnDate(profile.getId(), LocalDate.now());
            if (!today_sExpenses.isEmpty()) {
                StringBuilder table = new StringBuilder();
                table.append("<table style='border-collapse:collapse;width:100%;'>");
                table.append("<tr style='background-color:#f2f2f2;'><th style='border:1px solid #ddd;padding:8px;'>S.No</th><th style='border: 1px solid #ddd; padding: 8px;'>Name</th> <th style='border: 1px solid #ddd; padding: 8px;'>Amount</th> <th style='border: 1px solid #ddd; padding: 8px;'>Category</th> <th style='border: 1px solid #ddd; padding: 8px;'>Date</th></tr>");
                int i = 1;

                for (ExpenseDTO expense : today_sExpenses) {
                    table.append("<tr>");
                    table.append("<td style='border:1px solid #ddd;padding:8px;'>").append(i++).append("</td>");
                    table.append("<td style='border:1px solid #ddd;padding:8px;'>").append(expense.getName()).append("</td>");
                    table.append("<td style='border:1px solid #ddd;padding:8px;'>").append(expense.getAmount()).append("</td>");
                    table.append("<td style='border:1px solid #ddd;padding:8px;'>").append(expense.getCategoryId() != null ? expense.getCategoryName() : "N/A").append("</td>");
                    table.append("<td style='border:1px solid #ddd;padding:8px;'>").append(expense.getCreatedAt().getHour()).append(":").append(expense.getCreatedAt().getMinute()).append("</td>");
                    table.append("</tr>");
                }

                table.append("</table>");
                String body = "Hi " + profile.getFullname() + ",<br/><br/> Here is a summary of your expenses for today:<br/><br/>"+ table + "<br/><br/>Best regards,<br/>Money Manager Team";
                emailService.sendEmail(profile.getEmail(),"Your Daily Expense Summary",body);
            }
        }
        log.info("\nJob completed: sendDailyExpenseSummary()");

    }
}

