<template>
  <div>
    <h3>患者首页</h3>
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="8"
        ><el-card
          ><template #header>预约挂号</template>
          <p style="margin-bottom: 16px; color: #606266; line-height: 1.6">在线预约专家号源，方便快捷</p>
          <el-button type="primary" @click="$router.push('/patient/book')"
            >立即挂号</el-button
          ></el-card
        ></el-col
      >
      <el-col :span="8"
        ><el-card
          ><template #header>我的挂号</template>
          <p style="margin-bottom: 16px; color: #606266; line-height: 1.6">查看挂号记录和支付状态</p>
          <el-button
            type="success"
            @click="$router.push('/patient/my-registrations')"
            >查看记录</el-button
          ></el-card
        ></el-col
      >
      <el-col :span="8"
        ><el-card
          ><template #header>个人信息</template>
          <p style="margin-bottom: 16px; color: #606266; line-height: 1.6">管理个人资料和密码</p>
          <el-button @click="infoVisible = true">修改信息</el-button></el-card
        ></el-col
      >
    </el-row>

    <el-dialog v-model="infoVisible" title="修改个人信息" width="500px">
      <el-form :model="form" :rules="infoRules" ref="infoFormRef" label-width="80px">
        <el-form-item label="姓名" prop="name"
          ><el-input v-model="form.name"
        /></el-form-item>
        <el-form-item label="性别"
          ><el-radio-group v-model="form.gender"
            ><el-radio :value="1">男</el-radio
            ><el-radio :value="0">女</el-radio></el-radio-group
          ></el-form-item
        >
        <el-form-item label="年龄"
          ><el-input-number v-model="form.age" :min="0" :max="150"
        /></el-form-item>
        <el-form-item label="手机号" prop="phone"
          ><el-input v-model="form.phone"
        /></el-form-item>
        <el-form-item label="住址"
          ><el-input v-model="form.address"
        /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="infoVisible = false">取消</el-button>
        <el-button type="primary" @click="updateInfo" :loading="updating"
          >保存</el-button
        >
      </template>
    </el-dialog>

    <el-dialog v-model="pwdVisible" title="修改密码" width="400px">
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef">
        <el-form-item label="旧密码" prop="oldPassword"
          ><el-input
            v-model="pwdForm.oldPassword"
            type="password"
            show-password
        /></el-form-item>
        <el-form-item label="新密码" prop="newPassword"
          ><el-input
            v-model="pwdForm.newPassword"
            type="password"
            show-password
        /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="changePwd" :loading="changingPwd"
          >确认修改</el-button
        >
      </template>
    </el-dialog>
    <el-button style="margin-top: 20px" @click="pwdVisible = true"
      >修改密码</el-button
    >
  </div>
</template>

<script setup>
import { ref, reactive, watch } from "vue";
import { useUserStore } from "@/stores/user";
import { updateCurrentUser } from "@/api/user";
import { changePassword } from "@/api/auth";
import { ElMessage } from "element-plus";

const userStore = useUserStore();
const infoVisible = ref(false);
const pwdVisible = ref(false);
const updating = ref(false);
const changingPwd = ref(false);
const pwdFormRef = ref();
const infoFormRef = ref();

const form = reactive({
  name: "",
  gender: 1,
  age: null,
  phone: "",
  address: "",
});

const infoRules = {
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: "手机号格式不正确", trigger: "blur" },
  ],
};

watch(infoVisible, (val) => {
  if (val) {
    form.name = userStore.userInfo?.name || "";
    form.gender = userStore.userInfo?.gender ?? 1;
    form.age = userStore.userInfo?.age || null;
    form.phone = userStore.userInfo?.phone || "";
    form.address = userStore.userInfo?.address || "";
  }
});

const pwdForm = reactive({ oldPassword: "", newPassword: "" });
const pwdRules = {
  oldPassword: [{ required: true, message: "请输入旧密码", trigger: "blur" }],
  newPassword: [
    {
      required: true,
      min: 6,
      max: 20,
      message: "密码长度6-20位",
      trigger: "blur",
    },
  ],
};

async function updateInfo() {
  const valid = await infoFormRef.value.validate().catch(() => false);
  if (!valid) return;
  updating.value = true;
  try {
    await updateCurrentUser(form);
    ElMessage.success("信息修改成功");
    infoVisible.value = false;
    userStore.fetchUserInfo();
  } finally {
    updating.value = false;
  }
}

async function changePwd() {
  const valid = await pwdFormRef.value.validate().catch(() => false);
  if (!valid) return;
  changingPwd.value = true;
  try {
    await changePassword(pwdForm);
    ElMessage.success("密码修改成功");
    pwdVisible.value = false;
  } finally {
    changingPwd.value = false;
  }
}
</script>
