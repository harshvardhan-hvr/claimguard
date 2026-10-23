import React from 'react';
import { RiskLevel } from '../../types';
import { AlertTriangle, CheckCircle2, ShieldAlert } from 'lucide-react';

interface RiskBadgeProps {
  score: number;
  level: RiskLevel;
  showScore?: boolean;
}

export const RiskBadge: React.FC<RiskBadgeProps> = ({ score, level, showScore = true }) => {
  const getBadgeStyle = () => {
    switch (level) {
      case 'HIGH':
        return {
          bg: 'bg-red-50 text-red-700 border-red-200',
          dot: 'bg-red-500',
          icon: <ShieldAlert className="w-3.5 h-3.5 text-red-600" />,
        };
      case 'MEDIUM':
        return {
          bg: 'bg-amber-50 text-amber-700 border-amber-200',
          dot: 'bg-amber-500',
          icon: <AlertTriangle className="w-3.5 h-3.5 text-amber-600" />,
        };
      case 'LOW':
      default:
        return {
          bg: 'bg-emerald-50 text-emerald-700 border-emerald-200',
          dot: 'bg-emerald-500',
          icon: <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />,
        };
    }
  };

  const style = getBadgeStyle();

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold border ${style.bg}`}
    >
      {style.icon}
      <span>{level}</span>
      {showScore && <span className="opacity-80 font-mono">({score})</span>}
    </span>
  );
};
