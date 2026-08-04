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
    { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/projects', icon: FolderKanban, label: 'Projects' },
    { to: '/integrations', icon: Network, label: 'Integrations' },
    { to: '/analyze', icon: BarChart3, label: 'Analyze' },
    { to: '/builder', icon: Bot, label: 'Automation Builder' },
  ];

  return (
    <div className="bg-moving-gradient" style={{ display: 'flex', height: '100vh', width: '100vw', overflow: 'hidden' }}>
      {/* Sidebar */}
      <div style={{
        width: '260px',
        background: 'rgba(10, 14, 28, 0.85)',
        backdropFilter: 'blur(20px)',
        borderRight: '1px solid rgba(0, 243, 255, 0.15)',
        boxShadow: '2px 0 20px rgba(176, 38, 255, 0.05)',
        display: 'flex',
        flexDirection: 'column',
        zIndex: 50
      }}>
        <div style={{ padding: '2rem 1.5rem', display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div className="neon-border" style={{ padding: '8px', borderRadius: '10px', background: 'rgba(0, 243, 255, 0.1)' }}>
            <LayoutTemplate size={24} color="#00f3ff" />
          </div>
          <h1 className="neon-text" style={{ margin: 0, fontSize: '1.25rem', fontWeight: 700 }}>
            FlowForge AI
          </h1>
        </div>

        <nav style={{ flex: 1, padding: '0 1rem', display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <div style={{ fontSize: '0.7rem', textTransform: 'uppercase', letterSpacing: '1.5px', color: 'var(--text-muted)', marginBottom: '8px', paddingLeft: '8px' }}>
            Main Menu
          </div>
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              style={({ isActive }) => ({
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
                padding: '12px 16px',
                borderRadius: '12px',
                textDecoration: 'none',
                color: isActive ? '#00f3ff' : '#cbd5e1',
                background: isActive ? 'rgba(0, 243, 255, 0.1)' : 'transparent',
                borderLeft: isActive ? '3px solid #00f3ff' : '3px solid transparent',
                boxShadow: isActive ? 'inset 10px 0 20px rgba(0, 243, 255, 0.05)' : 'none',
                transition: 'all 0.2s ease',
                fontWeight: isActive ? 600 : 500
              })}
            >
              <item.icon size={20} color={window.location.pathname.startsWith(item.to) ? '#00f3ff' : '#94a3b8'} />
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div style={{ padding: '2rem 1rem', borderTop: '1px solid rgba(255,255,255,0.08)' }}>
          <NavLink to="/settings" style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '12px 16px', borderRadius: '12px', textDecoration: 'none', color: '#cbd5e1', transition: 'all 0.2s ease' }}>
            <Settings size={20} color="#94a3b8" /> Settings
          </NavLink>
          <button onClick={handleLogout} style={{ width: '100%', background: 'transparent', border: 'none', display: 'flex', alignItems: 'center', gap: '12px', padding: '12px 16px', borderRadius: '12px', color: '#f87171', cursor: 'pointer', transition: 'all 0.2s ease', marginTop: '8px' }}>
            <LogOut size={20} color="#f87171" /> Logout
          </button>
        </div>
      </div>

      {/* Main Content Area */}
      <div style={{ flex: 1, overflowY: 'auto', position: 'relative' }}>
        <Outlet />
      </div>
    </div>
  );
};
