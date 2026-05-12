<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2 style="text-align: center; margin-bottom: 20px">用户注册</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="form.role">
            <el-radio :value="0">患者</el-radio>
            <el-radio :value="1">医生</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="18位身份证号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="6-20位密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄">
          <el-input-number v-model="form.age" :min="0" :max="150" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="11位手机号" />
        </el-form-item>
        <el-form-item label="住址">
          <el-input v-model="form.address" placeholder="联系地址" />
        </el-form-item>
        <template v-if="form.role === 1">
          <el-form-item label="医院" prop="hospital">
            <el-input v-model="form.hospital" placeholder="所在医院" />
          </el-form-item>
          <el-form-item label="科室" prop="department">
            <el-select v-model="form.department" placeholder="选择科室" style="width: 100%">
              <el-option v-for="d in deptOptions" :key="d" :label="d" :value="d" />
            </el-select>
          </el-form-item>
          <el-form-item label="职称">
            <el-input v-model="form.title" placeholder="如：主任医师" />
          </el-form-item>
          <el-form-item label="专长">
            <el-input
              v-model="form.specialty"
              type="textarea"
              placeholder="专业特长描述"
            />
          </el-form-item>
        </template>
        <el-form-item>
          <el-button
            type="primary"
            style="width: 100%"
            @click="handleRegister"
            :loading="loading"
            >注 册</el-button
          >
        </el-form-item>
      </el-form>
      <div style="text-align: center">
        <el-link type="primary" @click="$router.push('/login')"
          >已有账号？去登录</el-link
        >
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { register } from "@/api/auth";
import { ElMessage } from "element-plus";

const deptOptions = ["内科", "外科", "儿科", "妇产科", "骨科", "眼科", "皮肤科", "神经科", "心血管科", "肿瘤科"];
const router = useRouter();
const formRef = ref();
const loading = ref(false);
const form = reactive({
  role: 0,
  idCard: "",
  password: "",
  name: "",
  gender: 1,
  age: null,
  phone: "",
  address: "",
  hospital: "",
  department: "",
  title: "",
  specialty: "",
});
const rules = {
  role: [{ required: true, message: "请选择角色", trigger: "change" }],
  idCard: [
    { required: true, message: "请输入身份证号", trigger: "blur" },
    {
      pattern: /^\d{17}[\dXx]$/,
      message: "身份证号格式不正确",
      trigger: "blur",
    },
  ],
  password: [
    {
      required: true,
      min: 6,
      max: 20,
      message: "密码长度6-20位",
      trigger: "blur",
    },
  ],
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  phone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "手机号格式不正确", trigger: "blur" },
  ],
  hospital: [{ required: true, message: "请输入所在医院", trigger: "blur" }],
  department: [{ required: true, message: "请输入科室", trigger: "blur" }],
};

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  loading.value = true;
  try {
    const data = { ...form };
    if (form.role === 0) {
      delete data.hospital;
      delete data.department;
      delete data.title;
      delete data.specialty;
    }
    const res = await register(data);
    ElMessage.success(res.message || "注册成功，请等待管理员审批");
    router.push("/login");
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.register-card {
  width: 500px;
  padding: 20px;
  max-height: 90vh;
  overflow-y: auto;
}
</style>
