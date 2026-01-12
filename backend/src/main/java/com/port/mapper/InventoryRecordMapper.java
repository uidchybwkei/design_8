package com.port.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.port.entity.InventoryRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InventoryRecordMapper extends BaseMapper<InventoryRecord> {

    @Select("SELECT * FROM inventory_record WHERE item_id = #{itemId} ORDER BY create_time DESC")
    List<InventoryRecord> selectByItemId(Long itemId);

    @Select("SELECT * FROM inventory_record WHERE order_id = #{orderId} ORDER BY create_time DESC")
    List<InventoryRecord> selectByOrderId(Long orderId);

    @Select("SELECT * FROM inventory_record ORDER BY create_time DESC LIMIT #{limit}")
    List<InventoryRecord> selectRecentRecords(int limit);
}
