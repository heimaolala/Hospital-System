<template>
  <div>
    <h3>我的病历</h3>
    <el-card style="margin-top: 20px">
      <el-timeline v-if="records.length > 0">
        <el-timeline-item
          v-for="record in records"
          :key="record.id"
          :timestamp="record.createdAt"
          placement="top"
        >
          <el-card>
            <p style="white-space: pre-wrap">{{ record.content }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无病历记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { getMyRecords } from "@/api/medicalRecord";

const records = ref([]);

onMounted(async () => {
  try {
    const res = await getMyRecords();
    records.value = res.data;
  } catch {
    records.value = [];
  }
});
</script>
