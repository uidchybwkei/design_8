import request from '@/utils/request'

export function getWorkOrderPage(params) {
  return request({
    url: '/workorder/page',
    method: 'get',
    params
  })
}

export function getWorkOrderById(id) {
  return request({
    url: `/workorder/${id}`,
    method: 'get'
  })
}

export function getWorkOrderLogs(id) {
  return request({
    url: `/workorder/${id}/logs`,
    method: 'get'
  })
}

export function assignWorkOrder(id, assigneeId) {
  return request({
    url: `/workorder/${id}/assign`,
    method: 'post',
    data: { assigneeId }
  })
}

export function reassignWorkOrder(id, assigneeId) {
  return request({
    url: `/workorder/${id}/reassign`,
    method: 'post',
    data: { assigneeId }
  })
}

export function verifyWorkOrder(id, verifyRemark) {
  return request({
    url: `/workorder/${id}/verify`,
    method: 'post',
    data: { verifyRemark }
  })
}

export function archiveWorkOrder(id) {
  return request({
    url: `/workorder/${id}/archive`,
    method: 'post'
  })
}

export function getAssignableUsers() {
  return request({
    url: '/workorder/assignable-users',
    method: 'get'
  })
}

export function getFacilityHistory(facilityId) {
  return request({
    url: `/workorder/facility/${facilityId}/history`,
    method: 'get'
  })
}
