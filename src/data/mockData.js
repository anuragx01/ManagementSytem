export const currentUser = {
  name: 'Anurag Raj',
  role: 'Product Operations Lead',
  employeeId: 'NEXSTAR-0421',
  department: 'Operations',
  location: 'Bengaluru, India',
  email: 'anurag.raj@nexstar.example',
  phone: '+91 98765 43210',
  avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=240&q=80',
  manager: 'Priya Sharma',
  joined: '12 Jan 2022',
};

export const stats = [
  { label: 'Check In', value: '09:18 AM', trend: 'On time', type: 'success' },
  { label: 'Check Out', value: '06:12 PM', trend: 'Estimated', type: 'warning' },
  { label: 'Working Hours', value: '7h 44m', trend: '+18m vs avg', type: 'success' },
  { label: 'Pending Tasks', value: '08', trend: '3 high priority', type: 'danger' },
  { label: 'Completed Tasks', value: '24', trend: '+12% this week', type: 'success' },
  { label: 'Assigned Tasks', value: '32', trend: 'Across 4 projects', type: 'info' },
];

export const weeklyAttendance = [
  { day: 'Mon', present: 8.6, target: 8 },
  { day: 'Tue', present: 8.1, target: 8 },
  { day: 'Wed', present: 7.8, target: 8 },
  { day: 'Thu', present: 8.4, target: 8 },
  { day: 'Fri', present: 7.2, target: 8 },
  { day: 'Sat', present: 4.5, target: 4 },
];

export const taskProgress = [
  { name: 'Completed', value: 46 },
  { name: 'In Progress', value: 32 },
  { name: 'Pending', value: 22 },
];

export const tasks = [
  {
    id: 1,
    title: 'Finalize onboarding checklist',
    description: 'Review all documents, asset handover steps, and department-specific onboarding tasks.',
    dueDate: '28 Jun 2026',
    priority: 'High',
    status: 'In Progress',
    progress: 68,
  },
  {
    id: 2,
    title: 'Update attendance exception report',
    description: 'Audit late entries and missing check-outs before payroll cut-off.',
    dueDate: '30 Jun 2026',
    priority: 'Medium',
    status: 'Pending',
    progress: 35,
  },
  {
    id: 3,
    title: 'Department pulse summary',
    description: 'Compile weekly sentiment highlights and blockers for leadership review.',
    dueDate: '01 Jul 2026',
    priority: 'Low',
    status: 'Review',
    progress: 82,
  },
  {
    id: 4,
    title: 'Policy acknowledgment follow-up',
    description: 'Send reminders to employees who have not accepted updated compliance policies.',
    dueDate: '03 Jul 2026',
    priority: 'High',
    status: 'Pending',
    progress: 18,
  },
];

export const attendanceRows = [
  { date: '27 Jun 2026', checkIn: '09:18 AM', checkOut: '06:12 PM', hours: '7h 44m', status: 'Present' },
  { date: '26 Jun 2026', checkIn: '09:02 AM', checkOut: '06:18 PM', hours: '8h 16m', status: 'Present' },
  { date: '25 Jun 2026', checkIn: '09:41 AM', checkOut: '06:05 PM', hours: '7h 24m', status: 'Late' },
  { date: '24 Jun 2026', checkIn: '09:07 AM', checkOut: '05:58 PM', hours: '7h 51m', status: 'Present' },
  { date: '23 Jun 2026', checkIn: '-', checkOut: '-', hours: '-', status: 'Leave' },
];

export const calendarDays = Array.from({ length: 30 }, (_, index) => {
  const day = index + 1;
  const status = [2, 9, 16, 23].includes(day) ? 'leave' : [5, 12, 19, 26].includes(day) ? 'late' : 'present';
  return { day, status };
});

export const notifications = [
  { id: 1, type: 'New Task', title: 'New task assigned', message: 'Finalize onboarding checklist was assigned to you.', time: '12 min ago', unread: true },
  { id: 2, type: 'Deadline', title: 'Deadline approaching', message: 'Attendance exception report is due in 3 days.', time: '1h ago', unread: true },
  { id: 3, type: 'Announcement', title: 'Quarterly townhall', message: 'Company townhall starts Friday at 4:00 PM.', time: 'Yesterday', unread: false },
  { id: 4, type: 'Reminder', title: 'Profile update pending', message: 'Please verify your emergency contact details.', time: '2 days ago', unread: false },
];

export const announcements = [
  'New leave policy takes effect from July 1.',
  'Payroll review window closes on June 29.',
  'Wellness week registration is now open.',
];

export const activityTimeline = [
  { title: 'Checked in', detail: 'Office check-in recorded at 09:18 AM', time: 'Today' },
  { title: 'Task moved', detail: 'Onboarding checklist moved to In Progress', time: 'Today' },
  { title: 'Report submitted', detail: 'Daily work report shared with manager', time: 'Yesterday' },
  { title: 'Profile verified', detail: 'Contact and emergency details confirmed', time: '25 Jun' },
];

export const employees = [
  { id: 1, name: 'Priya Sharma', role: 'People Success Manager', department: 'HR', status: 'Available', avatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=160&q=80' },
  { id: 2, name: 'Rahul Mehta', role: 'Frontend Engineer', department: 'Engineering', status: 'In Meeting', avatar: 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=160&q=80' },
  { id: 3, name: 'Aisha Khan', role: 'Finance Analyst', department: 'Finance', status: 'Available', avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=160&q=80' },
  { id: 4, name: 'Karan Patel', role: 'Support Lead', department: 'Support', status: 'Away', avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=160&q=80' },
  { id: 5, name: 'Neha Verma', role: 'Project Coordinator', department: 'Operations', status: 'Available', avatar: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=160&q=80' },
  { id: 6, name: 'Dev Singh', role: 'QA Specialist', department: 'Engineering', status: 'Focus', avatar: 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=160&q=80' },
];

export const reportEntries = [
  { area: 'Operations', work: 'Reviewed onboarding checklist and aligned open items with HR.', hours: '2.5h', status: 'Completed' },
  { area: 'Attendance', work: 'Validated late check-in exceptions for payroll processing.', hours: '1.5h', status: 'In Progress' },
  { area: 'Team Support', work: 'Responded to employee profile and policy queries.', hours: '2h', status: 'Completed' },
];
