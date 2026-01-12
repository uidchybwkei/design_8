import request from '../utils/request.js'

export function wxLogin(code) {
  return request({
    url: '/auth/wx-login',
    method: 'POST',
    data: { code }
  })
}

export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'GET'
  })
}
