import React from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { LayoutDashboard, FolderKanban, Network, BarChart3, Bot, LayoutTemplate, LogOut, Settings } from 'lucide-react';

export const Layout: React.FC = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('flowforge_token');
    navigate('/login');
  };

  const navItems = [
    { to: '/projects', label: 'Projects' },
    { to: '/integrations', label: 'Integrations' },
    { to: '/analyze', label: 'Analyze ⌄' },
    { to: '/builder', label: 'Automation Builder' },
  ];

  return (
    <div className="bg-workflow-theme" style={{ display: 'flex', flexDirection: 'column', height: '100vh', width: '100vw', overflow: 'hidden' }}>
      {/* Top Navbar */}
      <div style={{
        height: '60px',
        background: '#151515',
        borderBottom: '1px solid #2a2a2a',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0 24px',
        zIndex: 50
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '32px' }}>
          {/* Logo */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }} onClick={() => navigate('/dashboard')}>
            <div style={{ display: 'flex', gap: '2px', flexWrap: 'wrap', width: '20px' }}>
              <div style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#00f3ff' }}></div>
              <div style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#fff' }}></div>
              <div style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#fff' }}></div>
              <div style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#00f3ff' }}></div>
            </div>
            <h1 style={{ margin: 0, fontSize: '14px', fontWeight: 600, color: '#e2e8f0', letterSpacing: '1px' }}>
              WORKFLOW.IO
            </h1>
          </div>

          {/* Nav Links */}
          <nav style={{ display: 'flex', gap: '24px' }}>
            {navItems.map((item) => {
              const isActive = window.location.pathname.startsWith(item.to);
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  style={{
                    textDecoration: 'none',
                    color: isActive ? '#3b82f6' : '#94a3b8',
                    fontSize: '13px',
                    fontWeight: 500,
                    borderBottom: isActive ? '2px solid #3b82f6' : '2px solid transparent',
                    padding: '20px 0',
                    transition: 'all 0.2s ease'
                  }}
                >
                  {item.label}
                </NavLink>
              );
            })}
          </nav>
        </div>

        {/* Right Actions */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <button style={{
            background: '#fff',
            color: '#000',
            border: 'none',
            borderRadius: '6px',
            padding: '6px 16px',
            fontSize: '13px',
            fontWeight: 600,
            cursor: 'pointer'
          }}>Deploy</button>
          <button onClick={handleLogout} style={{ background: 'transparent', border: 'none', color: '#94a3b8', cursor: 'pointer', padding: '6px' }}>
            <LogOut size={16} />
          </button>
        </div>
      </div>

      {/* Main Content Area */}
      <div style={{ flex: 1, overflow: 'hidden', position: 'relative', background: '#0a0a0a' }}>
        <Outlet />
      </div>
    </div>
  );
};
