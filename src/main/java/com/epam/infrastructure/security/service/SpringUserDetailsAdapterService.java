package com.epam.infrastructure.security.service;

import com.epam.domain.model.User;
import com.epam.domain.port.UserRepository;
import com.epam.infrastructure.security.adapter.DomainUserDetailsAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SpringUserDetailsAdapterService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    SpringUserDetailsAdapterService(UserRepository repository) {
        this.userRepository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
        return new DomainUserDetailsAdapter(user);
    }

}
