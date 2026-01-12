package com.port.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.port.entity.Facility;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FacilityMapper extends BaseMapper<Facility> {

    @Select("SELECT f.*, c.category_name FROM facility f " +
            "LEFT JOIN facility_category c ON f.category_id = c.id " +
            "WHERE f.deleted = 0 AND f.facility_code = #{code}")
    Facility selectByCode(@Param("code") String code);

    @Select("<script>" +
            "SELECT f.*, c.category_name FROM facility f " +
            "LEFT JOIN facility_category c ON f.category_id = c.id " +
            "WHERE f.deleted = 0 " +
            "<if test='categoryId != null'> AND f.category_id = #{categoryId} </if>" +
            "<if test='status != null'> AND f.status = #{status} </if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (f.facility_name LIKE CONCAT('%',#{keyword},'%') OR f.facility_code LIKE CONCAT('%',#{keyword},'%')) </if>" +
            "ORDER BY f.create_time DESC" +
            "</script>")
    IPage<Facility> selectPageWithCategory(Page<Facility> page,
                                           @Param("categoryId") Long categoryId,
                                           @Param("status") Integer status,
                                           @Param("keyword") String keyword);
}
