# Agama OpenID Connect Project

Use this project to delegate authentication to an external OAuth 2.0 or OpenID Connect provider (OP) using the *authorization code flow*.

## Anatomy

The project consists of three flows that provide incremental functionality:

- `org.gluu.inbound.oauth2.AuthzCode`: With this flow the user's browser is redirected to the authorization page of an external OP (the specifics are passed in the input parameters). Authentication takes place there and subsequently an access token is obtained and returned to the caller of the flow

- `org.gluu.inbound.oauth2.AuthzCodeWithUserInfo`: This flow launches `AuthzCode` and then obtains the profile data of the authenticated user by presenting an access token. Both the token and profile data are returned to the caller

- `org.gluu.inbound.openid`: This flow displays an identity provider selection page and launches `AuthzCodeWithUserInfo`. Once the browser returns back from the external site, an entry in the local Jans database for the user in question is inserted. The list of supported providers and their associated settings are supplied through the project configuration. This is data supplied post-deployment

## Configuration properties

This project must be configured using a JSON document that follows the structure below:

```
{
    "org.gluu.inbound.openid": {
       "OP1_ID": { configs for identity provider 1 },
       "OP2_ID": { configs for identity provider 2 },
       ...
    }
}
```

The configuration for a given OP is as follows:

|Name|Description|Notes|
|-|-|-|
|`host`|Location of the identity provider, eg. `https://my.idp.co`|Required if DCR is enabled|
|`dcr`|A JSON object configuring _Dynamic Client Registration_ (DCR) - see [below](#dcr-settings)|Required|
|`oauth`|A JSON object following the structure referenced [here](#authzcodewithuserinfo-and-authzcode)||
|`acrValues`|A string supplying _Authentication Context Class Reference_ values|Optional|
|`uidPrefix`|A string value used for user provisioning: the user inserted in local DB will have an `uid` equal to the concatenation of `uidPrefix` and the `sub` released by the external OP.|Optional|

Regarding the `oauth` sectino, **not all fields** marked as required are necessary when DCR is used. It suffices to supply `scopes`.
 
### Example


Here is an example that configures an OP with DCR and an OAuth 2.0 provider:

```
{
    "org.gluu.inbound.openid":{
        "Gluu": {
            "host": "https://my.gluu.co", 
            "dcr": { 
                "enabled": true,
                "useCachedClient": true 
            },
            "oauth": { 
                "scopes": [ "openid" ] 
            },
            "uidPrefix": "gluu-"
        },
        "Github": {
            "oauth": {
                "authzEndpoint": "https://github.com/login/oauth/authorize",
                "tokenEndpoint": "https://github.com/login/oauth/access_token",
                "userInfoEndpoint": "https://api.github.com/user",
                "clientId": "mangled",
                "clientSecret": "twisted",
                "scopes": [ "user" ]
            },
            "uidPrefix": "github-"
        }    
    }
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
