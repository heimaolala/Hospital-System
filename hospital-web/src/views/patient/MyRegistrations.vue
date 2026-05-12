<template>
  <div>
    <h3>我的挂号记录</h3>
    <el-card style="margin-top: 20px">
      <el-form :inline="true">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item
          ><el-button type="primary" @click="fetchRegistrations()"
            >查询</el-button
          ></el-form-item
        >
      </el-form>
    </el-card>
    <el-card style="margin-top: 20px">
      <el-table
        :data="registrations"
        border
        stripe
        v-loading="loading"
        empty-text="暂无挂号记录"
      >
        <el-table-column prop="doctorName" label="医生" width="130" />
        <el-table-column prop="department" label="科室" width="130" />
        <el-table-column prop="hospital" label="医院" min-width="180" />
        <el-table-column prop="registrationDate" label="挂号日期" width="130" />
        <el-table-column label="时段" width="100"
          ><template #default="{ row }">{{
            row.timeSlotDesc
          }}</template></el-table-column
        >
        <el-table-column label="状态" width="100">
          <template #default="{ row }"
            ><el-tag
              :type="
                row.status === 0 ? '' : row.status === 1 ? 'danger' : 'success'
              "
              >{{ row.statusDesc }}</el-tag
            ></template
          >
        </el-table-column>
        <el-table-column label="支付" width="100">
          <template #default="{ row }"
            ><el-tag :type="row.paymentStatus === 1 ? 'success' : 'warning'">{{
              row.paymentStatusDesc
            }}</el-tag></template
          >
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button
              v-if="row.paymentStatus === 0 && row.status === 0"
              type="success"
              size="small"
              @click="handlePay(row.id)"
              :loading="payingId === row.id"
              >支付</el-button
            >
            <el-button
              v-if="row.status === 0 && row.paymentStatus === 0"
              type="danger"
              size="small"
              @click="handleCancel(row.id)"
              :loading="cancellingId === row.id"
              >取消</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import {
  getMyRegistrations,
  payRegistration,
  cancelRegistration,
} from "@/api/registration";
import { ElMessage, ElMessageBox } from "element-plus";

const dateRange = ref([]);
const registrations = ref([]);
const loading = ref(false);
const payingId = ref(null);
const cancellingId = ref(null);

onMounted(() => fetchRegistrations());

async function fetchRegistrations() {
  loading.value = true;
  try {
    const params = {};
    if (dateRange.value?.length === 2) {
      params.startDate = dateRange.value[0];
      params.endDate = dateRange.value[1];
    }
    const res = await getMyRegistrations(params);
    registrations.value = res.data;
  } catch {
    registrations.value = [];
  } finally {
    loading.value = false;
  }
}

async function handlePay(id) {
  payingId.value = id;
  try {
    await payRegistration(id);
    ElMessage.success("支付成功");
    fetchRegistrations();
  } finally {
    payingId.value = null;
  }
}

async function handleCancel(id) {
  try {
    await ElMessageBox.confirm("确定取消该挂号吗？", "提示", { confirmButtonText: '确定', cancelButtonText: '取消',
      type: "warning",
    });
    cancellingId.value = id;
    await cancelRegistration(id);
    ElMessage.success("已取消");
    fetchRegistrations();
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('取消失败');
    }
  } finally {
    cancellingId.value = null;
  }
}
</script>
