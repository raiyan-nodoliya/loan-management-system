package com.bank.LMS.Repository;

import com.bank.LMS.Entity.Role;
import com.bank.LMS.Entity.StaffUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByActiveTrueOrderByRoleNameAsc();

    Optional<Role> findByRoleName(String roleName);

    @Query("select su from StaffUsers su join fetch su.role where su.email = :email")
    Optional<StaffUsers> findByEmailWithRole(@Param("email") String email);
}