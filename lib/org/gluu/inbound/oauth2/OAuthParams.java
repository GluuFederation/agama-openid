package org.gluu.inbound.oauth2;

import java.util.*;

public class OAuthParams extends SimpleOAuthParams implements Serializable {

    private List<String> scopes;

    private String redirectUri;
    
    private boolean clientCredsInRequestBody;
    private Map<String, String> custParamsAuthReq;
    private Map<String, String> custParamsTokenReq;

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public String getRedirectUri() {
        return redirectUri;
    }
    
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
    
    public boolean isClientCredsInRequestBody() {
        return clientCredsInRequestBody;
    }
    
    public void setClientCredsInRequestBody(boolean clientCredsInRequestBody) {
        this.clientCredsInRequestBody = clientCredsInRequestBody;
    }
    
    public Map<String, String> getCustParamsAuthReq() {
        return custParamsAuthReq;
    }
    
    public void setCustParamsAuthReq(Map<String, String> custParamsAuthReq) {
        this.custParamsAuthReq = custParamsAuthReq;
    }
    
    public Map<String, String> getCustParamsTokenReq() {
        return custParamsTokenReq;
    }
    
    public void setCustParamsTokenReq(Map<String, String> custParamsTokenReq) {
        this.custParamsTokenReq = custParamsTokenReq;
    }

    public static OAuthParams update(OAuthParams oap, SimpleOAuthParams sop, String acrValues)
            throws ReflectiveOperationException {

        String s = sop.getAuthzEndpoint();
        if (s != null) {
            oap.setAuthzEndpoint(s);
        }
        s = sop.getTokenEndpoint();
        if (s != null) {
            oap.setTokenEndpoint(s);
        }
        s = sop.getUserInfoEndpoint();
        if (s != null) {
            oap.setUserInfoEndpoint(s);
        }
        s = sop.getClientId();
        if (s != null) {
            oap.setClientId(s);
        }
        s = sop.getClientSecret();
        if (s != null) {
            oap.setClientSecret(s);
        }
        
        if (acrValues != null) {
            Map<String, String> custParams = Optional.ofNullable(oap.getCustParamsAuthReq())
                    .orElse(new HashMap<>());
            //overwrite acrs
            custParams.put("acr_values", acrValues);
            oap.setCustParamsAuthReq(custParams);
        }
        return oap;

    }

}
