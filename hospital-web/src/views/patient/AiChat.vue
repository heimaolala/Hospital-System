<template>
  <div class="ai-chat-container">
    <h3>AI 智能问诊</h3>
    <el-card class="chat-card">
      <div class="chat-messages" ref="msgContainer">
        <div v-if="messages.length === 0" class="chat-welcome">
          <el-icon :size="48" color="#0d9488"><ChatDotRound /></el-icon>
          <p>您好，我是AI医疗助手</p>
          <p style="color: #909399; font-size: 13px">可以向我咨询症状、用药、健康管理等问题。紧急情况请及时就医。</p>
        </div>
        <div v-for="(msg, i) in messages" :key="i" :class="['chat-bubble', msg.role === 'user' ? 'chat-user' : 'chat-ai']">
          <div class="chat-avatar">
            <el-icon v-if="msg.role === 'user'" :size="20"><User /></el-icon>
            <el-icon v-else :size="20" color="#0d9488"><MagicStick /></el-icon>
          </div>
          <div class="chat-content">
            <div class="chat-role">{{ msg.role === 'user' ? '我' : 'AI医生' }}</div>
            <div class="chat-text" v-html="formatContent(msg.content)"></div>
          </div>
        </div>
        <div v-if="thinking" class="chat-bubble chat-ai">
          <div class="chat-avatar"><el-icon :size="20" color="#0d9488"><MagicStick /></el-icon></div>
          <div class="chat-content">
            <div class="chat-role">AI医生</div>
            <div class="chat-text typing"><span></span><span></span><span></span></div>
          </div>
        </div>
      </div>
      <div class="chat-input">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="2"
          placeholder="请输入您的健康问题..."
          @keydown.enter.exact="handleSend"
          :disabled="thinking"
        />
        <el-button type="primary" @click="handleSend" :loading="thinking" :disabled="!inputText.trim()" style="flex-shrink: 0">
          <el-icon><Promotion /></el-icon>发送
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick } from "vue";
import { sendMessage } from "@/api/ai";
import { ChatDotRound, User, MagicStick, Promotion } from "@element-plus/icons-vue";
import { marked } from "marked";

const messages = ref([]);
const inputText = ref("");
const thinking = ref(false);
const msgContainer = ref(null);

function formatContent(text) {
  if (!text) return "";
  return marked.parse(text);
}

async function scrollBottom() {
  await nextTick();
  if (msgContainer.value) {
    msgContainer.value.scrollTop = msgContainer.value.scrollHeight;
  }
}

async function handleSend() {
  const text = inputText.value.trim();
  if (!text || thinking.value) return;

  messages.value.push({ role: "user", content: text });
  inputText.value = "";
  await scrollBottom();

  thinking.value = true;
  try {
    const recentMessages = messages.value.slice(-10).map(m => ({ role: m.role, content: m.content }));
    const res = await sendMessage(recentMessages);
    if (res.data) {
      messages.value.push({ role: "assistant", content: res.data.content });
    }
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '';
    if (msg.includes('timeout') || msg.includes('Timeout') || msg.includes('ECONNABORTED')) {
      messages.value.push({ role: "assistant", content: "AI响应超时，请稍后重试或简化您的问题。如果是紧急医疗问题，请立即就医。" });
    } else {
      messages.value.push({ role: "assistant", content: "抱歉，AI服务暂时不可用。" + (msg ? "（" + msg + "）" : "") + "如果是紧急医疗问题，请立即就医。" });
    }
  } finally {
    thinking.value = false;
    await scrollBottom();
  }
}
</script>

<style scoped>
.ai-chat-container {
  height: calc(100vh - 110px);
  display: flex;
  flex-direction: column;
}
.chat-card {
  margin-top: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.chat-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 16px;
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
  margin-bottom: 12px;
}
.chat-input {
  flex-shrink: 0;
  display: flex;
  gap: 10px;
  align-items: flex-end;
  height: 25%;
  min-height: 100px;
  max-height: 150px;
}
.chat-input .el-textarea { flex: 1; height: 100%; }
.chat-input :deep(.el-textarea__inner) { height: 100% !important; }
.chat-welcome { text-align: center; padding: 60px 20px; color: #606266; }
.chat-welcome p { margin-top: 12px; }
.chat-bubble { display: flex; gap: 10px; margin-bottom: 16px; }
.chat-user { flex-direction: row-reverse; }
.chat-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  background: #e2e8f0; display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.chat-user .chat-avatar { background: #0d9488; color: #fff; }
.chat-role { font-size: 12px; color: #909399; margin-bottom: 4px; }
.chat-user .chat-role { text-align: right; }
.chat-text {
  background: #fff; padding: 10px 14px; border-radius: 12px;
  max-width: 560px; word-break: break-word; line-height: 1.6;
  font-size: 14px; box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
.chat-user .chat-text { background: #0d9488; color: #fff; }
.chat-text :deep(h1), .chat-text :deep(h2), .chat-text :deep(h3) { margin: 8px 0 4px; font-size: 1.1em; }
.chat-text :deep(ul), .chat-text :deep(ol) { padding-left: 20px; margin: 6px 0; }
.chat-text :deep(li) { margin: 2px 0; }
.chat-text :deep(code) { background: rgba(0,0,0,0.06); padding: 2px 6px; border-radius: 4px; font-size: 0.9em; }
.chat-text :deep(pre) { background: #f1f5f9; padding: 10px; border-radius: 6px; overflow-x: auto; margin: 8px 0; }
.chat-text :deep(pre code) { background: none; padding: 0; }
.chat-text :deep(blockquote) { border-left: 3px solid #0d9488; padding-left: 10px; margin: 8px 0; color: #64748b; }
.chat-text :deep(table) { border-collapse: collapse; margin: 8px 0; }
.chat-text :deep(th), .chat-text :deep(td) { border: 1px solid #e2e8f0; padding: 6px 10px; }
.chat-text :deep(p) { margin: 6px 0; }
.chat-text :deep(strong) { font-weight: 600; }
.chat-input { padding: 0 16px 8px; }
.typing span {
  display: inline-block; width: 6px; height: 6px; border-radius: 50%;
  background: #0d9488; margin: 0 2px; animation: typing 1.4s infinite;
}
.typing span:nth-child(2) { animation-delay: 0.2s; }
.typing span:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-6px); opacity: 1; }
}
</style>
