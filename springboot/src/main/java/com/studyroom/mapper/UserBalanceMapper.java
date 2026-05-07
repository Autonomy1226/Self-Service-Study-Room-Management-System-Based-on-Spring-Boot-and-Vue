package com.studyroom.mapper;

import com.studyroom.entity.UserBalance;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserBalanceMapper {
    
    UserBalance selectByUserId(Long userId);
    
    int insert(UserBalance userBalance);
    
    int updateById(UserBalance userBalance);
    
    UserBalance selectById(Long id);
}
