package in.dipak.money_manager.service;

import in.dipak.money_manager.dto.ProfileDTO;
import in.dipak.money_manager.dto.AuthDTO;
import in.dipak.money_manager.entity.ProfileEntity;
import in.dipak.money_manager.repository.ProfileRepository;
import in.dipak.money_manager.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${app.activation.url}")
    private String activationURL;

public ProfileDTO registerProfile(ProfileDTO profileDTO) {
    ProfileEntity newProfile = toEntity(profileDTO);
    newProfile.setActivationToken(UUID.randomUUID().toString());
    newProfile = profileRepository.save(newProfile);

    // send activation email
    String activationLink = activationURL + "/api/v2.0/activate?token=" + newProfile.getActivationToken();
    String subject = "✅ Activate Your Money Manager Account";

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
                    Thank you for registering with <strong>Money Manager</strong>. 
                    You're just one step away from taking control of your finances!
                </p>
                <p style="color:#555;font-size:15px;line-height:1.6;">
                    Please click the button below to activate your account:
                </p>

                <!-- CTA Button -->
                <div style="text-align:center;margin:30px 0;">
                    <a href="%s"
                       style="display:inline-block;padding:14px 36px;background:linear-gradient(135deg,#4CAF50,#2e7d32);
                              color:#ffffff;text-decoration:none;border-radius:8px;font-size:16px;
                              font-weight:bold;letter-spacing:0.5px;box-shadow:0 4px 10px rgba(76,175,80,0.4);">
                        🚀 Activate My Account
                    </a>
                </div>

                <!-- Fallback link -->
                <p style="color:#888;font-size:13px;text-align:center;">
                    Button not working? 
                    <a href="%s" style="color:#4CAF50;word-break:break-all;">Click here</a>
                </p>

                <!-- Warning -->
                <div style="background:#fff8e1;border-left:4px solid #FFC107;padding:12px 16px;border-radius:4px;margin-top:20px;">
                    <p style="margin:0;color:#7a6000;font-size:13px;">
                        ⚠️ This link will expire in <strong>24 hours</strong>. If you didn't create an account, please ignore this email.
                    </p>
                </div>
            </div>

            <!-- Footer -->
            <div style="background:#f5f5f5;padding:20px;text-align:center;border-top:1px solid #e0e0e0;">
                <p style="margin:0;color:#aaa;font-size:12px;">
                    © 2025 Money Manager • Made with ❤️ by Dipak
                </p>
            </div>

        </div>
        """.formatted(newProfile.getFullname(), activationLink, activationLink);

    try {
        emailService.sendEmail(newProfile.getEmail(), subject, body);
        log.info("✅ Activation email sent to: {}", newProfile.getEmail());
    } catch (Exception e) {
        log.error("❌ EMAIL FAILED TO SEND: {}", e.getMessage());
        log.warn("⚠️ MANUAL ACTIVATION LINK: {}", activationLink);
    }

    return toDTO(newProfile);
}
    public ProfileEntity toEntity(ProfileDTO profileDTO) {
        return ProfileEntity.builder()
                .id(profileDTO.getId())
                .fullname(profileDTO.getFullname())
                .email(profileDTO.getEmail())
                .password(passwordEncoder.encode(profileDTO.getPassword()))
                .profileImageURL(profileDTO.getProfileImageURL())
                .createdAt(profileDTO.getCreatedAt())
                .updatedAt(profileDTO.getUpdatedAt())
                .build();
    }
    public ProfileDTO toDTO(ProfileEntity profileEntity) {
        return ProfileDTO.builder()
                .id(profileEntity.getId())
                .fullname(profileEntity.getFullname())
                .email(profileEntity.getEmail())
                .profileImageURL(profileEntity.getProfileImageURL())
                .createdAt(profileEntity.getCreatedAt())
                .updatedAt(profileEntity.getUpdatedAt())
                .build();
    }

    public boolean activateProfile(String activationToken) {
        return profileRepository.findByActivationToken(activationToken)
                .map(profile -> {
                    profile.setIsActive(true);
                    profileRepository.save(profile);
                    return true;
                })
                .orElse(false);
    }
    public boolean isAccountActive(String email) {
        return profileRepository.findByEmail(email)
                .map(ProfileEntity::getIsActive)
                .orElse(false);
    }

    public ProfileEntity getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return profileRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: "+authentication.getName()));
    }
    public ProfileDTO getPublicProfile(String email) {
        ProfileEntity currentUser = null;
        if(email == null) {
            currentUser = getCurrentProfile();
        }
        else {
            currentUser = profileRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: "+email));
        }
        return ProfileDTO.builder()
                .id(currentUser.getId())
                .fullname(currentUser.getFullname())
                .email(currentUser.getEmail())
                .profileImageURL(currentUser.getProfileImageURL())
                .createdAt(currentUser.getCreatedAt())
                .updatedAt(currentUser.getUpdatedAt())
                .build();
    }

    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDTO.getEmail(),authDTO.getPassword()));
//            Generate JWT token
            String token = jwtUtil.generateToken(authDTO.getEmail());
            return Map.of(
                    "token",token,
                    "user", getPublicProfile(authDTO.getEmail())
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid Email or Password");
        }
    }
}
