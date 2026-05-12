<template>
  <div>
    <h3>患者病历管理</h3>
    <el-card style="margin-top: 20px">
      <el-form :inline="true" @submit.prevent="searchRecords">
        <el-form-item label="姓名">
          <el-input v-model="searchName" placeholder="患者姓名（支持模糊查询）" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="searchIdCard" placeholder="18位身份证号（精确查询）" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchPatients">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 20px" v-if="patients.length > 0">
      <div v-for="patient in patients" :key="patient.patientId" style="margin-bottom: 24px">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px">
          <div>
            <el-tag type="primary" size="large">{{ patient.name }}</el-tag>
            <span style="margin-left: 10px; color: #909399; font-size: 13px">身份证：{{ patient.idCard }}</span>
          </div>
          <el-button type="primary" size="small" @click="openAdd(patient)">新增病历</el-button>
        </div>
        <el-timeline v-if="patient.records && patient.records.length > 0">
          <el-timeline-item
            v-for="record in patient.records"
            :key="record.id"
            :timestamp="record.createdAt"
            placement="top"
          >
            <el-card>
              <p style="white-space: pre-wrap">{{ record.content }}</p>
              <el-button type="warning" size="small" @click="openEdit(record)" style="margin-top: 10px">编辑</el-button>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无病历记录" :image-size="60" />
        <el-divider />
      </div>
    </el-card>
    <el-card style="margin-top: 20px" v-else-if="searched">
      <el-empty description="未找到匹配的患者" />
    </el-card>

    <el-dialog v-model="addVisible" title="新增病历" width="500px">
      <p style="margin-bottom: 10px; color: #909399">患者：{{ currentPatient?.name }}（{{ currentPatient?.idCard }}）</p>
      <el-input v-model="newContent" type="textarea" :rows="6" placeholder="请输入病历内容" />
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑病历" width="500px">
      <el-input v-model="editContent" type="textarea" :rows="6" placeholder="请输入病历内容" />
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { searchPatients as searchPatientsApi, createRecord, updateRecord } from "@/api/medicalRecord";
import { ElMessage } from "element-plus";

const searchName = ref("");
const searchIdCard = ref("");
const patients = ref([]);
const searched = ref(false);
const addVisible = ref(false);
const editVisible = ref(false);
const newContent = ref("");
const editContent = ref("");
const editId = ref(null);
const currentPatient = ref(null);
const saving = ref(false);

async function searchPatients() {
  if (!searchName.value && !searchIdCard.value) {
    ElMessage.warning("请输入姓名或身份证号");
    return;
  }
  try {
    const params = {};
    if (searchName.value) params.name = searchName.value;
    if (searchIdCard.value) params.idCard = searchIdCard.value;
    const res = await searchPatientsApi(params);
    patients.value = res.data;
    searched.value = true;
  } catch {
    patients.value = [];
  }
}

function openAdd(patient) {
  currentPatient.value = patient;
  newContent.value = "";
  addVisible.value = true;
}

async function handleCreate() {
  if (!newContent.value) return;
  saving.value = true;
  try {
    await createRecord({ patientId: currentPatient.value.patientId, content: newContent.value });
    ElMessage.success("病历创建成功");
    addVisible.value = false;
    newContent.value = "";
    searchPatients();
  } finally {
    saving.value = false;
  }
}

function openEdit(record) {
  editId.value = record.id;
  editContent.value = record.content;
  editVisible.value = true;
}

async function handleUpdate() {
  saving.value = true;
  try {
    await updateRecord(editId.value, { content: editContent.value });
    ElMessage.success("病历更新成功");
    editVisible.value = false;
    searchPatients();
  } finally {
    saving.value = false;
  }
}
</script>
