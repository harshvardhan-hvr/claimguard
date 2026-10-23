import React, { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { claimsApi } from '../api/client';
import { BenefitCategory, Claim, ClaimStatus, PagedResponse, RiskLevel } from '../types';
import { StatusBadge } from '../components/common/StatusBadge';
import { RiskBadge } from '../components/common/RiskBadge';
import {
  Search,
  Filter,
  PlusCircle,
  RotateCcw,
  ChevronLeft,
  ChevronRight,
  ArrowUpDown,
  ArrowRight,
  ShieldAlert,
} from 'lucide-react';

export const ClaimsListPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [data, setData] = useState<PagedResponse<Claim> | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Search & Filter state
  const [search, setSearch] = useState<string>(searchParams.get('search') || '');
  const [status, setStatus] = useState<string>(searchParams.get('status') || '');
  const [category, setCategory] = useState<string>(searchParams.get('category') || '');
  const [riskLevel, setRiskLevel] = useState<string>(searchParams.get('riskLevel') || '');
  const [page, setPage] = useState<number>(Number(searchParams.get('page')) || 0);
  const [size, setSize] = useState<number>(10);
  const [sortBy, setSortBy] = useState<string>('createdAt');
  const [sortDir, setSortDir] = useState<string>('desc');

  const fetchClaims = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await claimsApi.getClaims({
        search: search.trim() || undefined,
        status: status || undefined,
        category: category || undefined,
        riskLevel: riskLevel || undefined,
        page,
        size,
        sortBy,
        sortDir,
      });
      setData(res);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch claims.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchClaims();
  }, [page, size, sortBy, sortDir, status, category, riskLevel]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    fetchClaims();
  };

  const handleResetFilters = () => {
    setSearch('');
    setStatus('');
    setCategory('');
    setRiskLevel('');
    setPage(0);
    setSortBy('createdAt');
    setSortDir('desc');
  };

  const toggleSort = (field: string) => {
    if (sortBy === field) {
      setSortDir(sortDir === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(field);
      setSortDir('desc');
    }
    setPage(0);
  };

  const formatCurrency = (val?: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(val || 0);
  };

  return (
    <div className="space-y-5">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Claims Directory</h1>
          <p className="text-sm text-slate-500 mt-0.5">
            Manage benefit claims, filter by risk score, and initiate investigations
          </p>
        </div>
        <Link to="/claims/new" className="btn-primary self-start sm:self-auto gap-2 text-sm shadow-sm">
          <PlusCircle className="w-4 h-4" />
          <span>Submit New Claim</span>
        </Link>
      </div>

      {/* Filter and Search Bar */}
      <div className="card p-4">
        <form onSubmit={handleSearchSubmit} className="grid grid-cols-1 md:grid-cols-12 gap-3">
          {/* Search text */}
          <div className="md:col-span-4 relative">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Search className="h-4 w-4 text-slate-400" />
            </div>
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search reference or claimant ID..."
              className="input-field pl-9 text-xs"
            />
          </div>

          {/* Status filter */}
          <div className="md:col-span-2">
            <select
              value={status}
              onChange={(e) => {
                setStatus(e.target.value);
                setPage(0);
              }}
              className="input-field text-xs"
            >
              <option value="">All Statuses</option>
              <option value="SUBMITTED">Submitted</option>
              <option value="UNDER_REVIEW">Under Review</option>
              <option value="FLAGGED">Flagged</option>
              <option value="CLEARED">Cleared</option>
              <option value="CONFIRMED_FRAUD">Confirmed Fraud</option>
            </select>
          </div>

          {/* Category filter */}
          <div className="md:col-span-2">
            <select
              value={category}
              onChange={(e) => {
                setCategory(e.target.value);
                setPage(0);
              }}
              className="input-field text-xs"
            >
              <option value="">All Categories</option>
              <option value="HEALTHCARE">Healthcare</option>
              <option value="UNEMPLOYMENT">Unemployment</option>
              <option value="DISABILITY">Disability</option>
              <option value="HOUSING">Housing</option>
              <option value="CHILDCARE">Childcare</option>
            </select>
          </div>

          {/* Risk Level filter */}
          <div className="md:col-span-2">
            <select
              value={riskLevel}
              onChange={(e) => {
                setRiskLevel(e.target.value);
                setPage(0);
              }}
              className="input-field text-xs"
            >
              <option value="">All Risk Tiers</option>
              <option value="LOW">Low Risk</option>
              <option value="MEDIUM">Medium Risk</option>
              <option value="HIGH">High Risk</option>
            </select>
          </div>

          {/* Action buttons */}
          <div className="md:col-span-2 flex items-center gap-2">
            <button type="submit" className="btn-primary w-full text-xs py-2">
              Apply
            </button>
            <button
              type="button"
              onClick={handleResetFilters}
              className="btn-secondary text-xs py-2 px-2.5 text-slate-500"
              title="Reset all filters"
            >
              <RotateCcw className="w-3.5 h-3.5" />
            </button>
          </div>
        </form>
      </div>

      {error && (
        <div className="p-3.5 rounded-lg bg-red-50 border border-red-200 text-red-700 text-xs">
          {error}
        </div>
      )}

      {/* Claims Table Card */}
      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-slate-600">
            <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-200 uppercase tracking-wider">
              <tr>
                <th
                  onClick={() => toggleSort('claimReference')}
                  className="py-3 px-4 cursor-pointer hover:bg-slate-100 transition-colors"
                >
                  <div className="flex items-center gap-1">
                    <span>Reference</span>
                    <ArrowUpDown className="w-3 h-3 text-slate-400" />
                  </div>
                </th>
                <th className="py-3 px-4">Claimant</th>
                <th className="py-3 px-4">Category</th>
                <th
                  onClick={() => toggleSort('amount')}
                  className="py-3 px-4 cursor-pointer hover:bg-slate-100 transition-colors"
                >
                  <div className="flex items-center gap-1">
                    <span>Amount</span>
                    <ArrowUpDown className="w-3 h-3 text-slate-400" />
                  </div>
                </th>
                <th
                  onClick={() => toggleSort('submissionDate')}
                  className="py-3 px-4 cursor-pointer hover:bg-slate-100 transition-colors"
                >
                  <div className="flex items-center gap-1">
                    <span>Submitted</span>
                    <ArrowUpDown className="w-3 h-3 text-slate-400" />
                  </div>
                </th>
                <th
                  onClick={() => toggleSort('riskScore')}
                  className="py-3 px-4 cursor-pointer hover:bg-slate-100 transition-colors"
                >
                  <div className="flex items-center gap-1">
                    <span>Risk Score</span>
                    <ArrowUpDown className="w-3 h-3 text-slate-400" />
                  </div>
                </th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {loading ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-slate-400">
                    Loading claims records...
                  </td>
                </tr>
              ) : data?.content.length === 0 ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-slate-400">
                    No claims match your search or filter criteria.
                  </td>
                </tr>
              ) : (
                data?.content.map((c) => (
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
                    <td className="py-3 px-4 text-right space-x-2">
                      <Link
                        to={`/claims/${c.id}`}
                        className="text-slate-600 hover:text-slate-900 font-medium text-xs px-2 py-1 rounded bg-slate-100 hover:bg-slate-200 transition-colors inline-block"
                      >
                        Details
                      </Link>
                      <Link
                        to={`/claims/${c.id}/investigate`}
                        className="text-blue-600 hover:text-blue-800 font-semibold text-xs px-2 py-1 rounded bg-blue-50 hover:bg-blue-100 transition-colors inline-block"
                      >
                        Investigate
                      </Link>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Footer */}
        {data && (
          <div className="p-4 border-t border-slate-100 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-slate-500">
            <div>
              Showing{' '}
              <span className="font-semibold text-slate-700">
                {data.totalElements === 0 ? 0 : data.page * data.size + 1}
              </span>{' '}
              to{' '}
              <span className="font-semibold text-slate-700">
                {Math.min((data.page + 1) * data.size, data.totalElements)}
              </span>{' '}
              of <span className="font-semibold text-slate-700">{data.totalElements}</span> results
            </div>

            <div className="flex items-center gap-2">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={data.page === 0 || loading}
                className="btn-secondary text-xs py-1.5 px-2.5 disabled:opacity-40"
              >
                <ChevronLeft className="w-3.5 h-3.5" />
                <span>Prev</span>
              </button>

              <span className="px-2 font-medium text-slate-700">
                Page {data.page + 1} of {Math.max(1, data.totalPages)}
              </span>

              <button
                onClick={() => setPage((p) => p + 1)}
                disabled={data.last || loading}
                className="btn-secondary text-xs py-1.5 px-2.5 disabled:opacity-40"
              >
                <span>Next</span>
                <ChevronRight className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
