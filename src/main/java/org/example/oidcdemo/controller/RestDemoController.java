package org.example.oidcdemo.controller;

import jakarta.servlet.http.HttpSession;
import org.example.oidcdemo.entity.OidcConfig;
import org.example.oidcdemo.entity.ValidAuto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class RestDemoController {

    @Value("${google.auth.url}")
    private String authUrl;

    @Value("${google.token.url}")
    private String tokenUrl;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    private String autoTokenUrl;

    private final WebClient webClient;

    public RestDemoController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @GetMapping("/")
    public String home(Model model) {
        OidcConfig oidcConfig = new OidcConfig();

        model.addAttribute("oidcConfig", oidcConfig);

        return "index";
    }

    public ValidAuto getOpenIdConfiguration(String issuer) {
        Mono<ValidAuto> openIdConfigurationMono = validIssuerUri(issuer);
        return openIdConfigurationMono.block();  // 阻塞并获取结果
    }

    @PostMapping("/authorize")
    public String authorize(OidcConfig oidcConfig, HttpSession session) {
        String id = oidcConfig.getId();
        String password = oidcConfig.getPassword();
        String authorizationRequest = "";

        session.setAttribute("clientId", oidcConfig.getId());
        session.setAttribute("clientSecret", oidcConfig.getPassword());

        if("google".equals(oidcConfig.getType())){
            // https://accounts.google.com/o/oauth2/v2/auth?client_id=860428485553-dmmotrc5ip6pievju5cau7a19l61k72a.apps.googleusercontent.com&redirect_uri=http://localhost:8080/oauth2/callback&response_type=code&scope=openid%20profile%20email
            authorizationRequest = authUrl
                    + "?client_id=" + URLEncoder.encode(id, StandardCharsets.UTF_8)
                    + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                    + "&response_type=code"
                    + "&scope=" + URLEncoder.encode("openid profile email", StandardCharsets.UTF_8);
        }

        if("auto".equals(oidcConfig.getType())){
            ValidAuto openIdConfiguration = getOpenIdConfiguration(oidcConfig.getIssuer());
            validateEndpoints(oidcConfig.getIssuer());
            autoTokenUrl = openIdConfiguration.getTokenEndpoint();
            authorizationRequest = openIdConfiguration.getAuthorizationEndpoint()
                    + "?client_id=" + URLEncoder.encode(id, StandardCharsets.UTF_8)
                    + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                    + "&response_type=code"
                    + "&scope=" + URLEncoder.encode("openid profile email", StandardCharsets.UTF_8);
        }

        return "redirect:" + authorizationRequest;
    }

    private Mono<ValidAuto> validIssuerUri(String issuer) {
        return webClient.get()
                .uri(issuer + "/.well-known/openid-configuration")
                .retrieve()
                .bodyToMono(ValidAuto.class)
                .onErrorResume(e -> Mono.empty());
    }

    public void validateEndpoints(String issuer) {
        ValidAuto openIdConfiguration = getOpenIdConfiguration(issuer);

        // 验证 Authorization Endpoint
        String authorizationEndpoint = openIdConfiguration.getAuthorizationEndpoint();
        System.out.println("Authorization Endpoint: " + authorizationEndpoint);

        // 验证 Token Endpoint
        String tokenEndpoint = openIdConfiguration.getTokenEndpoint();
        System.out.println("Token Endpoint: " + tokenEndpoint);

        // 验证 JWKS URI
        String jwksUri = openIdConfiguration.getJwksUri();
        System.out.println("JWKS URI: " + jwksUri);

        // 进一步验证步骤...
    }

    @GetMapping("/oauth2/callback")
    public String callback(@RequestParam String code, HttpSession session, Model model) {
        String clientId = (String) session.getAttribute("clientId");
        String clientSecret = (String) session.getAttribute("clientSecret");

        validateClientCredentials(clientId, clientSecret, code, model).block();

        return "result";
    }

    public Mono<Object> validateClientCredentials(String clientId, String clientSecret, String authorizationCode, Model model) {
        return webClient
            .post()
            .uri(autoTokenUrl)
            .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
            .body(BodyInserters.fromFormData("code", authorizationCode)
                    .with("redirect_uri", redirectUri)
                    .with("grant_type", "authorization_code"))
            .retrieve()
            .toEntity(String.class)
            .flatMap(response -> {
                if (response.getStatusCode().is2xxSuccessful()) {
                    model.addAttribute("message", "Client ID and Client Secret are valid!");
                } else {
                    model.addAttribute("message", "Invalid Client ID or Client Secret.");
                }
                return Mono.empty();
            })
            .onErrorResume(e -> {
                model.addAttribute("message", "Invalid Client ID or Client Secret.");
                return Mono.empty();
            });
    }


}
