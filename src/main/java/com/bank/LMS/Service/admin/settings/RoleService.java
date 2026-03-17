package com.bank.LMS.Service.admin.settings;

import com.bank.LMS.Entity.Role;
import com.bank.LMS.Repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    // ✅ For roles page
    public List<Role> listAll() {
        return roleRepository.findAll();
    }

    // ✅ For dropdown (only active roles)
    public List<Role> getActiveRoles() {
        return roleRepository.findByActiveTrueOrderByRoleNameAsc();
    }

    // ✅ Create role
    public Role createRole(String roleName) {

        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name is required");
        }

        Role role = Role.builder()
                .roleName(roleName.trim().toUpperCase())
                .active(true)
                .build();

        return roleRepository.save(role);
    }

    // ✅ Toggle enable/disable
    public Role toggleActive(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        role.setActive(!role.isActive());
        return roleRepository.save(role);
    }

    // ✅ Used in staff page for selected role
    public Role getRoleOrThrow(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
    }
}