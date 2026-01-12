package com.port.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.port.entity.WorkOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WorkOrderMapper extends BaseMapper<WorkOrder> {

    @Select("<script>" +
            "SELECT * FROM work_order WHERE deleted = 0 " +
            "<if test='status != null'> AND status = #{status} </if>" +
            "<if test='facilityId != null'> AND facility_id = #{facilityId} </if>" +
            "<if test='assigneeId != null'> AND assignee_id = #{assigneeId} </if>" +
            "<if test='reporterId != null'> AND reporter_id = #{reporterId} </if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (order_no LIKE CONCAT('%',#{keyword},'%') OR facility_name LIKE CONCAT('%',#{keyword},'%')) </if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    IPage<WorkOrder> selectPageByCondition(Page<WorkOrder> page,
                                           @Param("status") Integer status,
                                           @Param("facilityId") Long facilityId,
                                           @Param("assigneeId") Long assigneeId,
                                           @Param("reporterId") Long reporterId,
                                           @Param("keyword") String keyword);

    @Select("SELECT * FROM work_order WHERE deleted = 0 AND facility_id = #{facilityId} AND status >= 4 ORDER BY create_time DESC")
    List<WorkOrder> selectHistoryByFacilityId(@Param("facilityId") Long facilityId);

    @Select("<script>" +
            "SELECT * FROM work_order WHERE deleted = 0 AND assignee_id = #{assigneeId} " +
            "<if test='statusList != null and statusList.size() > 0'>" +
            "AND status IN <foreach collection='statusList' item='s' open='(' separator=',' close=')'>#{s}</foreach>" +
            "</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<WorkOrder> selectByAssigneeAndStatus(@Param("assigneeId") Long assigneeId,
                                              @Param("statusList") List<Integer> statusList);
}
