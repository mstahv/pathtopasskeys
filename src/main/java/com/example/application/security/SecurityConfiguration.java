package com.example.application.security;

import com.example.application.views.login.LoginView;
import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategy;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ott.InMemoryOneTimeTokenService;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {

    /*
     * webauthnId (~ domain name), and webauthnOrigin are drawn in from application.properties,
     * so that for TestApplication (used for local development) you get the value from src/test/resources.
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            @Value("${webauthn.id}") String webauthnId,
            @Value("${webauthn.origin}") String webauthnOrigin) {

        http.authorizeHttpRequests(auth -> {
            auth
                    // Passkey registration endpoints (login endpoints are handled before authorization)
                    .requestMatchers("/webauthn/register/**").authenticated()
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

        // Spring Security provides the endpoints for passkey registration and login,
        // see WebAuthnJs for the Vaadin integration. Passkeys are stored by
        // WebAuthnUserCredentialRepositoryImpl and WebAuthnPublicKeyCredentialUserEntityRepositoryImpl.
        http.webAuthn(webAuthn -> webAuthn
                .rpId(webauthnId)
                .rpName("PathToPasskeys")
                .allowedOrigins(webauthnOrigin)
                // We have the Profile view for this
                .disableDefaultRegistrationPage(true));

        // LoginView as the login page (passkeys and one-time tokens only)
        http.with(VaadinSecurityConfigurer.vaadin(), configurer -> {
            configurer.loginView(LoginView.class);
        });

        return http.build();
    }

    /**
     * Workaround: Spring Security's WebAuthn filters (e.g. the one serving
     * /webauthn/register/options) pick up the global SecurityContextHolderStrategy
     * when they are created, instead of the one configured for HttpSecurity. Vaadin
     * sets its strategy as the global one only after the filter chain is built, so
     * the filters would not see the logged-in user (HTTP 400). Setting it here,
     * when the strategy bean is created, happens early enough.
     */
    @Bean
    static SecurityContextHolderStrategy vaadinAwareSecurityContextHolderStrategy() {
        var strategy = new VaadinAwareSecurityContextHolderStrategy();
        SecurityContextHolder.setContextHolderStrategy(strategy);
        return strategy;
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
