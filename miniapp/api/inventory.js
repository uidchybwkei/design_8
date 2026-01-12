import request from '../utils/request.js'

export function getItemList() {
  return request({
    url: '/inventory/item/list',
    method: 'GET'
  })
}

export function addConsumption(data) {
  return request({
    url: '/inventory/consumption',
    method: 'POST',
    data
  })
}
