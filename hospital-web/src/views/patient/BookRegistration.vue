<template>
  <div>
    <h3>预约挂号</h3>
    <el-card style="margin-top: 20px">
      <el-steps :active="step" align-center finish-status="success" style="margin-bottom: 24px">
        <el-step title="选择号源" />
        <el-step title="确认信息" />
        <el-step title="完成支付" />
      </el-steps>

      <div v-if="step === 0">
        <el-form :inline="true">
          <el-form-item label="科室">
            <el-select
              v-model="department"
              placeholder="选择科室"
              clearable
              style="width: 180px"
            >
              <el-option
                v-for="d in departments"
                :key="d"
                :label="d"
                :value="d"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="日期">
            <el-date-picker
              v-model="searchDate"
              type="date"
              placeholder="选择日期"
              :disabled-date="disabledDate"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              @click="searchSchedules"
              :disabled="!department"
              >查询号源</el-button
            >
          </el-form-item>
        </el-form>

        <el-table
          :data="schedules"
          border
          stripe
          v-loading="loading"
          empty-text="请选择科室和日期查询号源"
          style="margin-top: 16px"
        >
          <el-table-column prop="doctorName" label="医生姓名" width="100" />
          <el-table-column prop="department" label="科室" width="100" />
          <el-table-column prop="title" label="职称" width="120" />
          <el-table-column prop="hospital" label="医院" width="150" />
          <el-table-column prop="scheduleDate" label="出诊日期" width="120" />
          <el-table-column label="时段" width="80">
            <template #default="{ row }">{{ row.timeSlotDesc }}</template>
          </el-table-column>
          <el-table-column label="剩余号源" width="100">
            <template #default="{ row }">
              <el-tag :type="row.remainingQuota > 0 ? 'success' : 'danger'">
                {{ row.remainingQuota }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                @click="handleBook(row)"
                :disabled="row.remainingQuota <= 0"
                :loading="bookingId === row.id"
              >
                挂号
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="step === 1" class="confirm-step">
        <el-descriptions title="挂号信息确认" :column="1" border>
          <el-descriptions-item label="医生">{{ currentBooking?.doctorName }}</el-descriptions-item>
          <el-descriptions-item label="科室">{{ currentBooking?.department }}</el-descriptions-item>
          <el-descriptions-item label="医院">{{ currentBooking?.hospital }}</el-descriptions-item>
          <el-descriptions-item label="日期">{{ currentBooking?.scheduleDate }} {{ currentBooking?.timeSlotDesc }}</el-descriptions-item>
          <el-descriptions-item label="职称">{{ currentBooking?.title }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 20px; text-align: center">
          <el-button @click="step = 0">返回修改</el-button>
          <el-button type="primary" @click="step = 2">确认，去支付</el-button>
        </div>
      </div>

      <div v-if="step === 2" class="pay-step">
        <el-result icon="success" title="模拟支付">
          <template #sub-title>
            <p>挂号费用将在就诊时结算</p>
            <p style="color: #909399; font-size: 13px">
              医生：{{ currentBooking?.doctorName }} |
              日期：{{ currentBooking?.scheduleDate }} {{ currentBooking?.timeSlotDesc }}
            </p>
          </template>
          <template #extra>
            <el-button type="primary" @click="handlePay" :loading="paying">
              确认支付
            </el-button>
            <el-button @click="step = 1">返回</el-button>
          </template>
        </el-result>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { getAvailableSchedules } from "@/api/schedule";
import { registerBooking, payRegistration } from "@/api/registration";
import { ElMessage } from "element-plus";

const departments = ref([
  "内科",
  "外科",
  "儿科",
  "妇产科",
  "骨科",
  "眼科",
  "皮肤科",
  "神经科",
  "心血管科",
  "肿瘤科",
]);
const department = ref("");
const searchDate = ref("");
const schedules = ref([]);
const loading = ref(false);
const step = ref(0);
const bookingId = ref(null);
const paying = ref(false);
const currentBooking = ref(null);
const bookedRegistrationId = ref(null);

function disabledDate(time) {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const maxDate = new Date(today);
  maxDate.setDate(maxDate.getDate() + 2);
  return time.getTime() < today.getTime() || time.getTime() > maxDate.getTime();
}

async function searchSchedules() {
  if (!department.value) return;
  loading.value = true;
  try {
    const params = { department: department.value };
    if (searchDate.value) {
      params.startDate = searchDate.value;
    }
    const res = await getAvailableSchedules(params);
    schedules.value = res.data;
  } catch {
    schedules.value = [];
  } finally {
    loading.value = false;
  }
}

async function handleBook(row) {
  bookingId.value = row.id;
  try {
    const res = await registerBooking({ scheduleId: row.id });
    bookedRegistrationId.value = res.data.id;
    currentBooking.value = row;
    step.value = 1;
  } catch {
    searchSchedules();
  } finally {
    bookingId.value = null;
  }
}

async function handlePay() {
  paying.value = true;
  try {
    await payRegistration(bookedRegistrationId.value);
    ElMessage.success("支付成功！挂号完成");
    step.value = 0;
    currentBooking.value = null;
    bookedRegistrationId.value = null;
    searchSchedules();
  } catch {
    // error handled
  } finally {
    paying.value = false;
  }
}
</script>

<style scoped>
.confirm-step, .pay-step {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}
</style>
