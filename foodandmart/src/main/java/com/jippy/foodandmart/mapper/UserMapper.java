package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.entity.User;

import java.time.LocalDateTime;

/**
 * Static utility class for creating {@link User} portal-login entities.
 *
 * <p>Why a separate mapper: user creation is always a side effect of
 * merchant or outlet onboarding. Centralising the field mapping here keeps
 * the service layer clean and makes it easy to add future fields (e.g.
 * a profile picture URL) in one place.</p>
 */
public final class UserMapper {

    /**
     * Private constructor — static utility class, must not be instantiated.
     */
    private UserMapper() {}

    /**
     * Creates a new {@link User} entity for portal login.
     *
     * <p>Why accept encodedPassword instead of raw password: the service layer
     * is responsible for hashing or encoding passwords before calling this mapper.
     * The mapper has no knowledge of the encoding algorithm, keeping concerns
     * separated.</p>
     *
     * <p>Why pass roleId and userType separately: the role and user type values
     * come from {@code AppConstants} in the service layer. Having the mapper
     * accept them as plain parameters avoids coupling the mapper to AppConstants.</p>
     *
     * <p>isActive defaults to "Y" — new portal users are immediately active and
     * can log in right after onboarding.</p>
     *
     * @param username        the generated login username (trimmed by service before passing in)
     * @param encodedPassword the password (plain or encoded, depending on calling service)
     * @param roleId          the role FK (e.g. ROLE_ID_MERCHANT or ROLE_ID_OUTLET from AppConstants)
     * @param employeeId      the FK linking this user to their employee record
     * @param userType        the user type string (e.g. "MERCHANT" or "OUTLET")
     * @param createdBy       the ID of the admin who triggered this creation (audit trail)
     * @return a transient {@link User} entity ready to persist
     */
    public static User toEntity(String username, String encodedPassword, Integer roleId,
                                Integer employeeId, String userType, Integer createdBy) {
        User entity = new User();
        entity.setUsername(username != null ? username.trim() : null);
        entity.setPassword(encodedPassword);
       // entity.setRoleId(roleId);
        entity.setEmployeeId(employeeId);
        entity.setUserType(userType);
        // New users are immediately active after onboarding
        entity.setIsActive("Y");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(createdBy);
        return entity;
    }
}
