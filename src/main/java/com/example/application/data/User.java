package com.example.application.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "application_user")
public class User extends AbstractEntity {

    private String username;
    private String name;
    @JsonIgnore
    private String hashedPassword;
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles;
    @Lob
    @Column(length = 1000000)
    private byte[] profilePicture;

    @OneToMany(mappedBy = "user")
    private Set<WebAuthnRecord> webAuthnRecords;

    @Column(columnDefinition="bytea")
    private byte[] webAuthnId;

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return the hashed password from the history of computer science
     * @deprecated use passkeys/webauthn instead, it is 2025!! Left this hanging
     * into the example os I don't need to change the test data sql dump 🤓
     */
    @Deprecated(forRemoval = true)
    public String getHashedPassword() {
        return hashedPassword;
    }
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
    public Set<Role> getRoles() {
        return roles;
    }
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    public byte[] getProfilePicture() {
        return profilePicture;
    }
    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }
    public Set<WebAuthnRecord> getWebAuthnRecords() {
        return webAuthnRecords;
    }
    public void setWebAuthnRecords(Set<WebAuthnRecord> webAuthnRecords) {
        this.webAuthnRecords = webAuthnRecords;
    }
    public byte[] getWebAuthnId() {
        return webAuthnId;
    }
    public void setWebAuthnId(byte[] webAuthnId) {
        this.webAuthnId = webAuthnId;
    }
}
