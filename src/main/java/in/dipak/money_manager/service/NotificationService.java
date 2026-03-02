package in.dipak.money_manager.service;

import in.dipak.money_manager.dto.ExpenseDTO;
import in.dipak.money_manager.entity.ProfileEntity;
import in.dipak.money_manager.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
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

//        @Scheduled(cron = "0 * * * * *", zone = "Asia/Kolkata")  // every minute
    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Kolkata")  // at 10 pm
    public void sendDailyIncomeExpenseReminder() {
        log.info("Job started: sendDailyIncomeExpenseReminder()");
        List<ProfileEntity> profiles = profileRepository.findAll();

        for (ProfileEntity profile : profiles) {
            try {
                String body = """
                    <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto;border:1px solid #e0e0e0;border-radius:12px;overflow:hidden;box-shadow:0 4px 12px rgba(0,0,0,0.1);">

                        <!-- Header -->
                        <div style="background:linear-gradient(135deg,#4CAF50,#2e7d32);padding:30px;text-align:center;">
                            <h1 style="color:#fff;margin:0;font-size:26px;letter-spacing:1px;">💰 Money Manager</h1>
                            <p style="color:#c8e6c9;margin:6px 0 0;">Your Personal Finance Companion</p>
                        </div>

                        <!-- Body -->
                        <div style="padding:35px 30px;background:#ffffff;">
                            <h2 style="color:#2e7d32;margin-top:0;">Hello, %s! 👋</h2>
                            <p style="color:#555;font-size:15px;line-height:1.6;">
                                Hope you're having a great day! This is your friendly reminder to log your
                                <strong>income and expenses</strong> for today.
                            </p>
                            <p style="color:#555;font-size:15px;line-height:1.6;">
                                Staying on top of your finances daily helps you reach your goals faster. 🎯
                            </p>

                            <!-- CTA Button -->
                            <div style="text-align:center;margin:30px 0;">
                                <a href="%s"
                                   style="display:inline-block;padding:14px 36px;
                                          background:linear-gradient(135deg,#4CAF50,#2e7d32);
                                          color:#ffffff;text-decoration:none;border-radius:8px;
                                          font-size:16px;font-weight:bold;letter-spacing:0.5px;
                                          box-shadow:0 4px 10px rgba(76,175,80,0.4);">
                                    📊 Add Today's Expenses
                                </a>
                            </div>

                            <!-- Tips Box -->
                            <div style="background:#f1f8e9;border-left:4px solid #4CAF50;padding:12px 16px;border-radius:4px;margin-top:10px;">
                                <p style="margin:0;color:#33691e;font-size:13px;">
                                    💡 <strong>Tip:</strong> Small daily entries make it easier to track your monthly spending patterns!
                                </p>
                            </div>
                        </div>

                        <!-- Footer -->
                        <div style="background:#f5f5f5;padding:20px;text-align:center;border-top:1px solid #e0e0e0;">
                            <p style="margin:0;color:#aaa;font-size:12px;">© 2025 Money Manager • Made with ❤️ by Dipak</p>
                            <p style="margin:4px 0 0;color:#ccc;font-size:11px;">You're receiving this because you registered at Money Manager.</p>
                        </div>

                    </div>
                    """.formatted(profile.getFullname(), frontendUrl);

                emailService.sendEmail(profile.getEmail(), "📅 Daily Reminder: Log Your Income & Expenses", body);
                log.info("✅ Reminder sent to: {}", profile.getEmail());
            } catch (Exception e) {
                log.error("❌ Failed to send reminder to: {} | Error: {}", profile.getEmail(), e.getMessage());
            }
        }
        log.info("Job completed: sendDailyIncomeExpenseReminder()");
    }

//        @Scheduled(cron = "* * * * * *", zone = "Asia/Kolkata") // every minute
    @Scheduled(cron = "0 0 23 * * *", zone = "Asia/Kolkata")  // at 11 pm
    public void sendDailyExpenseSummary() {
        log.info("\nJob started: sendDailyExpenseSummary()");
        List<ProfileEntity> profiles = profileRepository.findAll();

        for (ProfileEntity profile : profiles) {
            try {
                List<ExpenseDTO> todaysExpenses = expenseService.getExpensesForUserOnDate(
                        profile.getId(), LocalDate.now(ZoneId.of("Asia/Kolkata")));

                if (!todaysExpenses.isEmpty()) {

                    // Build table rows
                    StringBuilder rows = new StringBuilder();
                    BigDecimal total = BigDecimal.ZERO;
                    int i = 1;
                    for (ExpenseDTO expense : todaysExpenses) {
                        total = total.add(expense.getAmount());
                        String rowColor = (i % 2 == 0) ? "#f9f9f9" : "#ffffff";
                        rows.append("""
                            <tr style="background-color:%s;">
                                <td style="border:1px solid #ddd;padding:10px;text-align:center;">%d</td>
                                <td style="border:1px solid #ddd;padding:10px;">%s</td>
                                <td style="border:1px solid #ddd;padding:10px;text-align:right;color:#e53935;font-weight:bold;">₹ %s</td>
                                <td style="border:1px solid #ddd;padding:10px;text-align:center;">
                                    <span style="background:#e8f5e9;color:#2e7d32;padding:3px 10px;border-radius:12px;font-size:12px;">%s</span>
                                </td>
                                <td style="border:1px solid #ddd;padding:10px;text-align:center;color:#888;">%s</td>
                            </tr>
                            """.formatted(
                                rowColor,
                                i++,
                                expense.getName(),
                                expense.getAmount(),
                                expense.getCategoryId() != null ? expense.getCategoryName() : "N/A",
                                String.format("%02d:%02d", expense.getCreatedAt().getHour(), expense.getCreatedAt().getMinute())
                        ));
                    }

                    String body = """
                        <div style="font-family:Arial,sans-serif;max-width:650px;margin:auto;border:1px solid #e0e0e0;border-radius:12px;overflow:hidden;box-shadow:0 4px 12px rgba(0,0,0,0.1);">

                            <!-- Header -->
                            <div style="background:linear-gradient(135deg,#1565C0,#0d47a1);padding:30px;text-align:center;">
                                <h1 style="color:#fff;margin:0;font-size:26px;letter-spacing:1px;">💰 Money Manager</h1>
                                <p style="color:#bbdefb;margin:6px 0 0;">Daily Expense Summary</p>
                            </div>

                            <!-- Greeting -->
                            <div style="padding:30px 30px 10px;background:#ffffff;">
                                <h2 style="color:#1565C0;margin-top:0;">Hello, %s! 👋</h2>
                                <p style="color:#555;font-size:15px;line-height:1.6;">
                                    Here's a summary of your expenses for <strong>today</strong>. Keep tracking to stay on budget! 💪
                                </p>
                            </div>

                            <!-- Expense Table -->
                            <div style="padding:0 30px 20px;background:#ffffff;">
                                <table style="border-collapse:collapse;width:100%%;font-size:14px;">
                                    <thead>
                                        <tr style="background:linear-gradient(135deg,#1565C0,#0d47a1);color:#fff;">
                                            <th style="border:1px solid #ddd;padding:10px;text-align:center;">#</th>
                                            <th style="border:1px solid #ddd;padding:10px;text-align:left;">Name</th>
                                            <th style="border:1px solid #ddd;padding:10px;text-align:right;">Amount</th>
                                            <th style="border:1px solid #ddd;padding:10px;text-align:center;">Category</th>
                                            <th style="border:1px solid #ddd;padding:10px;text-align:center;">Time</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        %s
                                    </tbody>
                                </table>
                            </div>

                            <!-- Total -->
                            <div style="padding:0 30px 20px;background:#ffffff;text-align:right;">
                                <span style="font-size:16px;font-weight:bold;color:#b71c1c;">
                                    Total Spent Today: ₹ %s
                                </span>
                            </div>

                            <!-- CTA -->
                            <div style="text-align:center;padding:10px 30px 30px;background:#ffffff;">
                                <a href="%s"
                                   style="display:inline-block;padding:12px 32px;
                                          background:linear-gradient(135deg,#1565C0,#0d47a1);
                                          color:#fff;text-decoration:none;border-radius:8px;
                                          font-size:15px;font-weight:bold;
                                          box-shadow:0 4px 10px rgba(21,101,192,0.4);">
                                    📊 View Full Dashboard
                                </a>
                            </div>

                            <!-- Footer -->
                            <div style="background:#f5f5f5;padding:20px;text-align:center;border-top:1px solid #e0e0e0;">
                                <p style="margin:0;color:#aaa;font-size:12px;">© 2025 Money Manager • Made with ❤️ by Dipak</p>
                                <p style="margin:4px 0 0;color:#ccc;font-size:11px;">You're receiving this because you registered at Money Manager.</p>
                            </div>

                        </div>
                        """.formatted(profile.getFullname(), rows.toString(), total.setScale(2, RoundingMode.HALF_UP), frontendUrl);

                    emailService.sendEmail(profile.getEmail(), "📊 Your Daily Expense Summary", body);
                    log.info("✅ Expense summary sent to: {}", profile.getEmail());
                }
            } catch (Exception e) {
                log.error("❌ Failed to send expense summary to: {} | Error: {}", profile.getEmail(), e.getMessage());
            }
        }
        log.info("\nJob completed: sendDailyExpenseSummary()");
    }
}