package com.example.application.security;

import com.example.application.views.login.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ott.InMemoryOneTimeTokenService;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRpEntity;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;
import org.springframework.security.web.webauthn.management.Webauthn4JRelyingPartyOperations;

import java.util.Set;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(
                authorize -> authorize.requestMatchers(new AntPathRequestMatcher("/images/*.png")).permitAll());

        // Icons from the line-awesome addon
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(new AntPathRequestMatcher("/line-awesome/**/*.svg")).permitAll());

        http.oneTimeTokenLogin(withDefaults());

        http.webAuthn(withDefaults());
        // Allow the webauthn endpoints to be accessed without authentication,
        // used in login process
        // TODO refactor the login process to work like the registration (skip the default filters)
        http.authorizeHttpRequests(
                authorize -> authorize.requestMatchers(new AntPathRequestMatcher("/webauthn/**")).permitAll());
        http.csrf(cfg -> cfg.ignoringRequestMatchers(
                new AntPathRequestMatcher("/webauthn/**"),new AntPathRequestMatcher("/login/webauthn")));

        super.configure(http);
        // TODO as this example no more uses "form based login" (aka username/password)
        // you might want to craft a super class that don't configure it.
        // (Password encoder is not set, so probably non-functional anyways, but didn't test)
        setLoginView(http, LoginView.class);
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

    @Bean public WebAuthnRelyingPartyOperations relyingPartyOperations(PublicKeyCredentialUserEntityRepository userEntities, UserCredentialRepository userCredentials) {
        // Extends these so that they work for your (e.g. with deployment URL that the browser sees)
        return new Webauthn4JRelyingPartyOperations(userEntities, userCredentials,
                PublicKeyCredentialRpEntity.builder().id(
                                "localhost")
                        .name("}> WebAuthn + Spring Security Demo").build(), Set.of("http://localhost:8080"));
    }

}
