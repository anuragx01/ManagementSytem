const PREFIX = 'nexstar-daily-report';

function key(userId, date) {
  return `${PREFIX}:${userId || 'guest'}:${date}`;
}

export function todayKey() {
  return new Date().toISOString().slice(0, 10);
}

export function loadDailyReport(userId, date = todayKey()) {
  try {
    const raw = localStorage.getItem(key(userId, date));
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function saveDailyReport(userId, payload, date = todayKey()) {
  localStorage.setItem(key(userId, date), JSON.stringify({ ...payload, updatedAt: new Date().toISOString() }));
}

export function listDailyReportHistory(userId) {
  const prefix = `${PREFIX}:${userId || 'guest'}:`;
  return Object.keys(localStorage)
    .filter((k) => k.startsWith(prefix))
    .map((k) => {
      try {
        return { date: k.slice(prefix.length), ...JSON.parse(localStorage.getItem(k)) };
      } catch {
        return null;
      }
    })
    .filter(Boolean)
    .sort((a, b) => b.date.localeCompare(a.date));
}

