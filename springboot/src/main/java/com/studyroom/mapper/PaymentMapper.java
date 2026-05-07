package com.studyroom.mapper;

import com.studyroom.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PaymentMapper {
    
    Payment selectByPaymentNo(String paymentNo);
    
    List<Payment> selectByUserId(Long userId);
    
    List<Payment> selectByReservationId(Long reservationId);
    
    List<Payment> selectByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                   @Param("endTime") LocalDateTime endTime);
    
    int insert(Payment payment);
    
    int updateById(Payment payment);
    
    Payment selectById(Long id);
}
