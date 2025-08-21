package com.shiv.MoneyManager.service;

import com.shiv.MoneyManager.dataTransferObjects.AuthDTO;
import com.shiv.MoneyManager.dataTransferObjects.ProfileDTO;
import com.shiv.MoneyManager.entity.ProfileEntity;
import com.shiv.MoneyManager.repository.ProfileRepository;
import com.shiv.MoneyManager.util.JWTUtil;
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

@Service // Spring ko bolta hai ki ye ek service layer hai (business logic ka adda)
@RequiredArgsConstructor // Lombok ka shortcut: final fields ka constructor bana deta hai
public class ProfileService {

    // Database ke saath interact karne ka tool
    private final ProfileRepository profileRepository;

    // Email bhejne ka helper service
    private final EmailService emailService;

    // Password hash/encode karne ka tool
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private  final JWTUtil jwtUtil;

    /**
     * registerProfile()
     * Desi Style: Ye method naya user register karta hai.
     * Step:
     * 1. Frontend se aaya DTO ko Entity me badalna
     * 2. Ek random activation token banana (email verification ke liye)
     * 3. Entity ko database me save karna
     * 4. User ko email bhejna activation link ke saath
     * 5. Entity ko DTO me badal ke return karna
     */
    public ProfileDTO registerProfile(ProfileDTO profileDTO) {
        // Step 1: DTO -> Entity
        ProfileEntity newProfile = toEntity(profileDTO);

        // Step 2: Random token lagao (email activate karne ke liye)
        newProfile.setActivationToken(UUID.randomUUID().toString());

        // Step 3: Database me save karo
        newProfile = profileRepository.save(newProfile);

        // Step 4: Email bhejo user ko activation link ke saath
        String activationLink = "https://money-manager-api-a2fv.onrender.com/api/v1.0/activate?token=" + newProfile.getActivationToken();
        String subject = "Activate your Money Manager account";
        String body = "Click on the following link to activate your account " + activationLink;
        emailService.sendEmail(newProfile.getEmail(), subject, body);

        // Step 5: Entity -> DTO aur return
        return toDTO(newProfile);
    }

    /**
     * toEntity()
     * Desi Style: Ye method DTO ko Entity me badalta hai
     * Kyun? DTO frontend ka data hota hai, Entity DB ka format hota hai.
     */
    public ProfileEntity toEntity(ProfileDTO profileDTO) {
        return ProfileEntity.builder()
                .id(profileDTO.getId()) // Agar update hai to ID set karega
                .fullName(profileDTO.getFullName()) // Naam set karo
                .email(profileDTO.getEmail()) // Email set karo
                .password(passwordEncoder.encode(profileDTO.getPassword())) // Password ko hash/encode karo
                .profileImageUrl(profileDTO.getProfileImageUrl()) // Profile photo ka link
                .createdAt(profileDTO.getCreatedAt()) // Kab banaya gaya
                .updatedAt(profileDTO.getUpdatedAt()) // Kab update hua
                .build();
    }

    /**
     * toDTO()
     * Desi Style: Ye method Entity ko DTO me badalta hai
     * Kyun? Backend ka DB format frontend ko directly dena accha practice nahi hota.
     */
    public ProfileDTO toDTO(ProfileEntity profileEntity) {
        return ProfileDTO.builder()
                .id(profileEntity.getId()) // ID set karo
                .fullName(profileEntity.getFullName()) // Naam set karo
                .email(profileEntity.getEmail()) // Email set karo
                .profileImageUrl(profileEntity.getProfileImageUrl()) // Photo ka URL
                .createdAt(profileEntity.getCreatedAt()) // Created date
                .updatedAt(profileEntity.getUpdatedAt()) // Updated date
                .build();
    }

    /**
     * activateProfile()
     * Desi Style: Token ke basis pe account activate karta hai
     * Kyun? Email verify hone ke baad user ka account activate hota hai
     */
    public boolean activateProfile(String activationToken) {
        return profileRepository.findByActivationToken(activationToken) // Token se user dhoondo
                .map(profile -> {
                    profile.setIsActive(true); // Active karo
                    profileRepository.save(profile); // Save karo DB me
                    return true; // Ho gaya
                }).orElse(false); // Agar token galat hai to false
    }

    /**
     * isAccountActive()
     * Desi Style: Check karta hai user ka account active hai ya nahi
     */
    public boolean isAccountActive(String email) {
        return profileRepository.findByEmail(email) // Email se user dhoondo
                .map(ProfileEntity::getIsActive) // Active flag uthao
                .orElse(false); // Agar user nahi mila to false
    }

    /**
     * getCurrentProfile()
     * Desi Style: Login kiya hua user ka profile deta hai
     * Kaise? SecurityContext se email uthata hai aur DB me check karta hai
     */
    public ProfileEntity getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // Login info lelo
        return profileRepository.findByEmail(authentication.getName()) // Email se profile uthao
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Profile not found with email " + authentication.getName())); // Agar nahi mila to error
    }

    /**
     * getPublicProfile()
     * Desi Style: Public profile deta hai, agar email diya to uska, warna current user ka
     */
    public ProfileDTO getPublicProfile(String email) {
        ProfileEntity currentUser;
        if (email == null) {
            // Agar email nahi aayi to current logged in user ka profile
            currentUser = getCurrentProfile();
        } else {
            // Agar email aayi to us user ka profile
            currentUser = profileRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "Profile not found with email " + email));
        }

        // Entity -> DTO
        return ProfileDTO.builder()
                .id(currentUser.getId())
                .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .profileImageUrl(currentUser.getProfileImageUrl())
                .createdAt(currentUser.getCreatedAt())
                .updatedAt(currentUser.getUpdatedAt())
                .build();
    }

    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword()));
            //Generate JWT Token
            String token = jwtUtil.generateToken(authDTO.getEmail());
            return Map.of("token", token,
                    "user", getPublicProfile(authDTO.getEmail()));
        } catch (Exception e) {
            throw new RuntimeException("Invalid email and password");
        }
    }
}
