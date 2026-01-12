import request from '../utils/request.js'

export function getFacilityByCode(code) {
  return request({
    url: `/facility/scan/${code}`,
    method: 'GET'
  })
}

export function getFacilityById(id) {
  return request({
    url: `/facility/${id}`,
    method: 'GET'
  })
}
