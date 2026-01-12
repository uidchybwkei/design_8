package com.port.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.port.entity.WorkOrderLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WorkOrderLogMapper extends BaseMapper<WorkOrderLog> {

    @Select("SELECT * FROM work_order_log WHERE order_id = #{orderId} ORDER BY create_time ASC")
    List<WorkOrderLog> selectByOrderId(@Param("orderId") Long orderId);
}
