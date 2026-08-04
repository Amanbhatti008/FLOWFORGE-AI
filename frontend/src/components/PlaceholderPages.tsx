import React from 'react';
import { FolderKanban, Network, BarChart3, Settings } from 'lucide-react';

const PageWrapper: React.FC<{ title: string, icon: any, description: string }> = ({ title, icon: Icon, description }) => (
  <div style={{ padding: '3rem', maxWidth: '1200px', margin: '0 auto', height: '100%', display: 'flex', flexDirection: 'column' }}>
    <header className="animate-slide-up" style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '2rem' }}>
      <div style={{ background: 'rgba(99, 102, 241, 0.15)', padding: '12px', borderRadius: '12px' }}>
        <Icon size={28} color="#818cf8" />
      </div>
      <div>
        <h1 style={{ margin: 0, fontSize: '2rem', color: '#fff' }}>{title}</h1>
        <p style={{ margin: '4px 0 0', color: 'var(--text-muted)' }}>{description}</p>
      </div>
    </header>
    
    <div className="glass-panel animate-slide-up" style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', flexDirection: 'column', color: 'var(--text-muted)' }}>
      <Icon size={64} style={{ opacity: 0.1, marginBottom: '1rem' }} />
      <h2>Coming Soon</h2>
      <p>This module is currently under development in Phase 5.</p>
    </div>
  </div>
);

export const Projects: React.FC = () => <PageWrapper title="Projects" icon={FolderKanban} description="Organize workflows into dedicated team projects and environments." />;
export const Integrations: React.FC = () => <PageWrapper title="Integrations" icon={Network} description="Manage API keys, OAuth tokens, and external service webhooks." />;
export const Analyze: React.FC = () => <PageWrapper title="Analyze" icon={BarChart3} description="Deep analytics, cost tracking, and execution bottlenecks." />;
export const SettingsPage: React.FC = () => <PageWrapper title="Settings" icon={Settings} description="Workspace configuration and user management." />;
