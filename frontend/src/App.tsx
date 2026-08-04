import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Login } from './components/Login';
import { Dashboard } from './components/Dashboard';
import { WorkflowBuilder } from './components/WorkflowBuilder';
import { ExecutionViewer } from './components/ExecutionViewer';
import { Layout } from './components/Layout';
import { Projects, Integrations, Analyze, SettingsPage } from './components/PlaceholderPages';
import './index.css';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        
        {/* Protected Routes inside Layout */}
        <Route element={<Layout />}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/projects" element={<Projects />} />
          <Route path="/integrations" element={<Integrations />} />
          <Route path="/analyze" element={<Analyze />} />
          <Route path="/builder" element={<WorkflowBuilder />} />
          <Route path="/settings" element={<SettingsPage />} />
          <Route path="/executions/:id" element={<ExecutionViewer />} />
        </Route>
        
        <Route path="/" element={<Navigate to="/login" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
