package com.itm.space.backendresources;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.mockito.ArgumentMatchers.any;

import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.UUID;

@SpringBootTest
public class UserControllerIT extends BaseIntegrationTest{

    @Test
    void contextLoads () {
    }

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(roles = "MODERATOR")
    void shouldCreateUser() throws Exception {

        UserRequest request = new UserRequest(
                "Test",
                "Test@Gmail.com",
                "1234",
                "Test",
                "Test");

        mvc.perform(requestWithContent(post("/api/users"), request))
                .andExpect(status().isOk());

        verify(userService).createUser(any());
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void shouldReturnUser() throws Exception {

        UUID id = UUID.randomUUID();

        UserResponse response = new UserResponse(
                "Test",
                "Test",
                "Test@gmail.com",
                List.of("Test"),
                List.of("Test"));

        when(userService.getUserById(id)).thenReturn(response);

        mvc.perform(get("/api/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("Test@gmail.com"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "MODERATOR")
    void shouldReturnUsername() throws Exception {

        mvc.perform(get("/api/users/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("testUser"));
    }

    @Test
    @WithMockUser
    void shouldReturn403_whenNoRole() throws Exception {

        mvc.perform(get("/api/users/hello"))
                .andExpect(status().isForbidden());
    }
}
