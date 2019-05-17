package com.rengu.cosimulation.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.*;

/**
 * Author: XYmar
 * Date: 2019/2/12 14:59
 */
@Entity
@Data
public class UserEntity implements UserDetails, Serializable {
    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private String username;
    @JsonIgnore
    private String password;
    private int secretClass;                       // 人员密级
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;                 // 是否可用：可用  禁用
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<RoleEntity> roleEntities;
    @ManyToOne
    private DepartmentEntity departmentEntity;               // 所属部门

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        for (RoleEntity roleEntity : roleEntities) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + roleEntity.getName().toUpperCase()));
        }
        return grantedAuthorities;
    }
}
