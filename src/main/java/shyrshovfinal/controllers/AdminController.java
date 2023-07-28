package shyrshovfinal.controllers;

import shyrshovfinal.Filter.dto.ApplicationStatusDto;
import shyrshovfinal.jwt.JwtUtils;
import shyrshovfinal.models.*;
import shyrshovfinal.pojo.ActivateUser;
import shyrshovfinal.pojo.MessageResponse;
import shyrshovfinal.pojo.SignUpRequest;
import shyrshovfinal.repository.*;
import shyrshovfinal.service.UserDetailsImpl;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Api(value = "Get users")

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {
    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AccessTokenRepository accessTokenRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    PaidRepository paidRepository;

    @Operation(summary = "get user",
            operationId = "getuser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request")
            })
    @SecurityRequirement(name = "JWT")
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<User> all = userRepository.findAll();
        all.removeIf(user -> user.getEmail().equals(userDetails.getEmail()));
        return new ResponseEntity<>(all, HttpStatus.OK);
    }

    @SecurityRequirement(name = "JWT")

    @GetMapping("/user")
    @Operation(summary = "Get authenticated user",
            operationId = "Getauthenticateduser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request")
            })
    public ResponseEntity<?> getUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetailsImpl) principal).getEmail();
            Optional<User> byEmail = userRepository.findByEmail(email);
            return new ResponseEntity<>(byEmail, HttpStatus.OK);
        }
        return new ResponseEntity<>("User details not found", HttpStatus.NOT_FOUND);
    }

    @PreAuthorize("#user.isIs_superuser()")
    @PostMapping("/users/reg")
    @Operation(summary = "Create a new user",
            operationId = "CreateUser",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User created successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AccessToken.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request - missing required fields or invalid email format"),
                    @ApiResponse(responseCode = "409", description = "Email already exists")
            })
    public ResponseEntity<?> createUser(@RequestBody SignUpRequest signUpRequest, @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user) {
        String randomPassword = generateRandomPassword(20);
        if (StringUtils.isBlank(signUpRequest.getEmail())
                || StringUtils.isBlank(signUpRequest.getName())
                || StringUtils.isBlank(signUpRequest.getUsername())) {
            return new ResponseEntity<>("All fields are required", HttpStatus.BAD_REQUEST);
        }
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        if (!Pattern.matches(emailRegex, signUpRequest.getEmail())) {
            return new ResponseEntity<>("Invalid email format", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error : Email is exist"));
        }
        String nameRegex = "^[a-zA-Zа-яА-ЯёЁґҐєЄіїІЇ -]{1,20}$";
        if (!Pattern.matches(nameRegex, signUpRequest.getName())) {
            return new ResponseEntity<>("Invalid name format", HttpStatus.BAD_REQUEST);
        }

        User newuser = new User(signUpRequest.getEmail());
        newuser.setPassword(passwordEncoder.encode(randomPassword));

        Profile profile = new Profile(signUpRequest.getName(), signUpRequest.getUsername());
        profile.setUser(newuser);
        userRepository.save(newuser);
        profileRepository.save(profile);

        String accessToken = jwtUtils.generateAccessToken(newuser);
        AccessToken accessTokenEntity = new AccessToken();
        accessTokenEntity.setToken(accessToken);
        accessTokenEntity.setUser(newuser);
        accessTokenEntity.setCreatedAt(LocalDateTime.now());
        accessTokenEntity.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        accessTokenRepository.save(accessTokenEntity);

        String refreshToken = jwtUtils.generateRefreshToken(newuser);
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setToken(refreshToken);
        refreshTokenEntity.setUser(newuser);
        refreshTokenEntity.setCreatedAt(LocalDateTime.now());
        refreshTokenEntity.setExpiresAt(LocalDateTime.now().plusMinutes(20));
        refreshTokenRepository.save(refreshTokenEntity);
        userRepository.save(newuser);
        return new ResponseEntity<>(accessTokenEntity, HttpStatus.CREATED);
    }

    @Operation(summary = "Access token",
            description = "Generate new token for registration",
            operationId = "accessToken",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = TokenWrapper.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request - user not found")
            })
    @PostMapping("/users/re_token/{id}")
    @PreAuthorize("#userDetails.isIs_superuser()")
    public ResponseEntity<?> accessToken(@Parameter(description = "User ID", required = true) @PathVariable long id, @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Optional<User> byId = userRepository.findById(id);
        if (!byId.isPresent()) {
            return new ResponseEntity<>(new MessageResponse("User not found"), HttpStatus.NOT_FOUND);
        }
        AccessToken byUser = accessTokenRepository.findByUser(byId);
        String token = byUser.getToken();
        AccessToken byToken = accessTokenRepository.findByToken(token);
        accessTokenRepository.delete(byToken);
        if (byToken == null) {
            return new ResponseEntity<>("Invalid access token", HttpStatus.BAD_REQUEST);
        }

        User user = byToken.getUser();
        String newAccessTokenJwt = jwtUtils.generateAccessToken(user);

        LocalDateTime now = LocalDateTime.now();
        AccessToken oldAccessToken = accessTokenRepository.findByUserAndExpiresAtAfter(user, now);
        if (oldAccessToken != null) {
            accessTokenRepository.delete(oldAccessToken);
        }

        AccessToken newAccessToken = new AccessToken();
        newAccessToken.setToken(newAccessTokenJwt);
        newAccessToken.setUser(user);
        newAccessToken.setCreatedAt(LocalDateTime.now());
        newAccessToken.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        accessTokenRepository.save(newAccessToken);

        return ResponseEntity.ok(new TokenWrapper(newAccessTokenJwt));
    }

    @Operation(summary = "Activate user with provided token and set password",
            operationId = "ActivateUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User activated successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad request - invalid access token"),
                    @ApiResponse(responseCode = "404", description = "Token not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    @PostMapping("/activate/{token}")
    public ResponseEntity<?> activateUser(@RequestBody ActivateUser activateUser,
                                          @PathVariable String token) {
        if (token == null) {
            return new ResponseEntity<>("Invalid access token", HttpStatus.BAD_REQUEST);
        }
        AccessToken byToken = accessTokenRepository.findByToken(token);
        User user = byToken.getUser();
        String password = activateUser.getPassword();
        if (activateUser.getPassword().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error : Password is empty"));
        } else if (activateUser.getPassword().length() < 4 || activateUser.getPassword().length() > 16) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Invalid password"));
        }
        user.setPassword(passwordEncoder.encode(password));
        System.out.println(password);
        user.setIs_active(true);
        user.setIs_blocked(false);
        user.setCreated(new Date());
        userRepository.save(user);
        return ResponseEntity.ok().body("User created");
    }

    public static String generateRandomPassword(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            sb.append(characters.charAt(randomIndex));
        }
        return sb.toString();
    }

    @PreAuthorize("#userDetails.isIs_active()")
    @Operation(summary = "Get applications by user ID",
            description = "Returns the count of applications and their statuses for a given user ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful response", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationStatusDto.class))),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    @GetMapping("/statistics/users/{id}")
    public ResponseEntity<?> getApplicationsByUserId(@Parameter(description = "User ID", required = true) @PathVariable Long id, @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Optional<User> byId = userRepository.findById(id);
        if (!byId.isPresent()) {
            return new ResponseEntity<>(new MessageResponse("User not found"), HttpStatus.NOT_FOUND);
        }
        String email = byId.get().getEmail();
        List<Paid> byUserEmail = paidRepository.findByUserEmail(email);

        Map<String, Integer> statuses = new HashMap<>();
        int inWorkCount = 0;
        int newCount = 0;
        int agreeCount = 0;
        int disagreeCount = 0;
        int doubleCount = 0;

        for (Paid paid : byUserEmail) {
            String status = paid.getStatus().toString();
            switch (status) {
                case "INWORK":
                    inWorkCount++;
                    break;
                case "NEW":
                    newCount++;
                    break;
                case "AGREE":
                    agreeCount++;
                    break;
                case "DISAGREE":
                    disagreeCount++;
                    break;
                case "DUBBING":
                    doubleCount++;
                    break;
            }
        }

        statuses.put("inWorkCount", inWorkCount);
        statuses.put("newCount", newCount);
        statuses.put("agreeCount", agreeCount);
        statuses.put("disagreeCount", disagreeCount);
        statuses.put("dubbingCount", doubleCount);

        Map<String, Object> response = new HashMap<>();
        response.put("totalCount", byUserEmail.size());
        response.put("statuses", statuses);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("#userDetails.isIs_active()")
    @Operation(summary = "Get all applications",
            description = "Returns the count of applications and their statuses",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful response", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationStatusDto.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    @GetMapping("/statistics/orders")
    public ResponseEntity<?> getAppAll(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<Paid> all = paidRepository.findAll();
        Map<String, Integer> statuses = new HashMap<>();
        int inWorkCount = 0;
        int newCount = 0;
        int agreeCount = 0;
        int disagreeCount = 0;
        int dubbingCount = 0;
        int nullCount = 0;

        for (Paid paid : all) {
            if (Objects.isNull(paid.getStatus())) {
                nullCount++;
            } else {
                String status = paid.getStatus().toString();
                switch (status) {
                    case "INWORK":
                        inWorkCount++;
                        break;
                    case "NEW":
                        newCount++;
                        break;
                    case "AGREE":
                        agreeCount++;
                        break;
                    case "DISAGREE":
                        disagreeCount++;
                        break;
                    case "DUBBING":
                        dubbingCount++;
                        break;
                }
            }
        }

        statuses.put("inWorkCount", inWorkCount);
        statuses.put("newCount", newCount);
        statuses.put("agreeCount", agreeCount);
        statuses.put("disagreeCount", disagreeCount);
        statuses.put("dubbingCount", dubbingCount);
        statuses.put("nullCount", nullCount);

        Map<String, Object> response = new HashMap<>();
        response.put("totalCount", all.size());
        response.put("statuses", statuses);

        return new ResponseEntity<>(response, HttpStatus.OK);


    }

    @PreAuthorize("#userDetails.isIs_superuser()")
    @Operation(summary = "Block user by ID",
            description = "Blocks the user", responses = {
            @ApiResponse(responseCode = "200", description = "User blocked successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/block/{id}")
    public ResponseEntity<MessageResponse> blockUserById(
            @Parameter(description = "User ID", required = true) @PathVariable long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Optional<User> byId = userRepository.findById(id);

        if (!byId.isPresent()) {
            return new ResponseEntity<>(new MessageResponse("User not found"), HttpStatus.NOT_FOUND);
        }

        if (userDetails.getId() == byId.get().getId()) {
            return new ResponseEntity<>(new MessageResponse("You can't block yourself"), HttpStatus.BAD_REQUEST);
        }

        User user = byId.get();
        user.setIs_active(false);
        user.setIs_blocked(true);
        userRepository.save(user);

        return new ResponseEntity<>(new MessageResponse("User blocked"), HttpStatus.OK);
    }


    @PreAuthorize("#userDetails.isIs_superuser()")
    @Operation(summary = "Unblock user",
            description = "Unblock the user", responses = {
            @ApiResponse(responseCode = "200", description = "User unblocked successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/unblock/{id}")
    public ResponseEntity<?> unblockUserById(@Parameter(description = "User ID", required = true) @PathVariable long id, @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Optional<User> byId = userRepository.findById(id);
        if (!byId.isPresent()) {
            return new ResponseEntity<>(new MessageResponse("User not found"), HttpStatus.NOT_FOUND);
        }
        byId.get().setIs_active(true);
        byId.get().setIs_blocked(false);
        userRepository.save(byId.get());

        return new ResponseEntity<>(new MessageResponse("User unblocked"), HttpStatus.OK);
    }

    @PreAuthorize("#userDetails.isIs_superuser()")
    @Operation(summary = "delete user",
            description = "delete the user", responses = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@Parameter(description = "User ID", required = true) @PathVariable long id, @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Optional<User> byId = userRepository.findById(id);
        if (!byId.isPresent()) {
            return new ResponseEntity<>(new MessageResponse("User not found"), HttpStatus.NOT_FOUND);
        }
        if (userDetails.getId() == byId.get().getId()) {
            return new ResponseEntity<>(new MessageResponse("you cant delete yourself"), HttpStatus.BAD_REQUEST);
        }
        User user = byId.get();
        List<AccessToken> allByUserId = accessTokenRepository.findAllByUserId(user.getId());
        accessTokenRepository.deleteAll(allByUserId);
        List<RefreshToken> allByUserId1 = refreshTokenRepository.findAllByUserId(user.getId());
        refreshTokenRepository.deleteAll(allByUserId1);
        userRepository.delete(user);
        return new ResponseEntity<>(new MessageResponse("user deleted"), HttpStatus.OK);
    }
}

