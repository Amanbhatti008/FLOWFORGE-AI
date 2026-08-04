import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Activity, Play, AlertTriangle, Plus, LayoutDashboard, Eye, Clock, XCircle, CheckCircle2, Zap, Server, Database, GitBranch } from 'lucide-react';
import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

export const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const [stats, setStats] = useState({ workflows: 0, running: 0, failed: 0, success: 0 });
  const [executions, setExecutions] = useState<any[]>([]);

  const fetchData = async () => {
    try {
      const token = localStorage.getItem('flowforge_token'); 
      
      const wfRes = await axios.get(`${API_BASE}/workflows`, { headers: { Authorization: `Bearer ${token}` }});
      const execRes = await axios.get(`${API_BASE}/executions`, { headers: { Authorization: `Bearer ${token}` }});
      
      const wfs = wfRes.data.data || [];
      const execs = execRes.data.data || [];
      
      setExecutions(execs);
      
      const running = execs.filter((e: any) => e.status === 'RUNNING').length;
      const failed = execs.filter((e: any) => e.status === 'FAILED').length;
      const success = execs.filter((e: any) => e.status === 'COMPLETED' || e.status === 'SUCCESS').length;
      
      setStats({ workflows: wfs.length, running, failed, success });
    } catch (err) {
      console.error("Error fetching dashboard data", err);
    }
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 3000);
    return () => clearInterval(interval);
  }, []);

  const getStatusBadge = (status: string) => {
    switch(status) {
      case 'SUCCESS':
      case 'COMPLETED':
        return <span className="badge badge-success"><CheckCircle2 size={12}/> {status}</span>;
      case 'FAILED':
        return <span className="badge badge-error"><XCircle size={12}/> {status}</span>;
      case 'RUNNING':
        return <span className="badge badge-running"><Activity size={12}/> {status}</span>;
      default:
        return <span className="badge badge-queued"><Clock size={12}/> {status}</span>;
    }
  };

  const timeAgo = (dateStr: string) => {
    if (!dateStr) return '—';
    const diff = Date.now() - new Date(dateStr).getTime();
    const mins = Math.floor(diff / 60000);
    if (mins < 1) return 'Just now';
    if (mins < 60) return `${mins}m ago`;
    const hours = Math.floor(mins / 60);
    if (hours < 24) return `${hours}h ago`;
    return `${Math.floor(hours / 24)}d ago`;
  };

  return (
    <div style={{ height: '100%', padding: '2rem' }}>
      <div style={{ maxWidth: '1280px', margin: '0 auto' }}>
        {/* Header */}
        <header className="animate-slide-up" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2.5rem' }}>
          <div>
            <h2 className="text-gradient" style={{ fontSize: '2.25rem', display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.25rem' }}>
              <LayoutDashboard size={32} color="#818cf8" /> 
              FlowForge Hub
            </h2>
            <p style={{ color: 'var(--text-muted)', margin: 0, fontSize: '0.9rem' }}>
              <span className="live-dot"></span> System Online • Real-time Monitoring
            </p>
          </div>
          <button className="btn-primary" onClick={() => navigate('/builder')} style={{
            background: 'linear-gradient(135deg, #6366f1, #8b5cf6)',
            boxShadow: '0 4px 20px rgba(99, 102, 241, 0.4)',
            padding: '0.85rem 1.75rem',
            fontSize: '1.05rem'
          }}>
            <Plus size={18} /> New Workflow
          </button>
        </header>

        {/* Stats Grid */}
        <div className="animate-slide-up" style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '1.5rem', marginBottom: '3rem' }}>
          <div className="glass-panel hoverable stat-card" style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', padding: '1.75rem' }}>
            <div style={{ background: 'rgba(99, 102, 241, 0.12)', padding: '1rem', borderRadius: '14px', border: '1px solid rgba(99, 102, 241, 0.2)' }}>
              <Zap color="#818cf8" size={28} />
            </div>
            <div>
              <h3 style={{ margin: 0, fontSize: '2rem', fontWeight: 700 }}>{stats.workflows}</h3>
              <p style={{ margin: '0.15rem 0 0 0', color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Workflows</p>
            </div>
          </div>

          <div className="glass-panel hoverable stat-card" style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', padding: '1.75rem' }}>
            <div style={{ background: 'rgba(245, 158, 11, 0.12)', padding: '1rem', borderRadius: '14px', border: '1px solid rgba(245, 158, 11, 0.2)' }}>
              <Play color="#fbbf24" size={28} />
            </div>
            <div>
              <h3 style={{ margin: 0, fontSize: '2rem', fontWeight: 700 }}>{stats.running}</h3>
              <p style={{ margin: '0.15rem 0 0 0', color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Active Runs</p>
            </div>
          </div>

          <div className="glass-panel hoverable stat-card" style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', padding: '1.75rem' }}>
            <div style={{ background: 'rgba(16, 185, 129, 0.12)', padding: '1rem', borderRadius: '14px', border: '1px solid rgba(16, 185, 129, 0.2)' }}>
              <CheckCircle2 color="#34d399" size={28} />
            </div>
            <div>
              <h3 style={{ margin: 0, fontSize: '2rem', fontWeight: 700 }}>{stats.success}</h3>
              <p style={{ margin: '0.15rem 0 0 0', color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Completed</p>
            </div>
          </div>

          <div className="glass-panel hoverable stat-card" style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', padding: '1.75rem' }}>
            <div style={{ background: 'rgba(239, 68, 68, 0.12)', padding: '1rem', borderRadius: '14px', border: '1px solid rgba(239, 68, 68, 0.2)' }}>
              <AlertTriangle color="#f87171" size={28} />
            </div>
            <div>
              <h3 style={{ margin: 0, fontSize: '2rem', fontWeight: 700 }}>{stats.failed}</h3>
              <p style={{ margin: '0.15rem 0 0 0', color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Failed</p>
            </div>
          </div>
          
          {/* New Production Metrics */}
          <div className="glass-panel hoverable stat-card" style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', padding: '1.75rem' }}>
            <div style={{ background: 'rgba(56, 189, 248, 0.12)', padding: '1rem', borderRadius: '14px', border: '1px solid rgba(56, 189, 248, 0.2)' }}>
              <Activity color="#38bdf8" size={28} />
            </div>
            <div>
              <h3 style={{ margin: 0, fontSize: '2rem', fontWeight: 700 }}>1.2k<span style={{ fontSize: '1rem', color: 'var(--text-muted)' }}>/min</span></h3>
              <p style={{ margin: '0.15rem 0 0 0', color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Throughput</p>
            </div>
          </div>

          <div className="glass-panel hoverable stat-card" style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', padding: '1.75rem' }}>
            <div style={{ background: 'rgba(168, 85, 247, 0.12)', padding: '1rem', borderRadius: '14px', border: '1px solid rgba(168, 85, 247, 0.2)' }}>
              <Database color="#a855f7" size={28} />
            </div>
            <div>
              <h3 style={{ margin: 0, fontSize: '2rem', fontWeight: 700 }}>24</h3>
              <p style={{ margin: '0.15rem 0 0 0', color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Queue Size</p>
            </div>
          </div>

          <div className="glass-panel hoverable stat-card" style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', padding: '1.75rem' }}>
            <div style={{ background: 'rgba(34, 197, 94, 0.12)', padding: '1rem', borderRadius: '14px', border: '1px solid rgba(34, 197, 94, 0.2)' }}>
              <Server color="#22c55e" size={28} />
            </div>
            <div>
              <h3 style={{ margin: 0, fontSize: '2rem', fontWeight: 700 }}>12/12</h3>
              <p style={{ margin: '0.15rem 0 0 0', color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Worker Health</p>
            </div>
          </div>

          <div className="glass-panel hoverable stat-card" style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', padding: '1.75rem' }}>
            <div style={{ background: 'rgba(236, 72, 153, 0.12)', padding: '1rem', borderRadius: '14px', border: '1px solid rgba(236, 72, 153, 0.2)' }}>
              <GitBranch color="#ec4899" size={28} />
            </div>
            <div>
              <h3 style={{ margin: 0, fontSize: '2rem', fontWeight: 700 }}>0<span style={{ fontSize: '1rem', color: 'var(--text-muted)' }}>ms</span></h3>
              <p style={{ margin: '0.15rem 0 0 0', color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Kafka Lag</p>
            </div>
          </div>
        </div>

        {/* Executions Table */}
        <div className="glass-panel animate-slide-up">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
            <h3 style={{ margin: 0, fontSize: '1.35rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Activity size={20} color="#818cf8" />
              Recent Executions
            </h3>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              Auto-refreshing every 3s
            </span>
          </div>
          {executions.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '4rem 0', color: 'var(--text-muted)' }}>
              <Clock size={48} style={{ opacity: 0.2, marginBottom: '1rem' }} />
              <p style={{ fontSize: '1.1rem' }}>No executions found. Deploy a workflow to see it here.</p>
            </div>
          ) : (
            <table className="premium-table">
              <thead>
                <tr>
                  <th>Workflow</th>
                  <th>Execution ID</th>
                  <th>Status</th>
                  <th>Started</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {executions.map((exec: any) => (
                  <tr key={exec.id} className="stagger-item">
                    <td>
                      <div style={{ fontWeight: 600, color: '#e2e8f0' }}>{exec.workflowName || 'Untitled'}</div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>v{exec.versionNumber}</div>
                    </td>
                    <td style={{ fontFamily: "'JetBrains Mono', monospace", fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                      {exec.id.substring(0, 8)}…
                    </td>
                    <td>{getStatusBadge(exec.status)}</td>
                    <td style={{ color: 'var(--text-secondary)' }}>
                      {timeAgo(exec.startedAt)}
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <button className="btn-ghost" onClick={() => navigate(`/executions/${exec.id}`)} style={{ 
                        padding: '8px 16px',
                        border: '1px solid rgba(255,255,255,0.1)',
                        borderRadius: '8px'
                      }}>
                        <Eye size={16} /> View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
};
