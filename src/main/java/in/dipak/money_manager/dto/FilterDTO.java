package in.dipak.money_manager.dto;

import lombok.Data;

import java.time.LocalDate;

@Data

public class FilterDTO {
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String keyword;
    private String sortField; // sorting based on date, amount and name
    private String sortOrder; // desc || asc

}
