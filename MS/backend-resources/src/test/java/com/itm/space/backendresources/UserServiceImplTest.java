package com.itm.space.backendresources;

import java.net.URI;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.exception.BackendResourcesException;
import com.itm.space.backendresources.mapper.UserMapper;
import com.itm.space.backendresources.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.UUID;

@SpringBootTest
public class UserServiceImplTest {

    @MockBean
    private Keycloak keycloak;

    @MockBean
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "realm", "testRealm");
    }

    @Test
    void shouldReturnUser() {

        UUID userId = UUID.randomUUID();

        RealmResource realmResource = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);
        UserResource userResource = mock(UserResource.class);
        RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
        MappingsRepresentation mappingsRepresentation = mock(MappingsRepresentation.class);

        UserRepresentation userRepresentation = new UserRepresentation();
        List<RoleRepresentation> roles = List.of(new RoleRepresentation());
        List<GroupRepresentation> groups = List.of(new GroupRepresentation());
        UserResponse expectedResponse = new UserResponse(
                "Test",
                "Test",
                "Test@gmail.com",
                List.of("Test"),
                List.of("Test"));

        when(keycloak.realm(any())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId.toString())).thenReturn(userResource);

        when(userResource.toRepresentation()).thenReturn(userRepresentation);

        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.getAll()).thenReturn(mappingsRepresentation);
        when(mappingsRepresentation.getRealmMappings()).thenReturn(roles);

        when(userResource.groups()).thenReturn(groups);

        when(userMapper.userRepresentationToUserResponse(userRepresentation, roles, groups))
                .thenReturn(expectedResponse);

        UserResponse result = userService.getUserById(userId);

        assertNotNull(result);
    }

    @Test
    void shouldThrowException_whenKeycloakFails() {
        when(keycloak.realm(any())).thenThrow(new RuntimeException("fail"));

        assertThrows(BackendResourcesException.class, () ->
                userService.getUserById(UUID.randomUUID()));
    }

    @Test
    void shouldCreateUser() {

        UserRequest request = new UserRequest(
                "Test",
                "Test@Gmail.com",
                "1234",
                "Test",
                "Test");

        RealmResource realmResource = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);

        Response response = mock(Response.class);

        URI location = URI.create("http://localhost/users/1234");
        when(response.getLocation()).thenReturn(location);
        when(response.getStatus()).thenReturn(201);
        when(response.getStatusInfo()).thenReturn(Response.Status.CREATED);

        when(keycloak.realm(any())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any())).thenReturn(response);

        userService.createUser(request);

        verify(usersResource).create(any());
    }

    @Test
    void shouldThrowException_whenCreateFails() {

        UserRequest request = new UserRequest(
                "Test",
                "Test@Gmail.com",
                "1234",
                "Test",
                "Test");

        RealmResource realmResource = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);

        when(keycloak.realm(any())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);

        when(usersResource.create(any())).thenThrow(new WebApplicationException("fail", 400));

        assertThrows(BackendResourcesException.class,
                () -> userService.createUser(request));
    }
}