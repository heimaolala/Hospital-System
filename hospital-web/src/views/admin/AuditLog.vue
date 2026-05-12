<template>
  <div>
    <h3>操作日志</h3>
    <el-card style="margin-top: 20px">
      <el-table
        :data="logs"
        border
        stripe
        v-loading="loading"
        empty-text="暂无操作日志"
      >
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="operatorId" label="操作者ID" width="90" />
        <el-table-column prop="targetType" label="目标类型" width="100" />
        <el-table-column prop="targetId" label="目标ID" width="80" />
        <el-table-column prop="action" label="操作" width="100">
          <template #default="{ row }">
            <el-tag
              :type="
                row.action === 'delete'
                  ? 'danger'
                  : row.action === 'approve'
                    ? 'success'
                    : 'warning'
              "
              >{{ row.action }}</el-tag
            >
          </template>
        </el-table-column>
        <el-table-column label="修改前" min-width="200">
          <template #default="{ row }">
            <span style="font-size: 12px; color: #909399">{{
              truncate(row.oldValue)
            }}</span>
          </template>
        </el-table-column>
        <el-table-column label="修改后" min-width="200">
          <template #default="{ row }">
            <span style="font-size: 12px; color: #409eff">{{
              truncate(row.newValue)
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="160" />
      </el-table>
      <el-pagination
        style="margin-top: 15px"
        background
        layout="prev,pager,next"
        :total="total"
        :page-size="10"
        @current-change="(p) => fetchLogs(p)"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { getAuditLogs } from "@/api/user";

const logs = ref([]);
const loading = ref(false);
const total = ref(0);

onMounted(() => fetchLogs());

async function fetchLogs(page = 1) {
  loading.value = true;
  try {
    const res = await getAuditLogs({ page, size: 10 });
    const data = res.data;
    if (Array.isArray(data)) {
      logs.value = data;
      total.value = data.length;
    } else if (data && data.records) {
      logs.value = data.records;
      total.value = data.total || 0;
    } else {
      logs.value = [];
      total.value = 0;
    }
  } catch {
    logs.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function truncate(str) {
  if (!str) return "-";
  return str.length > 80 ? str.substring(0, 80) + "..." : str;
}
</script>
