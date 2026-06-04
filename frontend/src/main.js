import { createApp } from 'vue'
import {
  BriefcaseBusiness,
  CalendarClock,
  FileUp,
  LogOut,
  Plus,
  Send,
  Sparkles,
  UserRound
} from 'lucide-vue-next'
import App from './App.vue'
import './styles.css'

const app = createApp(App)
app.component('BriefcaseBusiness', BriefcaseBusiness)
app.component('CalendarClock', CalendarClock)
app.component('FileUp', FileUp)
app.component('LogOut', LogOut)
app.component('Plus', Plus)
app.component('Send', Send)
app.component('Sparkles', Sparkles)
app.component('UserRound', UserRound)
app.mount('#app')
