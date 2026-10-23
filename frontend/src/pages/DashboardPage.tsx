import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { dashboardApi } from '../api/client';
import { Claim, ClaimsByStatus, DashboardSummary, RiskDistribution } from '../types';
import { StatusBadge } from '../components/common/StatusBadge';
import { RiskBadge } from '../components/common/RiskBadge';
import {
  FileText,
  Clock,
  ShieldAlert,
  CheckCircle,
  DollarSign,
  AlertTriangle,
  ArrowRight,
  TrendingUp,
  RefreshCw,
} from 'lucide-react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Cell,
  PieChart,
  Pie,
  Legend,
} from 'recharts';

export const DashboardPage: React.FC = () => {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [statusData, setStatusData] = useState<ClaimsByStatus | null>(null);
  const [riskData, setRiskData] = useState<RiskDistribution | null>(null);
  const [recentClaims, setRecentClaims] = useState<Claim[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchDashboardData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [sumRes, statRes, riskRes, recRes] = await Promise.all([
        dashboardApi.getSummary(),
        dashboardApi.getClaimsByStatus(),
        dashboardApi.getRiskDistribution(),
        dashboardApi.getRecentClaims(8),
      ]);
      setSummary(sumRes);
      setStatusData(statRes);
      setRiskData(riskRes);
      setRecentClaims(recRes);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch dashboard metrics.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  // Format currency
  const formatCurrency = (val?: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      maximumFractionDigits: 0,
    }).format(val || 0);
  };

  // Prepare chart datasets
  const statusBarChartData = statusData?.statusCounts
    ? Object.entries(statusData.statusCounts).map(([status, count]) => ({
        status: status.replace('_', ' '),
        rawStatus: status,
        count,
      }))
    : [];

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'CLEARED':
        return '#10b981';
      case 'CONFIRMED_FRAUD':
        return '#0f172a';
      case 'FLAGGED':
        return '#f43f5e';
      case 'UNDER_REVIEW':
        return '#6366f1';
      case 'SUBMITTED':
      default:
        return '#3b82f6';
    }
  };

  const riskPieChartData = riskData?.riskDistribution
    ? Object.entries(riskData.riskDistribution).map(([level, count]) => ({
        name: `${level} Risk`,
        value: count,
        level,
      }))
    : [];

  const RISK_COLORS: Record<string, string> = {
    LOW: '#10b981',
    MEDIUM: '#f59e0b',
    HIGH: '#ef4444',
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Executive Dashboard</h1>
          <p className="text-sm text-slate-500 mt-0.5">
            Real-time fraud surveillance, claims pipeline velocity, and review status
          </p>
        </div>
        <div className="flex items-center gap-2.5">
          <button
            onClick={fetchDashboardData}
            disabled={loading}
            className="btn-secondary text-xs py-2 px-3 inline-flex items-center gap-1.5"
            title="Refresh metrics"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>
          <Link to="/claims/new" className="btn-primary text-xs py-2 px-3.5 inline-flex items-center gap-1.5 shadow-sm">
            <span>+ New Claim</span>
          </Link>
        </div>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-red-50 border border-red-200 text-red-800 text-sm flex items-center gap-3">
          <AlertTriangle className="w-5 h-5 text-red-500 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Top Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Total Claims */}
        <div className="card p-5 border-l-4 border-l-blue-600 flex items-center justify-between">
          <div>
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider block">
              Total Claims
            </span>
            <div className="text-2xl font-bold text-slate-900 mt-1">
              {loading ? '...' : summary?.totalClaims.toLocaleString()}
            </div>
            <span className="text-[11px] text-slate-400 mt-0.5 block">
              Volume: {formatCurrency(summary?.totalClaimedAmount)}
            </span>
          </div>
          <div className="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center shrink-0">
            <FileText className="w-6 h-6" />
          </div>
        </div>

        {/* Awaiting Review */}
        <div className="card p-5 border-l-4 border-l-indigo-500 flex items-center justify-between">
          <div>
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider block">
              Awaiting Review
            </span>
            <div className="text-2xl font-bold text-indigo-950 mt-1">
              {loading ? '...' : summary?.awaitingReview.toLocaleString()}
            </div>
            <span className="text-[11px] text-indigo-500/80 mt-0.5 block">
              Submitted + Under Review
            </span>
          </div>
          <div className="w-12 h-12 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center shrink-0">
            <Clock className="w-6 h-6" />
          </div>
        </div>

        {/* High Risk Claims */}
        <div className="card p-5 border-l-4 border-l-rose-500 flex items-center justify-between">
          <div>
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider block">
              High Risk Alerts
            </span>
            <div className="text-2xl font-bold text-rose-950 mt-1">
              {loading ? '...' : summary?.highRiskClaims.toLocaleString()}
            </div>
            <span className="text-[11px] text-rose-500/80 mt-0.5 block">
              Risk Score ≥ 70 / Flagged
            </span>
          </div>
          <div className="w-12 h-12 rounded-xl bg-rose-50 text-rose-600 flex items-center justify-center shrink-0">
            <ShieldAlert className="w-6 h-6" />
          </div>
        </div>

        {/* Confirmed Fraud Stopped */}
        <div className="card p-5 border-l-4 border-l-slate-900 flex items-center justify-between">
          <div>
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider block">
              Fraud Prevented
            </span>
            <div className="text-2xl font-bold text-slate-900 mt-1">
              {loading ? '...' : formatCurrency(summary?.confirmedFraudAmount)}
            </div>
            <span className="text-[11px] text-emerald-600 font-medium mt-0.5 block">
              Cleared: {formatCurrency(summary?.clearedAmount)}
            </span>
          </div>
          <div className="w-12 h-12 rounded-xl bg-slate-900 text-white flex items-center justify-center shrink-0">
            <DollarSign className="w-6 h-6" />
          </div>
        </div>
      </div>

      {/* Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Claims By Status - Bar Chart */}
        <div className="card p-6 lg:col-span-2">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-base font-bold text-slate-900">Claims Distribution by Status</h2>
              <p className="text-xs text-slate-500">Live counts across workflow lifecycle phases</p>
            </div>
            <span className="text-xs font-medium text-slate-400">PostgreSQL Aggregation</span>
          </div>

          <div className="h-64 w-full">
            {loading ? (
              <div className="h-full flex items-center justify-center text-slate-400 text-sm">
                Loading analytics...
              </div>
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={statusBarChartData} margin={{ top: 10, right: 10, left: -20, bottom: 5 }}>
                  <XAxis dataKey="status" tick={{ fontSize: 11 }} />
                  <YAxis allowDecimals={false} tick={{ fontSize: 11 }} />
                  <Tooltip
                    cursor={{ fill: 'rgba(241, 245, 249, 0.6)' }}
                    contentStyle={{ borderRadius: '8px', border: '1px solid #e2e8f0', fontSize: '12px' }}
                  />
                  <Bar dataKey="count" radius={[6, 6, 0, 0]}>
                    {statusBarChartData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={getStatusColor(entry.rawStatus)} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>

        {/* Risk Distribution - Donut Chart */}
        <div className="card p-6">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-base font-bold text-slate-900">Risk Severity Tiers</h2>
              <p className="text-xs text-slate-500">Breakdown by rule engine score</p>
            </div>
            <TrendingUp className="w-4 h-4 text-slate-400" />
          </div>

          <div className="h-64 w-full flex items-center justify-center">
            {loading ? (
              <div className="text-slate-400 text-sm">Loading risk breakdown...</div>
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={riskPieChartData}
                    cx="50%"
                    cy="50%"
                    innerRadius={55}
                    outerRadius={80}
                    paddingAngle={4}
                    dataKey="value"
                  >
                    {riskPieChartData.map((entry, index) => (
                      <Cell key={`cell-risk-${index}`} fill={RISK_COLORS[entry.level] || '#94a3b8'} />
                    ))}
                  </Pie>
                  <Tooltip
                    contentStyle={{ borderRadius: '8px', border: '1px solid #e2e8f0', fontSize: '12px' }}
                  />
                  <Legend verticalAlign="bottom" height={36} iconType="circle" wrapperStyle={{ fontSize: '12px' }} />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>
      </div>

      {/* Recent Claims Feed */}
      <div className="card overflow-hidden">
        <div className="p-5 border-b border-slate-100 flex items-center justify-between">
          <div>
            <h2 className="text-base font-bold text-slate-900">Recently Submitted Claims</h2>
            <p className="text-xs text-slate-500">Latest submissions awaiting triage and review</p>
          </div>
          <Link
            to="/claims"
            className="text-xs font-semibold text-blue-600 hover:text-blue-800 flex items-center gap-1 group"
          >
            <span>View All Claims</span>
            <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-0.5 transition-transform" />
          </Link>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-slate-600">
            <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-200 uppercase tracking-wider">
              <tr>
                <th className="py-3 px-4">Claim Ref</th>
                <th className="py-3 px-4">Claimant</th>
                <th className="py-3 px-4">Category</th>
                <th className="py-3 px-4">Amount</th>
                <th className="py-3 px-4">Submission Date</th>
                <th className="py-3 px-4">Risk Level</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {recentClaims.length === 0 ? (
                <tr>
                  <td colSpan={8} className="py-8 text-center text-slate-400">
                    No recent claims found.
                  </td>
                </tr>
              ) : (
                recentClaims.map((c) => (
                  <tr key={c.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-3 px-4 font-mono font-bold text-blue-600">
                      <Link to={`/claims/${c.id}`} className="hover:underline">
                        {c.claimReference}
                      </Link>
                    </td>
                    <td className="py-3 px-4 font-mono text-slate-700">{c.claimantId}</td>
                    <td className="py-3 px-4">
                      <span className="px-2 py-0.5 rounded bg-slate-100 text-slate-700 font-medium text-[11px]">
                        {c.category}
                      </span>
                    </td>
                    <td className="py-3 px-4 font-semibold text-slate-900">
                      {formatCurrency(c.amount)}
                    </td>
                    <td className="py-3 px-4 text-slate-500">{c.submissionDate}</td>
                    <td className="py-3 px-4">
                      <RiskBadge score={c.riskScore} level={c.riskLevel} />
                    </td>
                    <td className="py-3 px-4">
                      <StatusBadge status={c.status} />
                    </td>
                    <td className="py-3 px-4 text-right">
                      <Link
                        to={`/claims/${c.id}`}
                        className="text-blue-600 hover:text-blue-800 font-semibold text-xs inline-flex items-center gap-1"
                      >
                        <span>Inspect</span>
                        <ArrowRight className="w-3 h-3" />
                      </Link>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
