import request from '../utils/request.js'

export function getMyStats() {
  return request({
    url: '/stats/user/my',
    method: 'GET'
  })
}
