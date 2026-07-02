package com.qoobot.agent.education_agent.mapper;

import com.qoobot.agent.education_agent.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapperX<User> {

    /**
     * 根据用户名查询
     */
    @Select("SELECT * FROM edu_user WHERE username = #{username} AND deleted = 0 LIMIT 1")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据手机号查询
     */
    @Select("SELECT * FROM edu_user WHERE phone = #{phone} AND deleted = 0 LIMIT 1")
    User selectByPhone(@Param("phone") String phone);

    /**
     * 更新最后登录时间
     */
    @Update("UPDATE edu_user SET last_login_time = NOW(), update_time = NOW() WHERE id = #{id}")
    int updateLastLoginTime(@Param("id") Long id);

    /**
     * 更新学段
     */
    @Update("UPDATE edu_user SET education_stage = #{stage}, update_time = NOW() WHERE id = #{id}")
    int updateEducationStage(@Param("id") Long id, @Param("stage") String stage);

    /**
     * 更新密码
     */
    @Update("UPDATE edu_user SET password_hash = #{passwordHash}, update_time = NOW() WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);
}
