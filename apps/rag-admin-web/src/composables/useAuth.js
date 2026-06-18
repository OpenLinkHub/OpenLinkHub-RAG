import { ref } from 'vue';
import { request } from '../api/request.js';

const token = ref(localStorage.getItem('rag_admin_token') || '');
const user = ref(null);

export function useAuth() {
  async function login(credentials) {
    const result = await request('/auth/login', {
      method: 'POST',
      body: credentials,
      auth: false
    });
    token.value = result.token;
    user.value = result.user;
    localStorage.setItem('rag_admin_token', token.value);
    return result;
  }

  async function fetchUser() {
    user.value = await request('/auth/me');
    return user.value;
  }

  function logout() {
    token.value = '';
    user.value = null;
    localStorage.removeItem('rag_admin_token');
  }

  function isAuthenticated() {
    return Boolean(token.value);
  }

  return {
    token,
    user,
    login,
    fetchUser,
    logout,
    isAuthenticated
  };
}
