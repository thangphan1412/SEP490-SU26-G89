package com.fpt.backend.service.impl.contract;

import com.fpt.backend.entity.Departments;
import com.fpt.backend.entity.Role;
import com.fpt.backend.entity.UserRole;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.UserStatus;
import com.fpt.backend.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractServiceImplStandaloneTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContractServiceImpl contractService;

    @Test
    void standaloneContextReturnsActiveUsersWithActualRoleAndDepartment() {
        Departments legal = new Departments();
        legal.setId(UUID.randomUUID());
        legal.setDepartmentCode("LEGAL");
        legal.setDepartmentName("Legal Department");

        Role reviewer = new Role();
        reviewer.setId(UUID.randomUUID());
        reviewer.setRoleCode("LEGAL_REVIEWER");
        reviewer.setRoleName("Legal reviewer");

        Users activeUser = user(
                "Active",
                "Reviewer",
                UserStatus.ACTIVE,
                legal,
                reviewer
        );
        Users inactiveUser = user(
                "Inactive",
                "Reviewer",
                UserStatus.INACTIVE,
                legal,
                reviewer
        );
        when(userRepository.findAll()).thenReturn(List.of(
                inactiveUser,
                activeUser
        ));

        var context = contractService.getStandaloneContext();

        assertThat(context.members()).hasSize(1);
        assertThat(context.members().getFirst().userId())
                .isEqualTo(activeUser.getId());
        assertThat(context.members().getFirst().roleCodes())
                .containsExactly("LEGAL_REVIEWER");
        assertThat(context.members().getFirst().departmentId())
                .isEqualTo(legal.getId());
        assertThat(context.members().getFirst().departmentName())
                .isEqualTo("Legal Department");
    }

    private Users user(
            String firstName,
            String lastName,
            UserStatus status,
            Departments department,
            Role role
    ) {
        Users user = new Users();
        user.setId(UUID.randomUUID());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(firstName.toLowerCase() + "@example.com");
        user.setStatus(status);
        user.setDepartment(department);
        user.setUserRoles(List.of(UserRole.builder()
                .user(user)
                .role(role)
                .build()));
        return user;
    }
}
