package com.fpt.backend.repository.project;

import com.fpt.backend.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {
    // Đếm số liên kết thành viên giữa một người dùng và một dự án.
    @Query("""
            SELECT COUNT(member)
            FROM ProjectMember member
            WHERE member.project.id = :projectId
                AND member.user.id = :userId
            """)
    long countByProjectIdAndUserId(
            @Param("projectId") UUID projectId,
            @Param("userId") UUID userId
    );

    // Lấy danh sách thành viên của dự án kèm thông tin người dùng.
    @Query("""
            SELECT member
            FROM ProjectMember member
            JOIN FETCH member.user user
            WHERE member.project.id = :projectId
            ORDER BY user.firstName, user.lastName, user.email
            """)
    List<ProjectMember> findByProjectId(@Param("projectId") UUID projectId);

    // Xóa toàn bộ thành viên thuộc một dự án.
    @Modifying
    @Query("DELETE FROM ProjectMember member WHERE member.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") UUID projectId);
}
