export function mapEmployee(employee) {
  const name = [employee.firstName, employee.lastName].filter(Boolean).join(' ') || employee.name || 'Employee';
  return {
    id: employee.id,
    name,
    role: employee.designationName || employee.role || employee.employmentType || 'Employee',
    department: employee.departmentName || employee.department || 'General',
    status: employee.status === 'ACTIVE' ? 'Available' : employee.status || 'Away',
    avatar: employee.profilePictureUrl || employee.avatar || 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=160&q=80',
    email: employee.email,
    phone: employee.phoneNumber,
    raw: employee,
  };
}

export function mapTask(task) {
  const progress = task.status === 'DONE' ? 100 : task.status === 'IN_PROGRESS' ? 60 : task.status === 'IN_REVIEW' ? 82 : 25;
  return {
    id: task.id,
    title: task.title,
    description: task.description || task.taskKey || 'No description added.',
    dueDate: task.dueDate || 'No due date',
    priority: titleCase(task.priority || 'MEDIUM'),
    status: titleCase((task.status || 'TODO').replaceAll('_', ' ')),
    progress: task.checklistTotal ? Math.round((task.checklistDone / task.checklistTotal) * 100) : progress,
    assigneeId: task.assigneeId,
    employeeName: task.assigneeName || task.employeeName || 'Unassigned',
    employeeId: task.employeeId || task.assigneeEmployeeId || '',
    department: task.departmentName || task.assigneeDepartmentName || '',
    designation: task.designationName || task.assigneeDesignationName || '',
    raw: task,
  };
}

export function mapAttendance(row) {
  return {
    employeeName: row.employeeName || '-',
    employeeCode: row.employeeCode || row.employeeId || '-',
    date: row.date || '-',
    checkIn: formatTime(row.clockIn),
    checkOut: formatTime(row.clockOut),
    hours: row.workedHours || '-',
    status: titleCase((row.status || 'PRESENT').replaceAll('_', ' ')),
    raw: row,
  };
}

export function mapNotification(note) {
  return {
    id: note.id,
    type: titleCase((note.type || 'SYSTEM').replaceAll('_', ' ')),
    title: note.title || 'Notification',
    message: note.message || '',
    time: note.createdAt ? new Date(note.createdAt).toLocaleString() : 'Now',
    unread: note.read === false || note.unread,
    raw: note,
  };
}

export function formatTime(value) {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function titleCase(value) {
  return String(value)
    .toLowerCase()
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}
