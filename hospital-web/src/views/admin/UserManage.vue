<template>
  <div>
    <h3>用户管理</h3>
    <el-card style="margin-top: 20px">
      <el-form :inline="true">
        <el-form-item label="角色"
          ><el-select v-model="filterRole" clearable placeholder="全部" style="width: 120px"
            ><el-option :value="0" label="患者" /><el-option :value="1" label="医生" /><el-option :value="2" label="管理员" /></el-select
        ></el-form-item>
        <el-form-item label="状态"
          ><el-select v-model="filterStatus" clearable placeholder="全部" style="width: 120px"
            ><el-option :value="0" label="待审批" /><el-option :value="1" label="已通过" /><el-option :value="2" label="已拒绝" /></el-select
        ></el-form-item>
        <el-form-item><el-button type="primary" @click="fetchUsers()">查询</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <span style="font-weight: bold">自动审批注册设置</span>
      </template>
      <el-form :inline="true">
        <el-form-item label="自动通过患者注册">
          <el-switch v-model="autoConfig.autoApprovePatient" />
        </el-form-item>
        <el-form-item label="自动通过医生注册">
          <el-switch v-model="autoConfig.autoApproveDoctor" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveAutoConfig" :loading="savingConfig">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 20px">
      <div style="margin-bottom: 12px">
        <el-button type="success" :disabled="selectedIds.length === 0" @click="batchApprove(1)">批量通过</el-button>
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="batchApprove(2)">批量拒绝</el-button>
        <span style="margin-left: 12px; color: #909399; font-size: 13px" v-if="selectedIds.length > 0">已选 {{ selectedIds.length }} 项</span>
      </div>
      <el-table :data="users" border stripe v-loading="loading" @selection-change="onSelectionChange" ref="tableRef">
        <el-table-column type="selection" width="45" :selectable="(row) => row.status === 0" />
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="idCard" label="身份证号" width="180" />
        <el-table-column label="角色" width="80"
          ><template #default="{ row }">{{ ["患者", "医生", "管理员"][row.role] }}</template></el-table-column
        >
        <el-table-column label="状态" width="80"
          ><template #default="{ row }"
            ><el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'danger' : 'warning'"
              >{{ ["待审批", "已通过", "已拒绝"][row.status] }}</el-tag
            ></template
          ></el-table-column
        >
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="操作" min-width="180">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="success" size="small" @click="handleApprove(row.id, 1)">通过</el-button>
            <el-button v-if="row.status === 0" type="danger" size="small" @click="handleApprove(row.id, 2)">拒绝</el-button>
            <el-button type="warning" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.role !== 2" type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top: 15px" background layout="prev,pager,next"
        :total="total" :page-size="size" @current-change="(p) => fetchUsers(p)"
      />
    </el-card>

    <el-dialog v-model="editVisible" title="编辑用户" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="姓名"><el-input v-model="editForm.name" /></el-form-item>
        <el-form-item label="年龄"><el-input-number v-model="editForm.age" :min="0" /></el-form-item>
        <el-form-item label="性别"
          ><el-radio-group v-model="editForm.gender"
            ><el-radio :value="1">男</el-radio><el-radio :value="0">女</el-radio></el-radio-group
          ></el-form-item
        >
        <el-form-item label="手机号"><el-input v-model="editForm.phone" /></el-form-item>
        <el-form-item label="住址"><el-input v-model="editForm.address" /></el-form-item>
        <template v-if="editForm.role === 1">
          <el-form-item label="医院"><el-input v-model="editForm.hospital" /></el-form-item>
          <el-form-item label="科室"
            ><el-select v-model="editForm.department" placeholder="选择科室" style="width: 100%">
              <el-option v-for="d in deptOptions" :key="d" :label="d" :value="d" />
            </el-select
          ></el-form-item>
          <el-form-item label="职称"><el-input v-model="editForm.title" /></el-form-item>
          <el-form-item label="专长"><el-input v-model="editForm.specialty" type="textarea" /></el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { getUsers, approveUser, updateUser, deleteUser, batchApproveUsers, getAutoApproveUserConfig, updateAutoApproveUserConfig } from "@/api/user";
import { ElMessage, ElMessageBox } from "element-plus";

const deptOptions = ["内科", "外科", "儿科", "妇产科", "骨科", "眼科", "皮肤科", "神经科", "心血管科", "肿瘤科"];
const users = ref([]);
const loading = ref(false);
const total = ref(0);
const size = ref(10);
const filterRole = ref(null);
const filterStatus = ref(null);
const editVisible = ref(false);
const saving = ref(false);
const savingConfig = ref(false);
const selectedIds = ref([]);
const autoConfig = reactive({ autoApprovePatient: false, autoApproveDoctor: false });
const editForm = reactive({ id: null, name: "", age: null, gender: 1, phone: "", address: "", role: null, hospital: "", department: "", title: "", specialty: "" });

onMounted(() => { fetchUsers(); fetchAutoConfig(); });

function onSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id);
}

async function fetchAutoConfig() {
  try {
    const res = await getAutoApproveUserConfig();
    if (res.data) {
      autoConfig.autoApprovePatient = res.data.autoApprovePatient || false;
      autoConfig.autoApproveDoctor = res.data.autoApproveDoctor || false;
    }
  } catch {}
}

async function saveAutoConfig() {
  savingConfig.value = true;
  try {
    await updateAutoApproveUserConfig(autoConfig);
    ElMessage.success("自动审批设置已保存");
  } catch { ElMessage.error("保存失败"); }
  finally { savingConfig.value = false; }
}

async function fetchUsers(page = 1) {
  loading.value = true;
  try {
    const params = { page, size: size.value };
    if (filterRole.value !== null && filterRole.value !== "") params.role = filterRole.value;
    if (filterStatus.value !== null && filterStatus.value !== "") params.status = filterStatus.value;
    const res = await getUsers(params);
    users.value = res.data.records;
    total.value = res.data.total;
  } catch { users.value = []; total.value = 0; }
  finally { loading.value = false; }
}

async function batchApprove(action) {
  const label = action === 1 ? '通过' : '拒绝';
  try {
    await ElMessageBox.confirm(`确定批量${label}选中的 ${selectedIds.value.length} 个用户吗？`, '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' });
    await batchApproveUsers({ ids: selectedIds.value, action });
    ElMessage.success(action === 1 ? '批量审批通过' : '批量已拒绝');
    selectedIds.value = [];
    fetchUsers();
  } catch {}
}

async function handleApprove(id, action) {
  try {
    const label = action === 1 ? '通过' : '拒绝';
    await ElMessageBox.confirm(`确定${label}该用户吗？`, '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' });
    await approveUser(id, action);
    ElMessage.success(action === 1 ? "审批通过" : "已拒绝");
    fetchUsers();
  } catch {}
}

function openEdit(row) {
  Object.assign(editForm, { id: row.id, name: row.name, age: row.age, gender: row.gender, phone: row.phone, address: row.address, role: row.role, hospital: row.hospital || "", department: row.department || "", title: row.title || "", specialty: row.specialty || "" });
  editVisible.value = true;
}

async function saveEdit() {
  saving.value = true;
  try { const { id, ...data } = editForm; await updateUser(id, data); ElMessage.success("修改成功"); editVisible.value = false; fetchUsers(); }
  finally { saving.value = false; }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm("确定删除该用户吗？", "提示", { type: "warning", confirmButtonText: "确定", cancelButtonText: "取消" });
    await deleteUser(id); ElMessage.success("删除成功"); fetchUsers();
  } catch {}
}
</script>
