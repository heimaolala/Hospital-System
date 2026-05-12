<template>
  <div>
    <h3>管理员首页</h3>
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="8"
        ><el-card
          ><template #header>用户管理</template>
          <p style="margin-bottom: 16px; color: #606266; line-height: 1.6">管理患者、医生账户，审批注册申请</p>
          <el-button type="primary" @click="$router.push('/admin/users')"
            >进入管理</el-button
          ></el-card
        ></el-col
      >
      <el-col :span="8"
        ><el-card
          ><template #header>出诊审核</template>
          <p style="margin-bottom: 16px; color: #606266; line-height: 1.6">审核医生发布的出诊信息</p>
          <el-button type="warning" @click="$router.push('/admin/schedules')"
            >进入审核</el-button
          ></el-card
        ></el-col
      >
      <el-col :span="8"
        ><el-card
          ><template #header>操作日志</template>
          <p style="margin-bottom: 16px; color: #606266; line-height: 1.6">查看系统操作审计记录</p>
          <el-button @click="$router.push('/admin/audit-logs')"
            >查看日志</el-button
          ></el-card
        ></el-col
      >
    </el-row>
  </div>
</template>
