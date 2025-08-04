package com.LDE.monFax_backend.services;

import com.LDE.monFax_backend.exceptions.UserNotFoundException;
import com.LDE.monFax_backend.models.User;
import com.LDE.monFax_backend.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public long numberUsers(){
        return userRepository.count();
    }

    public long getUsersRegisteredLast30Days() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return userRepository.countUsersRegisteredSince(thirtyDaysAgo);
    }

    public List<User> getLast5ConnectedUsers() {
        return userRepository.findTop5ByOrderByLastLoginDesc();
    }
}
