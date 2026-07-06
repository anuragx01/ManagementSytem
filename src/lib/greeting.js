export function getTimeGreeting(date = new Date()) {
  const hour = date.getHours();
  if (hour >= 5 && hour < 12) return { emoji: '🌅', label: 'Good Morning' };
  if (hour >= 12 && hour < 17) return { emoji: '☀️', label: 'Good Afternoon' };
  if (hour >= 17 && hour < 21) return { emoji: '🌇', label: 'Good Evening' };
  return { emoji: '🌙', label: 'Good Night' };
}

export function buildWelcomeTitle(roleLabel = 'User') {
  const { emoji, label } = getTimeGreeting();
  return `${emoji} ${label}, ${roleLabel}`;
}

