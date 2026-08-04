import React from 'react';
import { FolderKanban, Network, BarChart3, Settings, Plus, Search, MoreVertical, Key, Zap, Shield, ChevronRight, Activity, Clock, Server } from 'lucide-react';

export const Projects: React.FC = () => (
  <div style={{ padding: '3rem', maxWidth: '1200px', margin: '0 auto', height: '100%', overflowY: 'auto' }}>
    <header className="animate-slide-up" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2.5rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <div style={{ background: 'linear-gradient(135deg, rgba(59,130,246,0.15), rgba(139,92,246,0.15))', padding: '12px', borderRadius: '12px', border: '1px solid rgba(99,102,241,0.2)' }}>
          <FolderKanban size={28} color="#818cf8" />
        </div>
        <div>
          <h1 style={{ margin: 0, fontSize: '2rem', color: '#fff', fontWeight: 600 }}>Projects</h1>
          <p style={{ margin: '4px 0 0', color: '#94a3b8' }}>Organize and manage your workflow environments.</p>
        </div>
      </div>
      <button className="btn-primary" style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '10px 20px', background: 'linear-gradient(135deg, #6366f1, #8b5cf6)', border: 'none', borderRadius: '8px', color: '#fff', fontWeight: 600, cursor: 'pointer' }}>
        <Plus size={18} /> New Project
      </button>
    </header>

    <div className="animate-slide-up" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '1.5rem', animationDelay: '0.1s' }}>
      {[
        { name: 'Production Data Pipeline', env: 'Production', workflows: 12, health: '99.9%', color: '#10b981' },
        { name: 'Marketing Automations', env: 'Staging', workflows: 5, health: '100%', color: '#3b82f6' },
        { name: 'Nightly Syncs', env: 'Production', workflows: 3, health: '98.2%', color: '#f59e0b' },
        { name: 'Data Science Experiments', env: 'Development', workflows: 8, health: '85.4%', color: '#ec4899' },
      ].map((proj, i) => (
        <div key={i} className="glass-panel" style={{ padding: '1.5rem', cursor: 'pointer', transition: 'all 0.2s', position: 'relative', overflow: 'hidden' }}>
          <div style={{ position: 'absolute', top: 0, left: 0, width: '4px', height: '100%', background: proj.color }}></div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <div>
              <h3 style={{ margin: '0 0 8px 0', color: '#e2e8f0', fontSize: '1.2rem' }}>{proj.name}</h3>
              <span style={{ display: 'inline-block', padding: '4px 10px', background: 'rgba(255,255,255,0.05)', borderRadius: '20px', fontSize: '0.75rem', color: '#94a3b8', border: '1px solid rgba(255,255,255,0.1)' }}>
                {proj.env} Environment
              </span>
            </div>
            <button style={{ background: 'transparent', border: 'none', color: '#64748b', cursor: 'pointer' }}>
              <MoreVertical size={20} />
            </button>
          </div>
          <div style={{ display: 'flex', gap: '2rem', marginTop: '2rem', paddingTop: '1.5rem', borderTop: '1px solid rgba(255,255,255,0.05)' }}>
            <div>
              <div style={{ fontSize: '0.8rem', color: '#64748b', marginBottom: '4px' }}>Workflows</div>
              <div style={{ fontSize: '1.2rem', color: '#e2e8f0', fontWeight: 600 }}>{proj.workflows}</div>
            </div>
            <div>
              <div style={{ fontSize: '0.8rem', color: '#64748b', marginBottom: '4px' }}>Health Score</div>
              <div style={{ fontSize: '1.2rem', color: proj.color, fontWeight: 600 }}>{proj.health}</div>
            </div>
          </div>
        </div>
      ))}
    </div>
  </div>
);

export const Integrations: React.FC = () => (
  <div style={{ padding: '3rem', maxWidth: '1200px', margin: '0 auto', height: '100%', overflowY: 'auto' }}>
    <header className="animate-slide-up" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2.5rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <div style={{ background: 'linear-gradient(135deg, rgba(16,185,129,0.15), rgba(5,150,105,0.15))', padding: '12px', borderRadius: '12px', border: '1px solid rgba(16,185,129,0.2)' }}>
          <Network size={28} color="#34d399" />
        </div>
        <div>
          <h1 style={{ margin: 0, fontSize: '2rem', color: '#fff', fontWeight: 600 }}>Integrations</h1>
          <p style={{ margin: '4px 0 0', color: '#94a3b8' }}>Connect external services, databases, and APIs.</p>
        </div>
      </div>
      <div style={{ display: 'flex', gap: '12px' }}>
        <div style={{ position: 'relative' }}>
          <Search size={18} color="#64748b" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }} />
          <input type="text" placeholder="Search integrations..." style={{ background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', padding: '10px 16px 10px 40px', borderRadius: '8px', color: '#fff', width: '250px' }} />
        </div>
      </div>
    </header>

    <div className="animate-slide-up" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1.5rem', animationDelay: '0.1s' }}>
      {[
        { name: 'AWS S3', type: 'Storage', status: 'Connected', icon: Server, color: '#f59e0b' },
        { name: 'PostgreSQL', type: 'Database', status: 'Connected', icon: Server, color: '#3b82f6' },
        { name: 'Slack', type: 'Notifications', status: 'Configured', icon: Zap, color: '#ec4899' },
        { name: 'Salesforce', type: 'CRM', status: 'Disconnected', icon: Network, color: '#64748b' },
        { name: 'OpenAI', type: 'AI Services', status: 'Connected', icon: Zap, color: '#10b981' },
        { name: 'GitHub', type: 'Version Control', status: 'Connected', icon: Network, color: '#e2e8f0' },
      ].map((intg, i) => (
        <div key={i} className="glass-panel" style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', gap: '1rem', border: intg.status === 'Connected' ? '1px solid rgba(16,185,129,0.2)' : '1px solid rgba(255,255,255,0.05)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <div style={{ background: 'rgba(255,255,255,0.05)', padding: '12px', borderRadius: '12px' }}>
              <intg.icon size={24} color={intg.color} />
            </div>
            <span style={{ 
              fontSize: '0.75rem', padding: '4px 8px', borderRadius: '4px', fontWeight: 500,
              background: intg.status === 'Connected' ? 'rgba(16,185,129,0.1)' : 'rgba(255,255,255,0.05)',
              color: intg.status === 'Connected' ? '#34d399' : '#94a3b8'
            }}>
              {intg.status}
            </span>
          </div>
          <div>
            <h3 style={{ margin: '0 0 4px 0', color: '#e2e8f0', fontSize: '1.1rem' }}>{intg.name}</h3>
            <p style={{ margin: 0, color: '#64748b', fontSize: '0.85rem' }}>{intg.type}</p>
          </div>
          <button style={{ marginTop: 'auto', background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', padding: '8px', borderRadius: '6px', color: '#e2e8f0', cursor: 'pointer', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px', transition: 'all 0.2s' }}>
            {intg.status === 'Connected' ? 'Manage Settings' : 'Connect'}
          </button>
        </div>
      ))}
    </div>
  </div>
);

export const Analyze: React.FC = () => (
  <div style={{ padding: '3rem', maxWidth: '1200px', margin: '0 auto', height: '100%', overflowY: 'auto' }}>
    <header className="animate-slide-up" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2.5rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <div style={{ background: 'linear-gradient(135deg, rgba(245,158,11,0.15), rgba(217,119,6,0.15))', padding: '12px', borderRadius: '12px', border: '1px solid rgba(245,158,11,0.2)' }}>
          <BarChart3 size={28} color="#fbbf24" />
        </div>
        <div>
          <h1 style={{ margin: 0, fontSize: '2rem', color: '#fff', fontWeight: 600 }}>Analytics & Insights</h1>
          <p style={{ margin: '4px 0 0', color: '#94a3b8' }}>Monitor platform throughput, costs, and performance.</p>
        </div>
      </div>
      <select style={{ background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', color: '#fff', padding: '8px 16px', borderRadius: '8px', outline: 'none' }}>
        <option>Last 7 Days</option>
        <option>Last 30 Days</option>
        <option>This Year</option>
      </select>
    </header>

    <div className="animate-slide-up" style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '1.5rem', marginBottom: '2rem', animationDelay: '0.1s' }}>
      <div className="glass-panel" style={{ padding: '1.5rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#94a3b8', marginBottom: '1rem' }}><Activity size={18} /> Total Executions</div>
        <div style={{ fontSize: '2.5rem', color: '#fff', fontWeight: 700, marginBottom: '8px' }}>1.24M</div>
        <div style={{ color: '#34d399', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '4px' }}>↑ 12% vs last period</div>
      </div>
      <div className="glass-panel" style={{ padding: '1.5rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#94a3b8', marginBottom: '1rem' }}><Clock size={18} /> Avg Execution Time</div>
        <div style={{ fontSize: '2.5rem', color: '#fff', fontWeight: 700, marginBottom: '8px' }}>412ms</div>
        <div style={{ color: '#34d399', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '4px' }}>↓ 45ms vs last period</div>
      </div>
      <div className="glass-panel" style={{ padding: '1.5rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#94a3b8', marginBottom: '1rem' }}><Server size={18} /> Compute Cost</div>
        <div style={{ fontSize: '2.5rem', color: '#fff', fontWeight: 700, marginBottom: '8px' }}>$428.50</div>
        <div style={{ color: '#f87171', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '4px' }}>↑ $12 vs last period</div>
      </div>
    </div>

    <div className="glass-panel animate-slide-up" style={{ padding: '2rem', minHeight: '300px', display: 'flex', flexDirection: 'column', animationDelay: '0.2s' }}>
      <h3 style={{ margin: '0 0 1.5rem 0', color: '#e2e8f0', fontSize: '1.1rem' }}>Throughput Overview (Mock)</h3>
      <div style={{ flex: 1, display: 'flex', alignItems: 'flex-end', gap: '8px', paddingTop: '2rem' }}>
        {/* Mock Chart Bars */}
        {Array.from({ length: 30 }).map((_, i) => {
          const height = 20 + Math.random() * 80;
          return (
            <div key={i} style={{ 
              flex: 1, 
              background: `linear-gradient(to top, rgba(99,102,241,0.8), rgba(139,92,246,0.8))`, 
              height: `${height}%`, 
              borderRadius: '4px 4px 0 0',
              opacity: 0.8
            }}></div>
          );
        })}
      </div>
    </div>
  </div>
);

export const SettingsPage: React.FC = () => (
  <div style={{ padding: '3rem', maxWidth: '800px', margin: '0 auto', height: '100%', overflowY: 'auto' }}>
    <header className="animate-slide-up" style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '2.5rem' }}>
      <div style={{ background: 'rgba(255,255,255,0.05)', padding: '12px', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.1)' }}>
        <Settings size={28} color="#cbd5e1" />
      </div>
      <div>
        <h1 style={{ margin: 0, fontSize: '2rem', color: '#fff', fontWeight: 600 }}>Workspace Settings</h1>
        <p style={{ margin: '4px 0 0', color: '#94a3b8' }}>Manage members, roles, and global configuration.</p>
      </div>
    </header>

    <div className="glass-panel animate-slide-up" style={{ animationDelay: '0.1s' }}>
      {['General', 'Members & Roles', 'Security & SAML', 'Billing', 'API Keys'].map((tab, i) => (
        <div key={i} style={{ 
          padding: '1.25rem 1.5rem', 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center',
          borderBottom: i === 4 ? 'none' : '1px solid rgba(255,255,255,0.05)',
          cursor: 'pointer',
          color: '#e2e8f0',
          transition: 'all 0.2s',
        }} className="hover:bg-[rgba(255,255,255,0.02)]">
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            {i === 0 && <Settings size={18} color="#64748b" />}
            {i === 1 && <Network size={18} color="#64748b" />}
            {i === 2 && <Shield size={18} color="#64748b" />}
            {i === 3 && <Activity size={18} color="#64748b" />}
            {i === 4 && <Key size={18} color="#64748b" />}
            <span style={{ fontWeight: 500 }}>{tab}</span>
          </div>
          <ChevronRight size={18} color="#64748b" />
        </div>
      ))}
    </div>
  </div>
);
