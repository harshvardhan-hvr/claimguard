import React from 'react';
import { ClaimStatus } from '../../types';
import { Clock, Eye, AlertOctagon, CheckCircle, ShieldBan } from 'lucide-react';

interface StatusBadgeProps {
  status: ClaimStatus;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
  const getStatusConfig = () => {
    switch (status) {
      case 'SUBMITTED':
        return {
          label: 'Submitted',
          classes: 'bg-blue-50 text-blue-700 border-blue-200',
          icon: <Clock className="w-3.5 h-3.5" />,
        };
      case 'UNDER_REVIEW':
        return {
          label: 'Under Review',
          classes: 'bg-purple-50 text-purple-700 border-purple-200',
          icon: <Eye className="w-3.5 h-3.5" />,
        };
      case 'FLAGGED':
        return {
          label: 'Flagged',
          classes: 'bg-rose-50 text-rose-700 border-rose-200',
          icon: <AlertOctagon className="w-3.5 h-3.5" />,
        };
      case 'CLEARED':
        return {
          label: 'Cleared',
          classes: 'bg-emerald-50 text-emerald-700 border-emerald-200',
          icon: <CheckCircle className="w-3.5 h-3.5" />,
        };
      case 'CONFIRMED_FRAUD':
        return {
          label: 'Confirmed Fraud',
          classes: 'bg-slate-900 text-red-400 border-slate-700',
          icon: <ShieldBan className="w-3.5 h-3.5" />,
        };
      default:
        return {
          label: status,
          classes: 'bg-gray-100 text-gray-700 border-gray-200',
          icon: null,
        };
    }
  };

  const config = getStatusConfig();

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-semibold border ${config.classes}`}
    >
      {config.icon}
      <span>{config.label}</span>
    </span>
  );
};
