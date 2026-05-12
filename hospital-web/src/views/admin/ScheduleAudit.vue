<template>
  <div>
    <h3>出诊信息审核</h3>

    <el-card style="margin-top: 20px">
      <template #header><span style="font-weight: bold">自动审核设置</span></template>
      <el-form :inline="true">
        <el-form-item label="日期阈值（天）">
          <el-input-number v-model="configForm.maxDays" :min="1" :max="365" />
        </el-form-item>
        <el-form-item label="号源数上限">
          <el-input-number v-model="configForm.maxQuota" :min="1" :max="200" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveConfig" :loading="savingConfig">保存设置</el-button>
        </el-form-item>
      </el-form>
      <div style="color: #909399; font-size: 12px; margin-top: 8px">
        满足以下条件时自动通过审核：出诊日期在 <b>{{ configForm.maxDays }}</b> 天内、号源数 ≤ <b>{{ configForm.maxQuota }}</b>、且非周期性排班。
      </div>
    </el-card>

    <el-card style="margin-top: 20px">
      <div style="margin-bottom: 12px">
        <el-button type="success" :disabled="selectedIds.length === 0" @click="batchAudit(1)">批量通过</el-button>
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="batchAudit(2)">批量拒绝</el-button>
        <span style="margin-left: 12px; color: #909399; font-size: 13px" v-if="selectedIds.length > 0">已选 {{ selectedIds.length }} 项</span>
      </div>
      <el-table
        :data="schedules"
        border
        stripe
        v-loading="loading"
        empty-text="暂无待审核出诊信息"
        @selection-change="onSelectionChange"
      >
        <el-table-column type="selection" width="45" :selectable="(row) => row.status === 0" />
        <el-table-column prop="doctorName" label="医生" width="100" />
        <el-table-column prop="department" label="科室" width="100" />
        <el-table-column prop="hospital" label="医院" width="150" />
        <el-table-column prop="scheduleDate" label="出诊日期" width="120" />
        <el-table-column label="时段" width="80"
          ><template #default="{ row }">{{
            row.timeSlotDesc
          }}</template></el-table-column
        >
        <el-table-column prop="totalQuota" label="号源数" width="80" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button
              type="success"
              size="small"
              @click="handleAudit(row.id, 1)"
              >通过</el-button
            >
            <el-button
              type="danger"
              size="small"
              @click="handleAudit(row.id, 2)"
              >拒绝</el-button
            >
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top: 15px; display: flex; justify-content: center"
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="(p) => fetchPending(p)"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { getPendingSchedules, auditSchedule, batchAuditSchedules, getAutoAuditConfig, updateAutoAuditConfig } from "@/api/schedule";
import { ElMessage, ElMessageBox } from "element-plus";

const schedules = ref([]);
const loading = ref(false);
const savingConfig = ref(false);
const selectedIds = ref([]);
const total = ref(0);
const pageSize = ref(20);
const currentPage = ref(1);

function onSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id);
}
const configForm = reactive({ maxDays: 30, maxQuota: 15 });

onMounted(() => {
  fetchPending();
  fetchConfig();
});

async function fetchPending(page = 1) {
  currentPage.value = page;
  loading.value = true;
  try {
    const res = await getPendingSchedules({ page, size: pageSize.value });
    const data = res.data;
    schedules.value = data.records || data;
    total.value = data.total || 0;
  } catch {
    schedules.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

async function fetchConfig() {
  try {
    const res = await getAutoAuditConfig();
    if (res.data) {
      configForm.maxDays = res.data.maxDays;
      configForm.maxQuota = res.data.maxQuota;
    }
  } catch {}
}

async function saveConfig() {
  savingConfig.value = true;
  try {
    await updateAutoAuditConfig({ maxDays: configForm.maxDays, maxQuota: configForm.maxQuota });
    ElMessage.success("自动审核设置已保存");
  } catch {
    ElMessage.error("保存失败");
  } finally {
    savingConfig.value = false;
  }
}

async function batchAudit(action) {
  const label = action === 1 ? '通过' : '拒绝';
  try {
    let comment = "";
    if (action === 2) {
      try {
        const result = await ElMessageBox.prompt("请输入拒绝原因", "批量拒绝", { confirmButtonText: "确定", cancelButtonText: "取消" });
        comment = result.value || "";
      } catch { return; }
    }
    await ElMessageBox.confirm(`确定批量${label}选中的 ${selectedIds.value.length} 条排班吗？`, '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' });
    await batchAuditSchedules({ ids: selectedIds.value, action, comment });
    ElMessage.success(action === 1 ? '批量审核通过' : '批量已拒绝');
    selectedIds.value = [];
    fetchPending();
  } catch {}
}

async function handleAudit(id, action) {
  try {
    let comment = "";
    if (action === 2) {
      try {
        const result = await ElMessageBox.prompt("请输入拒绝原因", "拒绝审核", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
        });
        comment = result.value || "";
      } catch {
        return;
      }
    }
    await auditSchedule(id, action, comment);
    ElMessage.success(action === 1 ? "审核通过" : "已拒绝");
    fetchPending();
  } catch {
    /* error handled */
  }
}
</script>
