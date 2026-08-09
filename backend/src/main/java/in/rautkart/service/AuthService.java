package in.rautkart.service;

import in.rautkart.dto.AuthDtos;
import in.rautkart.entity.Role;
import in.rautkart.entity.User;
import in.rautkart.exception.ApiException;
import in.rautkart.repository.UserRepository;
import in.rautkart.security.AuthUser;
import in.rautkart.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw ApiException.conflict("An account with that email already exists");
        }

        String phone = request.phone() == null || request.phone().isBlank() ? null : request.phone();

        User user = User.builder()
                .name(request.name().trim())
                .email(request.email().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .phone(phone)
                .role(Role.CUSTOMER)
                .build();

        userRepository.save(user);

        AuthUser principal = new AuthUser(user);
        return new AuthDtos.AuthResponse(jwtService.generateToken(principal), Mappers.toUser(user));
    }

    /**
     * Shared by the customer and admin login endpoints. The admin endpoint sets
     * requireAdmin so a customer token can never be minted from /admin/login.
     */
    @Transactional(readOnly = true)
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request, boolean requireAdmin) {
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.email().trim().toLowerCase(), request.password()));
        } catch (BadCredentialsException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        AuthUser principal = (AuthUser) auth.getPrincipal();
        if (requireAdmin && !Role.ADMIN.name().equals(principal.getRole())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This login is for store admins only");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> ApiException.notFound("User"));

        return new AuthDtos.AuthResponse(jwtService.generateToken(principal), Mappers.toUser(user));
    }

    @Transactional(readOnly = true)
    public AuthDtos.UserResponse currentUser(Long userId) {
        return userRepository.findById(userId)
                .map(Mappers::toUser)
                .orElseThrow(() -> ApiException.notFound("User"));
    }
}
