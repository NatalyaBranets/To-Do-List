package com.softserve.itacademy.service;

import com.softserve.itacademy.model.User;
import com.softserve.itacademy.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;


    @Test
    public void getAllTest() {
        List<User> expected = new ArrayList<>();
        User user = new User();
        user.setEmail("new@email.com");
        user.setFirstName("New");
        user.setLastName("New");
        user.setPassword("1111");
        expected.add(user);
        when(userRepository.findAll()).thenReturn(expected);
        List<User> actual = userService.getAll();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void createTest() {
        User expected = new User();
        expected.setEmail("new@email.com");
        expected.setFirstName("New");
        expected.setLastName("New");
        expected.setPassword("1111");

        when(userRepository.save(any())).thenReturn(expected);

        User actual = userService.create(expected);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual);

    }

    @Test
    public void readByIdTest() {
        User expected = new User();
        expected.setId(4L);
        expected.setEmail("new@email.com");
        expected.setFirstName("New");
        expected.setLastName("New");
        expected.setPassword("1111");

        when(userRepository.findById(4L)).thenReturn(Optional.of(expected));

        User actual = userService.readById(expected.getId());
        Assertions.assertEquals(expected, actual);

    }


}
