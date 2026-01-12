import request from '@/utils/request'

export function getFacilityList() {
  return request({
    url: '/facility/list',
    method: 'get'
  })
}

export function getFacilityPage(params) {
  return request({
    url: '/facility/page',
    method: 'get',
    params
  })
}

export function getFacilityById(id) {
  return request({
    url: `/facility/${id}`,
    method: 'get'
  })
}

export function getFacilityByCode(code) {
  return request({
    url: `/facility/code/${code}`,
    method: 'get'
  })
}

export function createFacility(data) {
  return request({
    url: '/facility',
    method: 'post',
    data
  })
}

export function updateFacility(data) {
  return request({
    url: '/facility',
    method: 'put',
    data
  })
}

export function updateFacilityStatus(id, status) {
  return request({
    url: `/facility/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export function deleteFacility(id) {
  return request({
    url: `/facility/${id}`,
    method: 'delete'
  })
}

export function getCategories() {
  return request({
    url: '/facility/categories',
    method: 'get'
  })
}

export function getQrCodeContent(id) {
  return request({
    url: `/facility/qrcode/${id}`,
    method: 'get'
  })
}
