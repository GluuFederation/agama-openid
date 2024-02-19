# Agama OpenID Connect Project

Use this project to delegate authentication to an external OpenID Connect provider (OP) using the *authorization code flow*.

## Anatomy

The project consists of three flows that provide incremental functionality:

- `org.gluu.inbound.oauth2.AuthzCode`: With this flow the user's browser is redirected to the authorization page of an external OP (the specifics are passed in the input parameters). Authentication takes place there and subsequently an access token is obtained and returned to the caller of the flow

- `org.gluu.inbound.oauth2.AuthzCodeWithUserInfo`: This flow launches `AuthzCode` and then obtains the profile data of the authenticated user by presenting an access token. Both the token and profile data are returned to the caller

- `org.gluu.inbound.openid`: This flow launches `AuthzCodeWithUserInfo` and inserts an entry in the local Jans database for the user in question. Depending on how the flow is parameterized, this flow can perform a preliminar OpenID client registration 

## `openid` flow

Most of times, this is the flow that developers will want to reuse in their projects. It receives two input parameters:

- `opSettings`. An Agama map that specify the settings to be able to interact with the external OP
- `uidPrefix`. A string value used for user provisioning: the user inserted in local DB will have an `uid` equal to the concatenation of `uidPrefix` and the `sub` released by the external OP. This param can be omitted or set to `null` if no prefixing is desired 

### OP settings

The structure of `opSettings` is as follows:

|Name|Description|Notes|
|-|-|-|
|`host`|Location of the identity provider, eg. `https://my.idp.co`|Required if DCR is enabled, see below|
|`dcr`|The `openid` flow can make use of Dynamic Client Registration (DCR) - a feature some OPs provide|Required|
|`oauth`|A map following the same structure of [oauthParams](#authzcodewithuserinfo-and-authzcode)||
|`acrValues`|A string supplying _Authentication Context Class Reference_ values|Optional|

Regarding oauth map, **not all fields** marked as required are necessary when DCR is enabled. It suffices to supply `scopes`.

Here is a minimalistic value that can be supplied for `opSettings` when DCR is supported by the external OP:

```
{
    host: "https://my.idp.co", 
    dcr: { enabled: true, useCachedClient: true },
    oauth: { scopes: [ "openid" ] } 
}
```

### DCR settings

The structure of `dcr` is as follows:

|Name|Description|Notes|
|-|-|-|
|`enabled`|A boolean value indicating if DCR will be used for the external OP|Required<!--Optional. `false` value assumed if missing-->|
|`useCachedClient`|Once the first client registration takes place, no more registration attempts will be made until the client is about to expire. Set this to `false` to force registration every time `openid` flow is launched|Required|


## `AuthzCodeWithUserInfo` and `AuthzCode`

Each of these flows receive an input parameter (`oauthParams`) to drive their behavior. `oauthParams` is expected to be an Agama map with the following structure:

|Name|Description|Notes|
|-|-|-|
|`authzEndpoint`|The authorization endpoint as in section 3.1 of [RFC 7649](https://www.ietf.org/rfc/rfc6749)|Required| 
|`tokenEndpoint`|The token endpoint as in section 3.2 of [RFC 7649](https://www.ietf.org/rfc/rfc6749)|Required|
|`userInfoEndpoint`|The endpoint where profile data can be retrieved. This is not part of the OAuth2 specification|Optional|
|`clientId`|The identifier of the client to use, see section 1.1 and 2.2 of [RFC7649](https://www.ietf.org/rfc/rfc6749). This client is assumed to be *confidential* as in section 2.1|Required|
|`clientSecret`|Secret associated to the client|Required|
|`scopes`|An array of strings that represent the scopes of the access tokens to retrieve|Required|
|`redirectUri`|Redirect URI as in section 3.1.2 of [RFC 7649](https://www.ietf.org/rfc/rfc6749)|Optional (auto filled when missing)|
|`clientCredsInRequestBody`|`true` indicates the client authenticates at the token endpoint by including the credentials in the body of the request, otherwise, HTTP Basic authentication is assumed. See section 2.3.1 of [RFC 7649](https://www.ietf.org/rfc/rfc6749)|Optional. `false` is assumed if not supplied|
|`custParamsAuthReq`|An Agama map (keys and values expected to be strings) with extra parameters to pass to the authorization endpoint if desired|Optional|
|`custParamsTokenReq`|An Agama map (keys and values expected to be strings) with extra parameters to pass to the token endpoint if desired|Optional|

## FAQ


### I don't use DCR and I am asked to provide a redirect URI in order to get a client ID/Secret

Supply the following: `https://<jans-server-host-name>/jans-auth/fl/callback`

### What methods for token endpoint authentication are supported?

Only `client_secret_basic` and `client_secret_post` are supported

