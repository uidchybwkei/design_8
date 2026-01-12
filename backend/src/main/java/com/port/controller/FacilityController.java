package com.port.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.port.common.PageResult;
import com.port.common.Result;
import com.port.dto.FacilityDTO;
import com.port.entity.Facility;
import com.port.entity.FacilityCategory;
import com.port.service.FacilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/facility")
public class FacilityController {

    @Autowired
    private FacilityService facilityService;

    @GetMapping("/page")
    public Result<PageResult<Facility>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        IPage<Facility> page = facilityService.pageList(pageNum, pageSize, categoryId, status, keyword);
        PageResult<Facility> result = new PageResult<>(page.getTotal(), page.getRecords());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Facility> getById(@PathVariable Long id) {
        Facility facility = facilityService.getById(id);
        return Result.success(facility);
    }

    @GetMapping("/code/{code}")
    public Result<Facility> getByCode(@PathVariable String code) {
        Facility facility = facilityService.getByCode(code);
        return Result.success(facility);
    }

    @GetMapping("/scan/{code}")
    public Result<Facility> scanByCode(@PathVariable String code) {
        Facility facility = facilityService.getByCode(code);
        return Result.success(facility);
    }

    @PostMapping
    public Result<Facility> create(@RequestBody FacilityDTO dto,
                                   @RequestAttribute("userId") Long userId) {
        Facility facility = facilityService.create(dto, userId);
        return Result.success(facility);
    }

    @PutMapping
    public Result<Facility> update(@RequestBody FacilityDTO dto,
                                   @RequestAttribute("userId") Long userId) {
        Facility facility = facilityService.update(dto, userId);
        return Result.success(facility);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Integer status,
                                     @RequestAttribute("userId") Long userId) {
        facilityService.updateStatus(id, status, userId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        facilityService.delete(id);
        return Result.success();
    }

    @GetMapping("/categories")
    public Result<List<FacilityCategory>> listCategories() {
        List<FacilityCategory> categories = facilityService.listCategories();
        return Result.success(categories);
    }

    @GetMapping("/qrcode/{id}")
    public Result<String> getQrCodeContent(@PathVariable Long id) {
        Facility facility = facilityService.getById(id);
        return Result.success(facility.getFacilityCode());
    }
}
