import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { claimsApi } from '../api/client';
import { ClaimDetail, ClaimStatus } from '../types';
import { useAuth } from '../context/AuthContext';
import { StatusBadge } from '../components/common/StatusBadge';
import { RiskBadge } from '../components/common/RiskBadge';
import {
  FileSearch,
  ArrowLeft,
  AlertTriangle,
  CheckCircle2,
  ShieldAlert,
  ShieldBan,
  Clock,
  CheckSquare,
  AlertOctagon,
} from 'lucide-react';

export const InvestigationPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user, isAdmin } = useAuth();

  const [claim, setClaim] = useState<ClaimDetail | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Decision Form State
  const [targetStatus, setTargetStatus] = useState<ClaimStatus>('UNDER_REVIEW');
  const [reason, setReason] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [showConfirmModal, setShowConfirmModal] = useState<boolean>(false);

  // Investigation Checklist State
  const [checkedItems, setCheckedItems] = useState({
    identityVerified: false,
    invoicesExamined: false,
    policyThresholdCrossChecked: false,
    claimantContactAttempted: false,
  });

  const fetchClaim = async () => {
    if (!id) return;
    setLoading(true);
    setError(null);
    try {
      const res = await claimsApi.getClaimById(Number(id));
      setClaim(res);

      // Default target status sensibly
      if (res.status === 'SUBMITTED') {
        setTargetStatus('UNDER_REVIEW');
      } else if (res.status === 'UNDER_REVIEW') {
        setTargetStatus('CLEARED');
      } else if (res.status === 'FLAGGED') {
        setTargetStatus('UNDER_REVIEW');
      } else {
        setTargetStatus('UNDER_REVIEW');
      }
    } catch (err: any) {
      setError(err.message || 'Failed to load claim for investigation.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchClaim();
  }, [id]);

  const handleToggleCheck = (key: keyof typeof checkedItems) => {
    setCheckedItems((prev) => ({ ...prev, [key]: !prev[key] }));
  };

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (reason.trim().length < 5) {
      setError('A mandatory justification reason (at least 5 characters) must be supplied.');
      return;
    }

    // Require confirmation for final dispositions
    if (targetStatus === 'CLEARED' || targetStatus === 'CONFIRMED_FRAUD') {
      setShowConfirmModal(true);
      return;
    }

    executeStatusTransition();
  };

  const executeStatusTransition = async () => {
    if (!id) return;
    setIsSubmitting(true);
    setError(null);
    try {
      await claimsApi.transitionStatus(Number(id), targetStatus, reason.trim());
      setShowConfirmModal(false);
      setSuccess(`Claim status successfully updated to ${targetStatus}.`);
      await fetchClaim();
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to update claim status.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const formatCurrency = (val?: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
    }).format(val || 0);
  };

  if (loading) {
    return (
      <div className="py-20 text-center text-slate-400 text-sm">
        Loading investigation workspace...
      </div>
    );
  }

  if (!claim) {
    return (
      <div className="card p-8 text-center text-slate-500">
        <h2 className="text-base font-bold text-slate-800">Claim Not Found</h2>
        <Link to="/claims" className="btn-secondary text-xs mt-4 inline-block">
          Return to Claims
        </Link>
      </div>
    );
  }

  // Determine allowed target status choices based on current status
  const getAllowedOptions = () => {
    switch (claim.status) {
      case 'SUBMITTED':
        return [
          { status: 'UNDER_REVIEW' as ClaimStatus, label: 'Move to Under Review' },
          { status: 'FLAGGED' as ClaimStatus, label: 'Flag for In-depth Investigation' },
          { status: 'CLEARED' as ClaimStatus, label: 'Clear & Approve Benefit' },
        ];
      case 'UNDER_REVIEW':
        return [
          { status: 'FLAGGED' as ClaimStatus, label: 'Flag for Suspicious Pattern' },
          { status: 'CLEARED' as ClaimStatus, label: 'Clear & Approve Claim' },
          { status: 'CONFIRMED_FRAUD' as ClaimStatus, label: 'Confirm as Fraudulent Claim' },
        ];
      case 'FLAGGED':
        return [
          { status: 'UNDER_REVIEW' as ClaimStatus, label: 'Assign for Review' },
          { status: 'CLEARED' as ClaimStatus, label: 'Clear After Review' },
          { status: 'CONFIRMED_FRAUD' as ClaimStatus, label: 'Confirm as Fraud' },
        ];
      case 'CLEARED':
      case 'CONFIRMED_FRAUD':
        if (isAdmin) {
          return [{ status: 'UNDER_REVIEW' as ClaimStatus, label: 'Reopen Claim to Under Review (Admin)' }];
        }
        return [];
      default:
        return [];
    }
  };

  const allowedOptions = getAllowedOptions();

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <Link
            to={`/claims/${claim.id}`}
            className="p-2 rounded-lg bg-white border border-slate-200 text-slate-600 hover:bg-slate-50 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
          </Link>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-bold text-slate-900 tracking-tight">Investigation Workspace</h1>
              <span className="font-mono text-sm text-blue-600 font-bold">({claim.claimReference})</span>
            </div>
            <p className="text-xs text-slate-500 mt-0.5">
              Authorized Reviewer Decisioning & Audit Tracking
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <StatusBadge status={claim.status} />
          <RiskBadge score={claim.riskScore} level={claim.riskLevel} />
        </div>
      </div>

      {success && (
        <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs flex items-center justify-between animate-fade-in">
          <div className="flex items-center gap-2.5">
            <CheckCircle2 className="w-4 h-4 text-emerald-600" />
            <span>{success}</span>
          </div>
          <Link to={`/claims/${claim.id}`} className="font-bold underline text-emerald-900">
            View Updated Dossier
          </Link>
        </div>
      )}

      {error && (
        <div className="p-4 rounded-xl bg-red-50 border border-red-200 text-red-700 text-xs flex items-center gap-2.5 animate-fade-in">
          <AlertTriangle className="w-4 h-4 text-red-500 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Claim Brief Banner */}
      <div className="card p-5 bg-slate-900 text-white grid grid-cols-2 sm:grid-cols-4 gap-4 text-xs">
        <div>
          <span className="text-slate-400 block text-[11px]">Claimant Identifier</span>
          <span className="font-mono font-bold text-white text-sm mt-0.5 block">{claim.claimantId}</span>
        </div>
        <div>
          <span className="text-slate-400 block text-[11px]">Category</span>
          <span className="font-bold text-blue-400 text-sm mt-0.5 block">{claim.category}</span>
        </div>
        <div>
          <span className="text-slate-400 block text-[11px]">Claimed Amount</span>
          <span className="font-bold text-emerald-400 text-sm mt-0.5 block">{formatCurrency(claim.amount)}</span>
        </div>
        <div>
          <span className="text-slate-400 block text-[11px]">Risk Assessment</span>
          <span className="font-bold text-amber-400 text-sm mt-0.5 block">
            {claim.riskScore}/100 ({claim.riskLevel})
          </span>
        </div>
      </div>

      {/* Investigation Tasks & Decision Card */}
      <div className="grid grid-cols-1 md:grid-cols-12 gap-6">
        {/* Verification Checklist */}
        <div className="md:col-span-5 card p-5 space-y-4">
          <h2 className="text-xs font-bold text-slate-800 uppercase tracking-wider flex items-center gap-1.5">
            <CheckSquare className="w-4 h-4 text-blue-600" />
            <span>Investigative Checklist</span>
          </h2>
          <p className="text-[11px] text-slate-500">
            Complete formal verification checkpoints prior to recording a binding decision:
          </p>

          <div className="space-y-2.5 text-xs">
            <label className="flex items-start gap-2.5 p-2 rounded-lg hover:bg-slate-50 cursor-pointer transition-colors">
              <input
                type="checkbox"
                checked={checkedItems.identityVerified}
                onChange={() => handleToggleCheck('identityVerified')}
                className="mt-0.5 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
              />
              <span className="text-slate-700">Synthetic identity checked against claimant master index</span>
            </label>

            <label className="flex items-start gap-2.5 p-2 rounded-lg hover:bg-slate-50 cursor-pointer transition-colors">
              <input
                type="checkbox"
                checked={checkedItems.invoicesExamined}
                onChange={() => handleToggleCheck('invoicesExamined')}
                className="mt-0.5 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
              />
              <span className="text-slate-700">Billing invoices/deeds authenticated with issuing entity</span>
            </label>

            <label className="flex items-start gap-2.5 p-2 rounded-lg hover:bg-slate-50 cursor-pointer transition-colors">
              <input
                type="checkbox"
                checked={checkedItems.policyThresholdCrossChecked}
                onChange={() => handleToggleCheck('policyThresholdCrossChecked')}
                className="mt-0.5 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
              />
              <span className="text-slate-700">Category benefit policy guidelines and caps cross-checked</span>
            </label>

            <label className="flex items-start gap-2.5 p-2 rounded-lg hover:bg-slate-50 cursor-pointer transition-colors">
              <input
                type="checkbox"
                checked={checkedItems.claimantContactAttempted}
                onChange={() => handleToggleCheck('claimantContactAttempted')}
                className="mt-0.5 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
              />
              <span className="text-slate-700">Direct claimant inquiry logged for supplementary details</span>
            </label>
          </div>
        </div>

        {/* Status Transition Decision Form */}
        <div className="md:col-span-7 card p-5 space-y-4">
          <h2 className="text-xs font-bold text-slate-800 uppercase tracking-wider flex items-center gap-1.5">
            <FileSearch className="w-4 h-4 text-blue-600" />
            <span>Record Formal Disposition</span>
          </h2>

          {allowedOptions.length === 0 ? (
            <div className="p-4 rounded-xl bg-slate-50 border border-slate-200 text-center text-xs text-slate-500">
              This claim is in finalized status <StatusBadge status={claim.status} />. Only administrators can reopen it.
            </div>
          ) : (
            <form onSubmit={handleFormSubmit} className="space-y-4 text-xs">
              <div>
                <label className="block font-semibold text-slate-700 uppercase tracking-wider text-[11px] mb-1.5">
                  Select Target Status *
                </label>
                <div className="space-y-2">
                  {allowedOptions.map((opt) => (
                    <label
                      key={opt.status}
                      className={`flex items-center gap-3 p-3 rounded-xl border cursor-pointer transition-all ${
                        targetStatus === opt.status
                          ? 'border-blue-600 bg-blue-50/50 shadow-sm'
                          : 'border-slate-200 hover:bg-slate-50'
                      }`}
                    >
                      <input
                        type="radio"
                        name="targetStatus"
                        value={opt.status}
                        checked={targetStatus === opt.status}
                        onChange={() => setTargetStatus(opt.status)}
                        className="text-blue-600 focus:ring-blue-500"
                      />
                      <div className="flex items-center gap-2">
                        <StatusBadge status={opt.status} />
                        <span className="font-semibold text-slate-800">{opt.label}</span>
                      </div>
                    </label>
                  ))}
                </div>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 uppercase tracking-wider text-[11px] mb-1.5">
                  Mandatory Justification Reason *
                </label>
                <textarea
                  rows={4}
                  required
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  placeholder="Provide precise rationale, evidence inspected, medical records reviewed, or lease verification details..."
                  className="input-field text-xs"
                />
                <span className="text-[11px] text-slate-400 mt-1 block">
                  This statement is permanently recorded into the immutable claim audit timeline with your reviewer identity ({user?.username}).
                </span>
              </div>

              <div className="pt-2 flex items-center justify-end gap-3">
                <Link to={`/claims/${claim.id}`} className="btn-secondary text-xs">
                  Cancel
                </Link>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="btn-primary text-xs px-5 py-2.5 shadow-sm"
                >
                  {isSubmitting ? 'Recording Decision...' : 'Commit Status Transition'}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>

      {/* Confirmation Dialog Modal for Final Transitions */}
      {showConfirmModal && (
        <div className="fixed inset-0 z-50 bg-slate-950/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-slate-200 animate-fade-in space-y-4">
            <div className="w-12 h-12 rounded-full bg-amber-50 text-amber-600 flex items-center justify-center mx-auto">
              <AlertTriangle className="w-6 h-6" />
            </div>

            <div className="text-center">
              <h3 className="text-base font-bold text-slate-900">
                Confirm {targetStatus === 'CONFIRMED_FRAUD' ? 'Fraud Designation' : 'Claim Clearance'}
              </h3>
              <p className="text-xs text-slate-500 mt-1.5 leading-relaxed">
                You are transitioning claim <span className="font-mono font-bold">{claim.claimReference}</span> to{' '}
                <span className="font-bold text-slate-900">{targetStatus}</span>. This decision is legally binding and will update the fraud prevention registers.
              </p>
            </div>

            <div className="bg-slate-50 p-3 rounded-lg border border-slate-200 text-xs text-slate-700 italic">
              "{reason}"
            </div>

            <div className="flex items-center justify-end gap-2.5 pt-2">
              <button
                type="button"
                onClick={() => setShowConfirmModal(false)}
                className="btn-secondary text-xs"
              >
                Go Back & Review
              </button>
              <button
                type="button"
                onClick={executeStatusTransition}
                disabled={isSubmitting}
                className={`text-xs px-4 py-2 rounded-lg font-semibold text-white cursor-pointer ${
                  targetStatus === 'CONFIRMED_FRAUD'
                    ? 'bg-red-600 hover:bg-red-700'
                    : 'bg-emerald-600 hover:bg-emerald-700'
                }`}
              >
                {isSubmitting ? 'Committing...' : 'Yes, Confirm Decision'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
