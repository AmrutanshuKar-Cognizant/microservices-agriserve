package com.cognizant.agriserve.userservice.service.impl;

import com.cognizant.agriserve.userservice.dao.UserRepository;
import com.cognizant.agriserve.userservice.dto.RegisterRequestDTO;
import com.cognizant.agriserve.userservice.dto.UserDTO;
import com.cognizant.agriserve.userservice.dto.UserRequestDTO;
import com.cognizant.agriserve.userservice.dto.UserResponseDTO;
import com.cognizant.agriserve.userservice.entity.User;
import com.cognizant.agriserve.userservice.exception.ResourceNotFoundException;
import com.cognizant.agriserve.userservice.exception.UserAlreadyExistsException;
import com.cognizant.agriserve.userservice.service.UserService;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Builder
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordencode;

    @Override
    public String getUserNameById(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with ID: " + userId));

        return user.getName();
    }

    @Override
    public UserResponseDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with ID: " + userId));
        return mapToDTO(user);
    }

    @Override
    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with email: " + email));
        return mapToDTO(user);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponseDTO> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponseDTO> getUsersByStatus(String status) {
        return userRepository.findByStatus(status)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(Long userId, UserRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with ID: " + userId));

        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());
        user.setStatus(dto.getStatus());

        return mapToDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with ID: " + userId));

        user.setStatus("INACTIVE");
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    private UserResponseDTO mapToDTO(User user) {
        return modelMapper.map(user, UserResponseDTO.class);
    }

    @Transactional
    public UserDTO findbyusername(String Username){
        log.info("Fetching user credentials for: {}", Username);
        User user = userRepository.findByEmail(Username)
                .orElseThrow(() -> new RuntimeException("Error: User not found"));

        return UserDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole().name()) // UPDATED: Dynamically fetch the role!
                .build();
    }

    @Override
    @Transactional
    public UserDTO register(RegisterRequestDTO dto){
        log.info("Registering the following user: {}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Registration failed: Email {} is already taken.", dto.getEmail());
            throw new UserAlreadyExistsException("Email is already in use!");
        }

        String encrypted = passwordencode.encode(dto.getPassword());

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(encrypted);
        user.setName(dto.getName());

        // Dynamically set the role passed from Auth Service
        if(dto.getRole() != null) {
            user.setRole(User.Role.valueOf(dto.getRole()));
        } else {
            user.setRole(User.Role.Farmer); // Fallback
        }

        user.setPhone(dto.getContactInfo()); // Assumes you added phone to RegisterRequestDTO
        user.setStatus("ACTIVE"); // Make sure this matches your DB convention

        // The Magic Moment: Database saves the user and generates the ID
        User savedUser = userRepository.save(user);
        log.info("User successfully saved to database with ID: {}", savedUser.getUserId());

        // Return the DTO so the Auth Service can extract the ID!
        return UserDTO.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .build();
    }
}