package com.fpt.backend.repository.permission;

import com.fpt.backend.entity.PermissionAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionActionRepository
        extends JpaRepository<PermissionAction, UUID> {
    List<PermissionAction>
    findByStatusTrueOrderByDisplayOrderAscActionCodeAsc();
}
