import request from '@/utils/request'

export function getMaintenancePlanList() {
  return request({
    url: '/maintenance-plan/list',
    method: 'get'
  })
}

export function getMaintenancePlanById(id) {
  return request({
    url: `/maintenance-plan/${id}`,
    method: 'get'
  })
}

export function createMaintenancePlan(data) {
  return request({
    url: '/maintenance-plan',
    method: 'post',
    data
  })
}

export function updateMaintenancePlan(id, data) {
  return request({
    url: `/maintenance-plan/${id}`,
    method: 'put',
    data
  })
}

export function enableMaintenancePlan(id) {
  return request({
    url: `/maintenance-plan/${id}/enable`,
    method: 'post'
  })
}

export function disableMaintenancePlan(id) {
  return request({
    url: `/maintenance-plan/${id}/disable`,
    method: 'post'
  })
}

export function deleteMaintenancePlan(id) {
  return request({
    url: `/maintenance-plan/${id}`,
    method: 'delete'
  })
}

export function triggerGenerate() {
  return request({
    url: '/maintenance-plan/trigger-generate',
    method: 'post'
  })
}

export function generateNow(id) {
  return request({
    url: `/maintenance-plan/${id}/generate-now`,
    method: 'post'
  })
}
