### oidc-auth

Authentication using an external OpenID Connect provider with the _code_ flow

Here's an example (Google) of how the configuration properties of flow `io.jans.inbound.oidc_code` may look like for actual deployment

```
 {    
      "oidc": {            
          "authzEndpoint": "https://accounts.google.com/o/oauth2/v2/auth",
          "tokenEndpoint": "https://oauth2.googleapis.com/token",
          "userInfoEndpoint": "https://openidconnect.googleapis.com/v1/userinfo",
          "clientId": "--- FILL YOUR VALUE HERE ---",
          "clientSecret": "--- FILL YOUR VALUE HERE ---",
          "scopes": ["openid", "email", "profile"],
          "clientCredsInRequestBody": true,
          "custParamsAuthReq": {},
          "custParamsTokenReq": {}
      },            
      "uidPrefix": "google-"    
}
```

Structure of `oidc` property is described below:


|Name|Description|
|-|-|
|`authzEndpoint`|The authorization endpoint as in section 3.1 of [RFC 7649](https://www.ietf.org/rfc/rfc6749)| 
|`tokenEndpoint`|The token endpoint as in section 3.2 of [RFC 7649](https://www.ietf.org/rfc/rfc6749)|
|`userInfoEndpoint`|The endpoint where profile data can be retrieved. This is not part of the OAuth2 specification|
|`clientId`|The identifier of the client to use, see section 1.1 and 2.2 of [RFC 7649](https://www.ietf.org/rfc/rfc6749). This client is assumed to be *confidential* as in section 2.1|
|`clientSecret`|Secret associated to the client|
|`scopes`|A JSON array of strings that represent the scopes of the access tokens to retrieve|
|`redirectUri`|Redirect URI as in section 3.1.2 of [RFC 7649](https://www.ietf.org/rfc/rfc6749)|
|`clientCredsInRequestBody`|`true` indicates the client authenticates at the token endpoint by including the credentials in the body of the request, otherwise, HTTP Basic authentication is assumed. See section 2.3.1 of [RFC 7649](https://www.ietf.org/rfc/rfc6749)|
|`custParamsAuthReq`|A JSON object (keys and values expected to be strings) with extra parameters to pass to the authorization endpoint if desired|
|`custParamsTokenReq`|A JSON object (keys and values expected to be strings) with extra parameters to pass to the token endpoint if desired|

Often, the first six properties are the only needed.

When generating client ID and secret at the external OP, provide for redirect URI the following: `https://<your-jans-host>/jans-auth/fl/callback`
