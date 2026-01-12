package com.port.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.port.entity.InventoryItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InventoryItemMapper extends BaseMapper<InventoryItem> {

    @Select("SELECT * FROM inventory_item WHERE deleted = 0 AND status = 1 AND current_stock <= warning_threshold ORDER BY current_stock ASC")
    List<InventoryItem> selectWarningList();

    @Select("SELECT * FROM inventory_item WHERE deleted = 0 ORDER BY create_time DESC")
    List<InventoryItem> selectAllList();

    @Select("SELECT * FROM inventory_item WHERE deleted = 0 AND item_code = #{itemCode}")
    InventoryItem selectByCode(String itemCode);
}
