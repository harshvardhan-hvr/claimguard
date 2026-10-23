export type Role = 'ADMIN' | 'CLAIMS_REVIEWER';

export type ClaimStatus = 'SUBMITTED' | 'UNDER_REVIEW' | 'FLAGGED' | 'CLEARED' | 'CONFIRMED_FRAUD';

export type BenefitCategory = 'HEALTHCARE' | 'UNEMPLOYMENT' | 'DISABILITY' | 'HOUSING' | 'CHILDCARE';

export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH';

export interface User {
  id: number;
  username: string;
  fullName: string;
  role: Role;
  enabled: boolean;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  fullName: string;
  role: Role;
}

export interface Claim {
  id: number;
  claimReference: string;
  claimantId: string;
  category: BenefitCategory;
  amount: number;
  submissionDate: string;
  status: ClaimStatus;
  riskScore: number;
  riskLevel: RiskLevel;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

export interface RiskRuleResult {
  id: number;
  ruleName: string;
  ruleCode: string;
  scoreContribution: number;
  triggered: boolean;
  explanation: string;
}

export interface RiskAssessment {
  id: number;
  claimId: number;
  totalScore: number;
  riskLevel: RiskLevel;
  evaluatedAt: string;
  evaluatorNote: string;
  ruleResults: RiskRuleResult[];
}

export interface StatusHistory {
  id: number;
  claimId: number;
  fromStatus: ClaimStatus;
  toStatus: ClaimStatus;
  changedByUserId?: number;
  changedByUsername: string;
  reason: string;
  createdAt: string;
}

export interface ClaimDetail extends Claim {
  latestRiskAssessment?: RiskAssessment;
  statusHistory?: StatusHistory[];
}

export interface DashboardSummary {
  totalClaims: number;
  awaitingReview: number;
  highRiskClaims: number;
  reviewedClaims: number;
  totalClaimedAmount: number;
  confirmedFraudAmount: number;
  clearedAmount: number;
}

export interface ClaimsByStatus {
  statusCounts: Record<string, number>;
}

export interface RiskDistribution {
  riskDistribution: Record<string, number>;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
