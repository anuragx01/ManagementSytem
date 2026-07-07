export function getTimeGreeting(date = new Date()) {
  const hour = date.getHours();
  if (hour >= 5 && hour < 12) return { icon: 'sunrise', label: 'Good Morning' };
  if (hour >= 12 && hour < 17) return { icon: 'sun', label: 'Good Afternoon' };
  if (hour >= 17 && hour < 21) return { icon: 'sunset', label: 'Good Evening' };
  return { icon: 'moon', label: 'Good Night' };
}

export function buildWelcomeTitle(roleLabel = 'User') {
  const { label } = getTimeGreeting();
  return `${label}, ${roleLabel}`;
}

