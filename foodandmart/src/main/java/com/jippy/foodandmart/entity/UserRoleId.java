package com.jippy.foodandmart.entity;//package com.jippy.merchant.entity;
//
//import java.io.Serializable;
//import java.util.Objects;
//
//public class UserRoleId implements Serializable {
//    private Integer userId; // Matches the field name in the Entity
//    private Integer roleId; // Matches the field name in the Entity
//
//    public UserRoleId() {}
//
//    public UserRoleId(Integer user, Integer role) {
//        this.userId = user;
//        this.roleId = role;
//    }
//
//    // Must implement equals and hashCode
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof UserRoleId)) return false;
//        UserRoleId that = (UserRoleId) o;
//        return Objects.equals(userId, that.userId) && Objects.equals(roleId, that.roleId);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(userId, roleId);
//    }
//}
