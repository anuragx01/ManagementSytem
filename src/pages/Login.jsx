import React, { useState } from "react";
import { ArrowRight, ShieldCheck } from "lucide-react";
import { useNavigate } from "react-router-dom";
import LogoMark from "../components/ui/LogoMark";
import { useAuth } from "../context/AuthContext";
import { useRole } from "../context/RoleContext";
import { roleOptions } from "../data/roles";

const slides = [
  "/login-slides/0202img.png",
  "/login-slides/0101img.png",
  "/login-slides/02img.png",
  "/login-slides/01img.png",
];

export default function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const { activeRole, previewRoleKey, setActiveRoleKey } = useRole();

  const [form, setForm] = useState({
    email: "",
    password: "",
  });

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      await login(form);
      navigate("/dashboard");
    } catch (err) {
      setError(
        err.message ||
          "Login failed. Please check backend and credentials."
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="relative isolate flex h-screen items-center justify-center overflow-hidden px-4 text-white sm:px-6 lg:px-12">
      {/* Animated Background */}
      <div className="absolute inset-0 -z-10 overflow-hidden bg-[#27384F]">
        {slides.map((slide, index) => (
          <img
            key={slide}
            src={slide}
            alt=""
            className="login-slide absolute inset-0 h-full w-full object-cover"
            style={{ animationDelay: `${index * 5}s` }}
          />
        ))}

        <div className="absolute inset-0 bg-black/50" />
        <div className="absolute inset-0 bg-[#27384F]/35" />
      </div>

      <div className="grid w-full max-w-6xl items-center gap-8 lg:grid-cols-[1fr_430px]">
        <section className="hidden max-w-xl lg:block">
          <div className="mb-5 inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/10 px-4 py-2 text-sm font-semibold text-white shadow-sm backdrop-blur-xl">
            <ShieldCheck className="h-4 w-4 text-[#E1141C]" />
            Enterprise people operations
          </div>

          <h2 className="text-5xl font-extrabold leading-tight tracking-tight text-white">
            Nexstar keeps every team function connected, secure, and visible.
          </h2>

          <p className="mt-5 max-w-lg text-base leading-7 text-white/75">
            A modern employee management workspace for attendance, tasks,
            daily reports, role-based access, and operational visibility across
            the organization.
          </p>

          <div className="mt-8 grid max-w-lg grid-cols-3 gap-3">
            {["HR Control", "Secure Access", "Live Reports"].map((item) => (
              <div
                key={item}
                className="rounded-2xl border border-white/15 bg-white/10 px-4 py-3 text-sm font-bold text-white backdrop-blur-xl"
              >
                {item}
              </div>
            ))}
          </div>
        </section>

        {/* Login Card */}
        <section className="login-card-scroll w-full max-w-[430px] max-h-[92vh] overflow-y-auto rounded-3xl border border-white/20 bg-white/10 p-5 shadow-[0_28px_90px_rgba(0,0,0,0.35)] backdrop-blur-xl sm:p-6 lg:ml-auto">
        {/* Logo */}
        <div className="mb-5 flex items-center gap-3">
          <LogoMark />

          <div>
            <h1 className="text-2xl font-extrabold text-white">
              NEXSTAR
            </h1>

            <p className="text-sm font-medium text-white/70">
              Employee Management System
            </p>
          </div>
        </div>

        {/* Badge */}
        <div className="mb-5 inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/10 px-4 py-1.5 text-xs font-semibold text-white shadow-sm">
          <ShieldCheck className="h-4 w-4 text-[#E1141C]" />
          Premium HR Workspace
        </div>

        {/* Heading */}
        <h2 className="text-3xl font-extrabold tracking-tight text-white">
          Welcome back
        </h2>

        <p className="mt-2 text-sm leading-5 text-white/70">
          Sign in to manage attendance, tasks, reports, and your secure team
          workspace.
        </p>

        {/* Form */}
        <form
          className="mt-5 space-y-3"
          onSubmit={handleSubmit}
        >
          {/* Role */}
          <label className="block">
            <span className="text-sm font-semibold text-white">
              Preview Role
            </span>

            <select
              className="mt-2 w-full rounded-2xl border border-white/20 bg-white/10 px-4 py-2.5 font-semibold text-white outline-none transition focus:border-[#E1141C] focus:ring-4 focus:ring-[#E1141C]/20"
              value={previewRoleKey}
              onChange={(event) =>
                setActiveRoleKey(event.target.value)
              }
            >
              {roleOptions.map((role) => (
                <option
                  key={role.key}
                  value={role.key}
                  className="bg-[#27384F] text-white"
                >
                  {role.label}
                </option>
              ))}
            </select>

            <span className="mt-1 block text-xs font-medium text-white/60">
              {activeRole.title}
            </span>
          </label>

          {/* Email */}
          <label className="block">
            <span className="text-sm font-semibold text-white">
              Email
            </span>

            <input
              type="email"
              className="mt-2 w-full rounded-2xl border border-white/20 bg-white/10 px-4 py-2.5 text-white outline-none transition placeholder:text-white/40 focus:border-[#E1141C] focus:ring-4 focus:ring-[#E1141C]/20"
              placeholder="name@nexstar.com"
              value={form.email}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  email: event.target.value,
                }))
              }
            />
          </label>

          {/* Password */}
          <label className="block">
            <span className="text-sm font-semibold text-white">
              Password
            </span>

            <input
              type="password"
              className="mt-2 w-full rounded-2xl border border-white/20 bg-white/10 px-4 py-2.5 text-white outline-none transition placeholder:text-white/40 focus:border-[#E1141C] focus:ring-4 focus:ring-[#E1141C]/20"
              placeholder="Enter password"
              value={form.password}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  password: event.target.value,
                }))
              }
            />
          </label>

          {/* Error */}
          {error && (
            <p className="rounded-2xl border border-[#E1141C]/30 bg-[#E1141C]/15 px-4 py-2 text-sm font-semibold text-white">
              {error}
            </p>
          )}

          {/* Remember */}
          <div className="flex items-center justify-between text-sm">
            <label className="flex items-center gap-2 text-white/70">
              <input
                type="checkbox"
                className="h-4 w-4 rounded border-white/30 accent-[#E1141C]"
              />
              Remember me
            </label>

            <a
              href="#"
              className="font-semibold text-[#ff5a60] transition hover:text-white"
            >
              Forgot password?
            </a>
          </div>

          {/* Login Button */}
          <button
            type="submit"
            disabled={loading}
            className="inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-[#E1141C] px-4 py-2.5 text-sm font-bold text-white shadow-[0_16px_40px_rgba(225,20,28,0.32)] transition hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-70"
          >
            {loading ? "Signing in..." : "Sign In"}

            <ArrowRight className="h-4 w-4" />
          </button>

          {/* Preview Button */}
          <button
            type="button"
            onClick={() => navigate("/dashboard")}
            className="inline-flex w-full items-center justify-center rounded-2xl border border-white/20 bg-white/10 px-4 py-2.5 text-sm font-bold text-white transition hover:bg-white/20"
          >
            Continue with Mock Preview
          </button>
        </form>
        </section>
      </div>
    </main>
  );
}
