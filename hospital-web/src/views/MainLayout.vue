<template>
  <el-container class="app-layout">
    <el-header class="app-header">
      <div class="header-left">
        <el-icon :size="24" class="header-icon"><Plus /></el-icon>
        <span class="app-title">医院专家挂号系统</span>
      </div>
      <div class="header-right">
        <el-icon><User /></el-icon>
        <span class="user-name">{{ userInfoName }}</span>
        <el-button type="danger" size="small" plain @click="handleLogout">退出</el-button>
      </div>
    </el-header>
    <el-container>
      <el-aside width="220px" class="app-aside">
        <el-menu :default-active="activeMenu" router>
          <template v-if="currentRole === 0">
            <el-menu-item index="/patient/dashboard">
              <el-icon><HomeFilled /></el-icon><span>首页</span>
            </el-menu-item>
            <el-menu-item index="/patient/book">
              <el-icon><Calendar /></el-icon><span>预约挂号</span>
            </el-menu-item>
            <el-menu-item index="/patient/my-registrations">
              <el-icon><List /></el-icon><span>我的挂号</span>
            </el-menu-item>
            <el-menu-item index="/patient/medical-records">
              <el-icon><Document /></el-icon><span>我的病历</span>
            </el-menu-item>
            <el-menu-item index="/patient/ai-chat">
              <el-icon><ChatDotRound /></el-icon><span>AI 智能问诊</span>
            </el-menu-item>
          </template>
          <template v-if="currentRole === 1">
            <el-menu-item index="/doctor/dashboard">
              <el-icon><HomeFilled /></el-icon><span>首页</span>
            </el-menu-item>
            <el-menu-item index="/doctor/my-schedules">
              <el-icon><Timer /></el-icon><span>出诊管理</span>
            </el-menu-item>
            <el-menu-item index="/doctor/medical-records">
              <el-icon><Document /></el-icon><span>病历管理</span>
            </el-menu-item>
          </template>
          <template v-if="currentRole === 2">
            <el-menu-item index="/admin/dashboard">
              <el-icon><HomeFilled /></el-icon><span>首页</span>
            </el-menu-item>
            <el-menu-item index="/admin/users">
              <el-icon><UserFilled /></el-icon><span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/schedules">
              <el-icon><Checked /></el-icon><span>出诊审核</span>
            </el-menu-item>
            <el-menu-item index="/admin/audit-logs">
              <el-icon><Notebook /></el-icon><span>操作日志</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useUserStore } from "@/stores/user";
import { logout as apiLogout } from "@/api/auth";
import { ElMessage } from "element-plus";
import { Plus, User, HomeFilled, Calendar, List, Document, Timer, UserFilled, Checked, Notebook, ChatDotRound } from "@element-plus/icons-vue";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const activeMenu = computed(() => route.path);
const userInfoName = computed(() => {
  return userStore.userInfo?.name || localStorage.getItem("userName") || "";
});
const currentRole = computed(() => {
  if (userStore.role != null) return userStore.role;
  const saved = parseInt(localStorage.getItem("userRole"));
  return !isNaN(saved) ? saved : null;
});

onMounted(() => {
  userStore.initFromStorage();
});

async function handleLogout() {
  try { await apiLogout(); } catch {}
  ElMessage.success("已退出登录");
  userStore.logout();
  router.replace("/login");
}
</script>

<style scoped>
.app-layout { min-height: 100vh; }
.app-header {
  background: linear-gradient(135deg, #0d9488 0%, #0f766e 100%) !important;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.header-icon {
  color: #fff;
}
.app-title { font-size: 18px; font-weight: bold; }
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-name { font-size: 14px; }
.app-aside {
  background: #f8fafc;
  min-height: calc(100vh - 60px);
  border-right: 1px solid #e2e8f0;
}
</style>
