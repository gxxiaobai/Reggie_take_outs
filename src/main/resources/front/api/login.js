function loginApi(data) {
    return $axios({
      'url': '/user/login',
      'method': 'post',
      data
    })
}

function sendMsgApi(data) {
    return $axios({
        'url': '/user/sendMsg',
        'method': 'post',
        data
    })
}

function loginoutApi() {
  return $axios({
    'url': '/user/loginout',
    'method': 'post',
  })
}

function queryUser() {
    return $axios({
        'url': '/user/queryUser',
        'method': 'get',
    })
}

function updateUser(form) {
    return $axios({
        'url': '/user',
        'method': 'put',
        data:{ ...form}
    })
}

  