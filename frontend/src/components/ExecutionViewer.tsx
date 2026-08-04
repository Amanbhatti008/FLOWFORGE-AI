import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import {
  ReactFlow,
  Background,
  BackgroundVariant,
  useNodesState,
  useEdgesState,
  Handle,
  Position
} from '@xyflow/react';
import type { Node, Edge } from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { ArrowLeft, CheckCircle2, Clock, XCircle, PlayCircle, Loader2, Code, Globe, Mail, Bot, Database, MessageSquare, RotateCcw, PartyPopper, Sparkles } from 'lucide-react';
import { Client } from '@stomp/stompjs';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

// Map taskRefName to a friendly label using the DAG definition
let taskNameMap: Record<string, string> = {};
let taskAppMap: Record<string, string> = {};

// Custom Node for Execution Viewer
const ExecutionNode = ({ data }: any) => {
  const isScript = data.label?.includes('Script') || data.type === 'SCRIPT';
  
  let borderColor = 'rgba(255, 255, 255, 0.1)';
  let glowColor = 'transparent';
  let iconBg = 'rgba(255,255,255,0.05)';
  
  if (data.status === 'SUCCESS') {
    borderColor = '#10b981';
    glowColor = 'rgba(16, 185, 129, 0.3)';
    iconBg = 'rgba(16, 185, 129, 0.2)';
  } else if (data.status === 'FAILED') {
    borderColor = '#ef4444';
    glowColor = 'rgba(239, 68, 68, 0.3)';
    iconBg = 'rgba(239, 68, 68, 0.2)';
  } else if (data.status === 'RUNNING') {
    borderColor = '#f59e0b';
    glowColor = 'rgba(245, 158, 11, 0.3)';
    iconBg = 'rgba(245, 158, 11, 0.2)';
  } else if (data.status === 'QUEUED') {
    borderColor = '#3b82f6';
    glowColor = 'rgba(59, 130, 246, 0.3)';
    iconBg = 'rgba(59, 130, 246, 0.2)';
  }

  const getIcon = () => {
    if (data.status === 'SUCCESS') return <CheckCircle2 size={20} color="#10b981" />;
    if (data.status === 'FAILED') return <XCircle size={20} color="#ef4444" />;
    if (data.status === 'RUNNING') return <Loader2 size={20} color="#f59e0b" className="animate-spin" />;
    if (data.app === 'Mail') return <Mail size={20} color="#94a3b8" />;
    if (data.app === 'Bot') return <Bot size={20} color="#94a3b8" />;
    if (data.app === 'Database') return <Database size={20} color="#94a3b8" />;
    if (data.app === 'MessageSquare') return <MessageSquare size={20} color="#94a3b8" />;
    if (isScript) return <Code size={20} color="#94a3b8" />;
    return <Globe size={20} color="#94a3b8" />;
  };

  return (
    <div className={data.status === 'RUNNING' ? 'node-running-pulse' : ''} style={{
      background: 'rgba(16, 23, 42, 0.9)',
      backdropFilter: 'blur(12px)',
      border: `2px solid ${borderColor}`,
      borderRadius: '12px',
      padding: '1rem',
      minWidth: '220px',
      boxShadow: `0 8px 32px ${glowColor}`,
      display: 'flex',
      alignItems: 'center',
      gap: '1rem',
      color: '#f8fafc',
      transition: 'all 0.5s ease'
    }}>
      <Handle type="target" position={Position.Top} style={{ background: '#94a3b8', border: 'none', width: '8px', height: '8px' }} />
      <div style={{
        background: iconBg,
        padding: '0.5rem',
        borderRadius: '8px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center'
      }}>
        {getIcon()}
      </div>
      <div>
        <div style={{ fontSize: '0.7rem', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '1px' }}>
          {data.status || 'PENDING'}
        </div>
        <div style={{ fontWeight: 600, marginTop: '2px', fontSize: '0.9rem' }}>{data.label}</div>
      </div>
      <Handle type="source" position={Position.Bottom} style={{ background: '#94a3b8', border: 'none', width: '8px', height: '8px' }} />
    </div>
  );
};

const nodeTypes = {
  custom: ExecutionNode
};

export const ExecutionViewer: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [execution, setExecution] = useState<any>(null);
  const [workflow, setWorkflow] = useState<any>(null);
  const [showCelebration, setShowCelebration] = useState(false);
  const [prevStatus, setPrevStatus] = useState<string>('');
  
  const [nodes, setNodes, onNodesChange] = useNodesState<Node>([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState<Edge>([]);

  const fetchExecution = useCallback(async () => {
    try {
      const token = localStorage.getItem('flowforge_token');
      const res = await axios.get(`${API_BASE}/executions/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      const execData = res.data.data;
      setExecution(execData);

      // Trigger celebration when execution completes
      if (execData.status === 'COMPLETED' && prevStatus === 'RUNNING') {
        setShowCelebration(true);
        setTimeout(() => setShowCelebration(false), 4000);
      }
      setPrevStatus(execData.status);

      if (!workflow) {
        const wfRes = await axios.get(`${API_BASE}/workflows/${execData.workflowId}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        const wfData = wfRes.data.data;
        setWorkflow(wfData);

        const version = (wfData.versions || []).find((v: any) => v.versionNumber === execData.versionNumber);
        if (version) {
          const def = JSON.parse(version.definitionJson);
          
          // Build name map from definition
          def.nodes.forEach((n: any) => {
            taskNameMap[n.id] = n.name;
            taskAppMap[n.id] = n.app;
          });

          const initialEdges = def.edges.map((e: any, i: number) => ({
            id: `e-${i}`,
            source: e.source,
            target: e.target,
            animated: true,
            style: { stroke: 'rgba(255,255,255,0.2)', strokeWidth: 2 }
          }));
          setEdges(initialEdges);

          const initialNodes = def.nodes.map((n: any, i: number) => ({
            id: n.id,
            type: 'custom',
            position: n.position || { x: 250, y: i * 150 + 50 },
            data: { label: n.name, type: n.type, app: n.app, status: 'PENDING' }
          }));
          setNodes(initialNodes);
        }
      }

      if (execData && execData.tasks) {
        setNodes((nds) => 
          nds.map((node) => {
            const task = (execData.tasks || []).find((t: any) => t.taskRefName === node.id);
            if (task) {
              return {
                ...node,
                data: { ...node.data, status: task.status }
              };
            }
            return node;
          })
        );
        
        // Update edge styles based on status
        setEdges((eds) => 
          eds.map((edge) => {
            const sourceTask = (execData.tasks || []).find((t: any) => t.taskRefName === edge.source);
            const targetTask = (execData.tasks || []).find((t: any) => t.taskRefName === edge.target);
            
            if (sourceTask && sourceTask.status === 'SUCCESS' && (!targetTask || targetTask.status !== 'RUNNING')) {
              return {
                ...edge,
                style: { stroke: '#10b981', strokeWidth: 3 },
                animated: false,
                className: ''
              };
            } else if (targetTask && (targetTask.status === 'RUNNING' || targetTask.status === 'QUEUED')) {
              return {
                ...edge,
                className: 'animated-data-flow animated-data-packet',
                animated: false,
                style: {}
              };
            }
            return { ...edge, className: '', animated: false };
          })
        );
      }
    } catch (err) {
      console.error("Error fetching execution", err);
    }
  }, [id, workflow, setEdges, setNodes, prevStatus]);

  useEffect(() => {
    fetchExecution();
    const interval = setInterval(() => {
      fetchExecution();
    }, 2000);
    return () => clearInterval(interval);
  }, [fetchExecution]);

  // STOMP WebSocket Client
  useEffect(() => {
    if (!id) return;
    const client = new Client({
      brokerURL: import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws',
      debug: function (str) {
        console.log(str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = function (frame) {
      console.log('STOMP Connected: ' + frame);
      client.subscribe(`/topic/executions/${id}`, (message) => {
        if (message.body) {
          console.log('WS Message:', message.body);
          // Immediate fetch to get full state including potential AI diagnoses
          fetchExecution(); 
        }
      });
    };

    client.onStompError = function (frame) {
      console.error('STOMP Error:', frame.headers['message']);
    };

    client.activate();

    return () => {
      client.deactivate();
    };
  }, [id, fetchExecution]);

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'SUCCESS': return <CheckCircle2 color="#10b981" size={16} />;
      case 'FAILED': return <XCircle color="#ef4444" size={16} />;
      case 'RUNNING': return <Loader2 color="#f59e0b" size={16} className="animate-spin" />;
      case 'SCHEDULED':
      case 'QUEUED': return <Clock color="#3b82f6" size={16} />;
      default: return <PlayCircle color="#64748b" size={16} />;
    }
  };

  const getDuration = (task: any) => {
    if (!task.startedAt) return null;
    const end = task.completedAt ? new Date(task.completedAt).getTime() : Date.now();
    const start = new Date(task.startedAt).getTime();
    const ms = end - start;
    if (ms < 1000) return `${ms}ms`;
    return `${(ms / 1000).toFixed(1)}s`;
  };

  if (!execution) {
    return <div className="flex-center">
      <Loader2 className="animate-spin" size={48} color="var(--accent-primary)" />
    </div>;
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      {/* Celebration Overlay */}
      {showCelebration && (
        <div style={{
          position: 'fixed', inset: 0, zIndex: 100,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          background: 'rgba(0,0,0,0.5)', backdropFilter: 'blur(8px)',
          animation: 'slideUpFade 0.5s ease'
        }}>
          <div style={{
            background: 'rgba(16, 23, 42, 0.95)',
            border: '2px solid rgba(16, 185, 129, 0.5)',
            borderRadius: '24px',
            padding: '3rem 4rem',
            textAlign: 'center',
            boxShadow: '0 0 60px rgba(16, 185, 129, 0.3)'
          }}>
            <PartyPopper size={56} color="#34d399" style={{ marginBottom: '1rem' }} />
            <h2 style={{ fontSize: '2rem', margin: '0.5rem 0', fontFamily: 'Outfit' }}>
              Pipeline Complete! 🎉
            </h2>
            <p style={{ color: 'var(--text-secondary)', margin: 0 }}>
              All {execution.tasks?.length || 0} tasks executed successfully
            </p>
          </div>
        </div>
      )}

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
            <h2 style={{ margin: 0, fontFamily: 'Outfit', fontSize: '1.25rem' }}>Execution Tracker</h2>
            <p style={{ margin: 0, fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              {execution.workflowName || 'Workflow'} • v{execution.versionNumber}
            </p>
          </div>
          <span className={`badge badge-${execution.status === 'COMPLETED' ? 'success' : execution.status === 'FAILED' ? 'error' : 'running'}`}>
            {execution.status}
          </span>
        </div>
      </header>

      <div style={{ display: 'flex', flex: 1, overflow: 'hidden' }}>
        <div style={{ flex: 1, position: 'relative' }}>
          <ReactFlow
            nodes={nodes}
            edges={edges}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            nodeTypes={nodeTypes}
            colorMode="dark"
            fitView
            nodesDraggable={false}
            nodesConnectable={false}
          >
            <Background variant={BackgroundVariant.Dots} gap={16} size={1} color="rgba(255,255,255,0.1)" />
          </ReactFlow>
        </div>

        <div style={{ 
          width: '420px', 
          background: 'rgba(15, 23, 42, 0.5)', 
          borderLeft: '1px solid rgba(255,255,255,0.1)',
          backdropFilter: 'blur(16px)',
          display: 'flex',
          flexDirection: 'column',
          zIndex: 5
        }}>
          <div style={{ padding: '1.5rem', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
            <h3 style={{ margin: 0, fontFamily: 'Outfit', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span className="live-dot"></span> Live Telemetry
            </h3>
            <p style={{ margin: '0.5rem 0 0 0', fontSize: '0.8rem', color: 'var(--text-muted)', fontFamily: "'JetBrains Mono', monospace" }}>
              {id?.substring(0, 12)}…
            </p>
          </div>
          
          <div style={{ flex: 1, overflowY: 'auto', padding: '1.5rem' }}>
            {execution.tasks && [...execution.tasks].sort((a: any, b: any) => {
              // Sort: RUNNING first, then SUCCESS, then others
              const order: Record<string, number> = { RUNNING: 0, SUCCESS: 1, QUEUED: 2, SCHEDULED: 3, PENDING: 4, FAILED: 5 };
              return (order[a.status] ?? 9) - (order[b.status] ?? 9);
            }).map((task: any, idx: number) => (
              <div key={task.id} className="stagger-item" style={{ 
                display: 'flex', gap: '1rem', marginBottom: '1rem', position: 'relative'
              }}>
                {idx < (execution.tasks?.length || 0) - 1 && (
                  <div style={{ position: 'absolute', left: '7px', top: '24px', bottom: '-16px', width: '2px', background: 'rgba(255,255,255,0.06)' }} />
                )}
                
                <div style={{ zIndex: 1, flexShrink: 0, paddingTop: '2px' }}>
                  {getStatusIcon(task.status)}
                </div>
                
                <div style={{ flex: 1, background: 'rgba(0,0,0,0.25)', padding: '1rem', borderRadius: '10px', border: '1px solid rgba(255,255,255,0.06)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.5rem' }}>
                    <div>
                      <strong style={{ fontSize: '0.95rem', display: 'block' }}>
                        {taskNameMap[task.taskRefName] || task.taskRefName}
                      </strong>
                    </div>
                    <span style={{ 
                      fontSize: '0.65rem', background: 'rgba(255,255,255,0.08)', 
                      padding: '2px 8px', borderRadius: '10px', color: 'var(--text-muted)',
                      flexShrink: 0
                    }}>
                      {task.type}
                    </span>
                  </div>
                  
                  <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
                    <span style={{ fontSize: '0.8rem', color: task.status === 'SUCCESS' ? '#34d399' : task.status === 'RUNNING' ? '#fbbf24' : task.status === 'FAILED' ? '#f87171' : 'var(--text-secondary)', fontWeight: 600 }}>
                      {task.status}
                    </span>
                    {getDuration(task) && (
                      <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                        ⏱ {getDuration(task)}
                      </span>
                    )}
                    {task.retryCount > 0 && (
                      <span style={{ fontSize: '0.8rem', color: '#fbbf24', display: 'flex', alignItems: 'center', gap: '3px' }}>
                        <RotateCcw size={11} /> {task.retryCount} retries
                      </span>
                    )}
                  </div>

                  {task.error && (
                    <div style={{ marginTop: '0.5rem', fontSize: '0.8rem', color: '#f87171', background: 'rgba(239,68,68,0.1)', padding: '0.5rem', borderRadius: '6px', border: '1px solid rgba(239,68,68,0.2)' }}>
                      {task.error}
                    </div>
                  )}
                  {task.aiDiagnosis && (
                    <div style={{ 
                      marginTop: '0.75rem', fontSize: '0.8rem', color: '#e2e8f0', 
                      background: 'linear-gradient(145deg, rgba(147, 51, 234, 0.1) 0%, rgba(79, 70, 229, 0.1) 100%)', 
                      padding: '0.75rem', borderRadius: '8px', 
                      border: '1px solid rgba(147, 51, 234, 0.3)',
                      boxShadow: '0 4px 12px rgba(147, 51, 234, 0.05)'
                    }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '4px', color: '#c084fc', fontWeight: 600 }}>
                        <Sparkles size={14} /> AI RCA Diagnosis
                      </div>
                      <div style={{ lineHeight: 1.4 }}>
                        {task.aiDiagnosis}
                      </div>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
