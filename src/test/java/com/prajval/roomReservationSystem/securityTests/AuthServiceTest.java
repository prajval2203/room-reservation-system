package com.prajval.roomReservationSystem.securityTests;

import com.prajval.roomReservationSystem.dto.LoginDto;
import com.prajval.roomReservationSystem.dto.SignUpRequestDto;
import com.prajval.roomReservationSystem.dto.UserDto;
import com.prajval.roomReservationSystem.entity.User;
import com.prajval.roomReservationSystem.entity.enums.Gender;
import com.prajval.roomReservationSystem.entity.enums.Role;
import com.prajval.roomReservationSystem.repository.UserRepository;
import com.prajval.roomReservationSystem.security.AuthService;
import com.prajval.roomReservationSystem.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private AuthService authService;
    @Spy
    private  ModelMapper modelMapper;
    @Mock
    private  PasswordEncoder passwordEncoder;
    @Mock
    private  AuthenticationManager authenticationManager;
    @Mock
    private  JwtService jwtService;

    private SignUpRequestDto mockSignupDto;
    private User mockUser;
    private LoginDto loginDto;

    @BeforeEach
    void setUp(){
        mockUser =User.builder()
                .id(10L)
                .name("Parth Kadam")
                .email("parth123@gmail.com")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(2002, 12, 20))
                .password("ParthKadam@123")
                .build();

        mockSignupDto = new SignUpRequestDto();
        mockSignupDto.setEmail("parth123@gmail.com");
        mockSignupDto.setPassword("ParthKadam@123");
        mockSignupDto.setName("Parth Kadam");
    }

    @Test
    void signUp_whenNewUser_thenReturnUserDto() {

        when(userRepository.findByEmail(mockSignupDto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(mockSignupDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UserDto result = authService.signUp(mockSignupDto);

        assertThat(result).isNotNull();
        verify(passwordEncoder).encode(mockSignupDto.getPassword());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(mockSignupDto.getEmail());
        assertThat(savedUser.getRoles()).contains(Role.GUEST);
    }

    @Test
    void signUp_whenDuplicateEmail_thenThrowException() {

        when(userRepository.findByEmail(mockSignupDto.getEmail())).thenReturn(Optional.of(mockUser));

        assertThrows(RuntimeException.class, () -> authService.signUp(mockSignupDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_whenValidCredentials_thenReturnTokens() {

        LoginDto loginDto = new LoginDto();
        loginDto.setEmail("parth123@gmail.com");
        loginDto.setPassword("ParthKadam@123");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mockUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtService.generateAccessToken(mockUser)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(mockUser)).thenReturn("refreshToken");

        String[] tokens = authService.login(loginDto);

        assertThat(tokens).hasSize(2);
        assertThat(tokens[0]).isEqualTo("accessToken");
        assertThat(tokens[1]).isEqualTo("refreshToken");
    }

    @Test
    void login_whenInvalidCredentials_thenThrowException() {

        LoginDto loginDto = new LoginDto();
        loginDto.setEmail("parth123@gmail.com");
        loginDto.setPassword("wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));
        assertThrows(BadCredentialsException.class, () -> authService.login(loginDto));
    }
}

