<template>
  <div>
    <h3>出诊管理</h3>
    <el-button
      type="primary"
      @click="openCreate()"
      style="margin-top: 20px"
      >发布出诊</el-button
    >

    <el-card style="margin-top: 20px">
      <div style="margin-bottom: 15px; display: flex; justify-content: space-between; align-items: center">
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button value="table">列表</el-radio-button>
          <el-radio-button value="calendar">日历</el-radio-button>
        </el-radio-group>
      </div>

      <el-table
        v-if="viewMode === 'table'"
        :data="schedules"
        border
        stripe
        v-loading="loading"
        empty-text="暂无出诊信息"
      >
        <el-table-column prop="scheduleDate" label="日期" width="120" />
        <el-table-column label="时段" width="80"
          ><template #default="{ row }">{{
            row.timeSlotDesc
          }}</template></el-table-column
        >
        <el-table-column prop="totalQuota" label="总号源" width="80" />
        <el-table-column prop="remainingQuota" label="剩余" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }"
            ><el-tag
              :type="
                row.status === 1
                  ? 'success'
                  : row.status === 2
                    ? 'danger'
                    : 'warning'
              "
              >{{ row.statusDesc }}</el-tag
            ></template
          >
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="openEdit(row)"
              >编辑</el-button
            >
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row.id)"
              :loading="deletingId === row.id"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <div v-else v-loading="loading">
        <div v-if="schedules.length === 0" style="text-align: center; padding: 40px">
          <el-empty description="暂无出诊信息" />
        </div>
        <div v-else class="calendar-view">
          <div
            v-for="s in schedules"
            :key="s.id"
            class="calendar-card"
            @click="openEdit(s)"
          >
            <div class="cal-date">{{ s.scheduleDate }}</div>
            <div class="cal-info">
              <el-tag :type="s.timeSlot === 0 ? 'primary' : 'warning'" size="small">{{ s.timeSlotDesc }}</el-tag>
              <el-tag
                :type="s.status === 1 ? 'success' : s.status === 2 ? 'danger' : 'warning'"
                size="small"
                style="margin-left: 6px"
              >{{ s.statusDesc }}</el-tag>
              <span style="margin-left: 8px; font-size: 13px; color: #909399">
                号源: {{ s.remainingQuota }}/{{ s.totalQuota }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <el-pagination
      style="margin-top: 15px; display: flex; justify-content: center"
      background
      layout="total, prev, pager, next"
      :total="total"
      :page-size="pageSize"
      :current-page="currentPage"
      @current-change="(p) => fetchSchedules(p)"
    />

    <el-dialog v-model="addVisible" :title="isEdit ? '编辑出诊信息' : '发布出诊信息'" width="450px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="出诊日期" prop="scheduleDate">
          <el-input v-model="form.scheduleDate" type="date" :min="defaultDate" style="width: 100%" />
        </el-form-item>
        <el-form-item label="时段" prop="timeSlot">
          <el-radio-group v-model="form.timeSlot">
            <el-radio :value="0">上午</el-radio>
            <el-radio :value="1">下午</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="号源数量" prop="totalQuota">
          <el-input-number v-model="form.totalQuota" :min="1" :max="200" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="周期出诊">
          <el-checkbox v-model="form.isRecurring" @change="onRecurringChange" />
          <span style="margin-left: 8px; color: #909399; font-size: 12px">启用后可批量生成每周固定日期的号源</span>
        </el-form-item>
        <template v-if="form.isRecurring && !isEdit">
          <el-form-item label="出诊星期">
            <el-select v-model="form.recurPattern" multiple placeholder="选择星期">
              <el-option label="周一" value="MON" />
              <el-option label="周二" value="TUE" />
              <el-option label="周三" value="WED" />
              <el-option label="周四" value="THU" />
              <el-option label="周五" value="FRI" />
              <el-option label="周六" value="SAT" />
              <el-option label="周日" value="SUN" />
            </el-select>
          </el-form-item>
        </template>
        <el-alert
          v-if="isEdit"
          title="编辑已发布的排班将重新进入待审核状态"
          type="warning"
          :closable="false"
          show-icon
          style="margin-top: 10px"
        />
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="creating"
          >{{ isEdit ? '保存' : '发布' }}</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount } from "vue";
import { getMySchedules, createSchedule, updateSchedule, deleteSchedule } from "@/api/schedule";
import { ElMessage, ElMessageBox } from "element-plus";

const schedules = ref([]);
const loading = ref(false);
const addVisible = ref(false);
const creating = ref(false);
const deletingId = ref(null);
const isEdit = ref(false);
const editId = ref(null);
const formRef = ref();
const viewMode = ref("table");
const total = ref(0);
const currentPage = ref(1);
const tablePageSize = 10;
const calendarPageSize = 30;
const pageSize = computed(() => viewMode.value === 'table' ? tablePageSize : calendarPageSize);
const today = new Date();
const defaultDate = today.getFullYear() + "-" + String(today.getMonth() + 1).padStart(2, "0") + "-" + String(today.getDate()).padStart(2, "0");
const form = reactive({ scheduleDate: defaultDate, timeSlot: 0, totalQuota: 30, isRecurring: false, recurPattern: [] });

function resetForm() {
  form.scheduleDate = defaultDate;
  form.timeSlot = 0;
  form.totalQuota = 30;
  form.isRecurring = false;
  form.recurPattern = [];
  isEdit.value = false;
  editId.value = null;
}

function openCreate() {
  resetForm();
  addVisible.value = true;
}

function openEdit(row) {
  isEdit.value = true;
  editId.value = row.id;
  form.scheduleDate = row.scheduleDate;
  form.timeSlot = row.timeSlot;
  form.totalQuota = row.totalQuota;
  form.isRecurring = false;
  form.recurPattern = [];
  addVisible.value = true;
}

function onRecurringChange(val) {
  if (!val) form.recurPattern = [];
}
const rules = {
  scheduleDate: [{ required: true, message: "请选择日期", trigger: "blur" }],
  totalQuota: [{ required: true, message: "请输入号源数量", trigger: "blur" }],
};

onMounted(() => fetchSchedules());

watch(viewMode, () => {
  currentPage.value = 1;
  fetchSchedules(1);
});

onBeforeUnmount(() => {
  addVisible.value = false;
});

async function fetchSchedules(page = 1) {
  currentPage.value = page;
  loading.value = true;
  try {
const res = await getMySchedules({ page, size: pageSize.value });
    const data = res.data;
    if (data && data.records) {
      schedules.value = data.records;
      total.value = data.total || 0;
    } else if (Array.isArray(data)) {
      schedules.value = data;
      total.value = data.length;
    }
  } catch (e) {
    console.error('获取排班失败', e);
    schedules.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  creating.value = true;
  try {
    const data = {
      scheduleDate: form.scheduleDate,
      timeSlot: form.timeSlot,
      totalQuota: form.totalQuota,
    };
    if (isEdit.value) {
      await updateSchedule(editId.value, data);
      ElMessage.success("修改成功，等待管理员审核");
    } else {
      if (form.isRecurring && form.recurPattern.length > 0) {
        data.isRecurring = 1;
        data.recurPattern = form.recurPattern.join(",");
      }
      await createSchedule(data);
      ElMessage.success("发布成功，等待管理员审核");
    }
    addVisible.value = false;
    resetForm();
    await fetchSchedules();
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || "操作失败");
  } finally {
    creating.value = false;
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm("确定删除该出诊信息吗？", "提示", {
      type: "warning",
      confirmButtonText: "确定",
      cancelButtonText: "取消",
    });
    deletingId.value = id;
    await deleteSchedule(id);
    ElMessage.success("删除成功");
    fetchSchedules();
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('删除失败');
    }
  } finally {
    deletingId.value = null;
  }
}
</script>

<style scoped>
.calendar-view {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.calendar-card {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px 16px;
  cursor: pointer;
  min-width: 200px;
  transition: all 0.2s;
}
.calendar-card:hover {
  border-color: #0d9488;
  box-shadow: 0 2px 8px rgba(13, 148, 136, 0.15);
}
.cal-date {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 6px;
}
.cal-info {
  display: flex;
  align-items: center;
}
</style>
