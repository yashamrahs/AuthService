package com.authService.controller;

import com.authService.entities.RefreshToken;
import com.authService.models.UserInfoDto;
import com.authService.response.JWTResponseDTO;
import com.authService.services.JWTService;
import com.authService.services.RefreshTokenService;
import com.authService.services.UserDetailsServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@AllArgsConstructor
@RestController
public class AuthController {

    @Autowired
    private JWTService jwtService;


    @Autowired
    private RefreshTokenService refreshTokenService;


    @Autowired
    private UserDetailsServiceImpl userDetailsService;


    /**
     * Endpoint to sign up a new user.
     * 1. Receives user info (username + password)
     * 2. Checks if user already exists
     * 3. If not, creates user, generates JWT + Refresh token
     * 4. Returns tokens and user ID
     */
    @PostMapping("auth/v1/signup")
    public ResponseEntity SignUp(@RequestBody UserInfoDto userInfoDto){
        try {
            String userId = userDetailsService.signupUser(userInfoDto);

            if(Objects.isNull(userId)){
                return new ResponseEntity<>("Already Exist", HttpStatus.BAD_REQUEST);
            }

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userInfoDto.getUsername());
            String jwtToken = jwtService.GenerateToken(userInfoDto.getUsername());

            return new ResponseEntity<>(JWTResponseDTO.builder().accessToken(jwtToken).
                    token(refreshToken.getToken()).userId(userId).build(), HttpStatus.OK);

        } catch (Exception ex){
            return new ResponseEntity<>("Exception in User Service", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Endpoint to verify if a user is authenticated (used as a protected test route).
     * - Extracts the current authenticated user's username
     * - Returns userId if authenticated, otherwise responds with 401 Unauthorized
     */
    @GetMapping("/auth/v1/ping")
    public ResponseEntity<String> ping() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String userId = userDetailsService.getUserByUsername(authentication.getName());

            if(Objects.nonNull(userId)){
                return ResponseEntity.ok(userId);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }


    /**
     * Simple health check endpoint.
     * Returns HTTP 200 OK with a boolean `true` to indicate the service is up.
     */
    @GetMapping("/health")
    public ResponseEntity<Boolean> checkHealth(){
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}
