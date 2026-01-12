package com.port.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.port.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
