package org.gluu.inbound.oauth2;

import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.*;
import com.nimbusds.oauth2.sdk.client.*;
import com.nimbusds.oauth2.sdk.http.*;
import com.nimbusds.oauth2.sdk.id.*;
import com.nimbusds.oauth2.sdk.token.*;
import com.nimbusds.openid.connect.sdk.rp.*;
import com.nimbusds.openid.connect.sdk.op.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jans.agama.model.Flow;
import io.jans.service.CacheService;
import io.jans.service.cdi.util.CdiUtil;

import net.minidev.json.*;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DCRUtil {
    
    private static final String KEY_PREFIX = "agama-openid-";
    
    private static Logger LOG = LoggerFactory.getLogger(Flow.class);
    private static ObjectMapper mapper = new ObjectMapper();
    
    public static SimpleOAuthParams registerClient(String opHost, boolean useCachedClient,
                String redirectUri, List<String> scopes) throws Exception {
        
        SimpleOAuthParams sop = null;
        String key = KEY_PREFIX + opHost;
        CacheService cache = CdiUtil.bean(CacheService.class);
        
        if (useCachedClient) {
            //Check if key is in cache
            String sopStr = Optional.ofNullable(cache.get(key)).map(Object::toString).orElse(null);
            if (sopStr != null) {
                try {
                    LOG.info("Parsing SimpleOAuthParams instance from cache...");
                    sop = mapper.readValue(sopStr, SimpleOAuthParams.class);
                    LOG.info("Client ID is {}", sop.getClientId());
                    //LOG.info(sopStr);
                } catch (Exception e) {
                    LOG.error(e.getMessage());
                    LOG.info("Removing entry from cache");
                    cache.remove(key);
                }
                return sop;
            }
        }
        
        LOG.info("Issuing a configuration request to OP {}", opHost);
        
        OIDCProviderMetadata opMetadata = OIDCProviderMetadata.resolve(new Issuer(opHost), 3000, 3000);
        URI registrationURI = opMetadata.getRegistrationEndpointURI();
        
        if (registrationURI == null) {
            LOG.error("Unable to determine client registration endpoint URI. " + 
                    "OP does not support dynamic client registration?");
            return null;
        }

        sop = new SimpleOAuthParams();
        sop.setAuthzEndpoint(opMetadata.getAuthorizationEndpointURI().toString());
        sop.setTokenEndpoint(opMetadata.getTokenEndpointURI().toString());
        sop.setUserInfoEndpoint(opMetadata.getUserInfoEndpointURI().toString());
        
        LOG.info("Sending a client registration request to {}", registrationURI);

        OIDCClientMetadata clMetadata = makeClientMetadata(redirectUri, scopes);
        OIDCClientRegistrationRequest regRequest = new OIDCClientRegistrationRequest(
                registrationURI, clMetadata, null);
        
        ClientRegistrationResponse response = OIDCClientRegistrationResponseParser.parse(
                getRegistrationResponse(regRequest));

        if (!response.indicatesSuccess()) {            
            throw CodeGrantUtil.exFromError(
                        ClientRegistrationErrorResponse.class.cast(response).getErrorObject());
        }
        
        OIDCClientInformation clientInfo = OIDCClientInformationResponse.class.cast(response)
                .getOIDCClientInformation();
        checkScopes(clientInfo.getOIDCMetadata(), scopes);

        String clId = clientInfo.getID();
        Secret secret = clientInfo.getSecret();
        Date expd = secret.getExpirationDate();
        boolean notExpiring = expd == null; 
        
        LOG.debug("Client ID is {}. Expiring {}", clId, notExpiring ? "NEVER" : expd);
        sop.setClientId(clId);
        sop.setClientSecret(secret.getValue());
        
        long expMinutes;
        
        if (notExpiring) {
            expMinutes = Integer.MAX_VALUE;
        } else {
            expMinutes = (expd.getTime() - System.currentTimeMillis()) / 1000L;
        }
        
        try {
            LOG.info("Writing SimpleOAuthParams instance to cache...");
            cache.put(expMinutes.intValue() - 1, key, mapper.writeValueAsString(sop));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return sop;
        
    }
    
    private static getRegistrationResponse(OIDCClientRegistrationRequest request) throws Exception {

        HTTPResponse response = request.toHTTPRequest().send();
        //"fix" apparently Jans non-compliance, see jans#7581
        String property = "backchannel_logout_uri";
        JSONObject json = response.getBodyAsJSONObject();
        Object blu = json.get(property);
        boolean nullify = blu != null;
        
        if (!nullify) return response;
        
        if (JSONArray.class.isInstance(blu)) {
            JSONArray list = (JSONArray) blu;
            
            if (!list.isEmpty()) {
                Object first = list.get(0);
            
                if (String.class.isInstance(first)) {
                    nullify = false;
                    LOG.debug("Setting {} to {}", property, first.toString());
                    json.put(property, first.toString());
                }
            }
        }
        if (nullify) {
            LOG.debug("Nullifying {}", property);
            json.put(property, null);
        }
        //Update body of response        
        response.setBody(json.toString());
        
        return response;
        
    }    
    
    private static OIDCClientMetadata makeClientMetadata(String redirectUri,
                List<String> scopes) throws Exception {
    
        LOG.debug("Building client metadata");        
        OIDCClientMetadata clientMetadata = new OIDCClientMetadata();
        
        //See https://javadoc.io/static/com.nimbusds/oauth2-oidc-sdk/11.7/com/nimbusds/openid/connect/sdk/rp/OIDCClientMetadata.html#applyDefaults()
        clientMetadata.applyDefaults();
        clientMetadata.setResponseTypes(Collections.singleton(ResponseType.CODE));
        clientMetadata.setScope(new Scope(scopes.toArray(new String[0])));
        clientMetadata.setRedirectionURI(URI.create(redirectUri));
        clientMetadata.setName(KEY_PREFIX + System.currentTimeMillis());
        
        return clientMetadata;
        
    }
    
    private static void checkScopes(ClientMetadata clientMetadata, List<String> originalScopes) {
        
        Set<String> scopes = clientMetadata.getScope().toStringList().stream().collect(Collectors.toSet());
        Set<String> originalScopesSet = originalScopes.stream().collect(Collectors.toSet());
        if (!scopes.equals(originalScopesSet)) {
            LOG.warn("Scopes differ!. Original: {}; scopes now: {}", originalScopesSet, scopes);
        }
        
    }
    
}
