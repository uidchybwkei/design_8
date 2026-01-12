package com.port.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.port.dto.FacilityDTO;
import com.port.entity.Facility;
import com.port.entity.FacilityCategory;
import com.port.exception.BusinessException;
import com.port.mapper.FacilityCategoryMapper;
import com.port.mapper.FacilityMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class FacilityService {

    @Autowired
    private FacilityMapper facilityMapper;

    @Autowired
    private FacilityCategoryMapper categoryMapper;

    public List<Facility> list() {
        LambdaQueryWrapper<Facility> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Facility::getDeleted, 0);
        wrapper.orderByDesc(Facility::getCreateTime);
        return facilityMapper.selectList(wrapper);
    }

    public IPage<Facility> pageList(Integer pageNum, Integer pageSize, Long categoryId, Integer status, String keyword) {
        Page<Facility> page = new Page<>(pageNum, pageSize);
        return facilityMapper.selectPageWithCategory(page, categoryId, status, keyword);
    }

    public Facility getById(Long id) {
        Facility facility = facilityMapper.selectById(id);
        if (facility == null) {
            throw new BusinessException("设施不存在");
        }
        if (facility.getCategoryId() != null) {
            FacilityCategory category = categoryMapper.selectById(facility.getCategoryId());
            if (category != null) {
                facility.setCategoryName(category.getCategoryName());
            }
        }
        return facility;
    }

    public Facility getByCode(String code) {
        Facility facility = facilityMapper.selectByCode(code);
        if (facility == null) {
            throw new BusinessException(404, "设施不存在或已删除");
        }
        if (facility.getStatus() == 0) {
            throw new BusinessException(400, "该设施已停用");
        }
        return facility;
    }

    public Facility getByCodeAllowDisabled(String code) {
        Facility facility = facilityMapper.selectByCode(code);
        if (facility == null) {
            throw new BusinessException(404, "设施不存在或已删除");
        }
        return facility;
    }

    public Facility create(FacilityDTO dto, Long userId) {
        if (dto.getFacilityName() == null || dto.getFacilityName().trim().isEmpty()) {
            throw new BusinessException("设施名称不能为空");
        }

        Facility facility = new Facility();
        BeanUtils.copyProperties(dto, facility);

        String code = generateFacilityCode();
        facility.setFacilityCode(code);
        facility.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        facility.setCreateBy(userId);
        facility.setCreateTime(LocalDateTime.now());

        facilityMapper.insert(facility);
        return facility;
    }

    public Facility update(FacilityDTO dto, Long userId) {
        if (dto.getId() == null) {
            throw new BusinessException("设施ID不能为空");
        }

        Facility existing = facilityMapper.selectById(dto.getId());
        if (existing == null) {
            throw new BusinessException("设施不存在");
        }

        if (dto.getFacilityName() != null) {
            existing.setFacilityName(dto.getFacilityName());
        }
        if (dto.getCategoryId() != null) {
            existing.setCategoryId(dto.getCategoryId());
        }
        if (dto.getLocation() != null) {
            existing.setLocation(dto.getLocation());
        }
        if (dto.getSpecification() != null) {
            existing.setSpecification(dto.getSpecification());
        }
        if (dto.getManufacturer() != null) {
            existing.setManufacturer(dto.getManufacturer());
        }
        if (dto.getInstallDate() != null) {
            existing.setInstallDate(dto.getInstallDate());
        }
        if (dto.getWarrantyDate() != null) {
            existing.setWarrantyDate(dto.getWarrantyDate());
        }
        if (dto.getRemark() != null) {
            existing.setRemark(dto.getRemark());
        }

        existing.setUpdateBy(userId);
        existing.setUpdateTime(LocalDateTime.now());

        facilityMapper.updateById(existing);
        return existing;
    }

    public void updateStatus(Long id, Integer status, Long userId) {
        Facility facility = facilityMapper.selectById(id);
        if (facility == null) {
            throw new BusinessException("设施不存在");
        }

        facility.setStatus(status);
        facility.setUpdateBy(userId);
        facility.setUpdateTime(LocalDateTime.now());
        facilityMapper.updateById(facility);
    }

    public void delete(Long id) {
        Facility facility = facilityMapper.selectById(id);
        if (facility == null) {
            throw new BusinessException("设施不存在");
        }
        facilityMapper.deleteById(id);
    }

    public List<FacilityCategory> listCategories() {
        LambdaQueryWrapper<FacilityCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FacilityCategory::getStatus, 1);
        wrapper.orderByAsc(FacilityCategory::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    private String generateFacilityCode() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String randomSuffix = IdUtil.simpleUUID().substring(0, 6).toUpperCase();
        return "FAC" + datePrefix + randomSuffix;
    }
}
