import React, { useEffect, useMemo, useRef, useState } from 'react';
import {
  Camera,
  KeyRound,
  Mail,
  MapPin,
  Pencil,
  Phone,
  ShieldCheck,
} from 'lucide-react';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import Modal, { ModalActions } from '../components/ui/Modal';
import { useChangePasswordMutation } from '../services/authApi';
import {
  useGetMeQuery,
  useUpdateEmployeeMutation,
} from '../services/employeeApi';
import { useUploadProfilePictureMutation } from '../services/documentApi';
import { mapEmployee } from '../lib/mappers';

const defaultProfile = {
  name: 'Employee',
  email: '',
  phone: '',
  location: '',
  role: 'Employee',
  department: 'General',
  avatar:
    'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=160&q=80',
  employeeId: '—',
  manager: '—',
  joined: '—',
};

function validateContactForm(form) {
  const errors = [];

  const phoneRegex = /^[6-9]\d{9}$/;
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
  const cityRegex = /^[A-Za-z]+(?:[ '-][A-Za-z]+)*$/;

  const phone = form.phone.trim();
  const personalEmail = form.personalEmail.trim();
  const address = form.address.trim();
  const city = form.city.trim();

  if (!phone) {
    errors.push('Phone number is required.');
  } else if (!phoneRegex.test(phone)) {
    errors.push('Enter a valid 10-digit Indian phone number.');
  }

  if (personalEmail && !emailRegex.test(personalEmail)) {
    errors.push('Enter a valid personal email address.');
  }

  if (!address) {
    errors.push('Address is required.');
  } else if (address.length < 5) {
    errors.push('Address must be at least 5 characters.');
  } else if (address.length > 200) {
    errors.push('Address cannot exceed 200 characters.');
  } else if (!/[A-Za-z]/.test(address)) {
    errors.push('Address must contain letters.');
  }

  if (!city) {
    errors.push('City is required.');
  } else if (!cityRegex.test(city)) {
    errors.push('City must contain only letters.');
  }

  return errors;
}

export default function Profile() {
  const fileInputRef = useRef(null);

  const [editing, setEditing] = useState(false);
  const [passwordOpen, setPasswordOpen] = useState(false);
  const [profileStatus, setProfileStatus] = useState('');
  const [profileErrors, setProfileErrors] = useState([]);
  const [previewAvatar, setPreviewAvatar] = useState('');

  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  const [draftProfile, setDraftProfile] = useState({
    phone: '',
    personalEmail: '',
    address: '',
    city: '',
  });

  const {
    data: rawEmployee,
    error,
    refetch,
  } = useGetMeQuery();

  const apiEmployee = rawEmployee ? mapEmployee(rawEmployee) : null;

  const [updateEmployee] = useUpdateEmployeeMutation();
  const [uploadPicture] = useUploadProfilePictureMutation();
  const [changePasswordRequest] = useChangePasswordMutation();

  const profile = apiEmployee
    ? {
        ...defaultProfile,
        id: apiEmployee.id || apiEmployee.raw?.id,
        name: apiEmployee.name,
        role: apiEmployee.raw?.designationName || apiEmployee.role,
        department: apiEmployee.department,
        email: apiEmployee.email || defaultProfile.email,
        phone: apiEmployee.phone || defaultProfile.phone,
        avatar: previewAvatar || apiEmployee.avatar,
        employeeId:
          apiEmployee.raw?.employeeId || defaultProfile.employeeId,
        location:
          [apiEmployee.raw?.city, apiEmployee.raw?.state]
            .filter(Boolean)
            .join(', ') || defaultProfile.location,
        manager:
          apiEmployee.raw?.reportingManagerName || defaultProfile.manager,
        joined:
          apiEmployee.raw?.dateOfJoining || defaultProfile.joined,
        employmentType: apiEmployee.raw?.employmentType || '',
        workLocation: apiEmployee.raw?.workLocation || '',
        raw: apiEmployee.raw,
      }
    : {
        ...defaultProfile,
        avatar: previewAvatar || defaultProfile.avatar,
      };

  useEffect(() => {
    return () => {
      if (previewAvatar) {
        URL.revokeObjectURL(previewAvatar);
      }
    };
  }, [previewAvatar]);

  const displayProfile = useMemo(
    () => ({
      ...profile,
      ...(editing ? draftProfile : {}),
    }),
    [profile, editing, draftProfile]
  );

  const details = [
    ['Employee ID', displayProfile.employeeId],
    ['Name', displayProfile.name],
    ['Email', displayProfile.email],
    ['Phone Number', displayProfile.phone],
    ['Department', displayProfile.department],
    ['Designation', displayProfile.role],
    ['Joining Date', displayProfile.joined],
    ['Employment Type', displayProfile.employmentType || '-'],
    ['Work Location', displayProfile.workLocation || '-'],
    ['Reporting Manager', displayProfile.manager],
  ];

  function openEditProfile() {
    setDraftProfile({
      phone: profile.phone || '',
      personalEmail: profile.raw?.personalEmail || '',
      address: profile.raw?.address || '',
      city: profile.raw?.city || '',
    });

    setProfileErrors([]);
    setProfileStatus('');
    setEditing(true);
  }

  function cancelEditProfile() {
    setEditing(false);
    setProfileErrors([]);
    setProfileStatus('');
  }

  async function saveProfile(event) {
    event.preventDefault();

    if (!apiEmployee?.raw?.id && !apiEmployee?.id) {
      setProfileErrors(['Unable to identify employee profile.']);
      return;
    }

    const validationErrors = validateContactForm(draftProfile);

    setProfileErrors(validationErrors);

    if (validationErrors.length > 0) {
      setProfileStatus('');
      return;
    }

    setProfileStatus('Updating profile...');

    try {
      await updateEmployee({
        id: profile.id,
        body: {
          ...apiEmployee.raw,
          phoneNumber: draftProfile.phone.trim(),
          personalEmail: draftProfile.personalEmail.trim(),
          address: draftProfile.address.trim(),
          city: draftProfile.city.trim(),
        },
      }).unwrap();

      await refetch();

      setProfileErrors([]);
      setProfileStatus('Profile updated successfully.');
      setEditing(false);
    } catch (err) {
      setProfileStatus(
        err?.data?.message ||
          err?.message ||
          'Request failed.'
      );
    }
  }

  async function uploadProfilePicture(event) {
    const file = event.target.files?.[0];

    if (!file) return;

    const allowedTypes = [
      'image/jpeg',
      'image/png',
      'image/webp',
    ];

    if (!allowedTypes.includes(file.type)) {
      setProfileStatus(
        'Please select a JPG, PNG, or WEBP image.'
      );
      event.target.value = '';
      return;
    }

    const maxFileSize = 5 * 1024 * 1024;

    if (file.size > maxFileSize) {
      setProfileStatus(
        'Profile picture must be smaller than 5 MB.'
      );
      event.target.value = '';
      return;
    }

    if (previewAvatar) {
      URL.revokeObjectURL(previewAvatar);
    }

    const objectUrl = URL.createObjectURL(file);

    setPreviewAvatar(objectUrl);
    setProfileStatus('Uploading profile picture...');

    try {
      await uploadPicture(file).unwrap();
      await refetch();
      setProfileStatus('Profile picture updated.');
    } catch (err) {
      URL.revokeObjectURL(objectUrl);
      setPreviewAvatar('');

      setProfileStatus(
        err?.data?.message ||
          err?.message ||
          'Request failed.'
      );
    } finally {
      event.target.value = '';
    }
  }

  async function changePassword() {
    if (
      !passwordForm.currentPassword ||
      !passwordForm.newPassword ||
      !passwordForm.confirmPassword
    ) {
      setProfileStatus('Please complete all password fields.');
      return;
    }

    if (passwordForm.newPassword.length < 6) {
      setProfileStatus(
        'New password must be at least 6 characters.'
      );
      return;
    }

    if (
      passwordForm.newPassword !== passwordForm.confirmPassword
    ) {
      setProfileStatus(
        'New password and confirmation do not match.'
      );
      return;
    }

    setProfileStatus('Updating password...');

    try {
      await changePasswordRequest({
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
      }).unwrap();

      setProfileStatus('Password changed successfully.');
      setPasswordOpen(false);

      setPasswordForm({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
      });
    } catch (err) {
      setProfileStatus(
        err?.data?.message ||
          err?.message ||
          'Request failed.'
      );
    }
  }

  return (
    <div className="page-stack">
      <div className="page-header">
        <p className="page-kicker">My Profile</p>
        <h1 className="page-title">Profile details</h1>
      </div>

      {error && (
        <p className="alert-warning" role="alert">
          {error?.data?.message ||
            error?.message ||
            'Unable to load profile.'}
        </p>
      )}

      {profileStatus && (
        <p className="alert-success" role="status">
          {profileStatus}
        </p>
      )}

      {profileErrors.length > 0 && (
        <div className="alert-warning" role="alert">
          {profileErrors.map((item) => (
            <p key={item}>{item}</p>
          ))}
        </div>
      )}

      <Card className="overflow-hidden">
        <div className="h-32 bg-gradient-to-r from-brand-primary via-info to-brand-secondary" />

        <div className="-mt-14 flex flex-col gap-6 p-6 md:flex-row md:items-end md:justify-between">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-end">
            <img
              src={displayProfile.avatar}
              alt={displayProfile.name}
              className="h-32 w-32 rounded-card border-4 border-white object-cover shadow-soft"
            />

            <div className="pb-2">
              <h2 className="text-3xl font-extrabold text-ink-primary">
                {displayProfile.name}
              </h2>

              <p className="mt-1 text-ink-secondary">
                {displayProfile.role}
              </p>

              <div className="mt-3 flex flex-wrap gap-3 text-sm text-ink-secondary">
                <span className="flex items-center gap-2">
                  <Mail
                    className="h-4 w-4 shrink-0 text-brand-primary"
                    aria-hidden="true"
                  />
                  {displayProfile.email}
                </span>

                <span className="flex items-center gap-2">
                  <Phone
                    className="h-4 w-4 shrink-0 text-brand-primary"
                    aria-hidden="true"
                  />
                  {displayProfile.phone}
                </span>
              </div>
            </div>
          </div>

          <div className="flex flex-wrap gap-3">
            <Button
              onClick={() => {
                if (editing) {
                  cancelEditProfile();
                } else {
                  openEditProfile();
                }
              }}
            >
              <Pencil className="h-4 w-4" />
              {editing ? 'Close Edit' : 'Edit Profile'}
            </Button>

            <Button
              variant="secondary"
              onClick={() => setPasswordOpen(true)}
            >
              <KeyRound className="h-4 w-4" />
              Change Password
            </Button>

            <Button
              variant="secondary"
              onClick={() => fileInputRef.current?.click()}
            >
              <Camera className="h-4 w-4" />
              Upload Picture
            </Button>

            <input
              ref={fileInputRef}
              type="file"
              accept="image/jpeg,image/png,image/webp"
              className="hidden"
              onChange={uploadProfilePicture}
            />
          </div>
        </div>
      </Card>

      {editing && (
        <Card className="p-5" interactive={false}>
          <h2 className="section-title">
            Edit Contact Information
          </h2>

          <form
            className="mt-5 grid gap-4 md:grid-cols-2"
            onSubmit={saveProfile}
            noValidate
          >
            <label className="block">
              <span className="text-sm font-semibold text-ink-primary">
                Phone Number
              </span>

              <input
                className="field-control mt-2"
                type="tel"
                inputMode="numeric"
                maxLength={10}
                placeholder="10-digit phone number"
                value={draftProfile.phone}
                onChange={(event) => {
                  const value = event.target.value
                    .replace(/\D/g, '')
                    .slice(0, 10);

                  setDraftProfile((current) => ({
                    ...current,
                    phone: value,
                  }));
                }}
                required
              />
            </label>

            <label className="block">
              <span className="text-sm font-semibold text-ink-primary">
                Personal Email
              </span>

              <input
                className="field-control mt-2"
                type="email"
                maxLength={100}
                placeholder="name@example.com"
                value={draftProfile.personalEmail}
                onChange={(event) =>
                  setDraftProfile((current) => ({
                    ...current,
                    personalEmail: event.target.value,
                  }))
                }
              />
            </label>

            <label className="block">
              <span className="text-sm font-semibold text-ink-primary">
                Address
              </span>

              <input
                className="field-control mt-2"
                type="text"
                minLength={5}
                maxLength={200}
                placeholder="Enter address"
                value={draftProfile.address}
                onChange={(event) =>
                  setDraftProfile((current) => ({
                    ...current,
                    address: event.target.value,
                  }))
                }
                required
              />
            </label>

            <label className="block">
              <span className="text-sm font-semibold text-ink-primary">
                City
              </span>

              <input
                className="field-control mt-2"
                type="text"
                minLength={2}
                maxLength={80}
                placeholder="Enter city"
                value={draftProfile.city}
                onChange={(event) => {
                  const value = event.target.value;

                  if (/^[A-Za-z '-]*$/.test(value)) {
                    setDraftProfile((current) => ({
                      ...current,
                      city: value,
                    }));
                  }
                }}
                required
              />
            </label>

            <div className="flex gap-3 md:col-span-2">
              <Button type="submit">
                Save Profile
              </Button>

              <Button
                type="button"
                variant="secondary"
                onClick={cancelEditProfile}
              >
                Cancel
              </Button>
            </div>
          </form>
        </Card>
      )}

      <section className="card-grid lg:grid-cols-[1fr_0.6fr]">
        <Card className="p-5">
          <h2 className="section-title">Employee Details</h2>

          <div className="mt-5 grid gap-4 sm:grid-cols-2">
            {details.map(([label, value]) => (
              <div
                key={label}
                className="rounded-2xl border border-line p-4"
              >
                <p className="text-xs font-bold uppercase text-ink-secondary">
                  {label}
                </p>

                <p className="mt-2 font-bold text-ink-primary">
                  {value}
                </p>
              </div>
            ))}
          </div>
        </Card>

        <Card className="p-5">
          <ShieldCheck
            className="h-8 w-8 text-brand-secondary"
            aria-hidden="true"
          />

          <h2 className="section-title mt-4">
            Verified employee
          </h2>

          <p className="mt-2 text-sm leading-6 text-ink-secondary">
            Profile, contact, and employment information are synced
            with the employee API.
          </p>

          <p className="mt-5 flex items-center gap-2 text-sm font-semibold text-ink-secondary">
            <MapPin
              className="h-4 w-4 shrink-0 text-brand-primary"
              aria-hidden="true"
            />
            {displayProfile.location}
          </p>
        </Card>
      </section>

      <Modal
        open={passwordOpen}
        onClose={() => setPasswordOpen(false)}
        title="Change Password"
        description="Update your account password securely."
        footer={
          <ModalActions
            onCancel={() => setPasswordOpen(false)}
            onConfirm={changePassword}
            confirmLabel="Update Password"
          />
        }
      >
        <div className="grid gap-4">
          <label className="block">
            <span className="text-sm font-semibold text-ink-primary">
              Current Password
            </span>

            <input
              type="password"
              className="field-control mt-2"
              value={passwordForm.currentPassword}
              onChange={(event) =>
                setPasswordForm((current) => ({
                  ...current,
                  currentPassword: event.target.value,
                }))
              }
              required
              minLength={6}
              autoComplete="current-password"
            />
          </label>

          <label className="block">
            <span className="text-sm font-semibold text-ink-primary">
              New Password
            </span>

            <input
              type="password"
              className="field-control mt-2"
              value={passwordForm.newPassword}
              onChange={(event) =>
                setPasswordForm((current) => ({
                  ...current,
                  newPassword: event.target.value,
                }))
              }
              required
              minLength={6}
              autoComplete="new-password"
            />
          </label>

          <label className="block">
            <span className="text-sm font-semibold text-ink-primary">
              Confirm New Password
            </span>

            <input
              type="password"
              className="field-control mt-2"
              value={passwordForm.confirmPassword}
              onChange={(event) =>
                setPasswordForm((current) => ({
                  ...current,
                  confirmPassword: event.target.value,
                }))
              }
              required
              minLength={6}
              autoComplete="new-password"
            />
          </label>
        </div>
      </Modal>
    </div>
  );
}