package com.port.service;

import cn.hutool.core.util.IdUtil;
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
public class InventoryService {

    @Autowired
    private InventoryItemMapper itemMapper;

    @Autowired
    private InventoryRecordMapper recordMapper;

    @Autowired
    private WorkOrderConsumptionMapper consumptionMapper;

    @Autowired
    private WorkOrderMapper workOrderMapper;

    public List<InventoryItem> getAllItems() {
        return itemMapper.selectAllList();
    }

    public InventoryItem getItemById(Long id) {
        InventoryItem item = itemMapper.selectById(id);
        if (item == null || item.getDeleted() == 1) {
            throw new BusinessException("备件不存在");
        }
        return item;
    }

    public List<InventoryItem> getWarningList() {
        return itemMapper.selectWarningList();
    }

    @Transactional
    public InventoryItem createItem(InventoryItem item) {
        if (itemMapper.selectByCode(item.getItemCode()) != null) {
            throw new BusinessException("备件编码已存在");
        }
        item.setCurrentStock(item.getCurrentStock() != null ? item.getCurrentStock() : 0);
        item.setWarningThreshold(item.getWarningThreshold() != null ? item.getWarningThreshold() : 10);
        item.setStatus(InventoryItem.STATUS_ENABLED);
        item.setDeleted(0);
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        itemMapper.insert(item);
        return item;
    }

    @Transactional
    public InventoryItem updateItem(Long id, InventoryItem item) {
        InventoryItem existing = getItemById(id);
        
        if (!existing.getItemCode().equals(item.getItemCode())) {
            InventoryItem byCode = itemMapper.selectByCode(item.getItemCode());
            if (byCode != null && !byCode.getId().equals(id)) {
                throw new BusinessException("备件编码已存在");
            }
        }

        existing.setItemCode(item.getItemCode());
        existing.setItemName(item.getItemName());
        existing.setSpecification(item.getSpecification());
        existing.setUnit(item.getUnit());
        existing.setWarningThreshold(item.getWarningThreshold());
        existing.setRemark(item.getRemark());
        existing.setUpdateTime(LocalDateTime.now());
        itemMapper.updateById(existing);
        return existing;
    }

    @Transactional
    public void enableItem(Long id) {
        InventoryItem item = getItemById(id);
        item.setStatus(InventoryItem.STATUS_ENABLED);
        item.setUpdateTime(LocalDateTime.now());
        itemMapper.updateById(item);
    }

    @Transactional
    public void disableItem(Long id) {
        InventoryItem item = getItemById(id);
        item.setStatus(InventoryItem.STATUS_DISABLED);
        item.setUpdateTime(LocalDateTime.now());
        itemMapper.updateById(item);
    }

    @Transactional
    public void deleteItem(Long id) {
        InventoryItem item = getItemById(id);
        item.setDeleted(1);
        item.setUpdateTime(LocalDateTime.now());
        itemMapper.updateById(item);
    }

    @Transactional
    public InventoryRecord stockIn(Long itemId, Integer quantity, String reason, Long operatorId, String operatorName) {
        if (quantity <= 0) {
            throw new BusinessException("入库数量必须大于0");
        }

        InventoryItem item = getItemById(itemId);
        if (item.getStatus() != InventoryItem.STATUS_ENABLED) {
            throw new BusinessException("该备件已停用，无法入库");
        }

        int beforeStock = item.getCurrentStock();
        int afterStock = beforeStock + quantity;

        item.setCurrentStock(afterStock);
        item.setUpdateTime(LocalDateTime.now());
        itemMapper.updateById(item);

        InventoryRecord record = new InventoryRecord();
        record.setItemId(itemId);
        record.setItemCode(item.getItemCode());
        record.setItemName(item.getItemName());
        record.setRecordType(InventoryRecord.TYPE_IN);
        record.setQuantity(quantity);
        record.setBeforeStock(beforeStock);
        record.setAfterStock(afterStock);
        record.setReason(reason);
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setCreateTime(LocalDateTime.now());
        recordMapper.insert(record);

        return record;
    }

    @Transactional
    public InventoryRecord stockOut(Long itemId, Integer quantity, String reason, Long orderId, Long operatorId, String operatorName) {
        if (quantity <= 0) {
            throw new BusinessException("出库数量必须大于0");
        }

        InventoryItem item = getItemById(itemId);
        if (item.getStatus() != InventoryItem.STATUS_ENABLED) {
            throw new BusinessException("该备件已停用，无法出库");
        }

        int beforeStock = item.getCurrentStock();
        if (quantity > beforeStock) {
            throw new BusinessException("库存不足，当前库存: " + beforeStock + "，请求出库: " + quantity);
        }

        int afterStock = beforeStock - quantity;

        item.setCurrentStock(afterStock);
        item.setUpdateTime(LocalDateTime.now());
        itemMapper.updateById(item);

        String orderNo = null;
        if (orderId != null) {
            WorkOrder order = workOrderMapper.selectById(orderId);
            if (order != null) {
                orderNo = order.getOrderNo();
            }
        }

        InventoryRecord record = new InventoryRecord();
        record.setItemId(itemId);
        record.setItemCode(item.getItemCode());
        record.setItemName(item.getItemName());
        record.setRecordType(InventoryRecord.TYPE_OUT);
        record.setQuantity(quantity);
        record.setBeforeStock(beforeStock);
        record.setAfterStock(afterStock);
        record.setReason(reason);
        record.setOrderId(orderId);
        record.setOrderNo(orderNo);
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setCreateTime(LocalDateTime.now());
        recordMapper.insert(record);

        return record;
    }

    public List<InventoryRecord> getRecordsByItemId(Long itemId) {
        return recordMapper.selectByItemId(itemId);
    }

    public List<InventoryRecord> getRecentRecords(int limit) {
        return recordMapper.selectRecentRecords(limit);
    }

    @Transactional
    public WorkOrderConsumption addConsumption(Long orderId, Long itemId, Integer quantity, Long operatorId, String operatorName) {
        WorkOrder order = workOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        if (order.getStatus() < WorkOrder.STATUS_SUBMITTED) {
            throw new BusinessException("工单需提交处理后才能关联备件消耗");
        }

        WorkOrderConsumption existing = consumptionMapper.selectByOrderAndItem(orderId, itemId);
        if (existing != null) {
            throw new BusinessException("该工单已关联此备件，请修改现有记录");
        }

        InventoryRecord record = stockOut(itemId, quantity, "工单消耗: " + order.getOrderNo(), orderId, operatorId, operatorName);

        InventoryItem item = getItemById(itemId);

        WorkOrderConsumption consumption = new WorkOrderConsumption();
        consumption.setOrderId(orderId);
        consumption.setOrderNo(order.getOrderNo());
        consumption.setItemId(itemId);
        consumption.setItemCode(item.getItemCode());
        consumption.setItemName(item.getItemName());
        consumption.setQuantity(quantity);
        consumption.setRecordId(record.getId());
        consumption.setOperatorId(operatorId);
        consumption.setOperatorName(operatorName);
        consumption.setCreateTime(LocalDateTime.now());
        consumptionMapper.insert(consumption);

        return consumption;
    }

    public List<WorkOrderConsumption> getConsumptionsByOrderId(Long orderId) {
        return consumptionMapper.selectByOrderId(orderId);
    }

    @Transactional
    public void deleteConsumption(Long consumptionId, Long operatorId, String operatorName) {
        WorkOrderConsumption consumption = consumptionMapper.selectById(consumptionId);
        if (consumption == null) {
            throw new BusinessException("消耗记录不存在");
        }

        WorkOrder order = workOrderMapper.selectById(consumption.getOrderId());
        if (order == null) {
            throw new BusinessException("关联工单不存在");
        }
        if (order.getStatus() == WorkOrder.STATUS_ARCHIVED) {
            throw new BusinessException("已归档工单的消耗记录不可删除");
        }

        stockIn(consumption.getItemId(), consumption.getQuantity(), 
                "删除消耗记录回退: " + consumption.getOrderNo(), 
                operatorId, operatorName);

        consumptionMapper.deleteById(consumptionId);
    }

    private String generateItemCode() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = IdUtil.simpleUUID().substring(0, 4).toUpperCase();
        return "SP" + datePrefix + randomSuffix;
    }
}
