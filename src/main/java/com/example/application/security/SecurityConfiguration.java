package com.example.application.security;

import com.example.application.views.login.LoginView;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ott.InMemoryOneTimeTokenService;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {

        http.authorizeHttpRequests(auth -> {
            auth
                    .requestMatchers("/images/*.png").permitAll()
                    .requestMatchers("/line-awesome/svg/*.svg").permitAll();
        });

        // One-time tokens ("magic links") as a fallback login. The login page is
        // set explicitly, otherwise Spring Security may serve its generated default
        // login page at /login instead of our LoginView. Setting the login page
        // also changes the processing URL, so reset it to the default.
        http.oneTimeTokenLogin(ott -> ott
                .loginPage("/login")
                .loginProcessingUrl("/login/ott"));

        // Username + password login with LoginView as the login page
        http.with(VaadinSecurityConfigurer.vaadin(), configurer -> {
            configurer.loginView(LoginView.class);
        });

        return http.build();
    }

    @Bean
    OneTimeTokenGenerationSuccessHandler tokenGenerationSuccessHandler() {
        return new OTTHandler();
    }

    @Bean
    OneTimeTokenService oneTimeTokenService() {
        // This is fine for single node apps, but for a cluster you would need share the
        // tokens between the nodes somehow
        return new InMemoryOneTimeTokenService();
    }

}
