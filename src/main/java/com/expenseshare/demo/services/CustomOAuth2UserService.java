package com.expenseshare.demo.services;

import com.expenseshare.demo.dto.CustomOAuth2User;
import com.expenseshare.demo.entity.User;
import com.expenseshare.demo.enums.Role;
import com.expenseshare.demo.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oauth2User.getAttribute("sub");
        String email = oauth2User.getAttribute("email");
        String firstName = oauth2User.getAttribute("given_name");
        String lastName = oauth2User.getAttribute("family_name");

        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setProvider(provider.toUpperCase());
                    newUser.setProviderId(providerId);
                    newUser.setEmailId(email);
                    newUser.setFirstName(firstName != null ? firstName : "");
                    newUser.setLastName(lastName != null ? lastName : "");
                    newUser.setUserName(email);
                    newUser.setRole(Role.USER);
                    return userRepository.save(newUser);
                });

        return new CustomOAuth2User(oauth2User, user);
    }
}
