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

const token = ref(localStorage.getItem('token') || '')
const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
const jobs = ref([])
const resumes = ref([])
const stats = ref(null)
const aiResult = ref('')
const loading = ref(false)
const authMode = ref('login')
const error = ref('')

const authForm = reactive({
  username: 'demo',
  email: 'demo@example.com',
  password: 'password123',
  fullName: 'Demo Student',
  school: 'PostgreSQL University',
  major: 'Computer Science',
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

const interviews = ref([])

const groupedJobs = computed(() => {
  const groups = Object.fromEntries(statuses.map(([key]) => [key, []]))
  for (const job of jobs.value) {
    groups[job.status]?.push(job)
  }
  return groups
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
    const body = await response.json().catch(() => ({}))
    throw new Error(body.message || `HTTP ${response.status}`)
  }
  if (response.status === 204) return null
  return response.json().catch(() => null)
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
  await loadInterviews()
}

async function createJob() {
  error.value = ''
  await request('/jobs', { method: 'POST', body: JSON.stringify(jobForm) })
  Object.assign(jobForm, { companyName: '', positionName: '', city: '', jobDescription: '', sourceUrl: '', deadline: '' })
  await loadAll()
}

async function updateStatus(job, status) {
  await request(`/applications/${job.applicationId}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status })
  })
  await loadAll()
}

async function analyze(job) {
  const candidateProfile = [
    user.value?.skills,
    resumeForm.skills,
    resumes.value[0]?.structuredContent
  ].filter(Boolean).join('\n')
  const data = await request('/ai/analyze-job-match', {
    method: 'POST',
    body: JSON.stringify({
      jobId: job.id,
      resumeId: resumes.value[0]?.id,
      jobDescription: job.jobDescription,
      candidateProfile: candidateProfile || 'Java, Spring Boot, PostgreSQL, Vue'
    })
  })
  aiResult.value = JSON.stringify(JSON.parse(data.result), null, 2)
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
        <p class="eyebrow">Internship Tracker</p>
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
      <div>
        <p class="eyebrow">Internship Tracker</p>
        <h2>{{ user?.fullName || user?.username }}</h2>
        <p>{{ user?.school }} · {{ user?.major }}</p>
      </div>
      <button class="ghost" @click="logout">
        <LogOut :size="18" />
        退出
      </button>
    </aside>

    <section class="content">
      <header class="topbar">
        <div>
          <h1>申请看板</h1>
          <p>管理岗位、投递状态、简历和面试准备。</p>
        </div>
        <div class="stats">
          <strong>{{ stats?.totalApplications || 0 }}</strong>
          <span>总投递</span>
          <strong>{{ stats?.interviewConversionRate || 0 }}%</strong>
          <span>面试转化</span>
        </div>
      </header>

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

      <section class="lower-grid">
        <div class="panel">
          <h3><FileUp :size="18" /> 简历</h3>
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
          </ul>
        </div>

        <div class="panel">
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
            <li v-for="item in interviews" :key="item.id">
              {{ item.companyName }} · {{ new Date(item.interviewTime).toLocaleString() }}
            </li>
          </ul>
        </div>
      </section>

      <section class="panel">
        <h3><Sparkles :size="18" /> AI 输出</h3>
        <pre>{{ aiResult || '点击岗位卡片上的 AI 分析按钮。' }}</pre>
      </section>
    </section>
  </main>
</template>
