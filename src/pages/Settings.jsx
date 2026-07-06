import React, { useRef, useState } from 'react';
import { Camera, User } from 'lucide-react';
import PageHeader from '../components/ui/PageHeader';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';
import useApiData from '../hooks/useApiData';
import { documentsApi, employeesApi } from '../lib/api';
import { mapEmployee } from '../lib/mappers';

export default function Settings() {
  const fileRef = useRef(null);
  const [status, setStatus] = useState('');
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

  return (
    <div className="page-stack">
      <PageHeader kicker="Settings" title="Account settings" description="Manage your profile picture and account details." />
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
      </section>
    </div>
  );
}

