import request from '@/utils/request'

export function getItemList() {
  return request({
    url: '/inventory/item/list',
    method: 'get'
  })
}

export function getItemById(id) {
  return request({
    url: `/inventory/item/${id}`,
    method: 'get'
  })
}

export function getWarningList() {
  return request({
    url: '/inventory/item/warning',
    method: 'get'
  })
}

export function createItem(data) {
  return request({
    url: '/inventory/item',
    method: 'post',
    data
  })
}

export function updateItem(id, data) {
  return request({
    url: `/inventory/item/${id}`,
    method: 'put',
    data
  })
}

export function enableItem(id) {
  return request({
    url: `/inventory/item/${id}/enable`,
    method: 'post'
  })
}

export function disableItem(id) {
  return request({
    url: `/inventory/item/${id}/disable`,
    method: 'post'
  })
}

export function deleteItem(id) {
  return request({
    url: `/inventory/item/${id}`,
    method: 'delete'
  })
}

export function stockIn(data) {
  return request({
    url: '/inventory/stock-in',
    method: 'post',
    data
  })
}

export function stockOut(data) {
  return request({
    url: '/inventory/stock-out',
    method: 'post',
    data
  })
}

export function getRecordsByItemId(itemId) {
  return request({
    url: `/inventory/record/item/${itemId}`,
    method: 'get'
  })
}

export function getRecentRecords(limit = 50) {
  return request({
    url: '/inventory/record/recent',
    method: 'get',
    params: { limit }
  })
}

export function addConsumption(data) {
  return request({
    url: '/inventory/consumption',
    method: 'post',
    data
  })
}

export function getConsumptionsByOrderId(orderId) {
  return request({
    url: `/inventory/consumption/order/${orderId}`,
    method: 'get'
  })
}

export function deleteConsumption(id) {
  return request({
    url: `/inventory/consumption/${id}`,
    method: 'delete'
  })
}
