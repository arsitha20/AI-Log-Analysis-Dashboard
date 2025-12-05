import { useEffect, useState } from "react";
import "./App.css";

const BACKEND_BASE_URL = "http://localhost:8080";

function App() {
  const [serviceName, setServiceName] = useState("OrderService");
  const [rawLogs, setRawLogs] = useState(
    "2025-12-05 10:15:30 ERROR OrderService - Failed to connect to database\n" +
      "2025-12-05 10:16:00 INFO OrderService - Retrying database connection\n" +
      "2025-12-05 10:16:30 ERROR OrderService - Timeout occurred while querying orders"
  );
  const [logs, setLogs] = useState([]);
  const [analysis, setAnalysis] = useState(null);
  const [loadingIngest, setLoadingIngest] = useState(false);
  const [loadingAnalysis, setLoadingAnalysis] = useState(false);
  const [error, setError] = useState("");

  // Fetch logs on first load
  useEffect(() => {
    fetchLogs();
  }, []);

  async function fetchLogs() {
    try {
      const res = await fetch(`${BACKEND_BASE_URL}/api/logs`);
      if (!res.ok) {
        throw new Error("Failed to fetch logs");
      }
      const data = await res.json();
      setLogs(data);
    } catch (err) {
      console.error(err);
      setError("Error fetching logs from backend.");
    }
  }

  async function handleIngest() {
    setError("");
    setAnalysis(null);
    setLoadingIngest(true);
    try {
      const lines = rawLogs
        .split("\n")
        .map((line) => line.trim())
        .filter((line) => line.length > 0);

      if (lines.length === 0) {
        setError("Please enter at least one log line.");
        setLoadingIngest(false);
        return;
      }

      const res = await fetch(`${BACKEND_BASE_URL}/api/logs/ingest`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          serviceName,
          lines,
        }),
      });

      if (!res.ok) {
        throw new Error("Failed to ingest logs");
      }

      await res.json(); // we don't strictly need the response here
      await fetchLogs(); // refresh table
    } catch (err) {
      console.error(err);
      setError("Error ingesting logs.");
    } finally {
      setLoadingIngest(false);
    }
  }

  async function handleAnalyze() {
    setError("");
    setLoadingAnalysis(true);
    try {
      const res = await fetch(`${BACKEND_BASE_URL}/api/analysis`);
      if (!res.ok) {
        throw new Error("Failed to analyze logs");
      }
      const data = await res.json();
      setAnalysis(data);
    } catch (err) {
      console.error(err);
      setError("Error analyzing logs.");
    } finally {
      setLoadingAnalysis(false);
    }
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>AI Log Intelligence Platform</h1>
        <p className="subtitle">
          Ingest application logs, detect patterns, and get AI-style insights.
        </p>
      </header>

      {error && <div className="error-banner">{error}</div>}

      <div className="layout">
        {/* Left panel: log input */}
        <section className="panel panel-left">
          <h2>1. Ingest Logs</h2>
          <label className="field-label">
            Service Name
            <input
              type="text"
              value={serviceName}
              onChange={(e) => setServiceName(e.target.value)}
              className="text-input"
            />
          </label>

          <label className="field-label">
            Paste Log Lines
            <textarea
              value={rawLogs}
              onChange={(e) => setRawLogs(e.target.value)}
              rows={10}
              className="textarea"
              placeholder="Paste logs here..."
            />
          </label>

          <button
            onClick={handleIngest}
            disabled={loadingIngest}
            className="button primary"
          >
            {loadingIngest ? "Ingesting..." : "Ingest Logs"}
          </button>

          <p className="hint">
            Tip: You can simulate logs from multiple services (AuthService,
            PaymentService, etc.).
          </p>
        </section>

        {/* Right panel: analysis */}
        <section className="panel panel-right">
          <div className="analysis-header">
            <h2>2. Analyze Logs</h2>
            <button
              onClick={handleAnalyze}
              disabled={loadingAnalysis}
              className="button secondary"
            >
              {loadingAnalysis ? "Analyzing..." : "Analyze Logs"}
            </button>
          </div>

          {analysis ? (
            <div className="analysis-results">
              <div className="summary-card">
                <h3>Overall Summary</h3>
                <p>{analysis.overallSummary}</p>
              </div>

              <h3>Error / Issue Clusters</h3>
              <div className="cluster-grid">
                {analysis.clusters?.map((cluster, idx) => (
                  <div key={idx} className="cluster-card">
                    <h4>{cluster.pattern}</h4>
                    <p className="cluster-count">
                      Count: <strong>{cluster.count}</strong>
                    </p>
                    <p className="cluster-explanation">
                      <strong>Explanation: </strong>
                      {cluster.explanation}
                    </p>
                    <p className="cluster-fix">
                      <strong>Suggested Fix: </strong>
                      {cluster.suggestedFix}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <p className="hint">
              Run an analysis to see clusters, explanations, and suggested
              fixes.
            </p>
          )}
        </section>
      </div>

      {/* Logs table */}
      <section className="panel panel-full">
        <h2>3. Stored Logs</h2>
        {logs.length === 0 ? (
          <p className="hint">No logs stored yet. Ingest some logs above.</p>
        ) : (
          <div className="table-wrapper">
            <table className="logs-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Timestamp</th>
                  <th>Level</th>
                  <th>Service</th>
                  <th>Message</th>
                </tr>
              </thead>
              <tbody>
                {logs.map((log) => (
                  <tr key={log.id}>
                    <td>{log.id}</td>
                    <td>{log.timestamp}</td>
                    <td>{log.level}</td>
                    <td>{log.serviceName}</td>
                    <td>{log.message}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}

export default App;
