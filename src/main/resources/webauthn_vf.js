// A slightly simplified/modified version of the original script used by Spring Security
// that is better suited for Vaadin Flow UIs to handle WebAuthn registration and authentication.

"use strict";
(() => {
  // lib/base64url.js
  var base64url_default = {
    encode: function(buffer) {
      const base64 = window.btoa(String.fromCharCode(...new Uint8Array(buffer)));
      return base64.replace(/=/g, "").replace(/\+/g, "-").replace(/\//g, "_");
    },
    decode: function(base64url) {
      const base64 = base64url.replace(/-/g, "+").replace(/_/g, "/");
      const binStr = window.atob(base64);
      const bin = new Uint8Array(binStr.length);
      for (let i = 0; i < binStr.length; i++) {
        bin[i] = binStr.charCodeAt(i);
      }
      return bin.buffer;
    }
  };

  // lib/http.js
  async function post(url, headers, body) {
    const options = {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...headers
      }
    };
    if (body) {
      options.body = JSON.stringify(body);
    }
    return fetch(url, options);
  }
  var http_default = { post };

  // lib/abort-controller.js
  var holder = {
    controller: new AbortController()
  };
  function newSignal() {
    if (!!holder.controller) {
      holder.controller.abort("Initiating new WebAuthN ceremony, cancelling current ceremony");
    }
    holder.controller = new AbortController();
    return holder.controller.signal;
  }
  var abort_controller_default = {
    newSignal
  };

  // lib/webauthn-core.js
  async function isConditionalMediationAvailable() {
    return !!(window.PublicKeyCredential && window.PublicKeyCredential.isConditionalMediationAvailable && await window.PublicKeyCredential.isConditionalMediationAvailable());
  }
  async function authenticate(headers, contextPath, useConditionalMediation) {
    let options;
    try {
      const optionsResponse = await http_default.post(`${contextPath}/webauthn/authenticate/options`, headers);
      if (!optionsResponse.ok) {
        throw new Error(`HTTP ${optionsResponse.status}`);
      }
      options = await optionsResponse.json();
    } catch (err) {
      throw new Error(`Authentication failed. Could not fetch authentication options: ${err.message}`, { cause: err });
    }
    const decodedOptions = {
      ...options,
      challenge: base64url_default.decode(options.challenge)
    };
    const credentialOptions = {
      publicKey: decodedOptions,
      signal: abort_controller_default.newSignal()
    };
    if (useConditionalMediation) {
      credentialOptions.mediation = "conditional";
    }
    let cred;
    try {
      cred = await navigator.credentials.get(credentialOptions);
    } catch (err) {
      throw new Error(`Authentication failed. Call to navigator.credentials.get failed: ${err.message}`, { cause: err });
    }
    const { response, type: credType } = cred;
    let userHandle;
    if (response.userHandle) {
      userHandle = base64url_default.encode(response.userHandle);
    }
    const body = {
      id: cred.id,
      rawId: base64url_default.encode(cred.rawId),
      response: {
        authenticatorData: base64url_default.encode(response.authenticatorData),
        clientDataJSON: base64url_default.encode(response.clientDataJSON),
        signature: base64url_default.encode(response.signature),
        userHandle
      },
      credType,
      clientExtensionResults: cred.getClientExtensionResults(),
      authenticatorAttachment: cred.authenticatorAttachment
    };
    let authenticationResponse;
    try {
      const authenticationCallResponse = await http_default.post(`${contextPath}/login/webauthn`, headers, body);
      if (!authenticationCallResponse.ok) {
        throw new Error(`HTTP ${authenticationCallResponse.status}`);
      }
      authenticationResponse = await authenticationCallResponse.json();
    } catch (err) {
      throw new Error(`Authentication failed. Could not process the authentication request: ${err.message}`, {
        cause: err
      });
    }
    if (!(authenticationResponse && authenticationResponse.authenticated && authenticationResponse.redirectUrl)) {
      throw new Error(
        `Authentication failed. Expected {"authenticated": true, "redirectUrl": "..."}, server responded with: ${JSON.stringify(authenticationResponse)}`
      );
    }
    return authenticationResponse.redirectUrl;
  }
  async function register(options, label) {
    const decodedExcludeCredentials = !options.excludeCredentials ? [] : options.excludeCredentials.map((cred) => ({
      ...cred,
      id: base64url_default.decode(cred.id)
    }));
    const decodedOptions = {
      ...options,
      user: {
        ...options.user,
        id: base64url_default.decode(options.user.id)
      },
      challenge: base64url_default.decode(options.challenge),
      excludeCredentials: decodedExcludeCredentials
    };
    let credentialsContainer;
    try {
      credentialsContainer = await navigator.credentials.create({
        publicKey: decodedOptions
      });
    } catch (e) {
      throw new Error(`Registration failed. Call to navigator.credentials.create failed: ${e.message}`, { cause: e });
    }
    const { response } = credentialsContainer;
    const credential = {
      id: credentialsContainer.id,
      rawId: base64url_default.encode(credentialsContainer.rawId),
      response: {
        attestationObject: base64url_default.encode(response.attestationObject),
        clientDataJSON: base64url_default.encode(response.clientDataJSON),
        transports: response.getTransports ? response.getTransports() : []
      },
      type: credentialsContainer.type,
      clientExtensionResults: credentialsContainer.getClientExtensionResults(),
      authenticatorAttachment: credentialsContainer.authenticatorAttachment
    };
    const publicKey = {
        credential,
        label
    };
    return publicKey;
  }
  var webauthn_core_default = {
    authenticate,
    register,
    isConditionalMediationAvailable
  };

  // lib/webauthn-login.js
  async function authenticateOrError() {
    const headers = [];
    const contextPath = "";
    const useConditionalMediation = false;
    try {
      const redirectUrl = await webauthn_core_default.authenticate(headers, contextPath, useConditionalMediation);
      window.location.href = redirectUrl;
    } catch (err) {
      console.error(err);
      window.location.href = `${contextPath}/login?error`;
    }
  }

  // lib/webauthn-registration.js
  function setVisibility(element, value) {
    if (!element) {
      return;
    }
    element.style.display = value ? "block" : "none";
  }
  function setError(ui, msg) {
    resetPopups(ui);
    const error = ui.getError();
    if (!error) {
      return;
    }
    error.textContent = msg;
    setVisibility(error, true);
  }
  function setSuccess(ui) {
    resetPopups(ui);
    const success = ui.getSuccess();
    if (!success) {
      return;
    }
    setVisibility(success, true);
  }
  function resetPopups(ui) {
    const success = ui.getSuccess();
    const error = ui.getError();
    setVisibility(success, false);
    setVisibility(error, false);
  }
  async function submitDeleteForm(contextPath, form, headers) {
    const options = {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        ...headers
      }
    };
    await fetch(form.action, options);
  }

  // lib/index.js
  window.authenticateOrError = authenticateOrError;
  window.register = webauthn_core_default.register;
})();
