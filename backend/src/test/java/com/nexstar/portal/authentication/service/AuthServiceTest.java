package com.nexstar.portal.authentication.service;

import com.nexstar.portal.authentication.dto.LoginRequest;
import com.nexstar.portal.authentication.dto.LoginResponse;
import com.nexstar.portal.authentication.dto.RefreshTokenRequest;
import com.nexstar.portal.authentication.dto.RegisterRequest;
import com.nexstar.portal.authentication.entity.RefreshToken;
import com.nexstar.portal.authentication.entity.Role;
import com.nexstar.portal.authentication.entity.User;
import com.nexstar.portal.authentication.repository.EmailVerificationTokenRepository;
import com.nexstar.portal.authentication.repository.PasswordResetTokenRepository;
import com.nexstar.portal.authentication.repository.RefreshTokenRepository;
import com.nexstar.portal.authentication.repository.RoleRepository;
import com.nexstar.portal.authentication.repository.UserRepository;
import com.nexstar.portal.common.exception.BusinessException;
import com.nexstar.portal.config.AppProperties;
import com.nexstar.portal.security.JwtTokenProvider;
import com.nexstar.portal.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private AuthService authService;

    private AppProperties.Jwt jwtProperties;
    private AppProperties.EmailVerification emailVerificationProperties;
    private HttpServletRequest httpRequest;

    @BeforeEach
    void setUp() {
        jwtProperties = new AppProperties.Jwt();
        jwtProperties.setAccessTokenExpirationMs(900000L);
        jwtProperties.setRefreshTokenExpirationMs(604800000L);

        emailVerificationProperties = new AppProperties.EmailVerification();
        emailVerificationProperties.setTokenExpiryHours(24);

        httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getHeader("User-Agent")).thenReturn("test-agent");
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);

        lenient().when(appProperties.getJwt()).thenReturn(jwtProperties);
        lenient().when(appProperties.getEmailVerification()).thenReturn(emailVerificationProperties);
    }

    @Test
    void login_withValidCredentials_returnsLoginResponse() {
        // Arrange
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        String email = "test@test.com";

        UserPrincipal userPrincipal = mock(UserPrincipal.class);
        when(userPrincipal.getId()).thenReturn(userId);
        when(userPrincipal.getAuthorities()).thenReturn(Collections.emptyList());

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        User user = User.builder()
                .email(email)
                .firstName("Test")
                .lastName("User")
                .password("encoded-pass")
                .active(true)
                .emailVerified(true)
                .build();

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(refreshTokenRepository).revokeAllUserTokens(any(User.class));

        when(tokenProvider.generateAccessToken(any(UserPrincipal.class))).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken()).thenReturn("refresh-token");

        RefreshToken savedToken = RefreshToken.builder()
                .user(user)
                .token("refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(savedToken);

        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword("pass");

        // Act
        LoginResponse result = authService.login(request, httpRequest);

        // Assert
        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getEmail()).isEqualTo(email);
    }

    @Test
    void login_withInvalidCredentials_throwsException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        LoginRequest request = new LoginRequest();
        request.setEmail("wrong@test.com");
        request.setPassword("wrongpass");

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request, httpRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refreshToken_withValidToken_returnsNewTokenPair() {
        // Arrange
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000002");

        User user = User.builder()
                .email("test@test.com")
                .firstName("Test")
                .lastName("User")
                .password("encoded-pass")
                .active(true)
                .emailVerified(true)
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token("old-refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("old-refresh-token")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tokenProvider.generateAccessToken(any(UserPrincipal.class))).thenReturn("new-access");
        when(tokenProvider.generateRefreshToken()).thenReturn("new-refresh");

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("old-refresh-token");

        // Act
        LoginResponse result = authService.refreshToken(request, httpRequest);

        // Assert
        assertThat(result.getAccessToken()).isEqualTo("new-access");
        assertThat(result.getRefreshToken()).isEqualTo("new-refresh");
    }

    @Test
    void refreshToken_withExpiredToken_throwsException() {
        // Arrange
        User user = User.builder()
                .email("test@test.com")
                .password("encoded-pass")
                .active(true)
                .build();

        // Expired token (expiresAt in the past)
        RefreshToken expiredToken = RefreshToken.builder()
                .user(user)
                .token("expired-token")
                .expiresAt(LocalDateTime.now().minusDays(1))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("expired-token");

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(request, httpRequest))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void register_withExistingEmail_throwsException() {
        // Arrange
        when(userRepository.existsByEmailAndDeletedFalse("existing@test.com")).thenReturn(true);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@test.com");
        request.setPassword("Password1!");
        request.setFirstName("Test");
        request.setLastName("User");

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email is already registered");
    }
}
