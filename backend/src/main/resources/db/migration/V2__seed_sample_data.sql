-- Seed Data for ClaimGuard
-- Default password for all seed users is 'password123'
-- BCrypt Hash: $2a$10$OMcaBSRs0C186r9gH6Va..7ykrxTWRLIgmTZRy6.OhYTSl0R.9aJ2

-- 1. Insert Users
INSERT INTO users (id, username, password_hash, full_name, role, enabled, created_at)
VALUES
(1, 'admin', '$2a$10$OMcaBSRs0C186r9gH6Va..7ykrxTWRLIgmTZRy6.OhYTSl0R.9aJ2', 'System Administrator', 'ADMIN', true, CURRENT_TIMESTAMP),
(2, 'reviewer1', '$2a$10$OMcaBSRs0C186r9gH6Va..7ykrxTWRLIgmTZRy6.OhYTSl0R.9aJ2', 'Sarah Connor (Senior Reviewer)', 'CLAIMS_REVIEWER', true, CURRENT_TIMESTAMP),
(3, 'reviewer2', '$2a$10$OMcaBSRs0C186r9gH6Va..7ykrxTWRLIgmTZRy6.OhYTSl0R.9aJ2', 'Marcus Vance (Fraud Investigator)', 'CLAIMS_REVIEWER', true, CURRENT_TIMESTAMP);

-- 2. Insert 32 Fictional Claims
INSERT INTO claims (id, claim_reference, claimant_id, category, amount, submission_date, status, risk_score, risk_level, description, created_at, updated_at)
VALUES
-- Low Risk Claims (Clean, Within Policy Limits)
(1, 'CLM-2026-1001', 'CLMT-88401', 'HEALTHCARE', 450.00, '2026-10-15', 'CLEARED', 0, 'LOW', 'Annual preventive medical examination and blood work', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'CLM-2026-1002', 'CLMT-88402', 'UNEMPLOYMENT', 950.00, '2026-10-16', 'CLEARED', 0, 'LOW', 'Standard bi-weekly job search assistance allowance', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'CLM-2026-1003', 'CLMT-88403', 'DISABILITY', 1200.00, '2026-10-17', 'CLEARED', 0, 'LOW', 'Approved ongoing physical therapy rehabilitation stipend', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'CLM-2026-1004', 'CLMT-88404', 'HOUSING', 1150.00, '2026-10-18', 'CLEARED', 0, 'LOW', 'Monthly residential rent support voucher verified by landlord', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'CLM-2026-1005', 'CLMT-88405', 'CHILDCARE', 800.00, '2026-10-19', 'CLEARED', 0, 'LOW', 'Licensed day care attendance subsidy for single working parent', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'CLM-2026-1006', 'CLMT-88406', 'HEALTHCARE', 1200.00, '2026-10-20', 'SUBMITTED', 0, 'LOW', 'Routine dental reconstruction and restoration following accident', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'CLM-2026-1007', 'CLMT-88407', 'UNEMPLOYMENT', 1400.00, '2026-10-20', 'SUBMITTED', 0, 'LOW', 'First-time unemployment benefit application with proof of separation', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'CLM-2026-1008', 'CLMT-88408', 'HOUSING', 1800.00, '2026-10-21', 'SUBMITTED', 0, 'LOW', 'Emergency rental assistance due to municipal redevelopment', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'CLM-2026-1009', 'CLMT-88409', 'CHILDCARE', 650.00, '2026-10-21', 'UNDER_REVIEW', 0, 'LOW', 'After-school enrichment care stipend for special needs minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'CLM-2026-1010', 'CLMT-88410', 'DISABILITY', 2800.00, '2026-10-22', 'UNDER_REVIEW', 0, 'LOW', 'Adaptive home modification grant (wheelchair access ramp)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Medium Risk Claims (Category Threshold Exceeded, +35 pts)
(11, 'CLM-2026-1011', 'CLMT-88411', 'HEALTHCARE', 6800.00, '2026-10-18', 'UNDER_REVIEW', 35, 'MEDIUM', 'Emergency surgical intervention and inpatient hospitalization ($6,800 > $5,000 threshold)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'CLM-2026-1012', 'CLMT-88412', 'UNEMPLOYMENT', 3200.00, '2026-10-19', 'UNDER_REVIEW', 35, 'MEDIUM', 'Extended severance gap compensation exceeding standard threshold ($3,200 > $2,500)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 'CLM-2026-1013', 'CLMT-88413', 'HOUSING', 5200.00, '2026-10-20', 'UNDER_REVIEW', 35, 'MEDIUM', 'Disaster displacement emergency lodging grant ($5,200 > $4,000 threshold)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'CLM-2026-1014', 'CLMT-88414', 'CHILDCARE', 4200.00, '2026-10-21', 'UNDER_REVIEW', 35, 'MEDIUM', 'Full-time specialized infant nursing care ($4,200 > $3,000 threshold)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 'CLM-2026-1015', 'CLMT-88415', 'DISABILITY', 9100.00, '2026-10-22', 'UNDER_REVIEW', 35, 'MEDIUM', 'Motorized wheelchair and assistive communication apparatus ($9,100 > $7,500 threshold)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Medium Risk Claims (Claimant Velocity Exceeded, +30 pts)
(16, 'CLM-2026-1016', 'CLMT-88416', 'HEALTHCARE', 900.00, '2026-10-02', 'CLEARED', 0, 'LOW', 'Prescription drug refill copay', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 'CLM-2026-1017', 'CLMT-88416', 'HEALTHCARE', 750.00, '2026-10-10', 'CLEARED', 0, 'LOW', 'Follow-up consultation fee', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 'CLM-2026-1018', 'CLMT-88416', 'HEALTHCARE', 820.00, '2026-10-22', 'FLAGGED', 30, 'MEDIUM', 'Third healthcare claim in 20 days by CLMT-88416 (+30 pts velocity)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Medium Risk Claims (Rapid Consecutive Submission, +25 pts)
(19, 'CLM-2026-1019', 'CLMT-88417', 'HOUSING', 1400.00, '2026-10-21', 'SUBMITTED', 0, 'LOW', 'First lease deposit voucher', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 'CLM-2026-1020', 'CLMT-88417', 'CHILDCARE', 950.00, '2026-10-22', 'FLAGGED', 25, 'LOW', 'Childcare subsidy submitted within 24 hours of housing voucher (+25 pts rapid)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- High Risk Claims (Duplicate Claim Detected: +40 pts Duplicate + 25 pts Rapid = 65 or + 35 pts Threshold = 75+)
(21, 'CLM-2026-1021', 'CLMT-88418', 'HEALTHCARE', 5600.00, '2026-10-14', 'CLEARED', 35, 'MEDIUM', 'Outpatient surgical procedure claim', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 'CLM-2026-1022', 'CLMT-88418', 'HEALTHCARE', 5600.00, '2026-10-22', 'FLAGGED', 75, 'HIGH', 'Duplicate claim submission: identical $5,600 healthcare amount and exceeds threshold (+40 pts duplicate + 35 pts threshold = 75)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- High Risk Claims (Category Threshold + Claimant Velocity + Rapid: 35 + 30 + 25 = 90 pts)
(23, 'CLM-2026-1023', 'CLMT-88419', 'UNEMPLOYMENT', 1200.00, '2026-10-05', 'CLEARED', 0, 'LOW', 'Weekly benefit payment', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(24, 'CLM-2026-1024', 'CLMT-88419', 'UNEMPLOYMENT', 1200.00, '2026-10-18', 'CLEARED', 0, 'LOW', 'Subsequent bi-weekly payment', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(25, 'CLM-2026-1025', 'CLMT-88419', 'UNEMPLOYMENT', 3400.00, '2026-10-23', 'FLAGGED', 65, 'MEDIUM', 'Retrospective lump-sum claim triggering threshold and velocity (+35 + 30 = 65)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- High Risk Claims (Quadruple Trigger: Duplicate + Threshold + Velocity + Rapid Burst = 100 capped)
(26, 'CLM-2026-1026', 'CLMT-88420', 'DISABILITY', 8400.00, '2026-10-10', 'CLEARED', 35, 'MEDIUM', 'Initial comprehensive disability prosthesis settlement', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(27, 'CLM-2026-1027', 'CLMT-88420', 'DISABILITY', 2100.00, '2026-10-18', 'CLEARED', 0, 'LOW', 'Home care assistant service fee', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(28, 'CLM-2026-1028', 'CLMT-88420', 'DISABILITY', 8400.00, '2026-10-23', 'FLAGGED', 100, 'HIGH', 'Critical Alert: identical duplicate $8,400 prosthesis claim, exceeding threshold, high velocity, within 5 days (+40 duplicate + 35 threshold + 30 velocity = 105 capped at 100)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Confirmed Fraud Claims (Investigated and Finalized with Evidence)
(29, 'CLM-2026-1029', 'CLMT-88421', 'HOUSING', 4800.00, '2026-10-08', 'CONFIRMED_FRAUD', 75, 'HIGH', 'Fabricated landlord lease agreement; property verified non-residential commercial parcel', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(30, 'CLM-2026-1030', 'CLMT-88422', 'HEALTHCARE', 7900.00, '2026-10-11', 'CONFIRMED_FRAUD', 75, 'HIGH', 'Forged medical billing codes from non-existent medical clinic', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(31, 'CLM-2026-1031', 'CLMT-88423', 'CHILDCARE', 4500.00, '2026-10-12', 'CONFIRMED_FRAUD', 65, 'MEDIUM', 'Claimant submitted identical childcare invoices under multiple synthetic identities', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Additional High Risk Investigation Claim
(32, 'CLM-2026-1032', 'CLMT-88424', 'HEALTHCARE', 8900.00, '2026-10-23', 'UNDER_REVIEW', 70, 'HIGH', 'High value inpatient claim undergoing clinical coding verification (+35 threshold + 35 risk)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 3. Insert Risk Assessments for Seed Claims
INSERT INTO risk_assessments (id, claim_id, total_score, risk_level, evaluator_note, evaluated_at)
VALUES
(1, 1, 0, 'LOW', 'Automated baseline evaluation. All parameters normal.', CURRENT_TIMESTAMP),
(2, 11, 35, 'MEDIUM', 'Category policy threshold exceeded for HEALTHCARE ($6,800.00 > $5,000.00).', CURRENT_TIMESTAMP),
(3, 18, 30, 'MEDIUM', 'Claimant velocity exceeded: 3 claims submitted within 30 days.', CURRENT_TIMESTAMP),
(4, 20, 25, 'LOW', 'Rapid submission spacing alert: 2 claims in consecutive 24 hours.', CURRENT_TIMESTAMP),
(5, 22, 75, 'HIGH', 'Potential duplicate healthcare claim and threshold exceeded (+75 pts). Recommended for human investigation.', CURRENT_TIMESTAMP),
(6, 28, 100, 'HIGH', 'Severe anomaly detected: Exact duplicate amount, threshold exceeded, elevated claimant frequency (+100 pts).', CURRENT_TIMESTAMP),
(7, 29, 75, 'HIGH', 'High risk indicators detected. Transferred to Special Investigations Unit.', CURRENT_TIMESTAMP),
(8, 30, 75, 'HIGH', 'High risk medical billing anomalies detected.', CURRENT_TIMESTAMP),
(9, 32, 70, 'HIGH', 'Claim amount significantly exceeds healthcare threshold; assigned for manual audit.', CURRENT_TIMESTAMP);

-- 4. Insert Risk Rule Results
INSERT INTO risk_rule_results (risk_assessment_id, rule_name, rule_code, score_contribution, triggered, explanation)
VALUES
(1, 'Category Threshold Exceeded', 'RULE_CATEGORY_THRESHOLD', 0, false, 'Claim amount $450.00 is within the HEALTHCARE category threshold of $5,000.00.'),
(1, 'Potential Duplicate Claim Submission', 'RULE_DUPLICATE_CLAIM', 0, false, 'No duplicate submissions detected for this claimant within the inspection window.'),
(1, 'High Claimant Submission Frequency', 'RULE_CLAIMANT_VELOCITY', 0, false, 'Claimant velocity is normal: 1 claim(s) in 30-day window (allowance: 2).'),
(1, 'Rapid Consecutive Submission Pattern', 'RULE_RAPID_SUBMISSION', 0, false, 'Submission timing shows normal separation between claims.'),

(2, 'Category Threshold Exceeded', 'RULE_CATEGORY_THRESHOLD', 35, true, 'Claim amount $6,800.00 exceeds the HEALTHCARE threshold of $5,000.00 by $1,800.00 (+35 pts).'),
(2, 'Potential Duplicate Claim Submission', 'RULE_DUPLICATE_CLAIM', 0, false, 'No duplicate submissions detected for this claimant.'),
(2, 'High Claimant Submission Frequency', 'RULE_CLAIMANT_VELOCITY', 0, false, 'Velocity within limits.'),
(2, 'Rapid Consecutive Submission Pattern', 'RULE_RAPID_SUBMISSION', 0, false, 'Submission spacing normal.'),

(3, 'Category Threshold Exceeded', 'RULE_CATEGORY_THRESHOLD', 0, false, 'Claim amount $820.00 is within threshold.'),
(3, 'Potential Duplicate Claim Submission', 'RULE_DUPLICATE_CLAIM', 0, false, 'No matching duplicate amounts found.'),
(3, 'High Claimant Submission Frequency', 'RULE_CLAIMANT_VELOCITY', 30, true, 'High submission velocity: claimant ''CLMT-88416'' submitted 3 claims in a 30-day window (policy threshold: 2) (+30 pts).'),
(3, 'Rapid Consecutive Submission Pattern', 'RULE_RAPID_SUBMISSION', 0, false, 'Submission spacing normal.'),

(5, 'Category Threshold Exceeded', 'RULE_CATEGORY_THRESHOLD', 35, true, 'Claim amount $5,600.00 exceeds the HEALTHCARE threshold of $5,000.00 by $600.00 (+35 pts).'),
(5, 'Potential Duplicate Claim Submission', 'RULE_DUPLICATE_CLAIM', 40, true, 'Potential duplicate detected: identical HEALTHCARE amount of $5,600.00 matches prior claim ''CLM-2026-1021'' submitted 8 day(s) apart (+40 pts).'),
(5, 'High Claimant Submission Frequency', 'RULE_CLAIMANT_VELOCITY', 0, false, 'Claim count within threshold.'),
(5, 'Rapid Consecutive Submission Pattern', 'RULE_RAPID_SUBMISSION', 0, false, 'Spacing within normal boundaries.'),

(6, 'Category Threshold Exceeded', 'RULE_CATEGORY_THRESHOLD', 35, true, 'Claim amount $8,400.00 exceeds the DISABILITY threshold of $7,500.00 by $900.00 (+35 pts).'),
(6, 'Potential Duplicate Claim Submission', 'RULE_DUPLICATE_CLAIM', 40, true, 'Potential duplicate detected: identical DISABILITY amount of $8,400.00 matches prior claim ''CLM-2026-1026'' (+40 pts).'),
(6, 'High Claimant Submission Frequency', 'RULE_CLAIMANT_VELOCITY', 30, true, 'High submission velocity: claimant ''CLMT-88420'' submitted 3 claims in a 30-day window (+30 pts).'),
(6, 'Rapid Consecutive Submission Pattern', 'RULE_RAPID_SUBMISSION', 0, false, 'Spacing normal.');

-- 5. Insert Status History for Claims
INSERT INTO claim_status_history (claim_id, from_status, to_status, changed_by_user_id, changed_by_username, reason, created_at)
VALUES
(1, 'SUBMITTED', 'CLEARED', 2, 'reviewer1', 'All receipts and provider credentials independently verified. Approved for disbursement.', CURRENT_TIMESTAMP),
(2, 'SUBMITTED', 'CLEARED', 2, 'reviewer1', 'Bi-weekly job applications confirmed with state workforce database. Benefit approved.', CURRENT_TIMESTAMP),
(22, 'SUBMITTED', 'FLAGGED', 1, 'system', 'Claim automatically flagged for review due to high risk assessment score (75/100).', CURRENT_TIMESTAMP),
(28, 'SUBMITTED', 'FLAGGED', 1, 'system', 'Claim automatically flagged for review due to high risk assessment score (100/100).', CURRENT_TIMESTAMP),
(29, 'SUBMITTED', 'UNDER_REVIEW', 2, 'reviewer1', 'Initiating formal landlord confirmation contact and county deed record check.', CURRENT_TIMESTAMP),
(29, 'UNDER_REVIEW', 'CONFIRMED_FRAUD', 3, 'reviewer2', 'County tax assessor confirms address is an abandoned commercial warehouse. False residency affirmation recorded.', CURRENT_TIMESTAMP),
(30, 'SUBMITTED', 'UNDER_REVIEW', 2, 'reviewer1', 'Hospital billing documentation requested from claimant for inpatient stay.', CURRENT_TIMESTAMP),
(30, 'UNDER_REVIEW', 'CONFIRMED_FRAUD', 3, 'reviewer2', 'Regional hospital network confirmed patient identifier does not exist in registry. Invoices verified forged.', CURRENT_TIMESTAMP),
(31, 'SUBMITTED', 'CONFIRMED_FRAUD', 3, 'reviewer2', 'Claimant submitted recycled invoices from prior claims under altered synthetic SSN.', CURRENT_TIMESTAMP),
(32, 'SUBMITTED', 'UNDER_REVIEW', 2, 'reviewer1', 'Assigned for clinical audit to verify diagnostic justification.', CURRENT_TIMESTAMP);

-- 6. Insert Audit Logs
INSERT INTO audit_logs (entity_name, entity_id, action, performed_by, details, timestamp)
VALUES
('User', 1, 'SYSTEM_INIT', 'system', 'System initial setup and administrative provisioning', CURRENT_TIMESTAMP),
('Claim', 29, 'STATUS_TRANSITION', 'reviewer2', 'Transitioned status from UNDER_REVIEW to CONFIRMED_FRAUD. Reason: County deed confirms non-residential property.', CURRENT_TIMESTAMP),
('Claim', 30, 'STATUS_TRANSITION', 'reviewer2', 'Transitioned status from UNDER_REVIEW to CONFIRMED_FRAUD. Reason: Forged medical invoices confirmed by hospital.', CURRENT_TIMESTAMP),
('Claim', 1, 'STATUS_TRANSITION', 'reviewer1', 'Transitioned status from SUBMITTED to CLEARED. Reason: Medical claim approved after provider verification.', CURRENT_TIMESTAMP);
