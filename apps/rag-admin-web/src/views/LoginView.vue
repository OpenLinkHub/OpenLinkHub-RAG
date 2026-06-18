<template>
  <main class="login-shell">
    <section class="login-panel">
      <p class="eyebrow">OpenLinkHub RAG Admin</p>
      <h1>知识库管理系统</h1>
      <form @submit.prevent="submit">
        <label>
          用户名
          <input v-model="loginForm.username" autocomplete="username" />
        </label>
        <label>
          密码
          <input v-model="loginForm.password" type="password" autocomplete="current-password" />
        </label>
        <button type="submit" :disabled="busy">登录</button>
      </form>
      <p class="hint">默认账号：admin / admin123</p>
      <p v-if="errorMessage" class="error-line">{{ errorMessage }}</p>
    </section>
  </main>
</template>

<script setup>
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuth } from '../composables/useAuth.js';

const route = useRoute();
const router = useRouter();
const { login } = useAuth();

const busy = ref(false);
const errorMessage = ref('');
const loginForm = ref({ username: 'admin', password: 'admin123' });

async function submit() {
  busy.value = true;
  errorMessage.value = '';
  try {
    await login(loginForm.value);
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard';
    await router.replace(redirect);
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    busy.value = false;
  }
}
</script>
