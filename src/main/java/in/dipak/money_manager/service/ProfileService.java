package in.dipak.money_manager.service;

import in.dipak.money_manager.dto.ProfileDTO;
import in.dipak.money_manager.dto.AuthDTO;
import in.dipak.money_manager.entity.ProfileEntity;
import in.dipak.money_manager.repository.ProfileRepository;
import in.dipak.money_manager.util.JwtUtil;
import lombok.RequiredArgsConstructor;
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

        // Create the link
        String activationLink = activationURL + "/api/v2.0/activate?token=" + newProfile.getActivationToken();
        String subject = "Activate Your Money Manager Account";
        String body = "Click on the following link to activate your account: " + activationLink;

        // 🛑 SAFETY BLOCK: Try to send email, but don't crash if it fails
        try {
            emailService.sendEmail(newProfile.getEmail(), subject, body);
            System.out.println("✅ Email sent successfully to: " + newProfile.getEmail());
        } catch (Exception e) {
            // If email fails, Log the error BUT let the user finish registration!
            System.err.println("❌ EMAIL FAILED TO SEND: " + e.getMessage());
            System.out.println("⚠️ MANUAL ACTIVATION LINK: " + activationLink);
        }

        return toDTO(newProfile);
    }
//    public ProfileDTO registerProfile(ProfileDTO profileDTO) {
//        ProfileEntity newProfile =  toEntity(profileDTO);
//        newProfile.setActivationToken(UUID.randomUUID().toString());
//        newProfile = profileRepository.save(newProfile);
//
////        send activation email
//        String activationLink = activationURL+"/api/v2.0/activate?token=" + newProfile.getActivationToken();
//        String subject = "Activate Your Money Manager Account";
//        String body = "Click on the following link to activate your account: "+ activationLink;
//        emailService.sendEmail(newProfile.getEmail(), subject,body);
//
//        return toDTO(newProfile);
//    }
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
