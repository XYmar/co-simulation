package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.*;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Author: XYmar
 * Date: 2019/2/12 16:15
 */
@Service
@Slf4j
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final DepartmentService departmentService;
    private final ProjectRepository projectRepository;
    private final SubtaskRepository subtaskRepository;
    private final SublibraryFilesRepository sublibraryFilesRepository;

    @Autowired
    public UserService(UserRepository userRepository, RoleService roleService, DepartmentService departmentService, ProjectRepository projectRepository, SubtaskRepository subtaskRepository, SublibraryFilesRepository sublibraryFilesRepository) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.departmentService = departmentService;
        this.projectRepository = projectRepository;
        this.subtaskRepository = subtaskRepository;
        this.sublibraryFilesRepository = sublibraryFilesRepository;
    }

    // 保存用户 , 一个角色
    @CacheEvict(value = "User_Cache", allEntries = true)
    public Users saveUser(String departmentName, Users users, String roleName) {
        if (users == null) {
            throw new ResultException(ResultCode.USER_ARGS_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(users.getUsername())){
            throw new ResultException(ResultCode.USER_USERNAME_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(users.getRealName())){
            throw new ResultException(ResultCode.USER_REALNAME_NOT_FOUND_ERROR);
        }
        if (hasUserByUsername(users.getUsername())) {
            throw new ResultException(ResultCode.USER_USERNAME_EXISTED_ERROR);
        }
        if(StringUtils.isEmpty(String.valueOf(users.getSecretClass()))){
            throw new ResultException(ResultCode.USER_SECRETCLASS_NOT_FOUND_ERROR);
        }
        users.setPassword(new BCryptPasswordEncoder().encode("123456"));

        if(StringUtils.isEmpty(roleName)){
            throw new ResultException(ResultCode.USER_ROLE_NOT_FOUND_ERROR);
        }
        Set<Role> roleSet = new HashSet<>();
        roleSet.add(roleService.getRoleByName(roleName));
        users.setRoleEntities(roleSet);
        if(StringUtils.isEmpty(String.valueOf(users.getSecretClass()))){
            users.setSecretClass(0);
        }

        if(!departmentService.hasDepartmentByName(departmentName)){
            throw new ResultException(ResultCode.USER_DEPARTMENT_ARGS_NOT_FOUND_ERROR);
        }
        Department department = departmentService.getDepartmentByName(departmentName);
        users.setDepartment(department);
        return userRepository.save(users);
    }

    // 查询所有用户
    public List<Users> getAll() {
        return userRepository.findByDeleted(false);
    }

    // 根据用户名查询用户是否存在
    public boolean hasUserByUsername(String username) {
        if (StringUtils.isEmpty(username)) {
            return false;
        }
        return userRepository.existsByUsernameAndDeleted(username, false);
    }

    // 根据用户名查询用户
    @Cacheable(value = "User_Cache", key = "#username")
    public Users getUserByUsername(String username) {
        if (!hasUserByUsername(username)) {
            throw new ResultException(ResultCode.USER_USERNAME_NOT_FOUND_ERROR);
        }
        return userRepository.findByUsernameAndDeleted(username, false).get();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getUserByUsername(username);
    }

    // 根据Id查询用户是否存在
    public boolean hasUserById(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return false;
        }
        return userRepository.existsById(userId);
    }

    // 根据id查询用户
    @Cacheable(value = "User_Cache", key = "#userId")
    public Users getUserById(String userId) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        return userRepository.findById(userId).get();
    }

    // 根据id修改用户
    @CachePut(value = "User_Cache", key = "#userId")
    public Users updateUserByUserId(String userId, Users usersArgs) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        Users users = getUserById(userId);
        if(usersArgs == null){
            throw new ResultException(ResultCode.USER_ARGS_NOT_FOUND_ERROR);
        }

        if(!StringUtils.isEmpty(usersArgs.getUsername()) && !users.getUsername().equals(usersArgs.getUsername())){
            if (hasUserByUsername(usersArgs.getUsername())) {
                throw new ResultException(ResultCode.USER_USERNAME_EXISTED_ERROR);
            }
            users.setUsername(usersArgs.getUsername());
        }

        if(!StringUtils.isEmpty(usersArgs.getRealName()) && !users.getRealName().equals(usersArgs.getRealName())){
            users.setRealName(usersArgs.getRealName());
        }

        return userRepository.save(users);
    }

    // 根据id删除用户
    @CacheEvict(value = "User_Cache", key = "#userId")
    public Users deleteByUserId(String userId) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        Users users = getUserById(userId);
        List<SubDepotFile> subDepotFiles = sublibraryFilesRepository.findByUsers(users);
        if(subDepotFiles.size() > 0){             // 用户为子库文件上传者时则假删除，否则直接删除
            users.setEnabled(false);
            users.setDeleted(true);
            userRepository.save(users);
        }else {
            List<Project> projectList = projectRepository.findByPicOrCreator(users, users);
            if(projectList.size() > 0){
                for(Project project : projectList){
                    if(project.getPic().getId().equals(userId)){
                        project.setPic(null);
                    }else {
                        project.setCreator(null);
                    }
                }
            }
            List<Subtask> subtaskList = subtaskRepository.findByUsersOrProofSetContainingOrAuditSetContainingOrCountSetContainingOrApproveSetContaining(users, users, users, users, users);
            if(subtaskList.size() > 0){
                for(Subtask subtask : subtaskList){
                    if(subtask.getUsers().getId().equals(userId)){
                        subtask.setUsers(null);
                    }
                    subtask.getProofSet().remove(users);
                    subtask.getAuditSet().remove(users);
                    subtask.getCountSet().remove(users);
                    subtask.getApproveSet().remove(users);
                }
                subtaskRepository.saveAll(subtaskList);
            }
            List<SubDepotFile> subDepotFileList = sublibraryFilesRepository.findByProofSetContainingOrAuditSetContainingOrCountSetContainingOrApproveSetContaining(users, users, users, users);
            if(subDepotFileList.size() > 0){
                for(SubDepotFile subDepotFile : subDepotFileList){
                    subDepotFile.getProofSet().remove(users);
                    subDepotFile.getAuditSet().remove(users);
                    subDepotFile.getCountSet().remove(users);
                    subDepotFile.getApproveSet().remove(users);
                }
                sublibraryFilesRepository.saveAll(subDepotFileList);
            }
            userRepository.delete(users);
        }
        return users;
    }

    // 根据id修改用户密码
    @CacheEvict(value = "User_Cache", key = "#userId")
    public Users updatePasswordById(String userId, String password) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(password)){
            throw new ResultException(ResultCode.USER_PASSWORD_ARGS_NOT_FOUND_ERROR);
        }
        Users users = getUserById(userId);
        users.setPassword(new BCryptPasswordEncoder().encode(password));
        return userRepository.save(users);
    }

    // 根据id修改用户所属部门
    public Users updateDepartmentById(String userId, String departmentId){
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        Users users = getUserById(userId);
        if(!departmentService.hasDepartmentById(departmentId)){
            throw new ResultException(ResultCode.DEPARTMENT_ID_NOT_FOUND_ERROR);
        }
        Department department = departmentService.getDepartmentById(departmentId);
        users.setDepartment(department);
        return userRepository.save(users);
    }

    // 安全保密员修改用户密级
    @CacheEvict(value = "User_Cache", key = "#userId")
    public Users updateSecretClassById(String userId, int secretClass) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(secretClass)){
            throw new ResultException(ResultCode.USER_SECRETCLASS_NOT_FOUND_ERROR);
        }
        Users users = getUserById(userId);
        users.setSecretClass(secretClass);
        return userRepository.save(users);
    }

    // 根据id分配用户权限,  一次只能分配一个角色, 再次传入角色的时候累加
    @CacheEvict(value = "User_Cache", key = "#userId")
    public Users distributeUserById(String userId, String[] ids) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        Users users = getUserById(userId);

        List<Role> roleList = new ArrayList<>();
        if(ids.length == 0){
            throw new ResultException(ResultCode.USER_ROLE_NOT_FOUND_ERROR);
        }
        for (String id : ids) {
            roleList.add(roleService.getRoleById(id));
        }

        HashSet<Role> roleHashSet = new HashSet<>(roleList);

        users.setRoleEntities(roleHashSet);
        return userRepository.save(users);
    }

    // 根据id禁用或解除禁用
    @CacheEvict(value = "User_Cache", key = "#userId")
    public Users assignUserById(String userId, Boolean enabled) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(enabled)){
            throw new ResultException(ResultCode.USER_ENABLED_NOT_SUPPORT_ERROR);
        }
        Users users = getUserById(userId);
        users.setEnabled(enabled);
        return userRepository.save(users);
    }
}
