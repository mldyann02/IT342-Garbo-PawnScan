package edu.cit.garbo.pawnscan.shared.security;

import edu.cit.garbo.pawnscan.shared.user.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;

        @Bean
        public UserDetailsService userDetailsService() {
                return username -> userRepository.findByEmail(username)
                                .map(user -> org.springframework.security.core.userdetails.User
                                                .withUsername(user.getEmail())
                                                .password(user.getPasswordHash())
                                                .authorities(Collections
                                                                .singletonList(() -> "ROLE_" + user.getRole().name()))
                                                .build())
                                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        }

        @Bean
        public AuthenticationManager authenticationManager() {
                return new ProviderManager(Collections.singletonList(daoAuthenticationProvider()));
        }

        @Bean
        public AuthenticationProvider daoAuthenticationProvider() {
                // FIX: Use default constructor and then setters
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
                provider.setUserDetailsService(userDetailsService());
                provider.setPasswordEncoder(passwordEncoder);
                return provider;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService);

                http.cors(org.springframework.security.config.Customizer.withDefaults())
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/auth/**").permitAll()
                                                .requestMatchers("/uploads/**").permitAll()
                                                // Updated to match your refactored verification path in tests
                                                .requestMatchers("/api/verification/**").hasRole("BUSINESS")
                                                .requestMatchers("/api/business-profiles/**").hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((request, response, authException) -> response
                                                                .sendError(HttpServletResponse.SC_UNAUTHORIZED,
                                                                                "Unauthorized"))
                                                .accessDeniedHandler(
                                                                (request, response, accessDeniedException) -> response
                                                                                .sendError(HttpServletResponse.SC_FORBIDDEN,
                                                                                                "Forbidden")))
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}