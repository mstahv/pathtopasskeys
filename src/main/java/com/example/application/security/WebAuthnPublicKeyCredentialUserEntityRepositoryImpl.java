package com.example.application.security;

import com.example.application.data.User;
import com.example.application.data.UserRepository;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Maps the users of the app to WebAuthn "user entities". The only WebAuthn
 * specific data is the random WebAuthn id of the user (a short byte array),
 * which we store in {@link User#getWebAuthnId()}. Spring Security creates it
 * when the user registers their first passkey.
 * <p>
 * Like with {@link WebAuthnUserCredentialRepositoryImpl}, Spring Security has a
 * ready-made JDBC alternative,
 * {@link org.springframework.security.web.webauthn.management.JdbcPublicKeyCredentialUserEntityRepository}
 * (with table definition in {@code org/springframework/security/user-entities-schema.sql}),
 * that you can use if it fits your architecture.
 */
@Service
@Transactional
public class WebAuthnPublicKeyCredentialUserEntityRepositoryImpl implements PublicKeyCredentialUserEntityRepository {

    private final UserRepository userRepository;

    public WebAuthnPublicKeyCredentialUserEntityRepositoryImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public PublicKeyCredentialUserEntity findById(Bytes id) {
        return userRepository.findByWebAuthnId(id.getBytes())
                .map(WebAuthnPublicKeyCredentialUserEntityRepositoryImpl::toUserEntity)
                .orElse(null);
    }

    @Override
    public PublicKeyCredentialUserEntity findByUsername(String username) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getWebAuthnId() != null)
                .map(WebAuthnPublicKeyCredentialUserEntityRepositoryImpl::toUserEntity)
                .orElse(null);
    }

    @Override
    public void save(PublicKeyCredentialUserEntity userEntity) {
        userRepository.findByUsername(userEntity.getName()).ifPresent(user -> {
            user.setWebAuthnId(userEntity.getId().getBytes());
            userRepository.save(user);
        });
    }

    @Override
    public void delete(Bytes id) {
        userRepository.findByWebAuthnId(id.getBytes()).ifPresent(user -> {
            user.setWebAuthnId(null);
            userRepository.save(user);
        });
    }

    private static PublicKeyCredentialUserEntity toUserEntity(User user) {
        return ImmutablePublicKeyCredentialUserEntity.builder()
                .id(new Bytes(user.getWebAuthnId()))
                .name(user.getUsername())
                .displayName(user.getName())
                .build();
    }
}
