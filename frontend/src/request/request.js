import axios from 'axios';

// HTTP 客户端
const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 15000,
});


// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const payload = response?.data ?? {};
    const { code, info, data } = payload;
    const hasCode = code !== undefined && code !== null;
    const successCodes = [200, '200', 0, '0'];

    if (hasCode && !successCodes.includes(code)) {
      const message = info || '服务异常';
      const err = new Error(message);
      err.code = code;
      err.isBizError = true;
      return Promise.reject(err);
    }

    return data !== undefined ? data : payload;
  },
  (error) => {
    return Promise.reject(error);
  },
);

export default request;
