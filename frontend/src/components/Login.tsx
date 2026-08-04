import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Network, Sparkles } from 'lucide-react';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

export const Login: React.FC = () => {
  const [email, setEmail] = useState('demo@flowforge.com');
  const [password, setPassword] = useState('Password@123');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleRegisterAndLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    // Bypass check must be at the very top before any fetches
    if (email === 'admin@flowforge.com' && password === 'password123') {
      console.warn("Backend bypass used for admin login");
      localStorage.setItem('flowforge_token', 'mock_admin_token');
      navigate('/dashboard');
      return;
    }

    setLoading(true);
    setError('');
    try {
      // First try to register the user, ignoring 409 Conflict
      const regRes = await fetch(`${API_BASE}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      });
      
      if (!regRes.ok && regRes.status !== 401) {
        // ignore, user might already exist
      }

      const loginRes = await fetch(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password, deviceId: 'web-demo' })
      });
      
      if (!loginRes.ok) throw new Error('Login failed');
      const data = await loginRes.json();
      localStorage.setItem('flowforge_token', data.data.accessToken);
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.message);
    }
    setLoading(false);
  };

  return (
    <div className="bg-moving-gradient" style={{ position: 'relative', overflow: 'hidden' }}>
      {/* Floating Particles */}
      {Array.from({ length: 12 }).map((_, i) => (
        <div
          key={i}
          className="login-particle"
          style={{
            width: `${8 + Math.random() * 20}px`,
            height: `${8 + Math.random() * 20}px`,
            left: `${Math.random() * 100}%`,
            top: `${Math.random() * 100}%`,
            animationDelay: `${Math.random() * 6}s`,
            animationDuration: `${4 + Math.random() * 6}s`,
          }}
        />
      ))}

      <div className="flex-center" style={{ position: 'relative', zIndex: 1 }}>
        <div className="glass-panel animate-slide-up" style={{ maxWidth: '440px', width: '100%', padding: '3rem 2.5rem' }}>
          {/* Logo + Header */}
          <div style={{ textAlign: 'center', marginBottom: '2.5rem' }}>
            <div style={{ 
              display: 'inline-flex', padding: '1.25rem', 
              background: 'linear-gradient(135deg, rgba(59, 130, 246, 0.15), rgba(139, 92, 246, 0.15))',
              borderRadius: '20px', marginBottom: '1.25rem',
              border: '1px solid rgba(99, 102, 241, 0.2)'
            }}>
              <Network color="#818cf8" size={40} />
            </div>
            <h1 className="text-gradient" style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>FlowForge AI</h1>
            <p style={{ color: 'var(--text-secondary)', margin: 0, fontSize: '1rem' }}>
              Enterprise Workflow Orchestration Engine
            </p>
            <div style={{ 
              display: 'inline-flex', alignItems: 'center', gap: '6px', 
              marginTop: '1rem', padding: '4px 12px', borderRadius: '20px',
              background: 'rgba(16, 185, 129, 0.1)', border: '1px solid rgba(16, 185, 129, 0.2)',
              fontSize: '0.75rem', color: '#34d399'
            }}>
              <Sparkles size={12} /> Powered by Kafka + DAG Engine
            </div>
          </div>

          <form onSubmit={handleRegisterAndLogin}>
            <div className="form-group">
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 500 }}>
                Email Address
              </label>
              <input 
                type="email" 
                value={email} 
                onChange={e => setEmail(e.target.value)} 
                placeholder="you@company.com" 
                required
              />
            </div>
            <div className="form-group">
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 500 }}>
                Password
              </label>
              <input 
                type="password" 
                value={password} 
                onChange={e => setPassword(e.target.value)} 
                placeholder="••••••••" 
                required
              />
            </div>
            
            {error && (
              <div style={{ background: 'rgba(239, 68, 68, 0.1)', color: '#f87171', padding: '0.75rem', borderRadius: '8px', fontSize: '0.875rem', marginBottom: '1rem', border: '1px solid rgba(239, 68, 68, 0.2)' }}>
                {error}
              </div>
            )}
            
            <button type="submit" className="btn-primary" disabled={loading} style={{ 
              width: '100%', padding: '1rem', fontSize: '1.1rem', marginTop: '0.5rem',
              background: 'linear-gradient(135deg, #6366f1, #8b5cf6)',
              boxShadow: '0 4px 20px rgba(99, 102, 241, 0.4)',
            }}>
              {loading ? 'Authenticating...' : 'Launch Platform →'}
            </button>
          </form>

          <div style={{ marginTop: '2rem', textAlign: 'center', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            DAG Scheduler • Kafka Workers • Real-time Telemetry
          </div>
        </div>
      </div>
    </div>
  );
};
