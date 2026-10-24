import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { claimsApi } from '../api/client';
import { BenefitCategory, ClaimDetail } from '../types';
import { RiskBadge } from '../components/common/RiskBadge';
import { StatusBadge } from '../components/common/StatusBadge';
import {
  ShieldAlert,
  CheckCircle2,
  AlertTriangle,
  ArrowRight,
  FileCheck,
  PlusCircle,
  HelpCircle,
} from 'lucide-react';

export const CreateClaimPage: React.FC = () => {
  const navigate = useNavigate();

  const [claimReference, setClaimReference] = useState('');
  const [claimantId, setClaimantId] = useState('CLMT-88450');
  const [category, setCategory] = useState<BenefitCategory>('HEALTHCARE');
  const [amount, setAmount] = useState<string>('1250.00');
  const [submissionDate, setSubmissionDate] = useState<string>(
    new Date().toISOString().split('T')[0]
  );
  const [description, setDescription] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [submittedClaim, setSubmittedClaim] = useState<ClaimDetail | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const parsedAmount = parseFloat(amount);
      if (isNaN(parsedAmount) || parsedAmount <= 0) {
        throw new Error('Claim amount must be a positive number greater than 0.');
      }

      const created = await claimsApi.createClaim({
        claimReference: claimReference.trim() || undefined,
        claimantId: claimantId.trim().toUpperCase(),
        category,
        amount: parsedAmount,
        submissionDate,
        description: description.trim() || undefined,
      });

      setSubmittedClaim(created);
    } catch (err: any) {
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError(err.message || 'Failed to submit claim.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleResetForm = () => {
    setSubmittedClaim(null);
    setClaimReference('');
    setDescription('');
    setAmount('1250.00');
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Submit Benefit Claim</h1>
        <p className="text-sm text-slate-500 mt-0.5">
          Ingest new claim data and execute automated real-time explainable risk scoring
        </p>
      </div>

      {submittedClaim ? (
        /* Immediate Risk Evaluation Outcome Card */
        <div className="card p-6 sm:p-8 border-2 border-blue-500/30 animate-fade-in space-y-6">
          <div className="flex items-center gap-3 text-emerald-600">
            <div className="w-10 h-10 rounded-full bg-emerald-50 flex items-center justify-center">
              <CheckCircle2 className="w-6 h-6" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-slate-900">Claim Ingested Successfully</h2>
              <p className="text-xs text-slate-500">
                Processed with reference{' '}
                <span className="font-mono font-bold text-blue-600">{submittedClaim.claimReference}</span>
              </p>
            </div>
          </div>

          {/* Risk Summary Banner */}
          <div className="p-4 rounded-xl bg-slate-50 border border-slate-200 grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider block">
                Calculated Risk Score
              </span>
              <div className="mt-1 flex items-baseline gap-2">
                <span className="text-3xl font-extrabold text-slate-900 font-mono">
                  {submittedClaim.riskScore}
                </span>
                <span className="text-xs text-slate-400">/ 100</span>
                <RiskBadge score={submittedClaim.riskScore} level={submittedClaim.riskLevel} />
              </div>
            </div>

            <div>
              <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider block">
                Workflow Status
              </span>
              <div className="mt-2">
                <StatusBadge status={submittedClaim.status} />
              </div>
            </div>

            <div>
              <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider block">
                Claim Amount & Category
              </span>
              <div className="text-sm font-semibold text-slate-800 mt-1">
                ₹{submittedClaim.amount.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} • {submittedClaim.category}
              </div>
              <span className="text-xs text-slate-500 font-mono">Claimant: {submittedClaim.claimantId}</span>
            </div>
          </div>

          {/* Rule Evaluation Breakdown */}
          {submittedClaim.latestRiskAssessment && (
            <div>
              <h3 className="text-sm font-bold text-slate-900 mb-3">
                Risk Engine Rule Breakdown ({submittedClaim.latestRiskAssessment.ruleResults.length} Rules Evaluated)
              </h3>
              <div className="space-y-2.5">
                {submittedClaim.latestRiskAssessment.ruleResults.map((rule, idx) => (
                  <div
                    key={idx}
                    className={`p-3.5 rounded-lg border text-xs flex items-start justify-between gap-4 ${
                      rule.triggered
                        ? 'bg-rose-50/70 border-rose-200 text-rose-900'
                        : 'bg-emerald-50/50 border-emerald-200 text-slate-700'
                    }`}
                  >
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span className="font-semibold text-slate-900">{rule.ruleName}</span>
                        {rule.triggered ? (
                          <span className="px-1.5 py-0.2 rounded bg-rose-200 text-rose-800 font-semibold text-[10px]">
                            Triggered (+{rule.scoreContribution} pts)
                          </span>
                        ) : (
                          <span className="px-1.5 py-0.2 rounded bg-emerald-100 text-emerald-800 font-medium text-[10px]">
                            Passed (0 pts)
                          </span>
                        )}
                      </div>
                      <p className="text-slate-600 leading-relaxed">{rule.explanation}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Navigation Actions */}
          <div className="flex items-center justify-between pt-4 border-t border-slate-200">
            <button onClick={handleResetForm} className="btn-secondary text-xs">
              + Ingest Another Claim
            </button>
            <div className="flex items-center gap-3">
              <Link to={`/claims/${submittedClaim.id}`} className="btn-primary text-xs gap-1.5">
                <span>View Full Claim File</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </Link>
            </div>
          </div>
        </div>
      ) : (
        /* Create Claim Form */
        <div className="card p-6 sm:p-8">
          {error && (
            <div className="mb-6 p-4 rounded-lg bg-red-50 border border-red-200 text-red-700 text-xs flex items-start gap-2.5">
              <AlertTriangle className="w-4 h-4 text-red-500 shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {/* Optional Reference */}
              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                  Claim Reference <span className="text-slate-400 font-normal lowercase">(optional)</span>
                </label>
                <input
                  type="text"
                  value={claimReference}
                  onChange={(e) => setClaimReference(e.target.value)}
                  placeholder="e.g. CLM-2026-9001 (auto if empty)"
                  className="input-field text-xs font-mono"
                />
                <span className="text-[11px] text-slate-400 mt-1 block">
                  Leave blank to automatically generate unique format.
                </span>
              </div>

              {/* Claimant ID */}
              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                  Synthetic Claimant Identifier *
                </label>
                <input
                  type="text"
                  required
                  value={claimantId}
                  onChange={(e) => setClaimantId(e.target.value)}
                  placeholder="e.g. CLMT-88450"
                  className="input-field text-xs font-mono"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              {/* Category */}
              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                  Benefit Category *
                </label>
                <select
                  value={category}
                  onChange={(e) => setCategory(e.target.value as BenefitCategory)}
                  className="input-field text-xs"
                >
                  <option value="HEALTHCARE">Healthcare (Limit: ₹5,000)</option>
                  <option value="UNEMPLOYMENT">Unemployment (Limit: ₹2,500)</option>
                  <option value="DISABILITY">Disability (Limit: ₹7,500)</option>
                  <option value="HOUSING">Housing (Limit: ₹4,000)</option>
                  <option value="CHILDCARE">Childcare (Limit: ₹3,000)</option>
                </select>
              </div>

              {/* Amount */}
              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                  Claim Amount (₹ INR) *
                </label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  required
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="input-field text-xs font-mono font-semibold"
                />
              </div>

              {/* Submission Date */}
              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                  Submission Date *
                </label>
                <input
                  type="date"
                  required
                  value={submissionDate}
                  onChange={(e) => setSubmissionDate(e.target.value)}
                  className="input-field text-xs"
                />
              </div>
            </div>

            {/* Description */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Claim Description & Supporting Notes
              </label>
              <textarea
                rows={3}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Details of medical procedure, rent voucher verification, or job search log..."
                className="input-field text-xs"
              />
            </div>

            {/* Submit Button */}
            <div className="pt-2 flex items-center justify-end gap-3">
              <Link to="/claims" className="btn-secondary text-xs">
                Cancel
              </Link>
              <button
                type="submit"
                disabled={loading}
                className="btn-primary text-xs px-5 py-2.5 shadow-sm"
              >
                {loading ? 'Evaluating Risk & Saving...' : 'Submit & Analyze Claim'}
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};
