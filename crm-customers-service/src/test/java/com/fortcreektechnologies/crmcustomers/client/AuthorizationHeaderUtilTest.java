package com.fortcreektechnologies.crmcustomers.client;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * Test class for the {@link AuthorizationHeaderUtil} utility class.
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationHeaderUtilTest {

    public static final String VALID_REGISTRATION_ID = "OIDC";
    public static final String SUB_VALUE = "123456";

    @Mock
    private OAuth2AuthorizedClientService clientService;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthorizationHeaderUtil authorizationHeaderUtil;

    @BeforeEach
    void setup() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getAuthorizationHeader_Authentication() {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("principal", "credentials");
        doReturn(authenticationToken).when(securityContext).getAuthentication();

        Optional<String> header = authorizationHeaderUtil.getAuthorizationHeader();

        Assertions.assertThat(header).isNotNull().isEmpty();
    }

    @Test
    void getAuthorizationHeader_JwtAuthentication() {
        JwtAuthenticationToken jwtToken = new JwtAuthenticationToken(
            new Jwt("tokenVal", Instant.now(), Instant.now().plus(Duration.ofMinutes(3)), Map.of("alg", "HS256"), Map.of("sub", SUB_VALUE))
        );
        doReturn(jwtToken).when(securityContext).getAuthentication();

        Optional<String> header = authorizationHeaderUtil.getAuthorizationHeader();

        Assertions.assertThat(header).isNotNull().isNotEmpty().get().isEqualTo("Bearer tokenVal");
    }

    @Test
    void getAuthorizationHeader_OAuth2Authentication_InvalidClient() {
        OAuth2AuthenticationToken oauth2Token = getTestOAuth2AuthenticationToken("INVALID");

        doReturn(oauth2Token).when(securityContext).getAuthentication();

        Assertions.assertThatThrownBy(() -> {
            authorizationHeaderUtil.getAuthorizationHeader();
        })
            .isInstanceOf(OAuth2AuthorizationException.class)
            .hasMessageContaining("[access_denied] The token is expired");
    }

    @Test
    void getAuthorizationHeader_OAuth2Authentication() {
        OAuth2AuthenticationToken oauth2Token = getTestOAuth2AuthenticationToken(VALID_REGISTRATION_ID);
        OAuth2AuthorizedClient authorizedClient = getTestOAuth2AuthorizedClient();

        doReturn(oauth2Token).when(securityContext).getAuthentication();
        doReturn(authorizedClient).when(clientService).loadAuthorizedClient(eq(VALID_REGISTRATION_ID), eq(SUB_VALUE));

        Optional<String> header = authorizationHeaderUtil.getAuthorizationHeader();
        Assertions.assertThat(header).isNotNull().isNotEmpty().get().isEqualTo("Bearer tokenVal");
    }

    @Test
    void getAuthorizationHeader_OAuth2Authentication_RefreshToken() {
        doReturn(restTemplateBuilder)
            .when(restTemplateBuilder)
            .additionalMessageConverters(any(HttpMessageConverter.class), any(HttpMessageConverter.class));
        doReturn(restTemplateBuilder).when(restTemplateBuilder).errorHandler(any(ResponseErrorHandler.class));
        doReturn(restTemplateBuilder).when(restTemplateBuilder).basicAuthentication(anyString(), anyString());

        OAuth2AuthenticationToken oauth2Token = getTestOAuth2AuthenticationToken(VALID_REGISTRATION_ID);
        OAuth2AuthorizedClient authorizedClient = getTestOAuth2AuthorizedClient(true);

        doReturn(oauth2Token).when(securityContext).getAuthentication();
        doReturn(authorizedClient).when(clientService).loadAuthorizedClient(eq(VALID_REGISTRATION_ID), eq(SUB_VALUE));

        RestTemplate restTemplate = mock(RestTemplate.class);
        ResponseEntity<OAuthIdpTokenResponseDTO> refreshResponse = ResponseEntity.of(getTestOAuthIdpTokenResponseDTO(true));
        doReturn(refreshResponse).when(restTemplate).exchange(any(RequestEntity.class), eq(OAuthIdpTokenResponseDTO.class));
        doReturn(restTemplate).when(restTemplateBuilder).build();

        Optional<String> header = authorizationHeaderUtil.getAuthorizationHeader();
        Assertions.assertThat(header).isNotNull().isNotEmpty().get().isEqualTo("Bearer tokenVal");
    }

    @Test
    void getAuthorizationHeader_OAuth2Authentication_RefreshToken_NoRefreshToken() {
        doReturn(restTemplateBuilder)
            .when(restTemplateBuilder)
            .additionalMessageConverters(any(HttpMessageConverter.class), any(HttpMessageConverter.class));
        doReturn(restTemplateBuilder).when(restTemplateBuilder).errorHandler(any(ResponseErrorHandler.class));
        doReturn(restTemplateBuilder).when(restTemplateBuilder).basicAuthentication(anyString(), anyString());

        OAuth2AuthenticationToken oauth2Token = getTestOAuth2AuthenticationToken(VALID_REGISTRATION_ID);
        OAuth2AuthorizedClient authorizedClient = getTestOAuth2AuthorizedClient(true);

        doReturn(oauth2Token).when(securityContext).getAuthentication();
        doReturn(authorizedClient).when(clientService).loadAuthorizedClient(eq(VALID_REGISTRATION_ID), eq(SUB_VALUE));

        RestTemplate restTemplate = mock(RestTemplate.class);
        ResponseEntity<OAuthIdpTokenResponseDTO> refreshResponse = ResponseEntity.of(getTestOAuthIdpTokenResponseDTO(false));
        doReturn(refreshResponse).when(restTemplate).exchange(any(RequestEntity.class), eq(OAuthIdpTokenResponseDTO.class));
        doReturn(restTemplate).when(restTemplateBuilder).build();

        Optional<String> header = authorizationHeaderUtil.getAuthorizationHeader();
        Assertions.assertThat(header).isNotNull().isNotEmpty().get().isEqualTo("Bearer tokenVal");
    }

    @Test
    void getAuthorizationHeader_OAuth2Authentication_RefreshTokenFails() {
        doReturn(restTemplateBuilder)
            .when(restTemplateBuilder)
            .additionalMessageConverters(any(HttpMessageConverter.class), any(HttpMessageConverter.class));
        doReturn(restTemplateBuilder).when(restTemplateBuilder).errorHandler(any(ResponseErrorHandler.class));
        doReturn(restTemplateBuilder).when(restTemplateBuilder).basicAuthentication(anyString(), anyString());

        OAuth2AuthenticationToken oauth2Token = getTestOAuth2AuthenticationToken(VALID_REGISTRATION_ID);
        OAuth2AuthorizedClient authorizedClient = getTestOAuth2AuthorizedClient(true);

        doReturn(oauth2Token).when(securityContext).getAuthentication();
        doReturn(authorizedClient).when(clientService).loadAuthorizedClient(eq(VALID_REGISTRATION_ID), eq(SUB_VALUE));

        RestTemplate restTemplate = mock(RestTemplate.class);
        doThrow(new OAuth2AuthorizationException(new OAuth2Error("E"), "error"))
            .when(restTemplate)
            .exchange(any(RequestEntity.class), eq(OAuthIdpTokenResponseDTO.class));
        doReturn(restTemplate).when(restTemplateBuilder).build();

        Assertions.assertThatThrownBy(() -> {
            authorizationHeaderUtil.getAuthorizationHeader();
        })
            .isInstanceOf(OAuth2AuthenticationException.class)
            .hasMessageContaining("error");
    }

    private OAuth2AuthorizedClient getTestOAuth2AuthorizedClient() {
        return getTestOAuth2AuthorizedClient(false);
    }

    private OAuth2AuthorizedClient getTestOAuth2AuthorizedClient(boolean accessTokenExpired) {
        Instant issuedAt = Instant.now();
        Instant expiresAt;
        if (accessTokenExpired) {
            expiresAt = issuedAt.plus(Duration.ofNanos(1));
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                fail("Error in Thread.sleep(1) : " + e.getMessage());
            }
        } else {
            expiresAt = issuedAt.plus(Duration.ofMinutes(3));
        }
        OAuth2AccessToken token = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "tokenVal", issuedAt, expiresAt);

        return new OAuth2AuthorizedClient(
            ClientRegistration.withRegistrationId(VALID_REGISTRATION_ID)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientId("web-app")
                .clientSecret("secret")
                .redirectUri("/login/oauth2/code/oidc")
                .authorizationUri("http://localhost:8080/auth/realms/master/protocol/openid-connect/auth")
                .tokenUri("https://localhost:8080/auth/realms/master/protocol/openid-connect/token")
                .build(),
            "sub",
            token,
            new OAuth2RefreshToken("refreshVal", Instant.now())
        );
    }

    private OAuth2AuthenticationToken getTestOAuth2AuthenticationToken(String registrationId) {
        return new OAuth2AuthenticationToken(
            new DefaultOidcUser(
                List.of(new SimpleGrantedAuthority("USER")),
                OidcIdToken.withTokenValue("tokenVal").claim("sub", SUB_VALUE).build()
            ),
            List.of(new SimpleGrantedAuthority("USER")),
            registrationId
        );
    }

    private Optional<OAuthIdpTokenResponseDTO> getTestOAuthIdpTokenResponseDTO(boolean hasRefreshToken) {
        OAuthIdpTokenResponseDTO dto = new OAuthIdpTokenResponseDTO();
        dto.setAccessToken("tokenVal");
        dto.setIdToken("tokenVal");
        dto.setNotBefore(0l);
        dto.setRefreshExpiresIn("1800");
        dto.setSessionState("ccea4a55");
        dto.setExpiresIn(300l);
        dto.setRefreshToken(hasRefreshToken ? "tokenVal" : null);
        dto.setScope("openid email profile offline_access");
        return Optional.of(dto);
    }
}
