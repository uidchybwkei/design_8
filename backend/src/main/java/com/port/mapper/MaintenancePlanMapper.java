package com.port.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.port.entity.MaintenancePlan;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

public interface MaintenancePlanMapper extends BaseMapper<MaintenancePlan> {

    @Select("SELECT * FROM maintenance_plan WHERE deleted = 0 AND status = 1 AND next_generate_date <= #{today}")
    List<MaintenancePlan> findDuePlans(LocalDate today);

    @Select("SELECT * FROM maintenance_plan WHERE deleted = 0 ORDER BY create_time DESC")
    List<MaintenancePlan> findAll();

    @Select("SELECT * FROM maintenance_plan WHERE deleted = 0 AND facility_id = #{facilityId}")
    List<MaintenancePlan> findByFacilityId(Long facilityId);
}
