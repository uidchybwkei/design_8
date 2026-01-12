package com.port.controller;

import com.port.common.Result;
import com.port.entity.*;
import com.port.service.InventoryService;
import com.port.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private UserService userService;

    @GetMapping("/item/list")
    public Result<List<InventoryItem>> getItemList() {
        return Result.success(inventoryService.getAllItems());
    }

    @GetMapping("/item/{id}")
    public Result<InventoryItem> getItemById(@PathVariable Long id) {
        return Result.success(inventoryService.getItemById(id));
    }

    @GetMapping("/item/warning")
    public Result<List<InventoryItem>> getWarningList() {
        return Result.success(inventoryService.getWarningList());
    }

    @PostMapping("/item")
    public Result<InventoryItem> createItem(@RequestBody InventoryItem item) {
        return Result.success(inventoryService.createItem(item));
    }

    @PutMapping("/item/{id}")
    public Result<InventoryItem> updateItem(@PathVariable Long id, @RequestBody InventoryItem item) {
        return Result.success(inventoryService.updateItem(id, item));
    }

    @PostMapping("/item/{id}/enable")
    public Result<Void> enableItem(@PathVariable Long id) {
        inventoryService.enableItem(id);
        return Result.success(null);
    }

    @PostMapping("/item/{id}/disable")
    public Result<Void> disableItem(@PathVariable Long id) {
        inventoryService.disableItem(id);
        return Result.success(null);
    }

    @DeleteMapping("/item/{id}")
    public Result<Void> deleteItem(@PathVariable Long id) {
        inventoryService.deleteItem(id);
        return Result.success(null);
    }

    @PostMapping("/stock-in")
    public Result<InventoryRecord> stockIn(@RequestBody Map<String, Object> params, @RequestHeader("Authorization") String token) {
        Long itemId = Long.valueOf(params.get("itemId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());
        String reason = params.get("reason") != null ? params.get("reason").toString() : null;

        User operator = userService.getCurrentUser(token.replace("Bearer ", ""));
        InventoryRecord record = inventoryService.stockIn(itemId, quantity, reason, operator.getId(), operator.getRealName());
        return Result.success(record);
    }

    @PostMapping("/stock-out")
    public Result<InventoryRecord> stockOut(@RequestBody Map<String, Object> params, @RequestHeader("Authorization") String token) {
        Long itemId = Long.valueOf(params.get("itemId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());
        String reason = params.get("reason") != null ? params.get("reason").toString() : null;
        Long orderId = params.get("orderId") != null ? Long.valueOf(params.get("orderId").toString()) : null;

        User operator = userService.getCurrentUser(token.replace("Bearer ", ""));
        InventoryRecord record = inventoryService.stockOut(itemId, quantity, reason, orderId, operator.getId(), operator.getRealName());
        return Result.success(record);
    }

    @GetMapping("/record/item/{itemId}")
    public Result<List<InventoryRecord>> getRecordsByItemId(@PathVariable Long itemId) {
        return Result.success(inventoryService.getRecordsByItemId(itemId));
    }

    @GetMapping("/record/recent")
    public Result<List<InventoryRecord>> getRecentRecords(@RequestParam(defaultValue = "50") int limit) {
        return Result.success(inventoryService.getRecentRecords(limit));
    }

    @PostMapping("/consumption")
    public Result<WorkOrderConsumption> addConsumption(@RequestBody Map<String, Object> params, @RequestHeader("Authorization") String token) {
        Long orderId = Long.valueOf(params.get("orderId").toString());
        Long itemId = Long.valueOf(params.get("itemId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());

        User operator = userService.getCurrentUser(token.replace("Bearer ", ""));
        WorkOrderConsumption consumption = inventoryService.addConsumption(orderId, itemId, quantity, operator.getId(), operator.getRealName());
        return Result.success(consumption);
    }

    @GetMapping("/consumption/order/{orderId}")
    public Result<List<WorkOrderConsumption>> getConsumptionsByOrderId(@PathVariable Long orderId) {
        return Result.success(inventoryService.getConsumptionsByOrderId(orderId));
    }

    @DeleteMapping("/consumption/{id}")
    public Result<Void> deleteConsumption(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        User operator = userService.getCurrentUser(token.replace("Bearer ", ""));
        inventoryService.deleteConsumption(id, operator.getId(), operator.getRealName());
        return Result.success(null);
    }
}
