import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { claimsApi } from '../api/client';
import { BenefitCategory, ClaimDetail, RiskAssessment } from '../types';
import { StatusBadge } from '../components/common/StatusBadge';
import { RiskBadge } from '../components/common/RiskBadge';
import {
  ShieldAlert,
  Clock,
  History,
  Edit3,
  RefreshCw,
  ArrowLeft,
  CheckCircle2,
  AlertTriangle,
  FileSearch,
  Calendar,
  DollarSign,
  User,
  Tag,
} from 'lucide-react';

export const ClaimDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [claim, setClaim] = useState<ClaimDetail | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [analyzing, setAnalyzing] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Edit modal state
  const [isEditing, setIsEditing] = useState<boolean>(false);
  const [editCategory, setEditCategory] = useState<BenefitCategory>('HEALTHCARE');
  const [editAmount, setEditAmount] = useState<string>('');
  const [editDescription, setEditDescription] = useState<string>('');
  const [editLoading, setEditLoading] = useState<boolean>(false);

  const fetchClaim = async () => {
    if (!id) return;
    setLoading(true);
    setError(null);
    try {
      const res = await claimsApi.getClaimById(Number(id));
      setClaim(res);
      setEditCategory(res.category);
      setEditAmount(res.amount.toString());
      setEditDescription(res.description || '');
    } catch (err: any) {
      setError(err.message || 'Failed to load claim details.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchClaim();
  }, [id]);

  const handleReanalyze = async () => {
    if (!id) return;
    setAnalyzing(true);
    try {
      await claimsApi.analyzeClaim(Number(id));
      await fetchClaim();
    } catch (err: any) {
      setError(err.message || 'Failed to re-analyze risk.');
    } finally {
      setAnalyzing(false);
    }
  };

  const handleSaveEdit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!id) return;
    setEditLoading(true);
    try {
      const updated = await claimsApi.updateClaim(Number(id), {
        category: editCategory,
        amount: parseFloat(editAmount),
        description: editDescription,
      });
      setClaim(updated);
      setIsEditing(false);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to update claim.');
    } finally {
      setEditLoading(false);
    }
  };

  const formatCurrency = (val?: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
    }).format(val || 0);
  };

  const formatDate = (isoString?: string) => {
    if (!isoString) return '—';
    return new Date(isoString).toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading) {
    return (
      <div className="py-20 text-center text-slate-400 text-sm">
        <RefreshCw className="w-6 h-6 animate-spin mx-auto mb-2 text-blue-600" />
        <span>Loading claim file and risk dossier...</span>
      </div>
    );
  }

  if (!claim) {
    return (
      <div className="card p-8 text-center text-slate-500">
        <AlertTriangle className="w-8 h-8 text-amber-500 mx-auto mb-2" />
        <h2 className="text-base font-bold text-slate-800">Claim Record Not Found</h2>
        <p className="text-xs text-slate-400 mt-1">The requested claim identifier does not exist.</p>
        <Link to="/claims" className="btn-secondary text-xs mt-4 inline-block">
          Return to Claims
        </Link>
      </div>
    );
  }

  const isEligibleForEdit = claim.status === 'SUBMITTED' || claim.status === 'UNDER_REVIEW';

  return (
    <div className="space-y-6">
      {/* Back button & Title Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div className="flex items-center gap-3">
          <Link
            to="/claims"
            className="p-2 rounded-lg bg-white border border-slate-200 text-slate-600 hover:bg-slate-50 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
          </Link>
          <div>
            <div className="flex items-center gap-2.5">
              <h1 className="text-2xl font-bold text-slate-900 font-mono tracking-tight">
                {claim.claimReference}
              </h1>
              <StatusBadge status={claim.status} />
              <RiskBadge score={claim.riskScore} level={claim.riskLevel} />
            </div>
            <p className="text-xs text-slate-500 mt-0.5">
              Claimant: <span className="font-mono font-semibold text-slate-700">{claim.claimantId}</span> • Ingested on{' '}
              {formatDate(claim.createdAt)}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2.5">
          {isEligibleForEdit && (
            <button
              onClick={() => setIsEditing(true)}
              className="btn-secondary text-xs py-2 px-3 gap-1.5"
            >
              <Edit3 className="w-3.5 h-3.5" />
              <span>Edit Claim</span>
            </button>
          )}

          <button
            onClick={handleReanalyze}
            disabled={analyzing}
            className="btn-secondary text-xs py-2 px-3 gap-1.5"
            title="Recalculate risk score against current rules"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${analyzing ? 'animate-spin' : ''}`} />
            <span>Re-Analyze</span>
          </button>

          <Link
            to={`/claims/${claim.id}/investigate`}
            className="btn-primary text-xs py-2 px-3.5 gap-1.5 shadow-sm"
          >
            <FileSearch className="w-3.5 h-3.5" />
            <span>Investigation Workspace</span>
          </Link>
        </div>
      </div>

      {error && (
        <div className="p-3.5 rounded-lg bg-red-50 border border-red-200 text-red-700 text-xs">
          {error}
        </div>
      )}

      {/* Main Grid: Left Column Details & Risk Breakdown, Right Column Timeline */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: Claim Snapshot & Risk Assessment Engine */}
        <div className="lg:col-span-2 space-y-6">
          {/* Claim Snapshot Card */}
          <div className="card p-6">
            <h2 className="text-sm font-bold text-slate-900 uppercase tracking-wider mb-4 border-b border-slate-100 pb-2">
              Claim Summary
            </h2>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-xs">
              <div className="space-y-1">
                <span className="text-slate-400 block font-medium">Benefit Category</span>
                <span className="font-semibold text-slate-800 text-sm">{claim.category}</span>
              </div>
              <div className="space-y-1">
                <span className="text-slate-400 block font-medium">Claim Amount</span>
                <span className="font-bold text-slate-900 text-sm">{formatCurrency(claim.amount)}</span>
              </div>
              <div className="space-y-1">
                <span className="text-slate-400 block font-medium">Submission Date</span>
                <span className="font-semibold text-slate-800">{claim.submissionDate}</span>
              </div>
              <div className="space-y-1">
                <span className="text-slate-400 block font-medium">Last Modified</span>
                <span className="font-semibold text-slate-800">{formatDate(claim.updatedAt)}</span>
              </div>
            </div>

            {claim.description && (
              <div className="mt-4 pt-4 border-t border-slate-100 text-xs">
                <span className="text-slate-400 block font-medium mb-1">Claim Notes / Rationale</span>
                <p className="text-slate-700 bg-slate-50 p-3 rounded-lg border border-slate-200/60 leading-relaxed">
                  {claim.description}
                </p>
              </div>
            )}
          </div>

          {/* Explainable Risk Assessment Breakdown Card */}
          <div className="card p-6 border-l-4 border-l-blue-600">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-6">
              <div>
                <h2 className="text-base font-bold text-slate-900">Explainable Risk Engine Dossier</h2>
                <p className="text-xs text-slate-500">
                  Automated deterministic business rules and anomaly contribution metrics
                </p>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-xs font-semibold text-slate-400">Total Score:</span>
                <span className="text-2xl font-extrabold font-mono text-slate-900">
                  {claim.riskScore}/100
                </span>
                <RiskBadge score={claim.riskScore} level={claim.riskLevel} />
              </div>
            </div>

            {/* Score Progress Bar */}
            <div className="mb-6">
              <div className="flex justify-between text-[11px] font-semibold text-slate-500 mb-1.5">
                <span>Low Risk (0–29)</span>
                <span>Medium Risk (30–69)</span>
                <span>High Risk (70–100)</span>
              </div>
              <div className="w-full h-3 rounded-full bg-slate-100 overflow-hidden flex">
                <div
                  className={`h-full transition-all duration-500 ${
                    claim.riskLevel === 'HIGH'
                      ? 'bg-rose-500'
                      : claim.riskLevel === 'MEDIUM'
                      ? 'bg-amber-500'
                      : 'bg-emerald-500'
                  }`}
                  style={{ width: `${claim.riskScore}%` }}
                />
              </div>
            </div>

            {/* Rule by Rule Evaluation Cards */}
            <div className="space-y-3">
              <h3 className="text-xs font-bold text-slate-700 uppercase tracking-wider">
                Individual Rule Evaluations
              </h3>

              {claim.latestRiskAssessment?.ruleResults.map((r, idx) => (
                <div
                  key={idx}
                  className={`p-4 rounded-xl border transition-colors ${
                    r.triggered
                      ? 'bg-rose-50/70 border-rose-200 text-rose-950'
                      : 'bg-slate-50/70 border-slate-200 text-slate-800'
                  }`}
                >
                  <div className="flex items-start justify-between gap-4">
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-xs">{r.ruleName}</span>
                        <span className="text-[10px] font-mono opacity-70">[{r.ruleCode}]</span>
                      </div>
                      <p className="text-xs leading-relaxed opacity-90">{r.explanation}</p>
                    </div>

                    <div className="shrink-0 text-right">
                      {r.triggered ? (
                        <span className="inline-block px-2 py-0.5 rounded bg-rose-200/80 text-rose-900 font-bold text-xs">
                          +{r.scoreContribution} pts
                        </span>
                      ) : (
                        <span className="inline-block px-2 py-0.5 rounded bg-emerald-100 text-emerald-800 font-medium text-xs">
                          Passed (0)
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right Col: Complete Status History Timeline */}
        <div className="space-y-6">
          <div className="card p-6">
            <div className="flex items-center justify-between mb-4 border-b border-slate-100 pb-2">
              <div className="flex items-center gap-2">
                <History className="w-4 h-4 text-blue-600" />
                <h2 className="text-sm font-bold text-slate-900 uppercase tracking-wider">
                  Audit & Status Timeline
                </h2>
              </div>
              <span className="text-[11px] text-slate-400">
                {claim.statusHistory?.length || 0} events
              </span>
            </div>

            {(!claim.statusHistory || claim.statusHistory.length === 0) ? (
              <p className="text-xs text-slate-400 text-center py-6">No previous status changes recorded.</p>
            ) : (
              <div className="relative pl-6 space-y-6 before:content-[''] before:absolute before:left-2.5 before:top-2 before:bottom-2 before:w-0.5 before:bg-slate-200">
                {claim.statusHistory.map((h, idx) => (
                  <div key={idx} className="relative text-xs">
                    {/* Circle marker */}
                    <div className="absolute -left-[1.65rem] top-1 w-3 h-3 rounded-full bg-blue-600 ring-4 ring-white" />

                    <div className="space-y-1">
                      <div className="flex items-center gap-1.5 flex-wrap">
                        <span className="font-semibold text-slate-700">{h.fromStatus}</span>
                        <span className="text-slate-400">→</span>
                        <StatusBadge status={h.toStatus} />
                      </div>

                      <div className="text-[11px] text-slate-400">
                        By <span className="font-semibold text-slate-600">{h.changedByUsername}</span> on{' '}
                        {formatDate(h.createdAt)}
                      </div>

                      <div className="bg-slate-50 p-2.5 rounded-lg border border-slate-200/80 text-slate-700 text-xs mt-1 leading-relaxed">
                        <span className="font-semibold text-slate-500 block text-[10px] uppercase">Justification:</span>
                        {h.reason}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Edit Claim Modal */}
      {isEditing && (
        <div className="fixed inset-0 z-50 bg-slate-950/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl border border-slate-200 animate-fade-in">
            <h2 className="text-base font-bold text-slate-900 mb-1">Edit Claim Information</h2>
            <p className="text-xs text-slate-500 mb-4">
              Updating amount or category will automatically trigger the explainable risk engine to recalculate scores.
            </p>

            <form onSubmit={handleSaveEdit} className="space-y-4 text-xs">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Benefit Category</label>
                <select
                  value={editCategory}
                  onChange={(e) => setEditCategory(e.target.value as BenefitCategory)}
                  className="input-field text-xs"
                >
                  <option value="HEALTHCARE">Healthcare</option>
                  <option value="UNEMPLOYMENT">Unemployment</option>
                  <option value="DISABILITY">Disability</option>
                  <option value="HOUSING">Housing</option>
                  <option value="CHILDCARE">Childcare</option>
                </select>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Claim Amount (₹)</label>
                <input
                  type="number"
                  step="0.01"
                  required
                  value={editAmount}
                  onChange={(e) => setEditAmount(e.target.value)}
                  className="input-field text-xs font-mono font-semibold"
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Description / Notes</label>
                <textarea
                  rows={3}
                  value={editDescription}
                  onChange={(e) => setEditDescription(e.target.value)}
                  className="input-field text-xs"
                />
              </div>

              <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsEditing(false)}
                  className="btn-secondary text-xs"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={editLoading}
                  className="btn-primary text-xs"
                >
                  {editLoading ? 'Saving...' : 'Save & Re-Score'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
