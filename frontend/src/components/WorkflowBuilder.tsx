import React, { useState, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  ReactFlow,
  Controls,
  Background,
  BackgroundVariant,
  useNodesState,
  useEdgesState,
  addEdge,
  Handle,
  Position,
  useReactFlow,
  ReactFlowProvider
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { ArrowLeft, Play, Code, Globe, Mail, Bot, Database, MessageSquare, GitBranch, Server, FileCheck, Upload, Bell, ChevronDown } from 'lucide-react';
import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

// ========== WORKFLOW TEMPLATES ==========
const TEMPLATES: Record<string, { name: string; description: string; nodes: any[]; edges: any[] }> = {
  'ai-triaging': {
    name: 'AI Support Triaging',
    description: 'Automated Sentiment Analysis & Escalation',
    nodes: [
      { id: '1', type: 'custom', position: { x: 300, y: 50 }, data: { label: 'Receive Support Email', app: 'Mail', type: 'HTTP' } },
      { id: '2', type: 'custom', position: { x: 300, y: 200 }, data: { label: 'AI Sentiment Analysis', app: 'Bot', type: 'SCRIPT' } },
      { id: '3', type: 'custom', position: { x: 100, y: 350 }, data: { label: 'Create Jira Ticket', app: 'Database', type: 'HTTP' } },
      { id: '4', type: 'custom', position: { x: 500, y: 350 }, data: { label: 'Send Slack Alert', app: 'MessageSquare', type: 'HTTP' } },
      { id: '5', type: 'custom', position: { x: 300, y: 500 }, data: { label: 'Reply to Customer', app: 'Mail', type: 'HTTP' } },
    ],
    edges: [
      { id: 'e1-2', source: '1', target: '2' },
      { id: 'e2-3', source: '2', target: '3' },
      { id: 'e2-4', source: '2', target: '4' },
      { id: 'e3-5', source: '3', target: '5' },
      { id: 'e4-5', source: '4', target: '5' },
    ]
  },
  'cicd-pipeline': {
    name: 'CI/CD Pipeline',
    description: 'Build → Test → Deploy with Approval Gate',
    nodes: [
      { id: '1', type: 'custom', position: { x: 300, y: 50 }, data: { label: 'Git Push Trigger', app: 'GitBranch', type: 'HTTP' } },
      { id: '2', type: 'custom', position: { x: 300, y: 200 }, data: { label: 'Build & Compile', app: 'Server', type: 'SCRIPT' } },
      { id: '3', type: 'custom', position: { x: 100, y: 350 }, data: { label: 'Unit Tests', app: 'FileCheck', type: 'SCRIPT' } },
      { id: '4', type: 'custom', position: { x: 500, y: 350 }, data: { label: 'Integration Tests', app: 'FileCheck', type: 'SCRIPT' } },
      { id: '5', type: 'custom', position: { x: 300, y: 500 }, data: { label: 'Deploy to Staging', app: 'Upload', type: 'HTTP' } },
      { id: '6', type: 'custom', position: { x: 300, y: 650 }, data: { label: 'Notify Team', app: 'Bell', type: 'HTTP' } },
    ],
    edges: [
      { id: 'e1-2', source: '1', target: '2' },
      { id: 'e2-3', source: '2', target: '3' },
      { id: 'e2-4', source: '2', target: '4' },
      { id: 'e3-5', source: '3', target: '5' },
      { id: 'e4-5', source: '4', target: '5' },
      { id: 'e5-6', source: '5', target: '6' },
    ]
  },
  'data-etl': {
    name: 'Data ETL Pipeline',
    description: 'Extract → Transform → Load with Validation',
    nodes: [
      { id: '1', type: 'custom', position: { x: 300, y: 50 }, data: { label: 'Extract from S3', app: 'Database', type: 'HTTP' } },
      { id: '2', type: 'custom', position: { x: 300, y: 200 }, data: { label: 'Transform Data', app: 'Bot', type: 'SCRIPT' } },
      { id: '3', type: 'custom', position: { x: 300, y: 350 }, data: { label: 'Validate Schema', app: 'FileCheck', type: 'SCRIPT' } },
      { id: '4', type: 'custom', position: { x: 300, y: 500 }, data: { label: 'Load to BigQuery', app: 'Database', type: 'HTTP' } },
      { id: '5', type: 'custom', position: { x: 300, y: 650 }, data: { label: 'Send Report', app: 'Mail', type: 'HTTP' } },
    ],
    edges: [
      { id: 'e1-2', source: '1', target: '2' },
      { id: 'e2-3', source: '2', target: '3' },
      { id: 'e3-4', source: '3', target: '4' },
      { id: 'e4-5', source: '4', target: '5' },
    ]
  }
};

// Custom Node
const CustomSciFiNode = ({ data }: any) => {
  const iconMap: Record<string, any> = {
    'Mail': <Mail size={20} color="#60a5fa" />,
    'Bot': <Bot size={20} color="#c084fc" />,
    'Database': <Database size={20} color="#f472b6" />,
    'MessageSquare': <MessageSquare size={20} color="#fbbf24" />,
    'GitBranch': <GitBranch size={20} color="#f97316" />,
    'Server': <Server size={20} color="#38bdf8" />,
    'FileCheck': <FileCheck size={20} color="#a3e635" />,
    'Upload': <Upload size={20} color="#2dd4bf" />,
    'Bell': <Bell size={20} color="#fb923c" />,
  };

  return (
    <div style={{
      background: 'rgba(16, 23, 42, 0.85)',
      backdropFilter: 'blur(12px)',
      border: `1px solid rgba(99, 102, 241, 0.3)`,
      borderRadius: '12px',
      padding: '1rem',
      minWidth: '220px',
      boxShadow: `0 8px 32px rgba(99, 102, 241, 0.1)`,
      display: 'flex',
      alignItems: 'center',
      gap: '1rem',
      color: '#f8fafc'
    }}>
      <Handle type="target" position={Position.Top} style={{ background: '#94a3b8', border: 'none', width: '8px', height: '8px' }} />
      <div style={{
        background: 'rgba(99, 102, 241, 0.15)',
        padding: '0.6rem',
        borderRadius: '8px'
      }}>
        {iconMap[data.app] || <Globe size={20} color="#60a5fa" />}
      </div>
      <div>
        <div style={{ fontSize: '0.65rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '1.5px' }}>
          {data.type} TASK
        </div>
        <div style={{ fontWeight: 600, marginTop: '2px', fontSize: '0.9rem' }}>{data.label}</div>
      </div>
      <Handle type="source" position={Position.Bottom} style={{ background: '#94a3b8', border: 'none', width: '8px', height: '8px' }} />
    </div>
  );
};

const nodeTypes = { custom: CustomSciFiNode };

const SidebarItem = ({ icon: Icon, label, color, type, app }: any) => (
  <div
    draggable
    onDragStart={(e) => {
      e.dataTransfer.setData('application/flowforge-node', JSON.stringify({ label, type: type || 'HTTP', app: app || 'Globe' }));
      e.dataTransfer.effectAllowed = 'move';
    }}
    style={{
      display: 'flex', alignItems: 'center', gap: '12px', padding: '10px 12px',
      background: 'rgba(255,255,255,0.03)', border: '1px solid rgba(255,255,255,0.08)',
      borderRadius: '8px', marginBottom: '6px', cursor: 'grab',
      transition: 'all 0.2s ease'
    }}
    onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.background = 'rgba(255,255,255,0.06)'; }}
    onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.background = 'rgba(255,255,255,0.03)'; }}
  >
    <div style={{ background: `rgba(255,255,255,0.05)`, padding: '6px', borderRadius: '6px' }}>
      <Icon size={16} color={color} />
    </div>
    <span style={{ fontSize: '0.85rem', color: '#cbd5e1', fontWeight: 500 }}>{label}</span>
  </div>
);

const WorkflowBuilderInner: React.FC = () => {
  const navigate = useNavigate();
  const [selectedTemplate, setSelectedTemplate] = useState('ai-triaging');
  const [showDropdown, setShowDropdown] = useState(false);
  const template = TEMPLATES[selectedTemplate];
  const reactFlowWrapper = useRef<HTMLDivElement>(null);
  const { screenToFlowPosition } = useReactFlow();
  let nodeIdCounter = useRef(100);

  const edgesWithStyle = template.edges.map(e => ({
    ...e,
    animated: true,
    style: { stroke: 'rgba(255,255,255,0.3)', strokeWidth: 2 }
  }));
  
  const [nodes, setNodes, onNodesChange] = useNodesState(template.nodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(edgesWithStyle);
  const [loading, setLoading] = useState(false);

  const onDragOver = useCallback((event: React.DragEvent) => {
    event.preventDefault();
    event.dataTransfer.dropEffect = 'move';
  }, []);

  const onDrop = useCallback((event: React.DragEvent) => {
    event.preventDefault();
    const raw = event.dataTransfer.getData('application/flowforge-node');
    if (!raw) return;
    const { label, type, app } = JSON.parse(raw);
    const position = screenToFlowPosition({ x: event.clientX, y: event.clientY });
    const newId = `drop-${++nodeIdCounter.current}`;
    const newNode = {
      id: newId,
      type: 'custom',
      position,
      data: { label, type, app }
    };
    setNodes((nds) => [...nds, newNode]);
  }, [screenToFlowPosition, setNodes]);

  const switchTemplate = (key: string) => {
    const t = TEMPLATES[key];
    setSelectedTemplate(key);
    setNodes(t.nodes);
    setEdges(t.edges.map(e => ({
      ...e,
      animated: true,
      style: { stroke: 'rgba(255,255,255,0.3)', strokeWidth: 2 }
    })));
    setShowDropdown(false);
  };

  const onConnect = useCallback((params: any) => setEdges((eds) => addEdge({...params, animated: true, style: { stroke: 'rgba(255,255,255,0.3)', strokeWidth: 2 }}, eds)), [setEdges]);

  const handleSaveAndRun = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('flowforge_token');
      if (!token) {
        alert('Authentication Error: Please login first!');
        navigate('/login');
        setLoading(false);
        return;
      }
      
      const backendNodes = nodes.map((n) => ({
        id: n.id,
        type: n.data.type || 'HTTP',
        name: n.data.label,
        app: n.data.app,
        position: n.position
      }));

      const execRes = await axios.post(`${API_BASE}/workflows/execute`, {
        nodes: backendNodes,
        edges: edges.map(e => ({ source: e.source, target: e.target }))
      }, { headers: { Authorization: `Bearer ${token}` } });

      const executionId = execRes.data.data.executionId;
      navigate(`/executions/${executionId}`);
    } catch (err: any) {
      alert('Error: ' + err.message);
    }
    setLoading(false);
  };

  const [aiPrompt, setAiPrompt] = useState('');
  const [aiLoading, setAiLoading] = useState(false);

  const handleAiGenerate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!aiPrompt.trim()) return;
    
    setAiLoading(true);
    try {
      const token = localStorage.getItem('flowforge_token');
      if (!token) {
        alert('Authentication Error: Please login first!');
        navigate('/login');
        setAiLoading(false);
        return;
      }
      const res = await axios.post('/api/v1/ai/generate-workflow', 
        { prompt: aiPrompt },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      
      const dag = res.data.data;
      if (dag && dag.nodes && dag.edges) {
        // Apply ReactFlow specific fields
        const formattedNodes = dag.nodes.map((n: any) => ({
          ...n,
          type: 'custom',
          data: { label: n.name, type: n.type, app: n.app || 'Globe' }
        }));
        
        const edgesWithStyle = dag.edges.map((e: any) => ({
          ...e,
          id: `e-${e.source}-${e.target}`,
          animated: true,
          style: { stroke: 'rgba(255,255,255,0.3)', strokeWidth: 2 }
        }));
        
        setNodes(formattedNodes);
        setEdges(edgesWithStyle);
        setAiPrompt('');
      }
    } catch (err: any) {
      alert('AI Generation Error: ' + err.message);
    }
    setAiLoading(false);
  };

  return (
    <div className="bg-moving-gradient" style={{ display: 'flex', flexDirection: 'column', height: '100vh', width: '100vw' }}>
      <header style={{ 
        padding: '1rem 2rem', 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        background: 'rgba(15, 23, 42, 0.6)',
        backdropFilter: 'blur(16px)',
        borderBottom: '1px solid rgba(255,255,255,0.1)',
        zIndex: 10
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <button className="btn-ghost" onClick={() => navigate('/dashboard')} style={{ padding: '0.5rem' }}>
            <ArrowLeft size={20} />
          </button>
          <div>
            <h2 style={{ margin: 0, fontFamily: 'Outfit, sans-serif', fontSize: '1.25rem' }}>AI Orchestrator</h2>
            <p style={{ margin: 0, fontSize: '0.8rem', color: 'var(--text-muted)' }}>Visual Pipeline Designer</p>
          </div>
        </div>

        {/* AI Prompt Input */}
        <form onSubmit={handleAiGenerate} style={{ flex: 1, maxWidth: '500px', margin: '0 2rem' }}>
          <div style={{ 
            display: 'flex', alignItems: 'center', background: 'rgba(255,255,255,0.05)', 
            border: '1px solid rgba(255,255,255,0.1)', borderRadius: '24px', padding: '4px 12px' 
          }}>
            <Bot size={18} color="#c084fc" />
            <input 
              type="text" 
              placeholder="✨ Describe your workflow to generate via AI..." 
              value={aiPrompt}
              onChange={(e) => setAiPrompt(e.target.value)}
              disabled={aiLoading}
              style={{
                background: 'transparent', border: 'none', color: '#fff', 
                width: '100%', padding: '8px 12px', outline: 'none', fontSize: '0.95rem'
              }}
            />
            {aiLoading && <span style={{ color: '#c084fc', fontSize: '0.8rem', animation: 'pulse 1.5s infinite' }}>Generating...</span>}
          </div>
        </form>

        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          {/* Template Switcher */}
          <div style={{ position: 'relative' }}>
            <button className="btn-ghost" onClick={() => setShowDropdown(!showDropdown)} style={{ 
              border: '1px solid rgba(255,255,255,0.15)', padding: '8px 16px', borderRadius: '8px', fontSize: '0.9rem'
            }}>
              {template.name} <ChevronDown size={14} />
            </button>
            {showDropdown && (
              <div style={{
                position: 'absolute', top: '100%', right: 0, marginTop: '8px',
                background: 'rgba(15, 23, 42, 0.95)', border: '1px solid rgba(255,255,255,0.15)',
                borderRadius: '12px', overflow: 'hidden', minWidth: '280px',
                backdropFilter: 'blur(16px)', zIndex: 50,
                boxShadow: '0 20px 40px rgba(0,0,0,0.5)'
              }}>
                {Object.entries(TEMPLATES).map(([key, t]) => (
                  <div key={key} onClick={() => switchTemplate(key)} style={{
                    padding: '12px 16px', cursor: 'pointer',
                    background: key === selectedTemplate ? 'rgba(99, 102, 241, 0.15)' : 'transparent',
                    borderLeft: key === selectedTemplate ? '3px solid #818cf8' : '3px solid transparent',
                    transition: 'all 0.2s ease'
                  }}
                  onMouseEnter={(e) => { if (key !== selectedTemplate) (e.currentTarget as HTMLElement).style.background = 'rgba(255,255,255,0.05)'; }}
                  onMouseLeave={(e) => { if (key !== selectedTemplate) (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
                  >
                    <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>{t.name}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '2px' }}>{t.description}</div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <button className="btn-success" onClick={handleSaveAndRun} disabled={loading} style={{ 
            boxShadow: '0 0 15px rgba(16, 185, 129, 0.4)', padding: '10px 20px'
          }}>
            <Play size={16} /> {loading ? 'Deploying...' : 'Deploy & Run Pipeline'}
          </button>
        </div>
      </header>
      
      <div style={{ flex: 1, display: 'flex' }}>
        {/* Sidebar */}
        <div style={{
          width: '260px',
          background: 'rgba(15, 23, 42, 0.4)',
          backdropFilter: 'blur(16px)',
          borderRight: '1px solid rgba(255,255,255,0.08)',
          padding: '1.25rem',
          display: 'flex',
          flexDirection: 'column',
          overflowY: 'auto'
        }}>
          <h3 style={{ color: '#94a3b8', fontSize: '0.7rem', textTransform: 'uppercase', letterSpacing: '1.5px', marginBottom: '1rem' }}>Triggers</h3>
          <SidebarItem icon={Mail} label="Email Webhook" color="#60a5fa" type="HTTP" app="Mail" />
          <SidebarItem icon={GitBranch} label="Git Push" color="#f97316" type="HTTP" app="GitBranch" />
          
          <h3 style={{ color: '#94a3b8', fontSize: '0.7rem', textTransform: 'uppercase', letterSpacing: '1.5px', margin: '1.25rem 0 1rem' }}>AI / Compute</h3>
          <SidebarItem icon={Bot} label="OpenAI GPT-4" color="#c084fc" type="SCRIPT" app="Bot" />
          <SidebarItem icon={Code} label="Script Runner" color="#a3e635" type="SCRIPT" app="Code" />
          <SidebarItem icon={Server} label="Build Server" color="#38bdf8" type="HTTP" app="Server" />
          
          <h3 style={{ color: '#94a3b8', fontSize: '0.7rem', textTransform: 'uppercase', letterSpacing: '1.5px', margin: '1.25rem 0 1rem' }}>Integrations</h3>
          <SidebarItem icon={MessageSquare} label="Slack" color="#fbbf24" type="HTTP" app="MessageSquare" />
          <SidebarItem icon={Database} label="Jira / DB" color="#f472b6" type="HTTP" app="Database" />
          <SidebarItem icon={Upload} label="Deploy" color="#2dd4bf" type="HTTP" app="Upload" />
          <SidebarItem icon={Bell} label="Notification" color="#fb923c" type="HTTP" app="Bell" />
        </div>
        
        {/* Canvas */}
        <div style={{ flex: 1 }} ref={reactFlowWrapper} onDragOver={onDragOver} onDrop={onDrop}>
          <ReactFlow
            nodes={nodes}
            edges={edges}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            onConnect={onConnect}
            nodeTypes={nodeTypes}
            colorMode="dark"
            fitView
          >
            <Controls style={{ background: 'rgba(15,23,42,0.8)', border: '1px solid rgba(255,255,255,0.1)', fill: '#fff' }} />
            <Background variant={BackgroundVariant.Dots} gap={16} size={1} color="rgba(255,255,255,0.08)" />
          </ReactFlow>
        </div>
      </div>
    </div>
  );
};

// Wrap with ReactFlowProvider so useReactFlow() works
export const WorkflowBuilder: React.FC = () => (
  <ReactFlowProvider>
    <WorkflowBuilderInner />
  </ReactFlowProvider>
);
