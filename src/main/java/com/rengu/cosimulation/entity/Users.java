package com.rengu.cosimulation.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * Author: XYmar
 * Date: 2019/2/12 14:59
 */
@Entity
@Data
public class Users implements UserDetails, Serializable {
    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private String username;
    @JsonIgnore
    private String password;
    private int secretClass;                       // 人员密级
    private String realName;                       // 姓名
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;                 // 是否可用：可用  禁用
    private boolean deleted = false;
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roleEntities;
    @ManyToOne(fetch = FetchType.EAGER)
    private Department department;               // 所属部门

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        for (Role role : roleEntities) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()));
        }
        return grantedAuthorities;
    }
}
