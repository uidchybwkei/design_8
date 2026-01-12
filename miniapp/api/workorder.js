import request from '../utils/request.js'

export function reportFault(data) {
  return request({
    url: '/workorder/report',
    method: 'POST',
    data
  })
}

export function getMyOrders(statusList) {
  return request({
    url: '/workorder/my',
    method: 'GET',
    data: { statusList }
  })
}

export function getOrderById(id) {
  return request({
    url: `/workorder/${id}`,
    method: 'GET'
  })
}

export function getOrderLogs(id) {
  return request({
    url: `/workorder/${id}/logs`,
    method: 'GET'
  })
}

export function acceptOrder(id) {
  return request({
    url: `/workorder/${id}/accept`,
    method: 'POST'
  })
}

export function submitOrder(id, data) {
  return request({
    url: `/workorder/${id}/submit`,
    method: 'POST',
    data
  })
}

export function uploadFile(filePath) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    uni.uploadFile({
      url: 'http://localhost:8080/file/upload',
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const data = JSON.parse(res.data)
          if (data.code === 200) {
            resolve(data.data)
          } else {
            reject(new Error(data.message || '上传失败'))
          }
        } else {
          reject(new Error('上传失败'))
        }
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}
