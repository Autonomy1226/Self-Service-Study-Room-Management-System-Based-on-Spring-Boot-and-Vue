package com.studyroom.mapper;

import com.studyroom.entity.RechargeRecord;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface RechargeRecordMapper {
    
    List<RechargeRecord> selectByUserId(Long userId);
    
    int insert(RechargeRecord rechargeRecord);
    
    int updateById(RechargeRecord rechargeRecord);
    
    RechargeRecord selectById(Long id);
}
