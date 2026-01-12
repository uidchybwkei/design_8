package com.port.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.port.entity.*;
import com.port.exception.BusinessException;
import com.port.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class WorkOrderService {

    @Autowired
    private WorkOrderMapper workOrderMapper;

    @Autowired
    private WorkOrderLogMapper workOrderLogMapper;

    @Autowired
    private FacilityMapper facilityMapper;

    @Autowired
    private UserMapper userMapper;

    public IPage<WorkOrder> pageList(Integer pageNum, Integer pageSize, Integer status, Integer orderType,
                                     Long facilityId, Long assigneeId, Long reporterId, String keyword) {
        Page<WorkOrder> page = new Page<>(pageNum, pageSize);
        IPage<WorkOrder> result = workOrderMapper.selectPageByCondition(page, status, orderType, facilityId, assigneeId, reporterId, keyword);
        result.getRecords().forEach(o -> {
            o.setStatusName(WorkOrder.getStatusName(o.getStatus()));
            o.setOrderTypeName(WorkOrder.getOrderTypeName(o.getOrderType()));
        });
        return result;
    }

    public WorkOrder getById(Long id) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        order.setStatusName(WorkOrder.getStatusName(order.getStatus()));
        return order;
    }

    public List<WorkOrderLog> getOrderLogs(Long orderId) {
        return workOrderLogMapper.selectByOrderId(orderId);
    }

    public List<WorkOrder> getByAssignee(Long assigneeId, List<Integer> statusList) {
        List<WorkOrder> list = workOrderMapper.selectByAssigneeAndStatus(assigneeId, statusList);
        list.forEach(o -> o.setStatusName(WorkOrder.getStatusName(o.getStatus())));
        return list;
    }

    public List<WorkOrder> getFacilityHistory(Long facilityId) {
        List<WorkOrder> list = workOrderMapper.selectHistoryByFacilityId(facilityId);
        list.forEach(o -> o.setStatusName(WorkOrder.getStatusName(o.getStatus())));
        return list;
    }

    @Transactional
    public WorkOrder createFaultReport(Long facilityId, String faultDescription, String faultImages,
                                       Long reporterId, String reporterName) {
        Facility facility = facilityMapper.selectById(facilityId);
        if (facility == null) {
            throw new BusinessException("设施不存在");
        }
        if (facility.getStatus() != 1) {
            throw new BusinessException("该设施已停用，无法创建工单");
        }

        WorkOrder order = new WorkOrder();
        order.setOrderNo(generateOrderNo());
        order.setOrderType(1);
        order.setFacilityId(facilityId);
        order.setFacilityCode(facility.getFacilityCode());
        order.setFacilityName(facility.getFacilityName());
        order.setStatus(WorkOrder.STATUS_PENDING);
        order.setFaultDescription(faultDescription);
        order.setFaultImages(faultImages);
        order.setReporterId(reporterId);
        order.setReporterName(reporterName);
        order.setReportTime(LocalDateTime.now());
        order.setCreateTime(LocalDateTime.now());

        workOrderMapper.insert(order);

        saveLog(order.getId(), order.getOrderNo(), WorkOrderLog.ACTION_CREATE,
                null, WorkOrder.STATUS_PENDING, reporterId, reporterName, "上报故障");

        return order;
    }

    @Transactional
    public WorkOrder createMaintenanceOrder(Long facilityId, String description, Long planId) {
        System.out.println("===== 开始创建保养工单 =====");
        System.out.println("facilityId: " + facilityId);
        System.out.println("description: " + description);
        
        Facility facility = facilityMapper.selectById(facilityId);
        if (facility == null) {
            throw new BusinessException("设施不存在");
        }
        if (facility.getStatus() != 1) {
            throw new BusinessException("该设施已停用，无法创建保养工单");
        }

        LocalDateTime now = LocalDateTime.now();
        String orderNo = generateOrderNo();
        System.out.println("生成工单号: " + orderNo);
        
        WorkOrder order = new WorkOrder();
        order.setOrderNo(orderNo);
        order.setOrderType(2);
        order.setFacilityId(facilityId);
        order.setFacilityCode(facility.getFacilityCode());
        order.setFacilityName(facility.getFacilityName());
        order.setStatus(WorkOrder.STATUS_PENDING);
        order.setFaultDescription(description != null ? description : "定期保养");
        order.setFaultImages("[]");
        order.setReporterId(0L);
        order.setReporterName("系统");
        order.setReportTime(now);
        order.setCreateTime(now);
        order.setUpdateTime(now);

        System.out.println("准备插入工单...");
        try {
            workOrderMapper.insert(order);
            System.out.println("工单插入成功，ID: " + order.getId());
        } catch (Exception e) {
            System.err.println("工单插入失败: " + e.getMessage());
            e.printStackTrace();
            throw new BusinessException("创建保养工单失败: " + e.getMessage());
        }

        saveLog(order.getId(), order.getOrderNo(), WorkOrderLog.ACTION_CREATE,
                null, WorkOrder.STATUS_PENDING, 0L, "系统", "保养计划自动生成");

        return order;
    }

    @Transactional
    public void assign(Long orderId, Long assigneeId, Long operatorId, String operatorName) {
        WorkOrder order = getById(orderId);
        
        if (order.getStatus() != WorkOrder.STATUS_PENDING) {
            throw new BusinessException("只有待派发的工单才能派单");
        }

        User assignee = userMapper.selectById(assigneeId);
        if (assignee == null) {
            throw new BusinessException("执行人不存在");
        }

        int oldStatus = order.getStatus();
        order.setStatus(WorkOrder.STATUS_ASSIGNED);
        order.setAssigneeId(assigneeId);
        order.setAssigneeName(assignee.getRealName());
        order.setAssignTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        workOrderMapper.updateById(order);

        saveLog(orderId, order.getOrderNo(), WorkOrderLog.ACTION_ASSIGN,
                oldStatus, WorkOrder.STATUS_ASSIGNED, operatorId, operatorName,
                "派单给: " + assignee.getRealName());
    }

    @Transactional
    public void reassign(Long orderId, Long newAssigneeId, Long operatorId, String operatorName) {
        WorkOrder order = getById(orderId);
        
        if (order.getStatus() != WorkOrder.STATUS_ASSIGNED && order.getStatus() != WorkOrder.STATUS_ACCEPTED) {
            throw new BusinessException("只有已派发或已接单的工单才能转派");
        }

        User assignee = userMapper.selectById(newAssigneeId);
        if (assignee == null) {
            throw new BusinessException("执行人不存在");
        }

        int oldStatus = order.getStatus();
        order.setStatus(WorkOrder.STATUS_ASSIGNED);
        order.setAssigneeId(newAssigneeId);
        order.setAssigneeName(assignee.getRealName());
        order.setAssignTime(LocalDateTime.now());
        order.setAcceptTime(null);
        order.setUpdateTime(LocalDateTime.now());

        workOrderMapper.updateById(order);

        saveLog(orderId, order.getOrderNo(), WorkOrderLog.ACTION_ASSIGN,
                oldStatus, WorkOrder.STATUS_ASSIGNED, operatorId, operatorName,
                "转派给: " + assignee.getRealName());
    }

    @Transactional
    public void accept(Long orderId, Long userId, String userName) {
        WorkOrder order = getById(orderId);
        
        if (order.getStatus() != WorkOrder.STATUS_ASSIGNED) {
            throw new BusinessException("只有已派发的工单才能接单");
        }

        if (!order.getAssigneeId().equals(userId)) {
            throw new BusinessException("只能接自己被分配的工单");
        }

        int oldStatus = order.getStatus();
        order.setStatus(WorkOrder.STATUS_ACCEPTED);
        order.setAcceptTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        workOrderMapper.updateById(order);

        saveLog(orderId, order.getOrderNo(), WorkOrderLog.ACTION_ACCEPT,
                oldStatus, WorkOrder.STATUS_ACCEPTED, userId, userName, "接单");
    }

    @Transactional
    public void submit(Long orderId, String processDescription, String processImages,
                       Long userId, String userName) {
        WorkOrder order = getById(orderId);
        
        if (order.getStatus() != WorkOrder.STATUS_ACCEPTED) {
            throw new BusinessException("只有已接单的工单才能提交");
        }

        if (!order.getAssigneeId().equals(userId)) {
            throw new BusinessException("只能提交自己的工单");
        }

        if (processImages == null || processImages.trim().isEmpty() || "[]".equals(processImages.trim())) {
            throw new BusinessException(400, "提交时必须上传至少1张处理图片");
        }

        int oldStatus = order.getStatus();
        order.setStatus(WorkOrder.STATUS_SUBMITTED);
        order.setProcessDescription(processDescription);
        order.setProcessImages(processImages);
        order.setSubmitTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        workOrderMapper.updateById(order);

        saveLog(orderId, order.getOrderNo(), WorkOrderLog.ACTION_SUBMIT,
                oldStatus, WorkOrder.STATUS_SUBMITTED, userId, userName, "提交处理结果");
    }

    @Transactional
    public void verify(Long orderId, String verifyRemark, Long verifierId, String verifierName) {
        WorkOrder order = getById(orderId);
        
        if (order.getStatus() != WorkOrder.STATUS_SUBMITTED) {
            throw new BusinessException("只有待验收的工单才能验收");
        }

        int oldStatus = order.getStatus();
        order.setStatus(WorkOrder.STATUS_COMPLETED);
        order.setVerifyTime(LocalDateTime.now());
        order.setVerifierId(verifierId);
        order.setVerifierName(verifierName);
        order.setVerifyRemark(verifyRemark);
        order.setUpdateTime(LocalDateTime.now());

        workOrderMapper.updateById(order);

        saveLog(orderId, order.getOrderNo(), WorkOrderLog.ACTION_VERIFY,
                oldStatus, WorkOrder.STATUS_COMPLETED, verifierId, verifierName,
                "验收通过" + (verifyRemark != null ? ": " + verifyRemark : ""));
    }

    @Transactional
    public void archive(Long orderId, Long operatorId, String operatorName) {
        WorkOrder order = getById(orderId);
        
        if (order.getStatus() != WorkOrder.STATUS_COMPLETED) {
            throw new BusinessException("只有已完成的工单才能归档");
        }

        int oldStatus = order.getStatus();
        order.setStatus(WorkOrder.STATUS_ARCHIVED);
        order.setArchiveTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        workOrderMapper.updateById(order);

        saveLog(orderId, order.getOrderNo(), WorkOrderLog.ACTION_ARCHIVE,
                oldStatus, WorkOrder.STATUS_ARCHIVED, operatorId, operatorName, "归档");
    }

    public List<User> getAssignableUsers() {
        return userMapper.selectList(null);
    }

    private void saveLog(Long orderId, String orderNo, String action,
                         Integer fromStatus, Integer toStatus,
                         Long operatorId, String operatorName, String remark) {
        WorkOrderLog log = new WorkOrderLog();
        log.setOrderId(orderId);
        log.setOrderNo(orderNo);
        log.setAction(action);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        workOrderLogMapper.insert(log);
    }

    private String generateOrderNo() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomSuffix = IdUtil.simpleUUID().substring(0, 4).toUpperCase();
        return "WO" + datePrefix + randomSuffix;
    }
}
