<template>
  <div>
    <h3>医生首页</h3>
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12"
        ><el-card
          ><template #header>出诊管理</template>
          <p style="margin-bottom: 16px; color: #606266; line-height: 1.6">发布和管理您的出诊信息</p>
          <el-button
            type="primary"
            @click="$router.push('/doctor/my-schedules')"
            >管理出诊</el-button
          ></el-card
        ></el-col
      >
      <el-col :span="12"
        ><el-card
          ><template #header>个人信息</template>
          <p style="margin-bottom: 16px; color: #606266; line-height: 1.6">管理个人资料</p>
          <el-button @click="infoVisible = true">修改信息</el-button></el-card
        ></el-col
      >
    </el-row>

    <el-button style="margin-top: 20px" @click="pwdVisible = true">修改密码</el-button>

    <el-dialog v-model="pwdVisible" title="修改密码" width="400px">
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef">
        <el-form-item label="旧密码" prop="oldPassword"
          ><el-input v-model="pwdForm.oldPassword" type="password" show-password
        /></el-form-item>
        <el-form-item label="新密码" prop="newPassword"
          ><el-input v-model="pwdForm.newPassword" type="password" show-password
        /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="changePwd" :loading="changingPwd">确认修改</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="infoVisible" title="修改个人信息" width="500px">
      <el-form :model="form" :rules="infoRules" ref="infoFormRef" label-width="80px">
        <el-form-item label="医院" prop="hospital"
          ><el-input v-model="form.hospital"
        /></el-form-item>
        <el-form-item label="科室" prop="department"
          ><el-select v-model="form.department" placeholder="选择科室" style="width: 100%">
            <el-option v-for="d in deptOptions" :key="d" :label="d" :value="d" />
          </el-select
        /></el-form-item>
        <el-form-item label="职称"
          ><el-input v-model="form.title"
        /></el-form-item>
        <el-form-item label="专长"
          ><el-input v-model="form.specialty" type="textarea"
        /></el-form-item>
        <el-form-item label="手机号" prop="phone"
          ><el-input v-model="form.phone"
        /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="infoVisible = false">取消</el-button>
        <el-button type="primary" @click="updateInfo" :loading="updating"
          >保存</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from "vue";
import { useUserStore } from "@/stores/user";
import { updateCurrentUser } from "@/api/user";
import { changePassword } from "@/api/auth";
import { ElMessage } from "element-plus";

const deptOptions = ["内科", "外科", "儿科", "妇产科", "骨科", "眼科", "皮肤科", "神经科", "心血管科", "肿瘤科"];
const userStore = useUserStore();
const infoVisible = ref(false);
const pwdVisible = ref(false);
const updating = ref(false);
const changingPwd = ref(false);
const infoFormRef = ref();
const pwdFormRef = ref();
const form = reactive({
  hospital: "",
  department: "",
  title: "",
  specialty: "",
  phone: "",
});

const infoRules = {
  hospital: [{ required: true, message: "请输入所在医院", trigger: "blur" }],
  department: [{ required: true, message: "请输入科室", trigger: "blur" }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: "手机号格式不正确", trigger: "blur" },
  ],
};

watch(infoVisible, (val) => {
  if (val) {
    form.hospital = userStore.userInfo?.hospital || "";
    form.department = userStore.userInfo?.department || "";
    form.title = userStore.userInfo?.title || "";
    form.specialty = userStore.userInfo?.specialty || "";
    form.phone = userStore.userInfo?.phone || "";
  }
});

const pwdForm = reactive({ oldPassword: "", newPassword: "" });
const pwdRules = {
  oldPassword: [{ required: true, message: "请输入旧密码", trigger: "blur" }],
  newPassword: [
    { required: true, min: 6, max: 20, message: "密码长度6-20位", trigger: "blur" },
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
