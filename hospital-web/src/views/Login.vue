<template>
  <div class="login-container">
    <div class="login-left">
      <div class="login-left-content">
        <h1>医院专家挂号系统</h1>
        <p>专业、便捷、高效的在线预约平台</p>
        <div class="features">
          <div class="feature-item">
            <el-icon><Calendar /></el-icon>
            <span>在线预约挂号</span>
          </div>
          <div class="feature-item">
            <el-icon><UserFilled /></el-icon>
            <span>专家门诊查询</span>
          </div>
          <div class="feature-item">
            <el-icon><Document /></el-icon>
            <span>电子病历管理</span>
          </div>
        </div>
      </div>
    </div>
    <div class="login-right">
      <el-card class="login-card" shadow="hover">
        <div class="login-header">
          <div class="login-logo">
            <el-icon :size="36"><Plus /></el-icon>
          </div>
          <h2>欢迎登录</h2>
          <p>请输入您的账户信息</p>
        </div>
        <el-form :model="form" :rules="rules" ref="formRef">
          <el-form-item prop="idCard">
            <el-input
              v-model="form.idCard"
              placeholder="身份证号/管理员账号"
              prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              prefix-icon="Lock"
              show-password
              size="large"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              style="width: 100%"
              size="large"
              @click="handleLogin"
              :loading="loading"
              >登 录</el-button
            >
          </el-form-item>
        </el-form>
        <div style="text-align: center">
          <el-link type="primary" @click="$router.push('/register')"
            >没有账号？立即注册</el-link
          >
        </div>
      </el-card>
    </div>

    <el-dialog
      v-model="pwdVisible"
      title="首次登录，请修改密码"
      width="420px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef">
        <el-form-item prop="newPassword">
          <el-input
            v-model="pwdForm.newPassword"
            type="password"
            placeholder="新密码（6-20位）"
            show-password
            size="large"
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="pwdForm.confirmPassword"
            type="password"
            placeholder="确认新密码"
            show-password
            size="large"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleForceChangePwd" :loading="changingPwd" style="width: 100%">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { login, forceChangePassword } from "@/api/auth";
import { ElMessage } from "element-plus";
import { Calendar, UserFilled, Document, Plus } from "@element-plus/icons-vue";

const router = useRouter();
const userStore = useUserStore();
const formRef = ref();
const pwdFormRef = ref();
const loading = ref(false);
const changingPwd = ref(false);
const pwdVisible = ref(false);
const loginResult = ref(null);

const form = reactive({ idCard: "", password: "" });
const rules = {
  idCard: [{ required: true, message: "请输入身份证号", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
};

const pwdForm = reactive({ newPassword: "", confirmPassword: "" });
const pwdRules = {
  newPassword: [
    { required: true, min: 6, max: 20, message: "密码长度6-20位", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请确认新密码", trigger: "blur" },
    {
      validator: (_, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error("两次密码输入不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
};

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  loading.value = true;
  try {
    const res = await login(form);
    userStore.setLogin(res.data);
    if (res.data.mustChangePwd) {
      localStorage.setItem('mustChangePwd', '1');
      loginResult.value = res.data;
      pwdVisible.value = true;
      await nextTick();
      pwdForm.newPassword = "";
      pwdForm.confirmPassword = "";
    } else {
      localStorage.removeItem('mustChangePwd');
      const roleRoutes = { 0: "/patient/dashboard", 1: "/doctor/dashboard", 2: "/admin/dashboard" };
      await router.replace(roleRoutes[res.data.role] || "/");
      ElMessage.success("登录成功");
    }
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false;
  }
}

async function handleForceChangePwd() {
  const valid = await pwdFormRef.value.validate().catch(() => false);
  if (!valid) return;
  changingPwd.value = true;
  try {
    await forceChangePassword({ newPassword: pwdForm.newPassword });
    localStorage.removeItem('mustChangePwd');
    pwdVisible.value = false;
    ElMessage.success("密码修改成功");
    const roleRoutes = { 0: "/patient/dashboard", 1: "/doctor/dashboard", 2: "/admin/dashboard" };
    await router.replace(roleRoutes[loginResult.value.role] || "/");
  } finally {
    changingPwd.value = false;
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
}
.login-left {
  flex: 1;
  background: linear-gradient(135deg, #0d9488 0%, #0f766e 50%, #134e4a 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.login-left-content {
  text-align: center;
  padding: 40px;
}
.login-left-content h1 {
  font-size: 32px;
  margin-bottom: 12px;
}
.login-left-content > p {
  font-size: 16px;
  opacity: 0.85;
  margin-bottom: 40px;
}
.features {
  display: flex;
  flex-direction: column;
  gap: 20px;
  text-align: left;
}
.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
}
.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
}
.login-card {
  width: 400px;
  padding: 30px 30px 20px;
  border-radius: 16px;
}
.login-header {
  text-align: center;
  margin-bottom: 28px;
}
.login-logo {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: linear-gradient(135deg, #0d9488, #0f766e);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}
.login-header h2 {
  margin: 0 0 8px;
  font-size: 22px;
  color: #1e293b;
}
.login-header p {
  margin: 0;
  color: #94a3b8;
  font-size: 14px;
}
@media (max-width: 768px) {
  .login-left {
    display: none;
  }
  .login-card {
    width: 90%;
  }
}
</style>
