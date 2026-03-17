package com.bank.LMS.Repository;

import com.bank.LMS.Entity.StaffUsers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaffUsersRepository extends JpaRepository<StaffUsers, Long> {

    Optional<StaffUsers> findByEmail(String email);

    // ✅ ADD THIS
    boolean existsByEmail(String email);

    List<StaffUsers> findByRole_RoleIdOrderByCreatedAtDesc(Long roleId);

    List<StaffUsers> findByRole_RoleIdAndNameContainingIgnoreCaseOrRole_RoleIdAndEmailContainingIgnoreCaseOrderByCreatedAtDesc(
            Long roleId1, String name,
            Long roleId2, String email
    );

    long countByRole_RoleNameIn(List<String> roleNames);

    List<StaffUsers> findByRole_RoleNameInAndActiveTrue(List<String> roles);

}