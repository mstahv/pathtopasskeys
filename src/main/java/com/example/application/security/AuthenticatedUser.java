package com.example.application.security;

import com.example.application.data.User;
import com.example.application.data.UserRepository;
import com.example.application.data.WebAuthnRecord;
import com.example.application.data.WebAuthnRecordRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.apache.commons.io.IOUtils;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialCreationOptions;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.jackson.WebauthnJackson2Module;
import org.springframework.security.web.webauthn.management.ImmutablePublicKeyCredentialCreationOptionsRequest;
import org.springframework.security.web.webauthn.management.ImmutableRelyingPartyRegistrationRequest;
import org.springframework.security.web.webauthn.management.RelyingPartyPublicKey;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.SessionScope;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@SessionScope
public class AuthenticatedUser {

    private static ObjectMapper om = Jackson2ObjectMapperBuilder.json().modules(new WebauthnJackson2Module()).build();

    private final UserRepository userRepository;
    private final AuthenticationContext authenticationContext;
    private final UserCredentialRepository userCredentialRepository;
    private final WebAuthnRecordRepository webAuthnRecordRepository;
    private final WebAuthnRelyingPartyOperations rpOperations;
    private PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions;

    public AuthenticatedUser(AuthenticationContext authenticationContext, UserRepository userRepository, UserCredentialRepository userCredentialRepository, WebAuthnRecordRepository webAuthnRecordRepository, WebAuthnRelyingPartyOperations rpOperations) {
        this.userRepository = userRepository;
        this.authenticationContext = authenticationContext;
        this.userCredentialRepository = userCredentialRepository;
        this.webAuthnRecordRepository = webAuthnRecordRepository;
        this.rpOperations = rpOperations;
    }

    @Transactional
    public Optional<User> get() {
        try {
            return authenticationContext.getAuthenticatedUser(UserDetails.class)
                    .flatMap(userDetails -> userRepository.findByUsername(userDetails.getUsername()));

        } catch (ClassCastException e) {
            // In case of WebAuthn authentication, principal is PublicKeyCredentialUserEntity
            // See WebAuthnPublicKeyCredentialUserEntityRepositoryImpl
            return authenticationContext.getAuthenticatedUser(PublicKeyCredentialUserEntity.class)
                    .flatMap(userEntity -> userRepository.findByWebAuthnId(userEntity.getId().getBytes()));
        }
    }

    public void logout() {
        authenticationContext.logout();
    }

    public List<WebAuthnRecord> getPasskeys() {
        User user = get().get();
        return webAuthnRecordRepository.findByUser(user);
    }

    /**
     * Start the WebAuthn registration process for the current user. This method does pretty much
     * the same as Spring Security's default filters can do, but in a more SPA friendly way.
     *
     * @return a {@link CompletableFuture} that completes when the registration is done.
     */
    public CompletableFuture<Void> startWebAuthnRegistration() {
        // These contain the user identity and the relying party information
        publicKeyCredentialCreationOptions = this.rpOperations.createPublicKeyCredentialCreationOptions(
                new ImmutablePublicKeyCredentialCreationOptionsRequest(SecurityContextHolder.getContext().getAuthentication()));
        try {
            loadWebauthJs();
            return UI.getCurrent().getPage().executeJs("""
                            const label = "%s";
                            const creds = %s;
                            const pk = await window.register(creds, label);
                            return JSON.stringify(pk);
                            """.formatted(
                            LocalDateTime.now().toString(),
                            om.writeValueAsString(publicKeyCredentialCreationOptions)))
                    .toCompletableFuture(String.class)
                    .thenAccept(publicKeyJson -> {
                        try {
                            RelyingPartyPublicKey publicKey = om.readValue(publicKeyJson, RelyingPartyPublicKey.class);
                            // this passkey negotiated with browser and the server now needs to be stored in the database
                            rpOperations.registerCredential(new ImmutableRelyingPartyRegistrationRequest(publicKeyCredentialCreationOptions, publicKey));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadWebauthJs() {
        try {
            String string = IOUtils.toString(AuthenticatedUser.class.getResourceAsStream("/webauthn_vf.js"));
            UI.getCurrent().getPage().executeJs(string);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void deletePasskey(WebAuthnRecord passkey) {
        webAuthnRecordRepository.delete(passkey);
    }
}
