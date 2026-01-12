package com.port.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.port.entity.WorkOrderConsumption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WorkOrderConsumptionMapper extends BaseMapper<WorkOrderConsumption> {

    @Select("SELECT c.*, i.unit FROM work_order_consumption c " +
            "LEFT JOIN inventory_item i ON c.item_id = i.id " +
            "WHERE c.order_id = #{orderId} ORDER BY c.create_time DESC")
    List<WorkOrderConsumption> selectByOrderId(Long orderId);

    @Select("SELECT c.*, i.unit FROM work_order_consumption c " +
            "LEFT JOIN inventory_item i ON c.item_id = i.id " +
            "WHERE c.order_id = #{orderId} AND c.item_id = #{itemId}")
    WorkOrderConsumption selectByOrderAndItem(Long orderId, Long itemId);
}
