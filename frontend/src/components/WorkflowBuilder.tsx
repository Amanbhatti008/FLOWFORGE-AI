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
import { Play, Bot, Database, MessageSquare, GitBranch, Server, FileCheck, Upload, Mail, Bell, Globe, ChevronDown, Code, Settings, X } from 'lucide-react';
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

// Custom Node matching the WORKFLOW.IO video mockup
const CustomSciFiNode = ({ data }: any) => {
  const iconMap: Record<string, any> = {
    'Mail': <Mail size={16} color="#3b82f6" />,
    'Bot': <Bot size={16} color="#8b5cf6" />,
    'Database': <Database size={16} color="#10b981" />,
    'MessageSquare': <MessageSquare size={16} color="#f59e0b" />,
    'GitBranch': <GitBranch size={16} color="#ef4444" />,
    'Server': <Server size={16} color="#06b6d4" />,
    'FileCheck': <FileCheck size={16} color="#84cc16" />,
    'Upload': <Upload size={16} color="#14b8a6" />,
    'Bell': <Bell size={16} color="#f97316" />,
    'Play': <Play size={16} color="#ef4444" />,
  };

  return (
    <div style={{
      background: '#222222',
      border: `1px solid #333333`,
      borderRadius: '8px',
      padding: '12px 16px',
      minWidth: '240px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      color: '#ffffff',
      boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.5)',
    }}>
      <Handle type="target" position={Position.Left} style={{ background: '#3b82f6', border: 'none', width: '10px', height: '10px', left: '-5px' }} />
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        <div style={{
          background: '#1a1a1a',
          padding: '8px',
          borderRadius: '6px',
          border: '1px solid #333'
        }}>
          {iconMap[data.app] || <Globe size={16} color="#3b82f6" />}
        </div>
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <div style={{ fontWeight: 500, fontSize: '0.9rem' }}>{data.label}</div>
          <div style={{ fontSize: '0.75rem', color: '#888' }}>
            ({data.type === 'SCRIPT' ? 'Python Script' : data.type === 'HTTP' ? 'Webhook' : data.type})
          </div>
        </div>
      </div>
      <div style={{ color: '#666', cursor: 'pointer' }}>⋮</div>
      <Handle type="source" position={Position.Right} style={{ background: '#3b82f6', border: 'none', width: '10px', height: '10px', right: '-5px' }} />
    </div>
  );
};

const nodeTypes = { custom: CustomSciFiNode };

const SidebarItem = ({ icon: Icon, label, type, app }: any) => (
  <div
    draggable
    onDragStart={(e) => {
      e.dataTransfer.setData('application/flowforge-node', JSON.stringify({ label, type: type || 'HTTP', app: app || 'Globe' }));
      e.dataTransfer.effectAllowed = 'move';
    }}
    style={{
      display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: '8px',
      background: '#1a1a1a', border: '1px solid #333',
      borderRadius: '8px', cursor: 'grab', padding: '16px 8px',
      transition: 'all 0.2s ease', color: '#999'
    }}
    onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.border = '1px solid #555'; (e.currentTarget as HTMLElement).style.color = '#fff'; }}
    onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.border = '1px solid #333'; (e.currentTarget as HTMLElement).style.color = '#999'; }}
  >
    <Icon size={20} />
    <span style={{ fontSize: '0.75rem', fontWeight: 500 }}>{label}</span>
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
    style: { stroke: '#3b82f6', strokeWidth: 2, filter: 'drop-shadow(0 0 5px rgba(59, 130, 246, 0.8))' }
  }));
  
  const [nodes, setNodes, onNodesChange] = useNodesState(template.nodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(edgesWithStyle);
  const [loading, setLoading] = useState(false);
  const [selectedNode, setSelectedNode] = useState<any>(null);
  const [selectedEdge, setSelectedEdge] = useState<any>(null);

  const onNodeClick = useCallback((_: React.MouseEvent, node: any) => {
    setSelectedNode(node);
    setSelectedEdge(null);
  }, []);

  const onEdgeClick = useCallback((_: React.MouseEvent, edge: any) => {
    setSelectedEdge(edge);
    setSelectedNode(null);
  }, []);
  
  const closePanel = () => {
    setSelectedNode(null);
    setSelectedEdge(null);
  };

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
      style: { stroke: '#3b82f6', strokeWidth: 2, filter: 'drop-shadow(0 0 5px rgba(59, 130, 246, 0.8))' }
    })));
    setShowDropdown(false);
  };

  const onConnect = useCallback((params: any) => setEdges((eds) => addEdge({...params, animated: true, style: { stroke: '#3b82f6', strokeWidth: 2, filter: 'drop-shadow(0 0 5px rgba(59, 130, 246, 0.8))' }}, eds)), [setEdges]);

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
        position: n.position,
        inputParameters: { ...n.data.inputParameters, _retries: n.data.retries, _timeout: n.data.timeout }
      }));

      const execRes = await axios.post(`${API_BASE}/workflows/execute`, {
        nodes: backendNodes,
        edges: edges.map(e => ({ source: e.source, target: e.target, condition: e.data?.condition }))
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
          style: { stroke: '#3b82f6', strokeWidth: 2, filter: 'drop-shadow(0 0 5px rgba(59, 130, 246, 0.8))' }
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
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', width: '100%' }}>
      <header style={{ 
        padding: '12px 24px', 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        background: '#151515',
        borderBottom: '1px solid #2a2a2a',
        zIndex: 10
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div>
            <h2 style={{ margin: 0, fontSize: '1.1rem', fontWeight: 500, color: '#e2e8f0' }}>Workspace</h2>
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

          <button onClick={handleSaveAndRun} disabled={loading} style={{ 
            background: '#3b82f6', color: '#fff', border: 'none', borderRadius: '6px', padding: '8px 16px', fontSize: '0.85rem', fontWeight: 500, display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer'
          }}>
            <Play size={14} /> {loading ? 'Running...' : 'Run Pipeline'}
          </button>
        </div>
      </header>
      
      <div style={{ flex: 1, display: 'flex' }}>
        {/* Left Palette (Steps) */}
        <div style={{
          width: '260px',
          background: '#111111',
          borderRight: '1px solid #2a2a2a',
          display: 'flex',
          flexDirection: 'column',
          overflowY: 'auto'
        }}>
          <div style={{ padding: '16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #2a2a2a' }}>
            <h3 style={{ margin: 0, color: '#fff', fontSize: '14px', fontWeight: 500 }}>Steps</h3>
            <span style={{ color: '#888' }}>...</span>
          </div>
          
          <div style={{ padding: '16px' }}>
            <h4 style={{ color: '#888', fontSize: '11px', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '12px', fontWeight: 500 }}>Triggers</h4>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '10px' }}>
              <SidebarItem icon={Play} label="Start" type="HTTP" app="Play" />
            </div>
            
            <h4 style={{ color: '#888', fontSize: '11px', textTransform: 'uppercase', letterSpacing: '0.5px', margin: '24px 0 12px', fontWeight: 500 }}>Actions</h4>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              <SidebarItem icon={Database} label="Database" type="HTTP" app="Database" />
              <SidebarItem icon={Globe} label="API" type="HTTP" app="Globe" />
              <SidebarItem icon={Code} label="Script" type="SCRIPT" app="Code" />
              <SidebarItem icon={GitBranch} label="Branch" type="SCRIPT" app="GitBranch" />
              <SidebarItem icon={Bot} label="AI Task" type="SCRIPT" app="Bot" />
              <SidebarItem icon={MessageSquare} label="Slack" type="HTTP" app="MessageSquare" />
            </div>
          </div>
        </div>
        
        {/* Canvas */}
        <div style={{ flex: 1, background: '#181818' }} ref={reactFlowWrapper} onDragOver={onDragOver} onDrop={onDrop}>
          <ReactFlow
            nodes={nodes}
            edges={edges}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            onConnect={onConnect}
            onNodeClick={onNodeClick}
            onEdgeClick={onEdgeClick}
            nodeTypes={nodeTypes}
            colorMode="dark"
            fitView
          >
            <Controls style={{ background: '#222', border: '1px solid #333', fill: '#fff' }} />
            <Background variant={BackgroundVariant.Dots} gap={20} size={2} color="#333" />
          </ReactFlow>
        </div>

        {/* Config Panel */}
        {(selectedNode || selectedEdge) && (
          <div style={{
            width: '320px',
            background: 'rgba(15, 23, 42, 0.95)',
            backdropFilter: 'blur(16px)',
            borderLeft: '1px solid rgba(255,255,255,0.08)',
            padding: '1.25rem',
            display: 'flex',
            flexDirection: 'column',
            overflowY: 'auto'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h3 style={{ margin: 0, fontSize: '1rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Settings size={18} color="#818cf8" />
                {selectedNode ? 'Node Configuration' : 'Edge Configuration'}
              </h3>
              <button className="btn-ghost" onClick={closePanel} style={{ padding: '4px' }}>
                <X size={18} />
              </button>
            </div>

            {selectedNode && (
              <>
                <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '4px', display: 'block' }}>Node Name</label>
                <input 
                  type="text" 
                  value={selectedNode.data.label}
                  onChange={(e) => {
                    setNodes(nds => nds.map(n => n.id === selectedNode.id ? { ...n, data: { ...n.data, label: e.target.value } } : n));
                  }}
                  style={{ width: '100%', padding: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '6px', color: '#fff', marginBottom: '1rem' }}
                />

                <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '4px', display: 'block' }}>Parameters (JSON)</label>
                <textarea 
                  rows={5}
                  value={selectedNode.data.inputParameters ? JSON.stringify(selectedNode.data.inputParameters, null, 2) : '{\n  \n}'}
                  onChange={(e) => {
                    try {
                      const parsed = JSON.parse(e.target.value);
                      setNodes(nds => nds.map(n => n.id === selectedNode.id ? { ...n, data: { ...n.data, inputParameters: parsed } } : n));
                    } catch (err) {
                      // ignore parse errors while typing
                    }
                  }}
                  style={{ width: '100%', padding: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '6px', color: '#fff', fontFamily: 'monospace', fontSize: '0.85rem', marginBottom: '1rem' }}
                />

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
                  <div>
                    <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '4px', display: 'block' }}>Max Retries</label>
                    <input 
                      type="number" 
                      min="0"
                      max="10"
                      value={selectedNode.data.retries || 0}
                      onChange={(e) => {
                        setNodes(nds => nds.map(n => n.id === selectedNode.id ? { ...n, data: { ...n.data, retries: parseInt(e.target.value) } } : n));
                      }}
                      style={{ width: '100%', padding: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '6px', color: '#fff' }}
                    />
                  </div>
                  <div>
                    <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '4px', display: 'block' }}>Timeout (ms)</label>
                    <input 
                      type="number" 
                      min="0"
                      step="1000"
                      value={selectedNode.data.timeout || 30000}
                      onChange={(e) => {
                        setNodes(nds => nds.map(n => n.id === selectedNode.id ? { ...n, data: { ...n.data, timeout: parseInt(e.target.value) } } : n));
                      }}
                      style={{ width: '100%', padding: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '6px', color: '#fff' }}
                    />
                  </div>
                </div>
              </>
            )}

            {selectedEdge && (
              <>
                <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '4px', display: 'block' }}>Branching Condition (JS)</label>
                <textarea 
                  rows={6}
                  placeholder="e.g., output.status === 200"
                  value={selectedEdge.data?.condition || ''}
                  onChange={(e) => {
                    setEdges(eds => eds.map(ed => ed.id === selectedEdge.id ? { ...ed, data: { ...ed.data, condition: e.target.value } } : ed));
                  }}
                  style={{ width: '100%', padding: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '6px', color: '#fff', fontFamily: 'monospace', fontSize: '0.85rem' }}
                />
                <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '8px' }}>
                  If provided, this edge will only be traversed if the JS expression evaluates to true based on the source node's "output" object.
                </p>
              </>
            )}
          </div>
        )}
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
