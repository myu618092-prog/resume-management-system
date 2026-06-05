<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

const API_BASE = '/api'
const statuses = [
  ['TODO', '待投递'],
  ['APPLIED', '已投递'],
  ['WRITTEN_TEST', '笔试'],
  ['INTERVIEW', '面试'],
  ['OFFER', 'Offer'],
  ['REJECTED', '拒绝']
]

const navItems = [
  ['overview', '总览', 'LayoutDashboard'],
  ['jobs', '岗位管理', 'BriefcaseBusiness'],
  ['resumes', '简历管理', 'FileUp'],
  ['interviews', '面试日程', 'CalendarClock'],
  ['ai', 'AI 分析', 'Sparkles']
]

const token = ref(localStorage.getItem('token') || '')
const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
const activeView = ref(localStorage.getItem('activeView') || 'overview')
const jobs = ref([])
const resumes = ref([])
const stats = ref(null)
const interviews = ref([])
const aiResult = ref('')
const aiProvider = ref('')
const selectedAiJobId = ref('')
const importMessage = ref('')
const loading = ref(false)
const importing = ref(false)
const authMode = ref('login')
const error = ref('')

const authForm = reactive({
  username: 'demo',
  email: 'demo@example.com',
  password: 'password123',
  fullName: '实习求职者',
  school: '示例大学',
  major: '计算机科学与技术',
  skills: 'Java, Spring Boot, PostgreSQL, Vue, Docker'
})

const jobForm = reactive({
  companyName: '',
  positionName: '',
  city: '',
  jobDescription: '',
  sourceUrl: '',
  deadline: ''
})

const importForm = reactive({
  source: 'REMOTIVE',
  limit: 15,
  keyword: 'java spring'
})

const resumeForm = reactive({
  title: '',
  file: null,
  skills: '',
  education: '',
  projects: ''
})

const interviewForm = reactive({
  applicationId: '',
  title: '',
  interviewTime: '',
  location: '',
  notes: ''
})

const groupedJobs = computed(() => {
  const groups = Object.fromEntries(statuses.map(([key]) => [key, []]))
  for (const job of jobs.value) {
    groups[job.status]?.push(job)
  }
  return groups
})

const displayProfile = computed(() => {
  const profile = user.value || {}
  return {
    name: profile.fullName === 'Demo Student' ? '实习求职者' : profile.fullName || profile.username,
    school: profile.school === 'PostgreSQL University' ? '示例大学' : profile.school || '示例大学',
    major: profile.major === 'Computer Science' ? '计算机科学与技术' : profile.major || '计算机科学与技术'
  }
})

const selectedAiJob = computed(() => jobs.value.find(job => job.id === Number(selectedAiJobId.value)) || jobs.value[0])
const recentJobs = computed(() => jobs.value.slice(0, 5))
const recentInterviews = computed(() => interviews.value.slice(0, 5))
const parsedAiResult = computed(() => {
  if (!aiResult.value) return null
  try {
    return JSON.parse(aiResult.value)
  } catch {
    return null
  }
})

async function request(path, options = {}) {
  const headers = options.headers || {}
  if (!(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json'
  }
  if (token.value) {
    headers.Authorization = `Bearer ${token.value}`
  }
  const response = await fetch(`${API_BASE}${path}`, { ...options, headers })
  if (!response.ok) {
    if (response.status === 401 || response.status === 403) {
      logout()
      throw new Error('登录状态已失效，请重新登录。')
    }
    const body = await response.json().catch(() => ({}))
    throw new Error(body.message || `HTTP ${response.status}`)
  }
  if (response.status === 204) return null
  return response.json().catch(() => null)
}

function switchView(view) {
  activeView.value = view
  localStorage.setItem('activeView', view)
}

async function authenticate() {
  error.value = ''
  loading.value = true
  try {
    const path = authMode.value === 'login' ? '/auth/login' : '/auth/register'
    const payload = authMode.value === 'login'
      ? { username: authForm.username, password: authForm.password }
      : authForm
    const data = await request(path, { method: 'POST', body: JSON.stringify(payload) })
    token.value = data.token
    user.value = data.user
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data.user))
    await loadAll()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function logout() {
  token.value = ''
  user.value = null
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}

async function loadAll() {
  if (!token.value) return
  const [jobData, resumeData, statsData] = await Promise.all([
    request('/jobs'),
    request('/resumes'),
    request('/stats')
  ])
  jobs.value = jobData || []
  resumes.value = resumeData || []
  stats.value = statsData
  if (!selectedAiJobId.value && jobs.value.length > 0) {
    selectedAiJobId.value = String(jobs.value[0].id)
  }
  await loadInterviews()
}

async function createJob() {
  error.value = ''
  await request('/jobs', { method: 'POST', body: JSON.stringify(jobForm) })
  Object.assign(jobForm, { companyName: '', positionName: '', city: '', jobDescription: '', sourceUrl: '', deadline: '' })
  await loadAll()
}

async function importJobs() {
  error.value = ''
  importMessage.value = ''
  importing.value = true
  try {
    const result = await request('/jobs/import', { method: 'POST', body: JSON.stringify(importForm) })
    importMessage.value = `已导入 ${result.importedCount} 条岗位，跳过 ${result.skippedCount} 条重复或无效数据。`
    await loadAll()
  } catch (e) {
    error.value = e.message
  } finally {
    importing.value = false
  }
}

async function updateStatus(job, status) {
  await request(`/applications/${job.applicationId}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status })
  })
  await loadAll()
}

function candidateProfile() {
  return [
    user.value?.skills,
    resumeForm.skills,
    resumes.value[0]?.structuredContent
  ].filter(Boolean).join('\n') || 'Java, Spring Boot, PostgreSQL, Vue'
}

async function analyze(job = selectedAiJob.value) {
  error.value = ''
  if (!job) {
    error.value = '请先添加或导入岗位。'
    return
  }
  try {
    const data = await request('/ai/analyze-job-match', {
      method: 'POST',
      body: JSON.stringify({
        jobId: job.id,
        resumeId: resumes.value[0]?.id,
        jobDescription: job.jobDescription,
        candidateProfile: candidateProfile()
      })
    })
    aiProvider.value = data.provider
    aiResult.value = JSON.stringify(JSON.parse(data.result), null, 2)
    switchView('ai')
  } catch (e) {
    error.value = e.message
  }
}

async function generateInterviewQuestions() {
  error.value = ''
  const job = selectedAiJob.value
  if (!job) {
    error.value = '请先添加或导入岗位。'
    return
  }
  try {
    const data = await request('/ai/interview-questions', {
      method: 'POST',
      body: JSON.stringify({
        jobId: job.id,
        jobDescription: job.jobDescription,
        candidateProfile: candidateProfile()
      })
    })
    aiProvider.value = data.provider
    aiResult.value = JSON.stringify(JSON.parse(data.result), null, 2)
  } catch (e) {
    error.value = e.message
  }
}

async function uploadResume() {
  const formData = new FormData()
  formData.append('title', resumeForm.title || '默认简历')
  formData.append('file', resumeForm.file)
  formData.append('skills', resumeForm.skills)
  formData.append('education', resumeForm.education)
  formData.append('projects', resumeForm.projects)
  await request('/resumes', { method: 'POST', body: formData, headers: {} })
  Object.assign(resumeForm, { title: '', file: null, skills: '', education: '', projects: '' })
  document.querySelector('#resume-file').value = ''
  await loadAll()
}

async function createInterview() {
  await request('/interviews', {
    method: 'POST',
    body: JSON.stringify({
      ...interviewForm,
      applicationId: Number(interviewForm.applicationId),
      interviewTime: new Date(interviewForm.interviewTime).toISOString()
    })
  })
  Object.assign(interviewForm, { applicationId: '', title: '', interviewTime: '', location: '', notes: '' })
  await loadInterviews()
}

async function loadInterviews() {
  const from = new Date()
  from.setMonth(from.getMonth() - 1)
  const to = new Date()
  to.setMonth(to.getMonth() + 3)
  interviews.value = await request(`/interviews?from=${encodeURIComponent(from.toISOString())}&to=${encodeURIComponent(to.toISOString())}`) || []
}

onMounted(loadAll)
</script>

<template>
  <main v-if="!token" class="auth-shell">
    <section class="auth-panel">
      <div>
        <p class="eyebrow">实习申请管理</p>
        <h1>AI 实习申请管理系统</h1>
      </div>
      <div class="segmented">
        <button :class="{ active: authMode === 'login' }" @click="authMode = 'login'">登录</button>
        <button :class="{ active: authMode === 'register' }" @click="authMode = 'register'">注册</button>
      </div>
      <form class="form-grid" @submit.prevent="authenticate">
        <input v-model="authForm.username" placeholder="用户名" />
        <input v-if="authMode === 'register'" v-model="authForm.email" placeholder="邮箱" />
        <input v-model="authForm.password" placeholder="密码" type="password" />
        <template v-if="authMode === 'register'">
          <input v-model="authForm.fullName" placeholder="姓名" />
          <input v-model="authForm.school" placeholder="学校" />
          <input v-model="authForm.major" placeholder="专业" />
          <textarea v-model="authForm.skills" placeholder="技能栈"></textarea>
        </template>
        <button class="primary" type="submit" :disabled="loading">
          <UserRound :size="18" />
          {{ authMode === 'login' ? '登录' : '创建账号' }}
        </button>
      </form>
      <p v-if="error" class="error">{{ error }}</p>
    </section>
  </main>

  <main v-else class="app-shell">
    <aside class="sidebar">
      <div class="profile-block">
        <p class="eyebrow">实习申请管理</p>
        <h2>{{ displayProfile.name }}</h2>
        <p>{{ displayProfile.school }} · {{ displayProfile.major }}</p>
      </div>
      <nav class="side-nav" aria-label="主导航">
        <button
          v-for="[view, label, icon] in navItems"
          :key="view"
          :class="{ active: activeView === view }"
          @click="switchView(view)"
        >
          <component :is="icon" :size="18" />
          {{ label }}
        </button>
      </nav>
      <button class="ghost" @click="logout">
        <LogOut :size="18" />
        退出
      </button>
    </aside>

    <section class="content">
      <header class="topbar">
        <div>
          <h1>{{ navItems.find(item => item[0] === activeView)?.[1] }}</h1>
          <p>管理岗位、投递状态、简历和面试准备。</p>
        </div>
        <div class="stats">
          <strong>{{ stats?.totalApplications || 0 }}</strong>
          <span>总投递</span>
          <strong>{{ stats?.interviewConversionRate || 0 }}%</strong>
          <span>面试转化</span>
        </div>
      </header>

      <p v-if="error" class="error">{{ error }}</p>

      <template v-if="activeView === 'overview'">
        <section class="summary-grid">
          <div v-for="[status, label] in statuses" :key="status" class="metric-card">
            <span>{{ label }}</span>
            <strong>{{ stats?.statusCounts?.[status] || 0 }}</strong>
          </div>
        </section>
        <section class="lower-grid">
          <div class="panel">
            <h3><BriefcaseBusiness :size="18" /> 最近岗位</h3>
            <ul class="compact-list">
              <li v-for="job in recentJobs" :key="job.id">{{ job.companyName }} · {{ job.positionName }}</li>
              <li v-if="recentJobs.length === 0">暂无岗位，去岗位管理页导入示例岗位。</li>
            </ul>
          </div>
          <div class="panel">
            <h3><CalendarClock :size="18" /> 最近面试</h3>
            <ul class="compact-list">
              <li v-for="item in recentInterviews" :key="item.id">{{ item.companyName }} · {{ new Date(item.interviewTime).toLocaleString() }}</li>
              <li v-if="recentInterviews.length === 0">暂无面试日程。</li>
            </ul>
          </div>
        </section>
      </template>

      <template v-if="activeView === 'jobs'">
        <section class="panel">
          <h3><Send :size="18" /> 导入真实岗位数据</h3>
          <form class="import-form" @submit.prevent="importJobs">
            <input v-model="importForm.keyword" placeholder="关键词，例如 java spring" />
            <input v-model.number="importForm.limit" type="number" min="1" max="30" />
            <button class="primary" type="submit" :disabled="importing">
              <Send :size="18" />
              {{ importing ? '导入中' : '导入示例岗位' }}
            </button>
          </form>
          <p v-if="importMessage" class="success">{{ importMessage }}</p>
        </section>

        <section class="panel">
          <h3><Plus :size="18" /> 新增岗位</h3>
          <form class="job-form" @submit.prevent="createJob">
            <input v-model="jobForm.companyName" placeholder="公司" required />
            <input v-model="jobForm.positionName" placeholder="岗位" required />
            <input v-model="jobForm.city" placeholder="城市" />
            <input v-model="jobForm.deadline" type="date" />
            <input v-model="jobForm.sourceUrl" placeholder="来源链接" />
            <textarea v-model="jobForm.jobDescription" placeholder="岗位 JD" required></textarea>
            <button class="primary" type="submit"><BriefcaseBusiness :size="18" /> 保存岗位</button>
          </form>
        </section>

        <section class="board">
          <div v-for="[status, label] in statuses" :key="status" class="column">
            <h3>{{ label }} <span>{{ groupedJobs[status].length }}</span></h3>
            <article v-for="job in groupedJobs[status]" :key="job.id" class="job-card">
              <div>
                <strong>{{ job.positionName }}</strong>
                <p>{{ job.companyName }} · {{ job.city || '远程/不限' }}</p>
              </div>
              <p class="jd">{{ job.jobDescription }}</p>
              <select :value="job.status" @change="updateStatus(job, $event.target.value)">
                <option v-for="[key, text] in statuses" :key="key" :value="key">{{ text }}</option>
              </select>
              <button class="secondary" @click="analyze(job)"><Sparkles :size="16" /> AI 分析</button>
            </article>
          </div>
        </section>
      </template>

      <template v-if="activeView === 'resumes'">
        <section class="panel">
          <h3><FileUp :size="18" /> 简历管理</h3>
          <form class="form-grid" @submit.prevent="uploadResume">
            <input v-model="resumeForm.title" placeholder="简历标题" />
            <input id="resume-file" type="file" required @change="resumeForm.file = $event.target.files[0]" />
            <textarea v-model="resumeForm.skills" placeholder="技能"></textarea>
            <textarea v-model="resumeForm.education" placeholder="教育经历"></textarea>
            <textarea v-model="resumeForm.projects" placeholder="项目经历"></textarea>
            <button class="primary" type="submit"><FileUp :size="18" /> 上传简历</button>
          </form>
          <ul class="compact-list">
            <li v-for="resume in resumes" :key="resume.id">{{ resume.title }} · {{ resume.originalFilename }}</li>
            <li v-if="resumes.length === 0">暂无简历。</li>
          </ul>
        </section>
      </template>

      <template v-if="activeView === 'interviews'">
        <section class="panel">
          <h3><CalendarClock :size="18" /> 面试日程</h3>
          <form class="form-grid" @submit.prevent="createInterview">
            <select v-model="interviewForm.applicationId" required>
              <option disabled value="">选择投递</option>
              <option v-for="job in jobs" :key="job.applicationId" :value="job.applicationId">
                {{ job.companyName }} - {{ job.positionName }}
              </option>
            </select>
            <input v-model="interviewForm.title" placeholder="标题" required />
            <input v-model="interviewForm.interviewTime" type="datetime-local" required />
            <input v-model="interviewForm.location" placeholder="地点/链接" />
            <textarea v-model="interviewForm.notes" placeholder="备注"></textarea>
            <button class="primary" type="submit"><CalendarClock :size="18" /> 添加日程</button>
          </form>
          <ul class="compact-list">
            <li v-for="item in interviews" :key="item.id">{{ item.companyName }} · {{ new Date(item.interviewTime).toLocaleString() }}</li>
            <li v-if="interviews.length === 0">暂无面试日程。</li>
          </ul>
        </section>
      </template>

      <template v-if="activeView === 'ai'">
        <section class="panel">
          <h3><Sparkles :size="18" /> AI 岗位匹配分析</h3>
          <form class="ai-form" @submit.prevent="analyze()">
            <select v-model="selectedAiJobId" required>
              <option v-for="job in jobs" :key="job.id" :value="job.id">
                {{ job.companyName }} - {{ job.positionName }}
              </option>
            </select>
            <button class="primary" type="submit"><Sparkles :size="18" /> 生成分析</button>
            <button class="secondary" type="button" @click="generateInterviewQuestions">
              <ListChecks :size="18" />
              生成面试题
            </button>
          </form>
          <p class="provider-line">
            当前 AI 来源：<strong>{{ aiProvider || '尚未生成' }}</strong>
          </p>
          <div v-if="parsedAiResult" class="ai-result">
            <div v-if="parsedAiResult.matchScore !== undefined" class="score-box">
              <span>匹配分</span>
              <strong>{{ parsedAiResult.matchScore }}</strong>
            </div>
            <div v-if="parsedAiResult.strengths" class="result-section">
              <h4>匹配优势</h4>
              <ul><li v-for="item in parsedAiResult.strengths" :key="item">{{ item }}</li></ul>
            </div>
            <div v-if="parsedAiResult.gaps" class="result-section">
              <h4>短板风险</h4>
              <ul><li v-for="item in parsedAiResult.gaps" :key="item">{{ item }}</li></ul>
            </div>
            <div v-if="parsedAiResult.resumeAdvice" class="result-section">
              <h4>简历建议</h4>
              <ul><li v-for="item in parsedAiResult.resumeAdvice" :key="item">{{ item }}</li></ul>
            </div>
            <div v-if="parsedAiResult.actionPlan" class="result-section">
              <h4>行动计划</h4>
              <ul><li v-for="item in parsedAiResult.actionPlan" :key="item">{{ item }}</li></ul>
            </div>
            <div v-if="parsedAiResult.technicalQuestions" class="result-section">
              <h4>技术面试题</h4>
              <ul><li v-for="item in parsedAiResult.technicalQuestions" :key="item">{{ item }}</li></ul>
            </div>
            <div v-if="parsedAiResult.behavioralQuestions" class="result-section">
              <h4>行为面试题</h4>
              <ul><li v-for="item in parsedAiResult.behavioralQuestions" :key="item">{{ item }}</li></ul>
            </div>
            <div v-if="parsedAiResult.projectDeepDive" class="result-section">
              <h4>项目深挖</h4>
              <ul><li v-for="item in parsedAiResult.projectDeepDive" :key="item">{{ item }}</li></ul>
            </div>
            <div v-if="parsedAiResult.preparationChecklist" class="result-section">
              <h4>准备清单</h4>
              <ul><li v-for="item in parsedAiResult.preparationChecklist" :key="item">{{ item }}</li></ul>
            </div>
          </div>
          <pre v-else>{{ aiResult || '选择一个岗位后生成匹配分析。' }}</pre>
        </section>
      </template>
    </section>
  </main>
</template>
