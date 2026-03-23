package com.example.application.security;

import com.example.application.views.login.LoginView;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ott.InMemoryOneTimeTokenService;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRpEntity;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;
import org.springframework.security.web.webauthn.management.Webauthn4JRelyingPartyOperations;

import java.util.Set;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {

        http.authorizeHttpRequests(auth -> {
            auth
                    .requestMatchers("/webauthn/**").permitAll()
                    .requestMatchers("/images/*.png").permitAll()
                    .requestMatchers("/line-awesome/svg/*.svg").permitAll();
        });

        http.oneTimeTokenLogin(withDefaults());

        http.webAuthn(withDefaults());

        http.csrf(cfg -> cfg.ignoringRequestMatchers("/webauthn/**", "/login/webauthn"));

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

    /*
     * webauthnId (~ domain name), and webauthnOrigin are drawing in from application.properties,
     * so that for TestApplication (used for local development) you get the value from src/test/resources.
     * Alternatively you could use e.g. Spring profiles or environment variables to configure suitable
     * values for both testing and production deployments.
     */
    @Bean public WebAuthnRelyingPartyOperations relyingPartyOperations(
            PublicKeyCredentialUserEntityRepository userEntities,
            UserCredentialRepository userCredentials,
            @Value("${webauthn.id}") String webauthnId,
            @Value("${webauthn.origin}") String webauthnOrigin) {
        return new Webauthn4JRelyingPartyOperations(userEntities, userCredentials,
                PublicKeyCredentialRpEntity.builder()
                        .id(webauthnId)
                        .name("}> WebAuthn + Spring Security Demo").build(),
                Set.of(webauthnOrigin));
    }

}
