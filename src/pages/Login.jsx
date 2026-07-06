import React, { useState } from "react";
import { ArrowRight, Check, CheckCircle2, Eye, EyeOff, Loader2, ShieldCheck } from "lucide-react";
import { useNavigate } from "react-router-dom";
import LogoMark from "../components/ui/LogoMark";
import { useLoginMutation } from "../services/authApi";


const slides = [
  "/login-slides/0202img.png",
  "/login-slides/0101img.png",
  "/login-slides/02img.png",
  "/login-slides/01img.png",
];

export default function Login() {
  const navigate = useNavigate();
  const [login, { isLoading }] = useLoginMutation();
  const [form, setForm] = useState({
    email: "",
    password: "",
  });

  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  const [touched, setTouched] = useState({});

  const emailError =
    touched.email && form.email && !/^\S+@\S+\.\S+$/.test(form.email)
      ? "Enter a valid work email."
      : "";
  const passwordError =
    touched.password && form.password && form.password.length < 6
      ? "Password must be at least 6 characters."
      : "";
  const isFormInvalid = !form.email || !form.password || emailError || passwordError;

  async function handleSubmit(event) {
    event.preventDefault();
    setTouched({ email: true, password: true });

    if (isFormInvalid) {
      setError("Please complete the highlighted fields before signing in.");
      return;
    }

    setError("");
    setSuccess(false);

    try {
      await login({ email: form.email, password: form.password }).unwrap();
      setSuccess(true);
      window.setTimeout(() => navigate("/dashboard"), 320);
    } catch (err) {
      if (import.meta.env.DEV) {
        console.error("[auth/login] RTK Query error", err);
      }

      setError(
        err.message ||
          "Login failed. Please check backend and credentials."
      );
    }
  }

  return (
    <main className="relative isolate flex min-h-screen items-center justify-center overflow-hidden px-4 py-8 text-white sm:px-6 lg:px-12">
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

        <div className="absolute inset-0 bg-black/[0.20]" />
        <div className="absolute inset-0 bg-[#1B2A4A]/15" />
        <div className="absolute inset-y-0 left-0 w-2/3 bg-[radial-gradient(circle_at_34%_38%,rgba(225,20,28,0.18),transparent_38%),radial-gradient(circle_at_28%_60%,rgba(255,255,255,0.12),transparent_36%)]" />
      </div>

      <div className="grid w-full max-w-7xl items-center gap-8 lg:grid-cols-[minmax(0,1fr)_520px] xl:gap-16">
        <section className="login-hero-enter relative hidden max-w-2xl lg:block">
          <div className="pointer-events-none absolute -left-8 top-8 h-72 w-72 rounded-full bg-[radial-gradient(circle,rgba(255,255,255,0.08),transparent_70%)]" />

          <div className="relative mb-8 inline-flex items-center gap-2 rounded-full border border-white/20 bg-white/[0.12] px-4 py-2 text-sm font-bold text-white shadow-sm backdrop-blur-xl">
            <ShieldCheck className="h-4 w-4 text-[#E1141C]" aria-hidden="true" />
            Enterprise people operations
          </div>

          <h2 className="relative max-w-2xl text-5xl font-extrabold leading-[1.06] tracking-tight text-white drop-shadow-[0_2px_24px_rgba(0,0,0,0.32)] xl:text-[3.5rem] xl:leading-[1.04]">
            Nexstar keeps every team function connected, secure, and visible.
          </h2>

          <p className="relative mt-6 max-w-xl text-base leading-7 text-white/85">
            A modern employee management workspace for attendance, tasks,
            daily reports, role-based access, and operational visibility across
            the organization.
          </p>

          <div className="relative mt-10 grid max-w-xl grid-cols-3 gap-4">
            {["HR Control", "Secure Access", "Live Reports"].map((item, index) => (
              <div
                key={item}
                className="login-float rounded-2xl border border-white/20 bg-white/[0.12] px-4 py-3 text-sm font-bold text-white shadow-[0_18px_50px_rgba(0,0,0,0.18)] backdrop-blur-xl"
                style={{ animationDelay: `${index * 140}ms` }}
              >
                {item}
              </div>
            ))}
          </div>
        </section>

        <section className="login-panel-enter login-card-scroll w-full max-w-[520px] max-h-[92vh] overflow-y-auto rounded-[32px] border border-white/25 bg-white/[0.14] p-8 shadow-[0_32px_100px_rgba(0,0,0,0.34)] backdrop-blur-2xl sm:p-10 lg:ml-auto">
          <div className="mb-2 block lg:hidden">
            <p className="text-sm font-bold text-white/80">Nexstar Employee Management</p>
          </div>

          <div className="mb-8 flex items-center gap-3">
            <LogoMark />

            <div>
              <h1 className="text-2xl font-extrabold tracking-tight text-white">
                NEXSTAR
              </h1>

              <p className="text-sm font-medium text-white/70">
                Employee Management System
              </p>
            </div>
          </div>

          <div className="mb-8 inline-flex items-center gap-2 rounded-full border border-white/20 bg-white/[0.12] px-4 py-2 text-xs font-bold text-white shadow-sm backdrop-blur-xl">
            <ShieldCheck className="h-4 w-4 text-[#E1141C]" aria-hidden="true" />
            Premium HR Workspace
          </div>

          <h2 className="text-[2rem] font-extrabold leading-tight tracking-tight text-white sm:text-4xl">
            Welcome back
          </h2>

          <p className="mt-3 text-sm leading-6 text-white/70">
            Sign in to manage attendance, tasks, reports, and your secure team
            workspace.
          </p>

          <form
            className="mt-8 space-y-6"
            onSubmit={handleSubmit}
            noValidate
          >
            <label className="block">
              <span className="text-sm font-semibold text-white">
                Email
              </span>

              <input
                type="email"
                className={`field-control-dark mt-2 ${emailError ? "border-[#E1141C] ring-4 ring-[#E1141C]/20" : ""}`}
                placeholder="name@nexstar.com"
                value={form.email}
                autoComplete="email"
                aria-invalid={Boolean(emailError)}
                aria-describedby={emailError ? "email-error" : undefined}
                onBlur={() => setTouched((current) => ({ ...current, email: true }))}
                onChange={(event) =>
                  setForm((current) => ({
                    ...current,
                    email: event.target.value,
                  }))
                }
              />
              {emailError && (
                <span id="email-error" className="mt-2 block text-xs font-semibold text-red-100" role="alert">
                  {emailError}
                </span>
              )}
            </label>

            <label className="block">
              <span className="text-sm font-semibold text-white">
                Password
              </span>

              <div className="relative mt-2">
                <input
                  type={showPassword ? "text" : "password"}
                  className={`field-control-dark pr-12 ${passwordError ? "border-[#E1141C] ring-4 ring-[#E1141C]/20" : ""}`}
                  placeholder="Enter password"
                  value={form.password}
                  autoComplete="current-password"
                  aria-invalid={Boolean(passwordError)}
                  aria-describedby={passwordError ? "password-error" : undefined}
                  onBlur={() => setTouched((current) => ({ ...current, password: true }))}
                  onChange={(event) =>
                    setForm((current) => ({
                      ...current,
                      password: event.target.value,
                    }))
                  }
                />
                <button
                  type="button"
                  className="absolute right-2 top-1/2 grid h-9 w-9 -translate-y-1/2 place-items-center rounded-xl text-white/70 transition duration-200 hover:bg-white/[0.12] hover:text-white focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-[#E1141C]/25 active:scale-95"
                  onClick={() => setShowPassword((value) => !value)}
                  aria-label={showPassword ? "Hide password" : "Show password"}
                  aria-pressed={showPassword}
                >
                  {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
              {passwordError && (
                <span id="password-error" className="mt-2 block text-xs font-semibold text-red-100" role="alert">
                  {passwordError}
                </span>
              )}
            </label>

            {error && (
              <p className="rounded-2xl border border-[#E1141C]/30 bg-[#E1141C]/15 px-4 py-3 text-sm font-semibold text-white shadow-sm" role="alert">
                {error}
              </p>
            )}

            {success && (
              <p className="flex items-center gap-2 rounded-2xl border border-emerald-300/40 bg-emerald-400/15 px-4 py-3 text-sm font-semibold text-white" role="status">
                <CheckCircle2 className="h-4 w-4 text-emerald-200" aria-hidden="true" />
                Signed in successfully.
              </p>
            )}

            <div className="flex min-h-6 items-center justify-between gap-4 text-sm">
              <label className="group flex cursor-pointer items-center gap-3 text-white/75">
                <input
                  type="checkbox"
                  className="sr-only"
                  checked={rememberMe}
                  onChange={(event) => setRememberMe(event.target.checked)}
                  aria-label="Remember me"
                />
                <span className="grid h-5 w-5 place-items-center rounded-md border border-white/30 bg-white/10 transition duration-200 group-hover:border-white/50 group-has-[:checked]:border-[#E1141C] group-has-[:checked]:bg-[#E1141C] has-[:focus-visible]:ring-4 has-[:focus-visible]:ring-[#E1141C]/25">
                  <Check className="h-3 w-3 text-white opacity-0 transition duration-200 group-has-[:checked]:opacity-100" aria-hidden="true" />
                </span>
                Remember me
              </label>

              <a
                href="#"
                className="inline-flex min-h-6 items-center rounded-lg font-bold text-red-100 transition duration-200 hover:text-white focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-[#E1141C]/25"
              >
                Forgot password?
              </a>
            </div>

            <button
              type="submit"
              disabled={isLoading || Boolean(success)}
              className="inline-flex min-h-12 w-full items-center justify-center gap-2 rounded-2xl bg-[#E1141C] px-4 py-3 text-sm font-extrabold text-white shadow-[0_18px_44px_rgba(225,20,28,0.36)] transition duration-200 ease-out hover:-translate-y-0.5 hover:bg-red-700 hover:shadow-lift active:translate-y-0 active:scale-[0.98] disabled:cursor-not-allowed disabled:translate-y-0 disabled:opacity-70 focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-[#E1141C]/25"
              aria-busy={isLoading}
            >
              {isLoading && <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />}
              {success && <CheckCircle2 className="h-4 w-4" aria-hidden="true" />}
              {isLoading ? "Signing in..." : success ? "Success" : "Sign In"}

              {!isLoading && !success && <ArrowRight className="h-4 w-4" aria-hidden="true" />}
            </button>
          </form>
        </section>
      </div>
    </main>
  );
}



