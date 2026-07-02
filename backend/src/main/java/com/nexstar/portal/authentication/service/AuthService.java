package com.nexstar.portal.authentication.service;

import com.nexstar.portal.authentication.dto.*;
import com.nexstar.portal.authentication.entity.*;
import com.nexstar.portal.authentication.repository.*;
import com.nexstar.portal.common.exception.BusinessException;
import com.nexstar.portal.common.exception.ResourceNotFoundException;
import com.nexstar.portal.config.AppProperties;
import com.nexstar.portal.security.JwtTokenProvider;
import com.nexstar.portal.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AppProperties appProperties;

    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByIdAndDeletedFalse(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        // Revoke old refresh tokens and generate new ones
        refreshTokenRepository.revokeAllUserTokens(user);
        String accessToken = tokenProvider.generateAccessToken(userPrincipal);
        String refreshTokenValue = tokenProvider.generateRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(
                        appProperties.getJwt().getRefreshTokenExpirationMs() / 1000))
                .userAgent(httpRequest.getHeader("User-Agent"))
                .ipAddress(getClientIp(httpRequest))
                .build();
        refreshTokenRepository.save(refreshToken);

        Set<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .collect(Collectors.toSet());

        Set<String> permissions = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> !a.startsWith("ROLE_"))
                .collect(Collectors.toSet());

        return LoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profilePictureUrl(user.getProfilePictureUrl())
                .roles(roles)
                .permissions(permissions)
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .accessTokenExpiresIn(appProperties.getJwt().getAccessTokenExpirationMs())
                .tokenType("Bearer")
                .build();
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new BusinessException("Email is already registered");
        }

        Role employeeRole = roleRepository.findByNameAndDeletedFalse("EMPLOYEE")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "EMPLOYEE"));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .emailVerified(false)
                .active(true)
                .build();
        user.getRoles().add(employeeRole);
        userRepository.save(user);

        // Send verification email
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(appProperties.getEmailVerification().getTokenExpiryHours()))
                .build();
        emailVerificationTokenRepository.save(verificationToken);
        emailService.sendEmailVerification(user.getEmail(), user.getFirstName(), token);
    }

    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException("Invalid refresh token", HttpStatus.UNAUTHORIZED));

        if (!refreshToken.isValid()) {
            throw new BusinessException("Refresh token has expired or been revoked", HttpStatus.UNAUTHORIZED);
        }

        User user = refreshToken.getUser();
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        UserPrincipal userPrincipal = new UserPrincipal(user);
        String newAccessToken = tokenProvider.generateAccessToken(userPrincipal);
        String newRefreshTokenValue = tokenProvider.generateRefreshToken();

        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .token(newRefreshTokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(
                        appProperties.getJwt().getRefreshTokenExpirationMs() / 1000))
                .userAgent(httpRequest.getHeader("User-Agent"))
                .ipAddress(getClientIp(httpRequest))
                .build();
        refreshTokenRepository.save(newRefreshToken);

        Set<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .collect(Collectors.toSet());

        Set<String> permissions = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> !a.startsWith("ROLE_"))
                .collect(Collectors.toSet());

        return LoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profilePictureUrl(user.getProfilePictureUrl())
                .roles(roles)
                .permissions(permissions)
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenValue)
                .accessTokenExpiresIn(appProperties.getJwt().getAccessTokenExpirationMs())
                .tokenType("Bearer")
                .build();
    }

    @Transactional
    public void logout(String refreshTokenValue, UserPrincipal currentUser) {
        User user = userRepository.findByIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));
        refreshTokenRepository.revokeAllUserTokens(user);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailAndDeletedFalse(request.getEmail()).ifPresent(user -> {
            passwordResetTokenRepository.deleteAllByUser(user);
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusMinutes(appProperties.getPasswordReset().getTokenExpiryMinutes()))
                    .build();
            passwordResetTokenRepository.save(resetToken);
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), token);
        });
        // Always return success to prevent email enumeration
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BusinessException("Invalid or expired reset token"));

        if (resetToken.isExpired() || resetToken.isUsed()) {
            throw new BusinessException("Reset token has expired or already been used");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Revoke all sessions after password reset
        refreshTokenRepository.revokeAllUserTokens(user);
    }

    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Invalid verification token"));

        if (verificationToken.isExpired() || verificationToken.isUsed()) {
            throw new BusinessException("Verification token has expired or already been used");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        emailVerificationTokenRepository.save(verificationToken);

        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, UserPrincipal currentUser) {
        User user = userRepository.findByIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        refreshTokenRepository.revokeAllUserTokens(user);
    }

    @Transactional
    public void resendVerification(String email) {
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (user.isEmailVerified()) {
            throw new BusinessException("Email is already verified");
        }

        emailVerificationTokenRepository.deleteAllByUser(user);
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(appProperties.getEmailVerification().getTokenExpiryHours()))
                .build();
        emailVerificationTokenRepository.save(verificationToken);
        emailService.sendEmailVerification(user.getEmail(), user.getFirstName(), token);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
