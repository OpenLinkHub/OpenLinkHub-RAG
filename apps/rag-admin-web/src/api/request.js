import { API_BASE } from './config.js';

let getToken = () => localStorage.getItem('rag_admin_token') || '';

export function setTokenGetter(fn) {
  getToken = fn;
}

export async function request(path, options = {}) {
  const headers = {};
  const isForm = options.body instanceof FormData;
  if (!isForm) {
    headers['Content-Type'] = 'application/json';
  }
  const token = getToken();
  if (options.auth !== false && token) {
    headers.Authorization = `Bearer ${token}`;
  }
  let url = `${API_BASE}${path}`;
  if (options.query && Object.keys(options.query).length) {
    const params = new URLSearchParams();
    Object.entries(options.query).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params.set(key, String(value));
      }
    });
    const qs = params.toString();
    if (qs) url += `?${qs}`;
  }
  const response = await fetch(url, {
    method: options.method || 'GET',
    headers,
    body: options.body ? (isForm ? options.body : JSON.stringify(options.body)) : undefined
  });
  const payload = await response.json();
  if (!response.ok || payload.success === false) {
    throw new Error(payload.message || `HTTP ${response.status}`);
  }
  return payload.data;
}
