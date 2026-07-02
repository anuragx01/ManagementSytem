import React, { useRef, useState } from 'react';
import { Camera, KeyRound, Monitor, Moon, ShieldCheck, User } from 'lucide-react';
import PageHeader from '../components/ui/PageHeader';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';
import Modal, { ModalActions } from '../components/ui/Modal';
import useApiData from '../hooks/useApiData';
import { authApi, documentsApi, employeesApi } from '../lib/api';
import { mapEmployee } from '../lib/mappers';

export default function Settings() {
  const fileRef = useRef(null);
  const [status, setStatus] = useState('');
  const [passwordOpen, setPasswordOpen] = useState(false);
  const [passwordForm, setPasswordForm] = useState({ currentPassword: '', newPassword: '' });
  const [preferences, setPreferences] = useState({ theme: 'System', notifications: true, language: 'English' });
  const { data: profile, loading, error, refresh } = useApiData(
    async () => mapEmployee(await employeesApi.me()),
    null,
    [],
  );

  async function uploadProfilePicture(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    setStatus('Uploading profile picture...');
    try {
      await documentsApi.uploadProfilePicture(file);
      refresh();
      setStatus('Profile picture updated.');
    } catch (err) {
      setStatus(err.message);
    }
  }

  async function changePassword() {
    setStatus('Updating password...');
    try {
      await authApi.changePassword(passwordForm);
      setPasswordOpen(false);
      setPasswordForm({ currentPassword: '', newPassword: '' });
      setStatus('Password changed successfully.');
    } catch (err) {
      setStatus(err.message);
    }
  }

  return (
    <div className="page-stack">
      <PageHeader kicker="Settings" title="Account settings" description="Profile, account security, preferences, and active sessions." />
      {loading && <p className="loading-text">Loading settings...</p>}
      {error && <p className="alert-warning" role="alert">{error}</p>}
      {status && <p className="alert-info" role="status">{status}</p>}

      <section className="section-grid xl:grid-cols-[0.8fr_1.2fr]">
        <Card className="p-5" interactive={false}>
          <h2 className="section-title flex items-center gap-2"><User className="h-5 w-5 text-brand-primary" /> Profile</h2>
          <div className="mt-5 flex items-center gap-4">
            <img src={profile?.avatar} alt={profile?.name || 'Profile'} className="h-20 w-20 rounded-2xl object-cover" />
            <div>
              <p className="text-xl font-extrabold text-ink-primary">{profile?.name || 'Employee'}</p>
              <p className="text-sm text-ink-secondary">{profile?.email || '-'}</p>
              <p className="text-sm text-ink-secondary">{profile?.phone || profile?.raw?.phoneNumber || '-'}</p>
            </div>
          </div>
          <Button className="mt-5" variant="secondary" onClick={() => fileRef.current?.click()}><Camera className="h-4 w-4" /> Profile Picture</Button>
          <input ref={fileRef} type="file" accept="image/*" className="hidden" onChange={uploadProfilePicture} />
        </Card>

        <Card className="p-5" interactive={false}>
          <h2 className="section-title flex items-center gap-2"><ShieldCheck className="h-5 w-5 text-brand-primary" /> Account</h2>
          <div className="mt-5 grid gap-3 md:grid-cols-3">
            <Button onClick={() => setPasswordOpen(true)}><KeyRound className="h-4 w-4" /> Change Password</Button>
            <Button variant="secondary" type="button">Two-Factor UI Ready</Button>
            <Button variant="secondary" type="button">Session Management</Button>
          </div>
        </Card>
      </section>

      <section className="section-grid xl:grid-cols-2">
        <Card className="p-5" interactive={false}>
          <h2 className="section-title flex items-center gap-2"><Moon className="h-5 w-5 text-brand-primary" /> Preferences</h2>
          <div className="mt-5 grid gap-4 md:grid-cols-3">
            <label className="block">
              <span className="text-sm font-semibold text-ink-primary">Theme</span>
              <select className="select-control mt-2" value={preferences.theme} onChange={(event) => setPreferences((current) => ({ ...current, theme: event.target.value }))}>
                <option>System</option>
                <option>Light</option>
                <option>Dark</option>
              </select>
            </label>
            <label className="block">
              <span className="text-sm font-semibold text-ink-primary">Notifications</span>
              <select className="select-control mt-2" value={preferences.notifications ? 'Enabled' : 'Disabled'} onChange={(event) => setPreferences((current) => ({ ...current, notifications: event.target.value === 'Enabled' }))}>
                <option>Enabled</option>
                <option>Disabled</option>
              </select>
            </label>
            <label className="block">
              <span className="text-sm font-semibold text-ink-primary">Language</span>
              <select className="select-control mt-2" value={preferences.language} onChange={(event) => setPreferences((current) => ({ ...current, language: event.target.value }))}>
                <option>English</option>
                <option>Hindi</option>
              </select>
            </label>
          </div>
        </Card>

        <Card className="p-5" interactive={false}>
          <h2 className="section-title flex items-center gap-2"><Monitor className="h-5 w-5 text-brand-primary" /> Security</h2>
          <div className="mt-5 space-y-3">
            <div className="rounded-2xl border border-line p-4">
              <p className="font-bold text-ink-primary">Recent Login Activity</p>
              <p className="mt-1 text-sm text-ink-secondary">Shown when backend login activity endpoint is connected.</p>
            </div>
            <div className="rounded-2xl border border-line p-4">
              <p className="font-bold text-ink-primary">Device Sessions</p>
              <p className="mt-1 text-sm text-ink-secondary">Session controls are UI-ready and will bind to session APIs when available.</p>
            </div>
          </div>
        </Card>
      </section>

      <Modal
        open={passwordOpen}
        onClose={() => setPasswordOpen(false)}
        title="Change Password"
        description="Update your account password securely."
        footer={<ModalActions onCancel={() => setPasswordOpen(false)} onConfirm={changePassword} confirmLabel="Update Password" />}
      >
        <div className="grid gap-4">
          <label className="block">
            <span className="text-sm font-semibold text-ink-primary">Current Password</span>
            <input type="password" className="field-control mt-2" value={passwordForm.currentPassword} onChange={(event) => setPasswordForm((current) => ({ ...current, currentPassword: event.target.value }))} />
          </label>
          <label className="block">
            <span className="text-sm font-semibold text-ink-primary">New Password</span>
            <input type="password" className="field-control mt-2" value={passwordForm.newPassword} onChange={(event) => setPasswordForm((current) => ({ ...current, newPassword: event.target.value }))} />
          </label>
        </div>
      </Modal>
    </div>
  );
}
