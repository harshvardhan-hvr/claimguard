import axios from 'axios';
import {
  AuthResponse,
  Claim,
  ClaimDetail,
  ClaimsByStatus,
  DashboardSummary,
  PagedResponse,
  RiskAssessment,
  RiskDistribution,
  StatusHistory,
  User,
} from '../types';

const API_BASE_URL = '/api';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach JWT token to requests if present
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('claimguard_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Intercept 401 Unauthorized and redirect to login if session expires
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('claimguard_token');
      localStorage.removeItem('claimguard_user');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// Authentication APIs
export const authApi = {
  login: async (username: string, password: string): Promise<AuthResponse> => {
    const res = await apiClient.post<AuthResponse>('/auth/login', { username, password });
    return res.data;
  },
  logout: async (): Promise<void> => {
    try {
      await apiClient.post('/auth/logout');
    } finally {
      localStorage.removeItem('claimguard_token');
      localStorage.removeItem('claimguard_user');
    }
  },
  getMe: async (): Promise<User> => {
    const res = await apiClient.get<User>('/auth/me');
    return res.data;
  },
};

// Claims Management APIs
export const claimsApi = {
  getClaims: async (params: {
    search?: string;
    status?: string;
    category?: string;
    riskLevel?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
  }): Promise<PagedResponse<Claim>> => {
    const res = await apiClient.get<PagedResponse<Claim>>('/claims', { params });
    return res.data;
  },

  getClaimById: async (id: number): Promise<ClaimDetail> => {
    const res = await apiClient.get<ClaimDetail>(`/claims/${id}`);
    return res.data;
  },

  createClaim: async (data: {
    claimReference?: string;
    claimantId: string;
    category: string;
    amount: number;
    submissionDate: string;
    description?: string;
  }): Promise<ClaimDetail> => {
    const res = await apiClient.post<ClaimDetail>('/claims', data);
    return res.data;
  },

  updateClaim: async (
    id: number,
    data: {
      category?: string;
      amount?: number;
      description?: string;
    }
  ): Promise<ClaimDetail> => {
    const res = await apiClient.put<ClaimDetail>(`/claims/${id}`, data);
    return res.data;
  },

  getClaimHistory: async (id: number): Promise<StatusHistory[]> => {
    const res = await apiClient.get<StatusHistory[]>(`/claims/${id}/history`);
    return res.data;
  },

  getRiskAssessment: async (id: number): Promise<RiskAssessment> => {
    const res = await apiClient.get<RiskAssessment>(`/claims/${id}/risk-assessment`);
    return res.data;
  },

  analyzeClaim: async (id: number): Promise<RiskAssessment> => {
    const res = await apiClient.post<RiskAssessment>(`/claims/${id}/analyze`);
    return res.data;
  },

  transitionStatus: async (
    id: number,
    toStatus: string,
    reason: string
  ): Promise<ClaimDetail> => {
    const res = await apiClient.post<ClaimDetail>(`/claims/${id}/status-transitions`, {
      toStatus,
      reason,
    });
    return res.data;
  },
};

// Dashboard APIs
export const dashboardApi = {
  getSummary: async (): Promise<DashboardSummary> => {
    const res = await apiClient.get<DashboardSummary>('/dashboard/summary');
    return res.data;
  },
  getClaimsByStatus: async (): Promise<ClaimsByStatus> => {
    const res = await apiClient.get<ClaimsByStatus>('/dashboard/claims-by-status');
    return res.data;
  },
  getRiskDistribution: async (): Promise<RiskDistribution> => {
    const res = await apiClient.get<RiskDistribution>('/dashboard/risk-distribution');
    return res.data;
  },
  getRecentClaims: async (limit: number = 8): Promise<Claim[]> => {
    const res = await apiClient.get<Claim[]>('/dashboard/recent-claims', {
      params: { limit },
    });
    return res.data;
  },
};

// User Management APIs (Admin only)
export const usersApi = {
  getUsers: async (): Promise<User[]> => {
    const res = await apiClient.get<User[]>('/users');
    return res.data;
  },
  createUser: async (data: {
    username: string;
    password: string;
    fullName: string;
    role: string;
  }): Promise<User> => {
    const res = await apiClient.post<User>('/users', data);
    return res.data;
  },
  toggleUserStatus: async (id: number): Promise<User> => {
    const res = await apiClient.put<User>(`/users/${id}/status`);
    return res.data;
  },
};
