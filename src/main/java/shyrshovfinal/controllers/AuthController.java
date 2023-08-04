package shyrshovfinal.controllers;

import shyrshovfinal.jwt.JwtUtils;
import shyrshovfinal.models.*;
import shyrshovfinal.pojo.JwtResponse;
import shyrshovfinal.pojo.LoginRequest;
import shyrshovfinal.repository.AccessTokenRepository;
import shyrshovfinal.repository.ProfileRepository;
import shyrshovfinal.repository.RefreshTokenRepository;
import shyrshovfinal.repository.UserRepository;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Date;

@Api(value = "Reg && auth controller")
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;


    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    ProfileRepository profileRepository;
    @Autowired
    AccessTokenRepository accessTokenRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;


    @Operation(summary = "Authenticate user",
            operationId = "authUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = JwtResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - user not found or blocked"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })

    @PostMapping("/signin")
    public ResponseEntity<?> authUser(@RequestBody LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        boolean isEmailValid = EmailValidator.getInstance().isValid(email); // проверяем валидность почты
        if (!isEmailValid) {
            return ResponseEntity.badRequest().body("Invalid email or password"); // возвращаем ошибку, если почта неверна
        }
        User byEmail = userRepository.findByEmail(email).orElseThrow(SecurityException::new);

        if (byEmail.isIs_blocked()){
            User byEmail1 = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(SecurityException::new);
            return new ResponseEntity<>(byEmail1,HttpStatus.OK);
        }
        if (StringUtils.isBlank(password)) {
            return ResponseEntity.badRequest().body("password can`t be null");
        }


        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

            user.setLast_login(new Date());
            user.setIs_blocked(false);
            userRepository.save(user);
            System.out.println("text");
            String accessTokenJwt = jwtUtils.generateAccessToken(user);
            String refreshTokenJwt = jwtUtils.generateRefreshToken(user);

            AccessToken accessToken = accessTokenRepository.findByUserAndExpiresAtAfter(user, LocalDateTime.now());
            RefreshToken refreshToken = refreshTokenRepository.findByUserAndExpiresAtAfter(user, LocalDateTime.now());


            if (accessToken == null && refreshToken != null) {
                String newAccessTokenJwt = jwtUtils.generateAccessToken(user);
                AccessToken newAccessToken = new AccessToken();
                newAccessToken.setToken(newAccessTokenJwt);
                newAccessToken.setUser(user);
                newAccessToken.setCreatedAt(LocalDateTime.now());
                newAccessToken.setExpiresAt(LocalDateTime.now().plusMinutes(10));
                accessTokenRepository.save(newAccessToken);
                return ResponseEntity.ok(createJwtResponse(user, newAccessTokenJwt, refreshToken.getToken()));
            }
            if (accessToken != null && refreshToken !=null){
                JwtResponse jwtResponse = createJwtResponse(user, accessToken.getToken(), refreshToken.getToken());
                return ResponseEntity.ok(jwtResponse);
            }
            if (accessToken!=null && refreshToken == null) {
                String newRefreshTokenJwt = jwtUtils.generateRefreshToken(user);
                RefreshToken newRefreshToken = new RefreshToken();
                newRefreshToken.setToken(newRefreshTokenJwt);
                newRefreshToken.setUser(user);
                newRefreshToken.setCreatedAt(LocalDateTime.now());
                newRefreshToken.setExpiresAt(LocalDateTime.now().plusMinutes(20));
                refreshTokenRepository.save(newRefreshToken);
                return ResponseEntity.ok(createJwtResponse(user, accessToken.getToken(), newRefreshTokenJwt));
            }
            if (accessToken == null && refreshToken == null) {
                AccessToken newAccessToken = createAccessToken(user, accessTokenJwt);
                RefreshToken newRefreshToken = createRefreshToken(user, refreshTokenJwt);
                JwtResponse jwtResponse = createJwtResponse(user, newAccessToken.getToken(), newRefreshToken.getToken());
                return ResponseEntity.ok(jwtResponse);
            }
            if (refreshToken != null) {
                AccessToken newAccessToken = createAccessToken(user, accessTokenJwt);
                refreshToken.setToken(refreshTokenJwt);
                refreshToken.setCreatedAt(LocalDateTime.now());
                refreshToken.setExpiresAt(LocalDateTime.now().plusMinutes(20));
                refreshTokenRepository.save(refreshToken);
                JwtResponse jwtResponse = createJwtResponse(user, newAccessToken.getToken(), refreshTokenJwt);
                return ResponseEntity.ok(jwtResponse);
            }


            throw new RuntimeException("User not found");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body("User not found");
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body("Email or password incorrect");
        } catch (LockedException e) {
            return ResponseEntity.badRequest().body("User blocked");
        } catch (DisabledException e) {
            return ResponseEntity.badRequest().body(null); // Ошибка: пользователь отключен
        }
    }

    @Operation(summary = "give new token",
            operationId = "newtoken",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = JwtResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request")
            })
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody TokenWrapper tokenWrapper) {
        String refreshToken = tokenWrapper.getToken();
        RefreshToken byToken = refreshTokenRepository.findByToken(refreshToken);
        if (byToken == null) {
            return new ResponseEntity<>("Invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        User user = byToken.getUser();
        String accessTokenJwt = jwtUtils.generateAccessToken(user);
        String refreshTokenJwt = jwtUtils.generateRefreshToken(user);

        LocalDateTime now = LocalDateTime.now();
        AccessToken accessTokens = accessTokenRepository.findByUserAndExpiresAtAfter(user, now);
        if (accessTokens != null) {
            accessTokenRepository.delete(accessTokens);
        }

        if (byToken.getExpiresAt().isBefore(now)) {
            return new ResponseEntity<>("Refresh token has expired", HttpStatus.BAD_REQUEST);
        } else {
            refreshTokenRepository.delete(byToken);
            RefreshToken refreshTokenEntity = createRefreshToken(user, refreshTokenJwt);
            AccessToken accessToken = createAccessToken(user, accessTokenJwt);
            JwtResponse jwtResponse = createJwtResponse(user, accessToken.getToken(), refreshTokenEntity.getToken());
            return new ResponseEntity<>(jwtResponse, HttpStatus.OK);
        }
    }

    private AccessToken createAccessToken(User user, String accessTokenJwt) {
        AccessToken accessTokenEntity = new AccessToken();
        accessTokenEntity.setToken(accessTokenJwt);
        accessTokenEntity.setUser(user);
        accessTokenEntity.setCreatedAt(LocalDateTime.now());
        accessTokenEntity.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        return accessTokenRepository.save(accessTokenEntity);
    }

    private RefreshToken createRefreshToken(User user, String refreshTokenJwt) {
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setToken(refreshTokenJwt);
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setCreatedAt(LocalDateTime.now());
        refreshTokenEntity.setExpiresAt(LocalDateTime.now().plusMinutes(20));
        return refreshTokenRepository.save(refreshTokenEntity);
    }

    private JwtResponse createJwtResponse(User user, String accessToken, String refreshToken) {
        user.setLast_login(new Date());
        userRepository.save(user);

        JwtResponse jwtResponse = new JwtResponse();
        BeanUtils.copyProperties(user, jwtResponse);
        jwtResponse.setAccess_token(accessToken);
        jwtResponse.setRefresh_token(refreshToken);

        return jwtResponse;
    }


    @PostConstruct
    public void adminReg() {
        if (userRepository.existsByEmail("admin@gmail.com")) {

        } else {
            String email = "admin@gmail.com";
            String password = "admin";
            User user = new User(email, passwordEncoder.encode(password));
            user.setIs_superuser(true);
            user.setCreated(new Date());
            user.setIs_blocked(false);
            userRepository.save(user);
            Profile profile = new Profile();
            profile.setUser(user);
            profile.setName("Admin");
            profile.setUsername("Admin");
            profileRepository.save(profile);
            // gen access token
            String accessToken = jwtUtils.generateAccessToken(user);
            AccessToken accessTokenEntity = new AccessToken();
            accessTokenEntity.setToken(accessToken);
            accessTokenEntity.setUser(user);
            accessTokenEntity.setCreatedAt(LocalDateTime.now());
            accessTokenEntity.setExpiresAt(LocalDateTime.now().plusMinutes(10));
            accessTokenRepository.save(accessTokenEntity);

            //gen refresh token
            String refreshToken = jwtUtils.generateRefreshToken(user);
            RefreshToken refreshTokenEntity = new RefreshToken();
            refreshTokenEntity.setToken(refreshToken);
            refreshTokenEntity.setUser(user);
            refreshTokenEntity.setCreatedAt(LocalDateTime.now());
            refreshTokenEntity.setExpiresAt(LocalDateTime.now().plusMinutes(20));
            refreshTokenRepository.save(refreshTokenEntity);
        }
        if (userRepository.existsByEmail("shyrshovolexandr@gmail.com")) {

        } else {
            String email = "shyrshovolexandr@gmail.com";
            String password = "superadmin";
            User user = new User(email, passwordEncoder.encode(password));
            user.setIs_superuser(true);
            user.setCreated(new Date());
            user.setIs_blocked(false);
            userRepository.save(user);
            Profile profile = new Profile();
            profile.setUser(user);
            profile.setName("Sasha");
            profile.setUsername("Shyrshov");
            profileRepository.save(profile);
            // gen access token
            String accessToken = jwtUtils.generateAccessToken(user);
            AccessToken accessTokenEntity = new AccessToken();
            accessTokenEntity.setToken(accessToken);
            accessTokenEntity.setUser(user);
            accessTokenEntity.setCreatedAt(LocalDateTime.now());
            accessTokenEntity.setExpiresAt(LocalDateTime.now().plusMinutes(10));
            accessTokenRepository.save(accessTokenEntity);

            //gen refresh token
            String refreshToken = jwtUtils.generateRefreshToken(user);
            RefreshToken refreshTokenEntity = new RefreshToken();
            refreshTokenEntity.setToken(refreshToken);
            refreshTokenEntity.setUser(user);
            refreshTokenEntity.setCreatedAt(LocalDateTime.now());
            refreshTokenEntity.setExpiresAt(LocalDateTime.now().plusMinutes(20));
            refreshTokenRepository.save(refreshTokenEntity);
        }
    }
}
