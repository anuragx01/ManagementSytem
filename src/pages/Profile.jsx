import React, { useEffect, useMemo, useRef, useState } from "react";
import { Camera, KeyRound, Mail, MapPin, Pencil, Phone, ShieldCheck } from 'lucide-react';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import Modal, { ModalActions } from '../components/ui/Modal';
import useApiData from '../hooks/useApiData';
import { authApi, documentsApi, employeesApi } from '../lib/api';
import { mapEmployee } from '../lib/mappers';

const defaultProfile = {
  name: 'Employee',
  email: '',
  phone: '',
  location: '',
  role: 'Employee',
  department: 'General',
  avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=160&q=80',
  employeeId: '—',
  manager: '—',
  joined: '—',
};

export default function Profile() {
  const fileInputRef = useRef(null);
  const [editing, setEditing] = useState(false);
  const [passwordOpen, setPasswordOpen] = useState(false);
  const [profileStatus, setProfileStatus] = useState('');
  const [previewAvatar, setPreviewAvatar] = useState('');
  const [passwordForm, setPasswordForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [draftProfile, setDraftProfile] = useState({
    phone: '',
    personalEmail: '',
    address: '',
    city: '',
  });
  const { data: apiEmployee, error, refresh } = useApiData(
    async () => mapEmployee(await employeesApi.me()),
    null,
    [],
  );
  const profile = apiEmployee
    ? {
        ...defaultProfile,
        name: apiEmployee.name,
        role: apiEmployee.raw?.designationName || apiEmployee.role,
        department: apiEmployee.department,
        email: apiEmployee.email || defaultProfile.email,
        phone: apiEmployee.phone || defaultProfile.phone,
        avatar: previewAvatar || apiEmployee.avatar,
        employeeId: apiEmployee.raw?.employeeId || defaultProfile.employeeId,
        location: [apiEmployee.raw?.city, apiEmployee.raw?.state].filter(Boolean).join(', ') || defaultProfile.location,
        manager: apiEmployee.raw?.reportingManagerName || defaultProfile.manager,
        joined: apiEmployee.raw?.dateOfJoining || defaultProfile.joined,
        raw: apiEmployee.raw,
      }
    : { ...defaultProfile, avatar: previewAvatar || defaultProfile.avatar };

  useEffect(() => () => {
    if (previewAvatar) URL.revokeObjectURL(previewAvatar);
  }, [previewAvatar]);

  const displayProfile = useMemo(() => ({ ...profile, ...(editing ? draftProfile : {}) }), [profile, editing, draftProfile]);

  const details = [
    ['Employee ID', displayProfile.employeeId],
    ['Name', displayProfile.name],
    ['Email', displayProfile.email],
    ['Phone Number', displayProfile.phone],
    ['Department', displayProfile.department],
    ['Designation', displayProfile.role],
    ['Joining Date', displayProfile.joined],
    ['Reporting Manager', displayProfile.manager],
  ];

  async function saveProfile(event) {
    event.preventDefault();
    if (!apiEmployee?.raw?.id && !apiEmployee?.id) return;
    setProfileStatus('Updating profile...');
    try {
      await employeesApi.updateMe({
        ...apiEmployee.raw,
        phoneNumber: draftProfile.phone,
        personalEmail: draftProfile.personalEmail,
        address: draftProfile.address,
        city: draftProfile.city,
      });
      refresh();
      setProfileStatus('Profile updated successfully.');
      setEditing(false);
    } catch (err) {
      setProfileStatus(err.message);
    }
  }

  async function uploadProfilePicture(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    if (previewAvatar) URL.revokeObjectURL(previewAvatar);
    setPreviewAvatar(URL.createObjectURL(file));
    setProfileStatus('Uploading profile picture...');
    try {
      await documentsApi.uploadProfilePicture(file);
      refresh();
      setProfileStatus('Profile picture updated.');
    } catch (err) {
      setPreviewAvatar('');
      setProfileStatus(err.message);
    }
  }

  async function changePassword() {
    if (!passwordForm.currentPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
      setProfileStatus('Please complete all password fields.');
      return;
    }
    if (passwordForm.newPassword.length < 6) {
      setProfileStatus('New password must be at least 6 characters.');
      return;
    }
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setProfileStatus('New password and confirmation do not match.');
      return;
    }
    setProfileStatus('Updating password...');
    try {
      await authApi.changePassword({
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
      });
      setProfileStatus('Password changed successfully.');
      setPasswordOpen(false);
      setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err) {
      setProfileStatus(err.message);
    }
  }

  return (
    <div className="page-stack">
      <div className="page-header">
        <p className="page-kicker">My Profile</p>
        <h1 className="page-title">Profile details</h1>
      </div>
      {error && <p className="alert-warning" role="alert">{error}</p>}
      {profileStatus && <p className="alert-success" role="status">{profileStatus}</p>}
      <Card className="overflow-hidden">
        <div className="h-32 bg-gradient-to-r from-brand-primary via-info to-brand-secondary" />
        <div className="-mt-14 flex flex-col gap-6 p-6 md:flex-row md:items-end md:justify-between">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-end">
            <img src={displayProfile.avatar} alt={displayProfile.name} className="h-32 w-32 rounded-card border-4 border-white object-cover shadow-soft" />
            <div className="pb-2">
              <h2 className="text-3xl font-extrabold text-ink-primary">{displayProfile.name}</h2>
              <p className="mt-1 text-ink-secondary">{displayProfile.role}</p>
              <div className="mt-3 flex flex-wrap gap-3 text-sm text-ink-secondary">
                <span className="flex items-center gap-2"><Mail className="h-4 w-4 shrink-0 text-brand-primary" aria-hidden="true" /> {displayProfile.email}</span>
                <span className="flex items-center gap-2"><Phone className="h-4 w-4 shrink-0 text-brand-primary" aria-hidden="true" /> {displayProfile.phone}</span>
              </div>
            </div>
          </div>
          <div className="flex flex-wrap gap-3">
            <Button onClick={() => {
              setDraftProfile({
                phone: profile.phone || '',
                personalEmail: profile.raw?.personalEmail || '',
                address: profile.raw?.address || '',
                city: profile.raw?.city || '',
              });
              setEditing((value) => !value);
            }}><Pencil className="h-4 w-4" /> Edit Profile</Button>
            <Button variant="secondary" onClick={() => setPasswordOpen(true)}><KeyRound className="h-4 w-4" /> Change Password</Button>
            <Button variant="secondary" onClick={() => fileInputRef.current?.click()}><Camera className="h-4 w-4" /> Upload Picture</Button>
            <input ref={fileInputRef} type="file" accept="image/*" className="hidden" onChange={uploadProfilePicture} />
          </div>
        </div>
      </Card>

      {editing && (
        <Card className="p-5" interactive={false}>
          <h2 className="section-title">Edit Contact Information</h2>
          <form className="mt-5 grid gap-4 md:grid-cols-2" onSubmit={saveProfile}>
            {[
              ['phone', 'Phone Number'],
              ['personalEmail', 'Personal Email'],
              ['address', 'Address'],
              ['city', 'City'],
            ].map(([key, label]) => (
              <label key={key} className="block">
                <span className="text-sm font-semibold text-ink-primary">{label}</span>
                <input className="field-control mt-2" value={draftProfile[key]} onChange={(event) => setDraftProfile((current) => ({ ...current, [key]: event.target.value }))} />
              </label>
            ))}
            <div className="flex gap-3 md:col-span-2">
              <Button type="submit">Save Profile</Button>
              <Button type="button" variant="secondary" onClick={() => setEditing(false)}>Cancel</Button>
            </div>
          </form>
        </Card>
      )}

      <section className="card-grid lg:grid-cols-[1fr_0.6fr]">
        <Card className="p-5">
          <h2 className="section-title">Employee Details</h2>
          <div className="mt-5 grid gap-4 sm:grid-cols-2">
            {details.map(([label, value]) => (
              <div key={label} className="rounded-2xl border border-line p-4">
                <p className="text-xs font-bold uppercase text-ink-secondary">{label}</p>
                <p className="mt-2 font-bold text-ink-primary">{value}</p>
              </div>
            ))}
          </div>
        </Card>
        <Card className="p-5">
          <ShieldCheck className="h-8 w-8 text-brand-secondary" aria-hidden="true" />
          <h2 className="section-title mt-4">Verified employee</h2>
          <p className="mt-2 text-sm leading-6 text-ink-secondary">Profile, contact, and employment information are synced with the employee API.</p>
          <p className="mt-5 flex items-center gap-2 text-sm font-semibold text-ink-secondary"><MapPin className="h-4 w-4 shrink-0 text-brand-primary" aria-hidden="true" /> {displayProfile.location}</p>
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
          <label className="block">
            <span className="text-sm font-semibold text-ink-primary">Confirm New Password</span>
            <input type="password" className="field-control mt-2" value={passwordForm.confirmPassword} onChange={(event) => setPasswordForm((current) => ({ ...current, confirmPassword: event.target.value }))} />
          </label>
        </div>
      </Modal>
    </div>
  );
}
