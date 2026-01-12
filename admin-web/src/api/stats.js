import request from '@/utils/request'

export function getWorkOrderOverview(params) {
  return request({
    url: '/stats/workorder/overview',
    method: 'get',
    params
  })
}

export function getAverageDuration(params) {
  return request({
    url: '/stats/workorder/duration',
    method: 'get',
    params
  })
}

export function getTopFaultFacilities(params) {
  return request({
    url: '/stats/facility/top-fault',
    method: 'get',
    params
  })
}
