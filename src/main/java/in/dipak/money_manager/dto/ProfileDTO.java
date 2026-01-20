package in.dipak.money_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileDTO {
    private Long id;
    private String fullname;
    private String email;
    private String password;
    private String profileImageURL;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
