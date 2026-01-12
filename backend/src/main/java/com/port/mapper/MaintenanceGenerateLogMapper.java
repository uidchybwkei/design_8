package com.port.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.port.entity.MaintenanceGenerateLog;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

public interface MaintenanceGenerateLogMapper extends BaseMapper<MaintenanceGenerateLog> {

    @Select("SELECT * FROM maintenance_generate_log WHERE plan_id = #{planId} AND generate_date = #{generateDate}")
    MaintenanceGenerateLog findByPlanAndDate(Long planId, LocalDate generateDate);
}
