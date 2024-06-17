package org.example.oidcdemo.config;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Configuration
public class Oauth2LoginConfig {

    @Bean
    public OAuth2ClientProperties config2prop(){
        OAuth2ClientProperties properties = new OAuth2ClientProperties();
        OAuth2ClientProperties.Provider provider = new OAuth2ClientProperties.Provider();
        OAuth2ClientProperties.Registration registration = new OAuth2ClientProperties.Registration();
        properties.getProvider().put("client", provider);
        properties.getRegistration().put("client", registration);

//        provider.setAuthorizationUri("https://accounts.google.com/o/oauth2/v2/auth");
//        provider.setTokenUri("https://oauth2.googleapis.com/token");
//        provider.setUserInfoUri("https://openidconnect.googleapis.com/v1/userinfo");
//        provider.setJwkSetUri("https://www.googleapis.com/oauth2/v3/certs");

        provider.setIssuerUri("https://accounts.google.com");
        provider.setUserNameAttribute("sub");


        registration.setClientId("860428485553-dmmotrc5ip6pievju5cau7a19l61k72a.apps.googleusercontent.com");
        registration.setClientSecret("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");
        registration.setAuthorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE.getValue());
        registration.setClientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue());
        registration.setScope(new HashSet<>(Arrays.asList("openid", "profile", "email")));

//        registration.setRedirectUri("{baseUrl}/login/oauth2/code/{registrationId}");
        return properties;
    }


}
