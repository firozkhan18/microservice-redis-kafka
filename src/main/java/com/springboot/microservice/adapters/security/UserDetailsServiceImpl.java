package com.springboot.microservice.adapters.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * @author: Firoz Khan
 * @version:
 * @date:
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    /**
     * Returns Spring UserDetails Object.
     * This service doesnt do any database look and it returns the UserDetails
     * object with the same user name.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new User(username, "", new ArrayList<>());
    }

}
