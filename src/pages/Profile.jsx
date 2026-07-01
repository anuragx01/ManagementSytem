import React from "react";
import { useState } from 'react';
import { Camera, KeyRound, Mail, MapPin, Pencil, Phone, ShieldCheck } from 'lucide-react';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import { currentUser } from '../data/mockData';
import useApiData from '../hooks/useApiData';
import { employeesApi } from '../lib/api';
import { mapEmployee } from '../lib/mappers';

export default function Profile() {
  const [editing, setEditing] = useState(false);
  const [profileStatus, setProfileStatus] = useState('');
  const [savedProfile, setSavedProfile] = useState({});
  const [draftProfile, setDraftProfile] = useState({
    name: currentUser.name,
    email: currentUser.email,
    phone: currentUser.phone,
    location: currentUser.location,
  });
  const { data: apiEmployee, error } = useApiData(
    async () => mapEmployee(await employeesApi.me()),
    null,
    [],
  );
  const profile = apiEmployee
    ? {
        ...currentUser,
        name: apiEmployee.name,
        role: apiEmployee.role,
        department: apiEmployee.department,
        email: apiEmployee.email || currentUser.email,
        phone: apiEmployee.phone || currentUser.phone,
        avatar: apiEmployee.avatar,
        employeeId: apiEmployee.raw?.employeeId || currentUser.employeeId,
        location: [apiEmployee.raw?.city, apiEmployee.raw?.state].filter(Boolean).join(', ') || currentUser.location,
        manager: apiEmployee.raw?.reportingManagerName || currentUser.manager,
        joined: apiEmployee.raw?.dateOfJoining || currentUser.joined,
      }
    : currentUser;
  const displayProfile = { ...profile, ...savedProfile, ...(editing ? draftProfile : {}) };
  const details = [
    ['Employee ID', displayProfile.employeeId],
    ['Department', displayProfile.department],
    ['Manager', displayProfile.manager],
    ['Joined', displayProfile.joined],
    ['Location', displayProfile.location],
    ['Role', displayProfile.role],
  ];

  function saveProfile(event) {
    event.preventDefault();
    setSavedProfile(draftProfile);
    setProfileStatus('Profile updated successfully.');
    setEditing(false);
  }

  return (
    <div className="space-y-6">
      <div>
        <p className="text-sm font-bold text-brand-primary">Employee Profile</p>
        <h1 className="mt-1 text-3xl font-extrabold text-ink-primary">Profile details</h1>
      </div>
      {error && <p className="rounded-2xl bg-orange-50 px-4 py-3 text-sm font-semibold text-orange-700">Showing mock profile because API is unavailable: {error}</p>}
      {profileStatus && <p className="rounded-2xl bg-brand-successSoft px-4 py-3 text-sm font-semibold text-emerald-700">{profileStatus}</p>}
      <Card className="overflow-hidden">
        <div className="h-32 bg-gradient-to-r from-brand-primary via-info to-brand-secondary" />
        <div className="-mt-14 flex flex-col gap-6 p-6 md:flex-row md:items-end md:justify-between">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-end">
            <img src={displayProfile.avatar} alt={displayProfile.name} className="h-32 w-32 rounded-card border-4 border-white object-cover shadow-soft" />
            <div className="pb-2">
              <h2 className="text-3xl font-extrabold text-ink-primary">{displayProfile.name}</h2>
              <p className="mt-1 text-ink-secondary">{displayProfile.role}</p>
              <div className="mt-3 flex flex-wrap gap-3 text-sm text-ink-secondary">
                <span className="flex items-center gap-2"><Mail className="h-4 w-4 text-brand-primary" /> {displayProfile.email}</span>
                <span className="flex items-center gap-2"><Phone className="h-4 w-4 text-brand-primary" /> {displayProfile.phone}</span>
              </div>
            </div>
          </div>
          <div className="flex flex-wrap gap-3">
            <Button onClick={() => {
              setDraftProfile({ name: profile.name, email: profile.email, phone: profile.phone, location: profile.location });
              setEditing((value) => !value);
            }}><Pencil className="h-4 w-4" /> Edit Profile</Button>
            <Button variant="secondary"><KeyRound className="h-4 w-4" /> Change Password</Button>
            <Button variant="secondary"><Camera className="h-4 w-4" /> Upload Picture</Button>
          </div>
        </div>
      </Card>
      {editing && (
        <Card className="p-5">
          <h3 className="text-lg font-extrabold text-ink-primary">Edit Profile</h3>
          <form className="mt-5 grid gap-4 md:grid-cols-2" onSubmit={saveProfile}>
            {[
              ['name', 'Name'],
              ['email', 'Email'],
              ['phone', 'Phone'],
              ['location', 'Location'],
            ].map(([key, label]) => (
              <label key={key} className="block">
                <span className="text-sm font-semibold text-ink-primary">{label}</span>
                <input className="mt-2 w-full rounded-2xl border border-line px-4 py-3 outline-none focus:border-brand-primary focus:ring-4 focus:ring-red-100" value={draftProfile[key]} onChange={(event) => setDraftProfile((current) => ({ ...current, [key]: event.target.value }))} />
              </label>
            ))}
            <div className="flex gap-3 md:col-span-2">
              <Button type="submit">Save Profile</Button>
              <Button type="button" variant="secondary" onClick={() => setEditing(false)}>Cancel</Button>
            </div>
          </form>
        </Card>
      )}
      <section className="grid gap-5 lg:grid-cols-[1fr_0.6fr]">
        <Card className="p-5">
          <h3 className="text-lg font-extrabold text-ink-primary">Employee Details</h3>
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
          <ShieldCheck className="h-8 w-8 text-brand-secondary" />
          <h3 className="mt-4 text-lg font-extrabold text-ink-primary">Verified employee</h3>
          <p className="mt-2 text-sm leading-6 text-ink-secondary">Profile, contact, and employment information are reviewed for the current payroll cycle.</p>
          <p className="mt-5 flex items-center gap-2 text-sm font-semibold text-ink-secondary"><MapPin className="h-4 w-4 text-brand-primary" /> {displayProfile.location}</p>
        </Card>
      </section>
    </div>
  );
}
