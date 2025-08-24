package com.shiv.MoneyManager.config;

import com.shiv.MoneyManager.security.JWTRequestFilter;
import com.shiv.MoneyManager.service.AppUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppUserDetailsService appUserDetailsService;
    private final JWTRequestFilter jwtRequestFilter;


    /// -------------------------------------------------------------------------------------------------------------------------------------------------------------------


    // This method defines the security filter chain (how requests are secured)
    @Bean // Tells Spring to manage this object in the application context
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors(Customizer.withDefaults()) // Enables Cross-Origin Resource Sharing with default settings
                .csrf(AbstractHttpConfigurer::disable) // Disables CSRF protection (good for APIs, but risky for web forms)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/status", "/health", "/register", "/activate/**", "/error", "/login") // Public endpoints (no login needed)
                        .permitAll() // Allow access to above endpoints without authentication
                        .anyRequest().authenticated() // All other requests must be authenticated
                )
                .sessionManagement(session ->
                                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        // Makes the app stateless (no HTTP session stored on server, good for JWT auth)
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build(); // Builds and returns the configured SecurityFilterChain
    }

    /// -------------------------------------------------------------------------------------------------------------------------------------------------------------------


    // This method creates a PasswordEncoder bean (for encrypting passwords)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Uses BCrypt hashing for password storage (secure)
    }

    /// -------------------------------------------------------------------------------------------------------------------------------------------------------------------


    // This method configures CORS (Cross-Origin Resource Sharing)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",   // local frontend
                "https://your-frontend-domain.onrender.com"  // deploy hone ke baad ka domain
        )); // Allow requests from all origins
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Allowed HTTP methods
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept")); // Allowed request headers
        configuration.setAllowCredentials(true); // Allow cookies/auth credentials to be sent
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply CORS settings to all endpoints
        return source;
    }


    // 1. CORS enable karte hain
    // CORS ka matlab - Cross-Origin Resource Sharing
    // Example: Agar tera backend localhost:8080 pe chal raha hai
    // aur frontend localhost:3000 pe chal raha hai
    // to normally browser bolega "Arre ye toh alag origin hai, block karo!"
    // CORS allow kar ke hum bolte hain "Nahi bhai, is origin se request aane do"

    // 2. CSRF disable karte hain
    // CSRF ka matlab - Cross-Site Request Forgery
    // Ye ek security attack hota hai jisme hacker tera browser ka session use karke
    // unwanted request bhej sakta hai
    // API's me jab hum JWT token use karte hain to CSRF ki zarurat nahi hoti, isliye disable kar dete hain

    // 3. Authorization rules set karte hain


    // 4. Session management
    // STATELESS ka matlab - server koi session store nahi karega
    // Har request ke sath token bhejna padega (JWT ya koi bhi)

    // 5. Security config apply karke object return karte hain

    /// -------------------------------------------------------------------------------------------------------------------------------------------------------------------


    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(appUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authenticationProvider);
    }

    /*
   Spring Security me authentication ka matlab hota hai:
👉 User ka username/password check karna
👉 Agar sahi ho to usko access dena, warna access deny karna

   Aur AuthenticationManager ek core interface hai jo authentication handle karta hai.
   Spring Security ke andar alag-alag tarike ke authentication hote hain (in-memory, DB se, LDAP, OAuth, etc.),
   hum yaha DAO Authentication use kar rahe hain — matlab DB se user ka data uthake verify karna.
    */

//    ⚡ Ye method ek AuthenticationManager bean create karega jo authentication ka boss hoga.
//    Spring Security jab bhi user ko authenticate karega, is manager ko use karega.

//    📦 Hum ek DaoAuthenticationProvider bana rahe hain.
//    DAO ka matlab hai — Database Access Object.
//    Ye provider user ka username DB se nikalke check karega (via UserDetailsService) aur password match karega.

//    🧑‍💻 Yaha hum bol rahe hain ki "bhai, user ka data kaise milega?"
//    appUserDetailsService humne banaya hoga jo UserDetailsService implement karta hai.
//    Ye DB se user ka details (username, password, roles) laata hai.

//    🔑 Yaha hum bol rahe hain ki password match kaise hoga.
//    Hum BCryptPasswordEncoder use kar rahe hain jo encrypted password ko match karta hai.
//    Iska fayda ye ki password plain-text me kabhi store ya compare nahi hota.

//    🎯 Ab hum ek ProviderManager bana ke return kar rahe hain.
//    Ye ek type ka container hai jo authentication providers ko rakhta hai.
//    Jab login request aayegi, ye DaoAuthenticationProvider ko bol dega "bhai check kar le".

    /// -------------------------------------------------------------------------------------------------------------------------------------------------------------------


}
