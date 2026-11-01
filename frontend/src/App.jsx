
import { useEffect, useState } from 'react'
import {
  Routes, Route, Navigate, useNavigate, useParams, Link, useSearchParams
} from 'react-router-dom'
import {
  Home, Search, Heart, PlusSquare, UserRound, LogOut,
  Camera, ImagePlus, Trash2, MessageCircle, Bookmark, MoreHorizontal,
  Bell, CheckCheck, X, ArrowLeft, ShieldCheck, Users,
  Eye, EyeOff, Mail, KeyRound, CheckCircle2, RefreshCw
} from 'lucide-react'
import api from './api'
import SearchPage from './Search'
import Messenger from './Messenger'

function Auth({ mode = 'login', onAuth }) {
  const [searchParams] = useSearchParams()
  const initialEmail = searchParams.get('email') || ''

  // Current view: 'login' | 'register' | 'verify-email' | 'forgot-password' | 'reset-password'
  const [view, setView] = useState(mode)

  useEffect(() => {
    setView(mode)
  }, [mode])

  // Registration & Login Form
  const [form, setForm] = useState({
    username: '',
    email: initialEmail,
    password: '',
    confirmPassword: ''
  })

  // Verification Form
  const [verifyEmail, setVerifyEmail] = useState(initialEmail)
  const [verificationCode, setVerificationCode] = useState('')

  // Password Reset Form
  const [resetEmail, setResetEmail] = useState(initialEmail)
  const [resetCode, setResetCode] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmNewPassword, setConfirmNewPassword] = useState('')

  // Show/Hide password toggles
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [showNewPassword, setShowNewPassword] = useState(false)
  const [showConfirmNewPassword, setShowConfirmNewPassword] = useState(false)

  // Status & Feedback
  const [error, setError] = useState('')
  const [successMsg, setSuccessMsg] = useState('')
  const [busy, setBusy] = useState(false)
  const [loginType, setLoginType] = useState('user')
  const [needsVerificationEmail, setNeedsVerificationEmail] = useState(null)

  // Resend Cooldown Timer
  const [cooldown, setCooldown] = useState(0)

  const navigate = useNavigate()

  useEffect(() => {
    if (cooldown > 0) {
      const timer = setTimeout(() => setCooldown(c => c - 1), 1000)
      return () => clearTimeout(timer)
    }
  }, [cooldown])

  // Switch views cleanly
  const switchView = (newView, emailPrefill = '') => {
    setError('')
    setSuccessMsg('')
    setNeedsVerificationEmail(null)
    const effectiveEmail = emailPrefill || form.email || verifyEmail || resetEmail
    if (effectiveEmail) {
      setForm(prev => ({ ...prev, email: effectiveEmail }))
      setVerifyEmail(effectiveEmail)
      setResetEmail(effectiveEmail)
    }
    setView(newView)
    navigate(`/${newView}${effectiveEmail ? `?email=${encodeURIComponent(effectiveEmail)}` : ''}`)
  }

  // 1. Submit Login
  const handleLogin = async e => {
    e.preventDefault()
    setError('')
    setSuccessMsg('')
    setNeedsVerificationEmail(null)
    setBusy(true)

    try {
      const endpoint = loginType === 'admin' ? '/auth/admin-login' : '/auth/login'
      const { data } = await api.post(endpoint, {
        email: form.email.trim(),
        password: form.password
      })
      onAuth(data)
      navigate(data.user.role === 'ADMIN' ? '/admin' : '/')
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Unable to continue. Check your details and try again.'
      setError(msg)
      if (
        msg.toLowerCase().includes('not verified') ||
        msg.toLowerCase().includes('unverified')
      ) {
        setNeedsVerificationEmail(form.email.trim())
      }
    } finally {
      setBusy(false)
    }
  }

  // 2. Submit Registration
  const handleRegister = async e => {
    e.preventDefault()
    setError('')
    setSuccessMsg('')
    setBusy(true)

    if (form.password.length < 6) {
      setError('Password must be at least 6 characters long.')
      setBusy(false)
      return
    }

    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match.')
      setBusy(false)
      return
    }

    try {
      const targetEmail = form.email.trim().toLowerCase()
      const { data } = await api.post('/auth/register', {
        username: form.username.trim(),
        email: targetEmail,
        password: form.password,
        confirmPassword: form.confirmPassword
      })

      setVerifyEmail(targetEmail)
      setCooldown(60)
      if (data.verificationCode) {
        setVerificationCode(data.verificationCode)
      }
      setSuccessMsg(data.message || `Verification code sent to ${targetEmail}`)
      switchView('verify-email', targetEmail)
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Registration failed. Please check your details.'
      )
    } finally {
      setBusy(false)
    }
  }

  // 3. Submit Email Verification
  const handleVerifyEmail = async e => {
    e.preventDefault()
    setError('')
    setSuccessMsg('')
    setBusy(true)

    const targetEmail = (verifyEmail || form.email).trim().toLowerCase()

    if (!targetEmail) {
      setError('Email address is missing.')
      setBusy(false)
      return
    }

    if (verificationCode.trim().length !== 6) {
      setError('Please enter the 6-digit verification code.')
      setBusy(false)
      return
    }

    try {
      const { data } = await api.post('/auth/verify-email', {
        email: targetEmail,
        code: verificationCode.trim()
      })
      setSuccessMsg(data.message || 'Account verified successfully! Welcome to KLMedia.')
      if (data.token) {
        onAuth(data)
        setTimeout(() => {
          navigate(data.user?.role === 'ADMIN' ? '/admin' : '/')
        }, 800)
      } else {
        setTimeout(() => {
          switchView('login')
        }, 1200)
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Invalid or expired verification code.'
      )
    } finally {
      setBusy(false)
    }
  }

  // Resend Verification Code
  const handleResendVerification = async () => {
    const targetEmail = (verifyEmail || form.email).trim().toLowerCase()
    if (!targetEmail) {
      setError('Please provide an email address.')
      return
    }

    setError('')
    setSuccessMsg('')
    setBusy(true)

    try {
      const { data } = await api.post('/auth/resend-verification', {
        email: targetEmail
      })
      setCooldown(60)
      if (data.verificationCode) {
        setVerificationCode(data.verificationCode)
      }
      setSuccessMsg(data.message || 'A new verification code has been sent to your email.')
    } catch (err) {
      setError(
        err.response?.data?.message ||
        'Could not resend verification code. Please wait before trying again.'
      )
    } finally {
      setBusy(false)
    }
  }

  // 4. Submit Forgot Password Request
  const handleForgotPassword = async e => {
    e.preventDefault()
    setError('')
    setSuccessMsg('')
    setBusy(true)

    const targetEmail = (resetEmail || form.email).trim().toLowerCase()

    try {
      const { data } = await api.post('/auth/forgot-password', {
        email: targetEmail
      })
      setResetEmail(targetEmail)
      setCooldown(60)
      if (data.resetCode) {
        setResetCode(data.resetCode)
      }
      setSuccessMsg(data.message || 'If an account exists, a 6-digit reset code has been sent.')
      switchView('reset-password', targetEmail)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not send reset code. Please try again.')
    } finally {
      setBusy(false)
    }
  }

  // 5. Submit Password Reset (Step 2)
  const handleResetPassword = async e => {
    e.preventDefault()
    setError('')
    setSuccessMsg('')
    setBusy(true)

    const targetEmail = (resetEmail || form.email).trim().toLowerCase()

    if (resetCode.trim().length !== 6) {
      setError('Please enter the 6-digit reset code.')
      setBusy(false)
      return
    }

    if (newPassword.length < 6) {
      setError('New password must be at least 6 characters long.')
      setBusy(false)
      return
    }

    if (newPassword !== confirmNewPassword) {
      setError('New passwords do not match.')
      setBusy(false)
      return
    }

    try {
      // First verify code
      await api.post('/auth/verify-reset-code', {
        email: targetEmail,
        code: resetCode.trim()
      })

      // Then reset password
      const { data } = await api.post('/auth/reset-password', {
        email: targetEmail,
        code: resetCode.trim(),
        newPassword,
        confirmPassword: confirmNewPassword
      })

      setSuccessMsg(data.message || 'Password reset successfully! Redirecting to login...')
      setTimeout(() => {
        switchView('login')
      }, 1800)
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Failed to reset password. Please check your code.'
      )
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="auth-wrap">
      <div className="auth-card">
        <div className="brand large klmedia-brand" style={{ justifyContent: 'center', marginBottom: 12 }}>
          <img src="/logo.png" alt="KLMedia Logo" className="klmedia-logo" />
          <span>KLMedia</span>
        </div>

        {/* VIEW: LOGIN */}
        {view === 'login' && (
          <>
            <div style={{ display: 'flex', gap: 10, margin: '20px 0' }}>
              <button
                type="button"
                className={loginType === 'user' ? 'primary' : ''}
                style={{ flex: 1 }}
                onClick={() => {
                  setLoginType('user')
                  setError('')
                  setNeedsVerificationEmail(null)
                }}
              >
                <UserRound size={17} /> User Login
              </button>

              <button
                type="button"
                className={loginType === 'admin' ? 'primary' : ''}
                style={{ flex: 1 }}
                onClick={() => {
                  setLoginType('admin')
                  setError('')
                  setNeedsVerificationEmail(null)
                }}
              >
                <ShieldCheck size={17} /> Admin Login
              </button>
            </div>

            <p className="muted center">
              {loginType === 'admin'
                ? 'Sign in to manage your community.'
                : 'Sign in to see what your community is sharing.'}
            </p>

            <form onSubmit={handleLogin}>
              <input
                type="email"
                placeholder="Email address"
                value={form.email}
                onChange={e => setForm({ ...form, email: e.target.value })}
                required
              />

              <div className="auth-input-wrapper">
                <input
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Password"
                  value={form.password}
                  onChange={e => setForm({ ...form, password: e.target.value })}
                  required
                />
                <button
                  type="button"
                  className="auth-password-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label="Toggle password visibility"
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>

              {loginType === 'user' && (
                <div style={{ textAlign: 'right', marginTop: -2, marginBottom: 4 }}>
                  <button
                    type="button"
                    className="auth-forgot-link"
                    style={{ background: 'none', border: 'none', cursor: 'pointer' }}
                    onClick={() => switchView('forgot-password', form.email)}
                  >
                    Forgot Password?
                  </button>
                </div>
              )}

              {error && <div className="error">{error}</div>}

              {needsVerificationEmail && (
                <div className="auth-info-banner" style={{ marginTop: 4 }}>
                  <p style={{ margin: '0 0 8px 0', fontSize: 13 }}>
                    Your email address is not yet verified.
                  </p>
                  <button
                    type="button"
                    className="primary"
                    style={{ fontSize: 12, padding: '6px 14px' }}
                    onClick={() => switchView('verify-email', needsVerificationEmail)}
                  >
                    Verify Email Now
                  </button>
                </div>
              )}

              {successMsg && (
                <div className="auth-success">
                  <CheckCircle2 size={18} /> {successMsg}
                </div>
              )}

              <button className="primary full" disabled={busy}>
                {busy
                  ? 'Please wait...'
                  : loginType === 'admin'
                    ? 'Admin Login'
                    : 'User Login'}
              </button>
            </form>

            <div className="auth-switch">
              Don't have an account?{' '}
              <button
                type="button"
                className="auth-resend-btn"
                onClick={() => switchView('register')}
              >
                Sign up
              </button>
            </div>
          </>
        )}

        {/* VIEW: REGISTER */}
        {view === 'register' && (
          <>
            <div style={{ textAlign: 'center', marginTop: 12 }}>
              <span className="auth-card-badge">Join KLMedia</span>
            </div>

            <p className="muted center">
              Create an account to share your moments.
            </p>

            <form onSubmit={handleRegister}>
              <input
                placeholder="Username"
                value={form.username}
                onChange={e => setForm({ ...form, username: e.target.value })}
                required
              />

              <input
                type="email"
                placeholder="Email address"
                value={form.email}
                onChange={e => setForm({ ...form, email: e.target.value })}
                required
              />

              <div className="auth-input-wrapper">
                <input
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Password (minimum 6 characters)"
                  minLength="6"
                  value={form.password}
                  onChange={e => setForm({ ...form, password: e.target.value })}
                  required
                />
                <button
                  type="button"
                  className="auth-password-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label="Toggle password visibility"
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>

              <div className="auth-input-wrapper">
                <input
                  type={showConfirmPassword ? 'text' : 'password'}
                  placeholder="Confirm Password"
                  minLength="6"
                  value={form.confirmPassword}
                  onChange={e => setForm({ ...form, confirmPassword: e.target.value })}
                  required
                />
                <button
                  type="button"
                  className="auth-password-toggle"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  aria-label="Toggle password visibility"
                >
                  {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>

              {error && <div className="error">{error}</div>}

              {successMsg && (
                <div className="auth-success">
                  <CheckCircle2 size={18} /> {successMsg}
                </div>
              )}

              <button className="primary full" disabled={busy}>
                {busy ? 'Creating account...' : 'Create account'}
              </button>
            </form>

            <div className="auth-switch">
              Already have an account?{' '}
              <button
                type="button"
                className="auth-resend-btn"
                onClick={() => switchView('login')}
              >
                Log in
              </button>
            </div>
          </>
        )}

        {/* VIEW: VERIFY EMAIL */}
        {view === 'verify-email' && (
          <>
            <div style={{ textAlign: 'center', marginTop: 12 }}>
              <span className="auth-card-badge">Email Verification</span>
            </div>

            <p className="muted center">
              We've sent a 6-digit verification code to<br />
              <strong style={{ color: 'var(--kl-text)' }}>{verifyEmail || form.email}</strong>.<br />
              Code expires in 5 minutes.
            </p>

            <form onSubmit={handleVerifyEmail}>
              <input
                type="text"
                className="auth-code-input"
                placeholder="000000"
                maxLength="6"
                value={verificationCode}
                onChange={e => setVerificationCode(e.target.value.replace(/\D/g, ''))}
                autoFocus
                required
              />

              {error && <div className="error">{error}</div>}

              {successMsg && (
                <div className="auth-success">
                  <CheckCircle2 size={18} /> {successMsg}
                </div>
              )}

              <button className="primary full" disabled={busy || verificationCode.length !== 6}>
                {busy ? 'Verifying...' : 'Verify Email'}
              </button>

              <div className="auth-resend-bar">
                <span>Didn't get the code?</span>
                <button
                  type="button"
                  className="auth-resend-btn"
                  disabled={busy || cooldown > 0}
                  onClick={handleResendVerification}
                >
                  {cooldown > 0 ? `Resend code in ${cooldown}s` : 'Resend Code'}
                </button>
              </div>
            </form>

            <div style={{ textAlign: 'center' }}>
              <button
                type="button"
                className="auth-back-link"
                style={{ background: 'none', border: 'none', cursor: 'pointer' }}
                onClick={() => switchView('login')}
              >
                <ArrowLeft size={16} /> Back to Login
              </button>
            </div>
          </>
        )}

        {/* VIEW: FORGOT PASSWORD */}
        {view === 'forgot-password' && (
          <>
            <div style={{ textAlign: 'center', marginTop: 12 }}>
              <span className="auth-card-badge">Password Recovery</span>
            </div>

            <p className="muted center">
              Enter your registered email address and we'll send you a 6-digit password reset code.
            </p>

            <form onSubmit={handleForgotPassword}>
              <input
                type="email"
                placeholder="Email address"
                value={resetEmail || form.email}
                onChange={e => {
                  setResetEmail(e.target.value)
                  setForm({ ...form, email: e.target.value })
                }}
                required
              />

              {error && <div className="error">{error}</div>}

              {successMsg && (
                <div className="auth-success">
                  <CheckCircle2 size={18} /> {successMsg}
                </div>
              )}

              <button className="primary full" disabled={busy}>
                {busy ? 'Sending reset code...' : 'Send Reset Code'}
              </button>
            </form>

            <div style={{ textAlign: 'center' }}>
              <button
                type="button"
                className="auth-back-link"
                style={{ background: 'none', border: 'none', cursor: 'pointer' }}
                onClick={() => switchView('login')}
              >
                <ArrowLeft size={16} /> Back to Login
              </button>
            </div>
          </>
        )}

        {/* VIEW: RESET PASSWORD (STEP 2) */}
        {view === 'reset-password' && (
          <>
            <div style={{ textAlign: 'center', marginTop: 12 }}>
              <span className="auth-card-badge">Set New Password</span>
            </div>

            <p className="muted center">
              Enter the 6-digit code sent to<br />
              <strong style={{ color: 'var(--kl-text)' }}>{resetEmail || form.email}</strong><br />
              and enter your new password.
            </p>

            <form onSubmit={handleResetPassword}>
              <input
                type="text"
                className="auth-code-input"
                placeholder="000000"
                maxLength="6"
                value={resetCode}
                onChange={e => setResetCode(e.target.value.replace(/\D/g, ''))}
                autoFocus
                required
              />

              <div className="auth-input-wrapper">
                <input
                  type={showNewPassword ? 'text' : 'password'}
                  placeholder="New Password (minimum 6 characters)"
                  minLength="6"
                  value={newPassword}
                  onChange={e => setNewPassword(e.target.value)}
                  required
                />
                <button
                  type="button"
                  className="auth-password-toggle"
                  onClick={() => setShowNewPassword(!showNewPassword)}
                  aria-label="Toggle password visibility"
                >
                  {showNewPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>

              <div className="auth-input-wrapper">
                <input
                  type={showConfirmNewPassword ? 'text' : 'password'}
                  placeholder="Confirm New Password"
                  minLength="6"
                  value={confirmNewPassword}
                  onChange={e => setConfirmNewPassword(e.target.value)}
                  required
                />
                <button
                  type="button"
                  className="auth-password-toggle"
                  onClick={() => setShowConfirmNewPassword(!showConfirmNewPassword)}
                  aria-label="Toggle password visibility"
                >
                  {showConfirmNewPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>

              {error && <div className="error">{error}</div>}

              {successMsg && (
                <div className="auth-success">
                  <CheckCircle2 size={18} /> {successMsg}
                </div>
              )}

              <button className="primary full" disabled={busy || resetCode.length !== 6}>
                {busy ? 'Updating password...' : 'Reset Password'}
              </button>

              <div className="auth-resend-bar">
                <span>Didn't get the code?</span>
                <button
                  type="button"
                  className="auth-resend-btn"
                  disabled={busy || cooldown > 0}
                  onClick={handleForgotPassword}
                >
                  {cooldown > 0 ? `Resend in ${cooldown}s` : 'Resend Code'}
                </button>
              </div>
            </form>

            <div style={{ textAlign: 'center' }}>
              <button
                type="button"
                className="auth-back-link"
                style={{ background: 'none', border: 'none', cursor: 'pointer' }}
                onClick={() => switchView('login')}
              >
                <ArrowLeft size={16} /> Back to Login
              </button>
            </div>
          </>
        )}
      </div>

      <div className="auth-foot">
        A little space for your everyday moments ✨
      </div>
    </div>
  )
}

function AdminDashboard({ user, logout }) {
  const [users, setUsers] = useState([])
  const [form, setForm] = useState({
    username: '',
    email: '',
    password: ''
  })
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  const loadUsers = async () => {
    try {
      const { data } = await api.get('/admin/users')
      setUsers(data)
      setError('')
    } catch (err) {
      setError(err.response?.data?.message || 'Could not load users.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadUsers()
  }, [])

  const addUser = async e => {
    e.preventDefault()
    setError('')
    setMessage('')
    setBusy(true)

    try {
      await api.post('/admin/users', form)
      setForm({ username: '', email: '', password: '' })
      setMessage('User added successfully.')
      await loadUsers()
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Could not add user.'
      )
    } finally {
      setBusy(false)
    }
  }

  const deleteUser = async id => {
    if (!confirm('Are you sure you want to delete this user?')) return

    setError('')
    setMessage('')

    try {
      await api.delete(`/admin/users/${id}`)
      setMessage('User deleted successfully.')
      await loadUsers()
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Could not delete user.'
      )
    }
  }

  return (
    <main className="main-layout">
      <section className="feed-column" style={{ width: '100%' }}>
        <div className="topbar">
          <span className="mobile-brand">
            <img src="/logo.png" alt="KLMedia Logo" className="klmedia-logo-mini" />
            <span>KLMedia</span>
          </span>
          <span className="page-title">Admin Dashboard</span>
          <button className="logout" onClick={logout}>
            <LogOut size={18} /> Log out
          </button>
        </div>

        <section className="create-card">
          <div className="create-title">
            <ShieldCheck size={20} /> Admin Panel
          </div>
          <p className="muted">
            Welcome, {user.username}. Manage registered community users here.
          </p>

          <div className="stats-grid">
            <div>
              <b>{users.length}</b>
              <small>Total users</small>
            </div>
            <div>
              <b>{users.filter(u => u.role === 'USER').length}</b>
              <small>Normal users</small>
            </div>
            <div>
              <b>{users.filter(u => u.role === 'ADMIN').length}</b>
              <small>Admins</small>
            </div>
          </div>
        </section>

        <section className="create-card">
          <div className="create-title">
            <PlusSquare size={19} /> Add New User
          </div>

          <form onSubmit={addUser}>
            <input
              placeholder="Username"
              value={form.username}
              onChange={e => setForm({ ...form, username: e.target.value })}
              required
            />

            <input
              type="email"
              placeholder="Email address"
              value={form.email}
              onChange={e => setForm({ ...form, email: e.target.value })}
              required
            />

            <input
              type="password"
              placeholder="Password (minimum 6 characters)"
              minLength="6"
              value={form.password}
              onChange={e => setForm({ ...form, password: e.target.value })}
              required
            />

            <button className="primary" disabled={busy}>
              {busy ? 'Adding...' : 'Add User'}
            </button>
          </form>
        </section>

        {message && <p style={{ color: 'green' }}>{message}</p>}
        {error && <p className="error">{error}</p>}

        <section className="create-card">
          <div className="create-title">
            <Users size={20} /> Registered Users
          </div>

          {loading ? (
            <p className="muted center">Loading users...</p>
          ) : users.length === 0 ? (
            <p className="muted center">No users found.</p>
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr>
                    <th style={{ textAlign: 'left', padding: 10 }}>ID</th>
                    <th style={{ textAlign: 'left', padding: 10 }}>Username</th>
                    <th style={{ textAlign: 'left', padding: 10 }}>Email</th>
                    <th style={{ textAlign: 'left', padding: 10 }}>Role</th>
                    <th style={{ textAlign: 'left', padding: 10 }}>Action</th>
                  </tr>
                </thead>

                <tbody>
                  {users.map(u => (
                    <tr key={u.id}>
                      <td style={{ padding: 10 }}>{u.id}</td>
                      <td style={{ padding: 10 }}>{u.username}</td>
                      <td style={{ padding: 10 }}>{u.email}</td>
                      <td style={{ padding: 10 }}>{u.role}</td>
                      <td style={{ padding: 10 }}>
                        {u.role === 'ADMIN' ? (
                          <span className="muted">Protected</span>
                        ) : (
                          <button
                            className="icon-btn"
                            title="Delete user"
                            onClick={() => deleteUser(u.id)}
                          >
                            <Trash2 size={18} />
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </section>
    </main>
  )
}

function Sidebar({ user, logout, unreadCount }) {
  const items = [
    [Home, 'Home', '/'],
    [Search, 'Search', '/search'],
    [MessageCircle, 'Messenger', '/messenger'],
    [Heart, 'Notifications', '/notifications'],
    [PlusSquare, 'Create', '#create'],
    [UserRound, 'Profile', '/profile']
  ]

  return <aside className="sidebar">
    <Link to="/" className="brand klmedia-brand">
      <img src="/logo.png" alt="KLMedia Logo" className="klmedia-logo" />
      <span>KLMedia</span>
    </Link>

    <nav>
      {items.map(([Icon, label, path]) =>
        <Link
          key={label}
          to={path}
          onClick={e => {
            if (path.startsWith('#')) {
              e.preventDefault()
              if (path === '#create') {
                document.getElementById('create-post')?.scrollIntoView({
                  behavior: 'smooth'
                })
              } else {
                alert(`${label} module is coming in the next development stage.`)
              }
            }
          }}
        >
          <Icon size={22} />
          <span>{label}</span>
          {label === 'Notifications' && unreadCount > 0 &&
            <span className="notification-badge">{unreadCount}</span>
          }
        </Link>
      )}
    </nav>

    <div className="sidebar-bottom">
      <div className="mini-user">
        <div className="avatar">{user?.username?.[0]?.toUpperCase()}</div>
        <div>
          <b>{user?.username}</b>
          <small>Personal account</small>
        </div>
      </div>

      <button className="logout" onClick={logout}>
        <LogOut size={18} /> Log out
      </button>
    </div>
  </aside>
}

function PostCard({ post, onLike, onDelete }) {
  const [comments, setComments] = useState([])
  const [showComments, setShowComments] = useState(false)
  const [commentText, setCommentText] = useState('')
  const [commentError, setCommentError] = useState('')

  const loadComments = async () => {
    try {
      const { data } = await api.get(`/posts/${post.id}/comments`)
      setComments(data)
    } catch {
      setCommentError('Could not load comments.')
    }
  }

  const toggleComments = async () => {
    const next = !showComments
    setShowComments(next)
    setCommentError('')
    if (next) await loadComments()
  }

  const addComment = async e => {
    e.preventDefault()
    if (!commentText.trim()) return

    try {
      await api.post(`/posts/${post.id}/comments`, { content: commentText })
      setCommentText('')
      setCommentError('')
      await loadComments()
    } catch (err) {
      setCommentError(err.response?.data?.message || 'Could not add comment.')
    }
  }

  return (
    <article className="post-card">
      <div className="post-head">
        <div className="avatar">{post.user.username?.[0]?.toUpperCase()}</div>
        <div className="post-author">
          <b>{post.user.username}</b>
          <small>{new Date(post.createdAt).toLocaleString()}</small>
        </div>
        <button
          className="icon-btn push"
          onClick={() => post.ownedByMe && onDelete(post.id)}
        >
          {post.ownedByMe ? <Trash2 size={18} /> : <MoreHorizontal size={20} />}
        </button>
      </div>

      {post.imageUrl &&
        <img className="post-image" src={post.imageUrl} alt="Post" />
      }

      <div className="post-actions">
        <button
          className={`icon-btn ${post.likedByMe ? 'liked' : ''}`}
          onClick={() => onLike(post.id)}
        >
          <Heart size={23} fill={post.likedByMe ? 'currentColor' : 'none'} />
        </button>
        <button className="icon-btn" onClick={toggleComments}>
          <MessageCircle size={23} />
        </button>
        <button className="icon-btn">
          <Bookmark size={22} />
        </button>
      </div>

      <div className="post-content">
        <b>{post.likesCount} likes</b>
        <p><b>{post.user.username}</b> {post.caption}</p>
      </div>

      {showComments && (
        <div className="comments-section">
          <h4>Comments</h4>
          {comments.map(comment => (
            <p key={comment.id}>
              <b>{comment.user?.username || 'User'}</b>{' '}
              {comment.content}
            </p>
          ))}
          {comments.length === 0 &&
            <small>No comments yet. Be the first!</small>
          }
          {commentError && <p className="error">{commentError}</p>}
          <form onSubmit={addComment}>
            <input
              value={commentText}
              onChange={e => setCommentText(e.target.value)}
              placeholder="Write a comment..."
              maxLength={1000}
              required
            />
            <button className="primary" disabled={!commentText.trim()}>
              Post
            </button>
          </form>
        </div>
      )}
    </article>
  )
}

function Dashboard({ user }) {
  const [posts, setPosts] = useState([])
  const [caption, setCaption] = useState('')
  const [imageUrl, setImageUrl] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [profile, setProfile] = useState(null)

  const load = async () => {
    try {
      const [feed, me] = await Promise.all([
        api.get('/posts'),
        api.get('/users/me')
      ])
      setPosts(feed.data)
      setProfile(me.data)
    } catch {
      setError('Could not load your feed. Check that the backend and database are running.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const create = async e => {
    e.preventDefault()
    setError('')
    try {
      await api.post('/posts', { caption, imageUrl })
      setCaption('')
      setImageUrl('')
      await load()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not create post.')
    }
  }

  const like = async id => {
    try {
      await api.post(`/posts/${id}/like`)
      await load()
    } catch {
      setError('Could not update like.')
    }
  }

  const remove = async id => {
    if (!confirm('Delete this post?')) return
    try {
      await api.delete(`/posts/${id}`)
      await load()
    } catch {
      setError('Could not delete post.')
    }
  }

  return <main className="main-layout home-layout">
    <section className="feed-column">
      <div className="topbar">
        <span className="mobile-brand">
          <img src="/logo.png" alt="KLMedia Logo" className="klmedia-logo-mini" />
          <span>KLMedia</span>
        </span>
        <span className="page-title">Home feed</span>
        <span className="muted">Welcome back, {user.username}</span>
      </div>

      <section className="create-card" id="create-post">
        <div className="create-title">
          <ImagePlus size={19} /> Create a post
        </div>
        <form onSubmit={create}>
          <textarea
            placeholder="Share something with your community..."
            value={caption}
            onChange={e => setCaption(e.target.value)}
            required
            maxLength="2000"
          />
          <input
            placeholder="Image URL (optional)"
            value={imageUrl}
            onChange={e => setImageUrl(e.target.value)}
          />
          <div className="create-footer">
            <small className="muted">Keep it positive. Be you.</small>
            <button className="primary" disabled={!caption.trim()}>
              Share post
            </button>
          </div>
        </form>
      </section>

      {error && <div className="error">{error}</div>}
      {loading ? (
        <p className="muted center">Loading your feed...</p>
      ) : posts.length ? (
        posts.map(post =>
          <PostCard
            key={post.id}
            post={post}
            onLike={like}
            onDelete={remove}
          />
        )
      ) : (
        <div className="empty">
          <Camera size={34} />
          <h3>Your feed is waiting</h3>
          <p>Be the first to share a moment.</p>
        </div>
      )}
    </section>

  </main>
}

function Profile({ user }) {
  const [profile, setProfile] = useState(null)
  const [posts, setPosts] = useState([])

  useEffect(() => {
    Promise.all([api.get('/users/me'), api.get('/posts')])
      .then(([a, b]) => {
        setProfile(a.data)
        setPosts(b.data.filter(p => p.ownedByMe))
      })
      .catch(() => {})
  }, [])

  return <main className="profile-page">
    <div className="profile-cover"></div>
    <div className="profile-header">
      <div className="avatar profile-avatar">{user.username?.[0]?.toUpperCase()}</div>
      <div>
        <h2>{user.username}</h2>
        <div className="profile-counts">
          <span><b>{profile?.postsCount ?? 0}</b> posts</span>
          <span><b>0</b> followers</span>
          <span><b>0</b> following</span>
        </div>
        <p>{profile?.bio || 'Welcome to my KLMedia profile.'}</p>
      </div>
    </div>
    <div className="profile-tabs">▦ &nbsp; POSTS</div>
    <div className="profile-grid">
      {posts.map(p =>
        <div key={p.id} className="profile-tile">
          {p.imageUrl
            ? <img src={p.imageUrl} alt="" />
            : <div className="tile-caption">{p.caption}</div>}
        </div>
      )}
    </div>
  </main>
}

function PublicProfile() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [profile, setProfile] = useState(null)
  const [followStatus, setFollowStatus] = useState(null)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  const loadProfile = async () => {
    try {
      const [profileResponse, statusResponse] = await Promise.all([
        api.get(`/users/${id}`),
        api.get(`/follows/${id}/status`)
      ])
      setProfile(profileResponse.data)
      setFollowStatus(statusResponse.data)
      setError('')
    } catch {
      setError('Could not load this profile. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    setLoading(true)
    loadProfile()
  }, [id])

  const toggleFollow = async () => {
    if (busy || !followStatus) return

    setBusy(true)
    setError('')

    try {
      if (followStatus.following) {
        await api.delete(`/follows/${id}`)
      } else {
        await api.post(`/follows/${id}`)
      }

      const { data } = await api.get(`/follows/${id}/status`)
      setFollowStatus(data)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not update follow status.')
    } finally {
      setBusy(false)
    }
  }

  if (loading) {
    return <main className="profile-page">
      <p className="muted center">Loading profile...</p>
    </main>
  }

  if (error && !profile) {
    return <main className="profile-page">
      <button className="notification-action" onClick={() => navigate('/search')}>
        <ArrowLeft size={17} /> Back to search
      </button>
      <p className="error">{error}</p>
    </main>
  }

  if (!profile) return null

  return <main className="profile-page">
    <button className="notification-action back-action-btn" onClick={() => navigate('/search')}>
      <ArrowLeft size={17} /> Back to search
    </button>

    <div className="profile-cover"></div>
    <div className="profile-header">
      <div className="avatar profile-avatar">
        {profile.username?.[0]?.toUpperCase()}
      </div>

      <div>
        <h2>{profile.username}</h2>
        <div className="profile-counts">
          <span><b>{profile.postsCount ?? 0}</b> posts</span>
          <span><b>{followStatus?.followersCount ?? 0}</b> followers</span>
          <span><b>{followStatus?.followingCount ?? 0}</b> following</span>
        </div>

        <p>{profile.bio || 'Welcome to my KLMedia profile.'}</p>

        <button className="primary" onClick={toggleFollow} disabled={busy}>
          {busy
            ? 'Please wait...'
            : followStatus?.following
              ? 'Unfollow'
              : 'Follow'}
        </button>
      </div>
    </div>

    {error && <p className="error">{error}</p>}

    <div className="profile-tabs">▦ &nbsp; POSTS</div>

    <div className="profile-grid">
      {profile.posts?.length ? profile.posts.map(post =>
        <div key={post.id} className="profile-tile">
          {post.imageUrl
            ? <img src={post.imageUrl} alt="Post" />
            : <div className="tile-caption">{post.caption}</div>}
        </div>
      ) : (
        <div className="empty">
          <Camera size={34} />
          <h3>No posts yet</h3>
          <p>This user hasn't shared any posts.</p>
        </div>
      )}
    </div>
  </main>
}

function Notifications({ onUnreadChange }) {
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const loadNotifications = async () => {
    try {
      const { data } = await api.get('/notifications')
      setNotifications(data)

      const unread = data.filter(item => !item.read).length
      onUnreadChange(unread)
      setError('')
    } catch (err) {
      setError(err.response?.data?.message || 'Could not load notifications.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadNotifications()
  }, [])

  const markRead = async id => {
    try {
      await api.patch(`/notifications/${id}/read`)
      await loadNotifications()
    } catch {
      setError('Could not mark notification as read.')
    }
  }

  const markAllRead = async () => {
    try {
      await api.patch('/notifications/read-all')
      await loadNotifications()
    } catch {
      setError('Could not mark all notifications as read.')
    }
  }

  const deleteOne = async id => {
    try {
      await api.delete(`/notifications/${id}`)
      await loadNotifications()
    } catch {
      setError('Could not delete notification.')
    }
  }

  const clearAll = async () => {
    if (!notifications.length) return
    if (!confirm('Delete all notifications?')) return

    try {
      await api.delete('/notifications')
      await loadNotifications()
    } catch {
      setError('Could not clear notifications.')
    }
  }

  const notificationText = type => {
    switch (type) {
      case 'LIKE':
        return 'liked your post.'
      case 'COMMENT':
        return 'commented on your post.'
      case 'FOLLOW':
        return 'started following you.'
      default:
        return 'interacted with you.'
    }
  }

  const notificationIcon = type => {
    switch (type) {
      case 'LIKE':
        return <Heart size={21} />
      case 'FOLLOW':
        return <UserRound size={21} />
      default:
        return <MessageCircle size={21} />
    }
  }

  return (
    <main className="notification-page">
      <div className="notification-header">
        <div>
          <h2><Bell size={25} /> Notifications</h2>
          <p className="muted">
            Stay updated with activity on your posts and profile.
          </p>
        </div>

        <div className="notification-controls">
          <button className="notification-action" onClick={markAllRead}>
            <CheckCheck size={17} /> Mark all read
          </button>

          <button className="notification-action" onClick={clearAll}>
            <Trash2 size={17} /> Clear all
          </button>
        </div>
      </div>

      {error && <div className="error">{error}</div>}

      {loading ? (
        <p className="muted center">Loading notifications...</p>
      ) : notifications.length === 0 ? (
        <div className="empty notification-empty">
          <Bell size={36} />
          <h3>You're all caught up!</h3>
          <p>
            When someone follows you, likes or comments on your posts,
            you'll see it here.
          </p>
        </div>
      ) : (
        <div className="notification-list">
          {notifications.map(item => (
            <div
              key={item.id}
              className={`notification-item ${item.read ? 'read' : 'unread'}`}
              onClick={() => !item.read && markRead(item.id)}
            >
              <div
                className={`notification-icon ${
                  item.type === 'LIKE'
                    ? 'like-notification'
                    : item.type === 'FOLLOW'
                      ? 'follow-notification'
                      : 'comment-notification'
                }`}
              >
                {notificationIcon(item.type)}
              </div>

              <div className="notification-details">
                <p>
                  <b>{item.actorUsername}</b>{' '}
                  {notificationText(item.type)}
                </p>

                <small>{new Date(item.createdAt).toLocaleString()}</small>

                {!item.read && (
                  <span className="unread-label">New</span>
                )}
              </div>

              <button
                className="notification-delete"
                title="Delete notification"
                onClick={e => {
                  e.stopPropagation()
                  deleteOne(item.id)
                }}
              >
                <X size={18} />
              </button>
            </div>
          ))}
        </div>
      )}
    </main>
  )
}

export default function App() {
  const [user, setUser] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('user'))
    } catch {
      return null
    }
  })

  const [unreadCount, setUnreadCount] = useState(0)

  useEffect(() => {
    if (!user || user.role === 'ADMIN') {
      setUnreadCount(0)
      return
    }

    api.get('/notifications/unread-count')
      .then(({ data }) => setUnreadCount(data.count || 0))
      .catch(() => {})
  }, [user])

  const saveAuth = data => {
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data.user))
    setUser(data.user)
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setUser(null)
  }

  return (
    <Routes>
      <Route
        path="/login"
        element={
          user
            ? <Navigate to={user.role === 'ADMIN' ? '/admin' : '/'} />
            : <Auth mode="login" onAuth={saveAuth} />
        }
      />

      <Route
        path="/register"
        element={
          user
            ? <Navigate to={user.role === 'ADMIN' ? '/admin' : '/'} />
            : <Auth mode="register" onAuth={saveAuth} />
        }
      />

      <Route
        path="/verify-email"
        element={
          user
            ? <Navigate to={user.role === 'ADMIN' ? '/admin' : '/'} />
            : <Auth mode="verify-email" onAuth={saveAuth} />
        }
      />

      <Route
        path="/forgot-password"
        element={
          user
            ? <Navigate to={user.role === 'ADMIN' ? '/admin' : '/'} />
            : <Auth mode="forgot-password" onAuth={saveAuth} />
        }
      />

      <Route
        path="/reset-password"
        element={
          user
            ? <Navigate to={user.role === 'ADMIN' ? '/admin' : '/'} />
            : <Auth mode="reset-password" onAuth={saveAuth} />
        }
      />

      <Route
        path="/admin"
        element={
          user?.role === 'ADMIN'
            ? <AdminDashboard user={user} logout={logout} />
            : user
              ? <Navigate to="/" />
              : <Navigate to="/login" />
        }
      />

      <Route
        path="/*"
        element={
          user?.role === 'ADMIN'
            ? <Navigate to="/admin" />
            : user
              ? (
                <div className="app-shell">
                  <Sidebar
                    user={user}
                    logout={logout}
                    unreadCount={unreadCount}
                  />

                  <Routes>
                    <Route path="/" element={<Dashboard user={user} />} />
                    <Route path="/profile" element={<Profile user={user} />} />
                    <Route path="/profile/:id" element={<PublicProfile />} />
                    <Route path="/search" element={<SearchPage />} />
                    <Route path="/messenger" element={<Messenger />} />
                    <Route
                      path="/notifications"
                      element={
                        <Notifications onUnreadChange={setUnreadCount} />
                      }
                    />
                    <Route path="*" element={<Navigate to="/" />} />
                  </Routes>
                </div>
              )
              : <Navigate to="/login" />
        }
      />
    </Routes>
  )
}
