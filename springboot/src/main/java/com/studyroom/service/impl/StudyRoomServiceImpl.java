package com.studyroom.service.impl;

import com.studyroom.entity.StudyRoom;
import com.studyroom.mapper.StudyRoomMapper;
import com.studyroom.service.StudyRoomService;
import com.studyroom.mapper.SeatMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class StudyRoomServiceImpl extends BaseServiceImpl<StudyRoom, StudyRoomMapper> implements StudyRoomService {
    
    @Autowired
    private SeatMapper seatMapper;
    
    @Override
    public List<StudyRoom> getByStatus(String status) {
        return baseMapper.selectByStatus(status);
    }
    
    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        baseMapper.updateStatus(id, status);
    }
    
    @Override
    public List<StudyRoom> getByCapacityRange(Integer minCapacity, Integer maxCapacity) {
        return baseMapper.selectByCapacityRange(minCapacity, maxCapacity);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        // 先删除该自习室的所有座位
        seatMapper.deleteByRoomId(id);
        // 再删除自习室
        baseMapper.deleteById(id);
    }
}