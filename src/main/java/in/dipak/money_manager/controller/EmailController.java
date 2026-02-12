package in.dipak.money_manager.controller;

import in.dipak.money_manager.entity.ProfileEntity;
import in.dipak.money_manager.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final ProfileService profileService;
    private final ExcelService excelService;


    @GetMapping("/income_excel")
    public ResponseEntity<Void> emailIncomeExcel() throws IOException {

        // 1. Get logged-in user's profile
        ProfileEntity profile = profileService.getCurrentProfile();

        // 2. Create Excel in memory
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        excelService.writeIncomeExcel(baos,incomeService.getCurrentMonthIncomesForCurrentUser());

        // 3. Send email with Excel attachment
        emailService.sendEmailWithAttachment(
                profile.getEmail(),
                "Your Income Report",
                "Please find attached your income report for the current month.",
                baos.toByteArray(),
                "income.xlsx"
        );

        return ResponseEntity.ok().build();
    }
}
