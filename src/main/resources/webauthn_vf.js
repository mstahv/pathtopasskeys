// Browser side of the WebAuthn ceremonies, used via WebAuthnJs.java.
// A trimmed version of Spring Security's spring-security-webauthn.js: that script
// only exposes functions tied to Spring Security's generated HTML pages, so we
// expose the core functions to be called from Vaadin instead. Both talk to the
// endpoints Spring Security provides.
"use strict";
(() => {
  if (window.webauthnVf) {
    return;
  }

  const base64url = {
    encode(buffer) {
      const base64 = window.btoa(String.fromCharCode(...new Uint8Array(buffer)));
      return base64.replace(/=/g, "").replace(/\+/g, "-").replace(/\//g, "_");
    },
    decode(value) {
      const binStr = window.atob(value.replace(/-/g, "+").replace(/_/g, "/"));
      const bin = new Uint8Array(binStr.length);
      for (let i = 0; i < binStr.length; i++) {
        bin[i] = binStr.charCodeAt(i);
      }
      return bin.buffer;
    }
  };

  async function post(url, headers, body) {
    const response = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json", ...headers },
      body: body ? JSON.stringify(body) : undefined
    });
    if (!response.ok) {
      throw new Error(`${url} responded with HTTP ${response.status}`);
    }
    return response.json();
  }

  async function authenticate(headers) {
    const options = await post("/webauthn/authenticate/options", headers);
    const cred = await navigator.credentials.get({
      publicKey: { ...options, challenge: base64url.decode(options.challenge) }
    });
    const { response } = cred;
    const result = await post("/login/webauthn", headers, {
      id: cred.id,
      rawId: base64url.encode(cred.rawId),
      response: {
        authenticatorData: base64url.encode(response.authenticatorData),
        clientDataJSON: base64url.encode(response.clientDataJSON),
        signature: base64url.encode(response.signature),
        userHandle: response.userHandle ? base64url.encode(response.userHandle) : undefined
      },
      credType: cred.type,
      clientExtensionResults: cred.getClientExtensionResults(),
      authenticatorAttachment: cred.authenticatorAttachment
    });
    if (!(result && result.authenticated && result.redirectUrl)) {
      throw new Error(`Authentication failed: ${JSON.stringify(result)}`);
    }
    return result.redirectUrl;
  }

  async function authenticateOrError(headers) {
    try {
      window.location.href = await authenticate(headers);
    } catch (err) {
      console.error(err);
      window.location.href = "/login?error";
    }
  }

  async function register(headers, label) {
    const options = await post("/webauthn/register/options", headers);
    const cred = await navigator.credentials.create({
      publicKey: {
        ...options,
        user: { ...options.user, id: base64url.decode(options.user.id) },
        challenge: base64url.decode(options.challenge),
        excludeCredentials: (options.excludeCredentials || []).map((c) => ({
          ...c,
          id: base64url.decode(c.id)
        }))
      }
    });
    const { response } = cred;
    const result = await post("/webauthn/register", headers, {
      publicKey: {
        credential: {
          id: cred.id,
          rawId: base64url.encode(cred.rawId),
          response: {
            attestationObject: base64url.encode(response.attestationObject),
            clientDataJSON: base64url.encode(response.clientDataJSON),
            transports: response.getTransports ? response.getTransports() : []
          },
          type: cred.type,
          clientExtensionResults: cred.getClientExtensionResults(),
          authenticatorAttachment: cred.authenticatorAttachment
        },
        label
      }
    });
    if (!(result && result.success)) {
      throw new Error(`Registration failed: ${JSON.stringify(result)}`);
    }
  }

  window.webauthnVf = { authenticateOrError, register };
})();
