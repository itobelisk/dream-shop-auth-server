package com.auth.server.services.role.impl;

import com.auth.server.base.BaseResponse;
import com.auth.server.entity.role.Role;
import com.auth.server.entity.role.request.RoleRequest;
import com.auth.server.entity.role.response.RoleResponse;
import com.auth.server.exception.AdminCanNotBeDeleteException;
import com.auth.server.exception.RoleAlreadyCreatedException;
import com.auth.server.exception.RoleIdNotFoundException;
import com.auth.server.mapper.RoleMapper;
import com.auth.server.repository.RoleRepository;
import com.auth.server.services.role.RoleControllerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleControllerServiceImpl implements RoleControllerService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private static final String ROLE_START = "ROLE_";

    @Override
    public BaseResponse<?> save(RoleRequest roleRequest) {
        String roleName = getFinalName(roleRequest);
        if (roleRepository.existsByName(roleName) != null) throw new RoleAlreadyCreatedException();
        RoleResponse response = null;
        try {
            Role role = roleMapper.toRoleEntity(roleName);
            response = roleMapper.toResponse(roleRepository.save(role));
        } catch (Exception e) {
            return new BaseResponse<>(new Date(), false, HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return new BaseResponse<>(new Date(), true, HttpStatus.OK, response);
    }


    @Override
    public BaseResponse<?> all() {
        List<RoleResponse> response = null;
        try {
            response = roleMapper.toResponseList(roleRepository.findAll());
        } catch (Exception e) {
            return new BaseResponse<>(new Date(), false, HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return new BaseResponse<>(new Date(), true, HttpStatus.OK, response);
    }

    @Override
    public BaseResponse<?> update(RoleRequest roleRequest) {
        if (!roleRepository.existsById(roleRequest.getId())) throw new RoleIdNotFoundException();
        String roleName = getFinalName(roleRequest);
        RoleResponse response = null;
        try {
            Role role = roleMapper.getRoleById(roleRepository.getOne(roleRequest.getId()));
            role.setId(roleRequest.getId());
            role.setName(roleName);
            response = roleMapper.toResponse(roleRepository.save(role));
        } catch (Exception e) {
            return new BaseResponse<>(new Date(), false, HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return new BaseResponse<>(new Date(), true, HttpStatus.OK, response);
    }

    @Override
    public BaseResponse<?> delete(Long id) {
        if (!roleRepository.existsById(id)) throw new RoleAlreadyCreatedException();
        Role role = roleRepository.getOne(id);
        if (role.getName().toLowerCase().contains("admin")) throw new AdminCanNotBeDeleteException();
        roleRepository.deleteById(id);
        if (roleRepository.existsById(id)) {
            return new BaseResponse<>(new Date(), false, HttpStatus.BAD_REQUEST, "Not deleted");
        }
        return new BaseResponse<>(new Date(), true, HttpStatus.OK, "Delete successfully");
    }

    private String getFinalName(RoleRequest roleRequest) {
        String finalRoleName = "";
        finalRoleName = roleRequest.getName().contains(" ") ? roleRequest.getName().replace(" ", "_") : roleRequest.getName().toUpperCase();
        return ROLE_START + finalRoleName.toLowerCase();
    }
}
