package com.authService.services;

import com.authService.entities.UserInfo;
import com.authService.eventProducer.UserInfoEvent;
import com.authService.eventProducer.UserInfoProducer;
import com.authService.models.UserInfoDto;
import com.authService.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
@Data
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final UserInfoProducer userInfoProducer;


    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Entering in loadUserByUsername Method...");

        UserInfo user = userRepository.findByUsername(username);

        if(user == null){
            log.error("Username not found: " + username);
            throw new UsernameNotFoundException("could not found user..!!");
        }

        log.info("User Authenticated Successfully..!!!");
        return new CustomUserDetails(user);
    }

    public UserInfo checkIfUserAlreadyExist(UserInfoDto userInfoDto) {
        return userRepository.findByUsername(userInfoDto.getUsername());
    }

    public String signupUser(UserInfoDto userInfoDto) {
        if (Objects.nonNull(checkIfUserAlreadyExist(userInfoDto))) return null;

        String userId = UUID.randomUUID().toString();
        userInfoDto.setPassword(passwordEncoder.encode(userInfoDto.getPassword()));

        userRepository.save(new UserInfo(userId, userInfoDto.getUsername(),
                userInfoDto.getPassword(), new HashSet<>()));

        userInfoProducer.sendEventToKafka(userInfoEventToPublish(userInfoDto, userId));

        return userId;
    }

    public String getUserByUsername(String username){
        return Optional.ofNullable(userRepository.findByUsername(username))
                .map(UserInfo::getUserId)
                .orElse(null);
    }

    private UserInfoEvent userInfoEventToPublish(UserInfoDto userInfoDto, String userId){
        return UserInfoEvent.builder()
                .userId(userId)
                .firstName(userInfoDto.getUsername())
                .lastName(userInfoDto.getLastName())
                .email(userInfoDto.getEmail())
                .phoneNumber(userInfoDto.getPhoneNumber()).build();

    }


}
