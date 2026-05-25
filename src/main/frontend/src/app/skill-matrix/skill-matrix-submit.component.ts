import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { forkJoin } from 'rxjs';
import { finalize, first } from 'rxjs/operators';
import Swal from 'sweetalert2';
import { LoaderService } from '../services/loader.service';
import { SkillMatrixService } from './skill-matrix.service';
import {
  SKILL_MATRIX_SUBMIT_DEPARTMENTS,
  SkillMatrixSubmitDeptConfig
} from './skill-matrix-submit-depts.data';

const STEP_LABELS = [
  'Basic info',
  'Select skills',
  'Rate & evidence',
  'Project history',
  'Aspirations',
  'Review & submit'
];

const RATING_LABELS = ['Beginner', 'Learner', 'Competent', 'Advanced', 'Expert'];
const RATING_HINTS = [
  'Needs guidance',
  'With supervision',
  'Independently',
  'Mentors others',
  'Domain expert'
];

const PROGRESS_PCT: Record<number, number> = {
  1: 5,
  2: 20,
  3: 40,
  4: 58,
  5: 75,
  6: 90,
  7: 100
};

interface ProjectRow {
  skill: string;
  level: string;
  contrib: string;
}

interface SubmitProjectHistoryRow {
  employeeTeamMapId: number | null;
  projectId: number | null;
  projectName: string | null;
  teamId: number | null;
  teamName: string | null;
  clientName: string | null;
  startDate: string | null;
  endDate: string | null;
  status: string | null;
}

interface ManualProjectDraft {
  tempId: string;
  projectName: string;
  clientName: string;
  duration: string;
  role: string;
  details: string;
}

interface DraftSkillDTO {
  skillId: number;
  skillName: string;
  required: boolean;
  selfRating: number;
  yearsExperience?: string;
  lastUsed?: string;
  usageFrequency?: string;
  whatCanYouDo: string;
  usedInProject?: boolean;
  githubPortfolioUrl?: string;
  colleagueEndorser?: string;
  knowledgeSessionNote?: string;
  certifications?: DraftSkillCertificationDTO[];
  trainings?: DraftSkillTrainingDTO[];
  selectedSubskills: { subskillId: number; subskillName: string }[];
}

interface DraftSkillCertificationDTO {
  certName: string;
  issuingBody: string;
  dateObtained: string; // yyyy-MM-dd
  expiryType?: string | null;
  expiryDate?: string | null; // yyyy-MM-dd
  credentialId?: string | null;
  credentialUrl?: string | null;
  fileReferenceKey?: string | null;
  originalFilename?: string | null;
  fileSizeBytes?: number | null;
  fileMimeType?: string | null;
}

interface DraftSkillTrainingDTO {
  courseName: string;
  platformInstitute?: string | null;
  completionYear?: number | null;
}

interface DraftProjectSkillDTO {
  skillId: number;
  skillName: string;
  levelUsed: number;
  specificContribution: string;
}

interface DraftProjectDTO {
  assessmentProjectId?: number | null;
  projectSource: 'hrms' | 'self_added';
  hrmsProjectId: number | null;
  projectName: string;
  clientOrType: string | null;
  projectStatus: string | null;
  startDate: string | null;
  endDate: string | null;
  durationText: string | null;
  employeeRole: string | null;
  allocationPct: number | null;
  contributionSummary: string;
  included: boolean;
  domainSpecific: boolean;
  skillDomainId: number | null;
  skillSubdomainId: number | null;
  skillDomainFeatureId: number | null;
  skillsApplied: DraftProjectSkillDTO[];
}

/** Mirrors {@code SkillMatrixSubmitPickSkillDTO} from the submit skill-pool API. */
interface SubmitPickSkill {
  skillId: number;
  skillName: string;
  skillType: string;
  categoryId?: number;
  categoryName?: string;
  subskills: { subskillId: number; subskillName: string }[];
}

interface SubmitCategoryRow {
  categoryId: number;
  categoryName: string;
}

@Component({
  standalone: false,
  selector: 'app-skill-matrix-submit',
  templateUrl: './skill-matrix-submit.component.html',
  styleUrls: ['./skill-matrix-submit.component.css']
})
export class SkillMatrixSubmitComponent implements OnInit {
  readonly stepLabels = STEP_LABELS;
  readonly ratingLabels = RATING_LABELS;
  readonly ratingHints = RATING_HINTS;

  pingError: string | null = null;
  contextLoading = true;
  contextError: string | null = null;
  isLocked = false;
  lockSubmissionId: string | null = null;
  lockPendingWith: string | null = null;
  lockPendingWithName: string | null = null;
  /** Add-skills flow: shows baseline approved data (read-only). */
  isAddSkillsMode = false;
  baseApprovedSubmissionId: string | null = null;
  approvedBlock = false;
  approvedBlockSubmissionId: string | null = null;
  rejectedBlock = false;
  rejectedBlockSubmissionId: string | null = null;
  approvedBaseline: any | null = null;
  private approvedSkillIdSet = new Set<number>();
  /** Last successful API payload (employee + skills). */
  submitContext: Record<string, unknown> | null = null;

  curStep = 1;
  stepError: string | null = null;

  selectedDept: SkillMatrixSubmitDeptConfig | null = null;

  requiredPool: SubmitPickSkill[] = [];
  optionalPool: SubmitPickSkill[] = [];
  /** Primitive ids for {@link app-my-select} (same pattern as Skill Matrix master configuration). */
  requiredPickIds: number[] = [];
  optionalPickIds: number[] = [];
  customSkillPicks: SubmitPickSkill[] = [];
  pendingCustomSkillRequests: { requestId: number; skillName: string; status: string }[] = [];

  categoryOptions: SubmitCategoryRow[] = [];
  selectedCategoryId: number | null = null;
  customSkillName = '';

  /** When true, show skill name / category / add button for proposing a skill not in the list. */
  showProposeSkillForm = false;

  skillProposeError: string | null = null;

  empName = '';
  empId = '';
  empMgr = '';
  hodName = '';
  doj = '';
  empRole = '';
  experienceInRole = '';
  mobileNo = '';
  deptDisplay = '';

  goalRole = '';

  /** Skill index → level 1–5 */
  ratings: Record<number, number> = {};
  expandedSkill = new Set<number>();
  certOn: Record<number, boolean> = {};
  certUploadLabel: Record<number, string> = {};
  certUploadError: Record<number, string> = {};
  /** Step 3: show Training & course fields when on (per skill index). */
  trainingOn: Record<number, boolean> = {};
  /** Step 3: show Additional evidence fields when on (per skill index). */
  additionalEvidenceOn: Record<number, boolean> = {};
  subChecked: Record<string, boolean> = {};

  aspPicked = new Set<string>();
  /** Free-text skills from Step 5 (merged into `aspirationSkillNames` on save; not required to match chip pool). */
  aspirationOtherSkillsText = '';
  /** When API returns at least one chip, `aspirationsPool()` uses this list instead of static dept config. */
  private aspirationChipsApiReady = false;
  private aspirationChipsApiList: string[] = [];
  /** Draft aspiration names kept until chip API returns so chip vs “other” split uses the DB-backed pool. */
  private pendingAspirationDraftNames: string[] | null = null;

  /** Project detail panels expanded (step 4). */
  projectDetail0 = true;
  projectDetail1 = true;
  projectDetail2 = false;
  ps0Rows: ProjectRow[] = [];
  ps1Rows: ProjectRow[] = [];

  projectHistory: SubmitProjectHistoryRow[] = [];
  /** Combined list (DB + manual drafts). Keep as array to avoid expensive recompute during change detection. */
  allProjects: SubmitProjectHistoryRow[] = [];
  projectHistoryLoading = false;
  projectHistoryError: string | null = null;
  projectOpen: boolean[] = [];
  projectSkillRows: Record<string, ProjectRow[]> = {};

  manualProjects: ManualProjectDraft[] = [];
  manualDraft: ManualProjectDraft = this.blankManualDraft();
  private manualIdToTempId: Record<number, string> = {};

  submissionId: string | null = null;
  draftSaving = false;
  draftSavedAt: string | null = null;
  draftError: string | null = null;
  private draftLoaded = false;
  /** Latest draft project rows from server; merged into UI after `allProjects` is rebuilt (e.g. when HRMS history loads). */
  private draftProjectsSnapshot: any[] | null = null;

  /** Step 3 text per skill index (draft) */
  whatCanDo: Record<number, string> = {};
  yearsExperience: Record<number, string> = {};
  lastUsed: Record<number, string> = {};
  usageFrequency: Record<number, string> = {};
  usedInProject: Record<number, boolean> = {};
  githubPortfolioUrl: Record<number, string> = {};
  colleagueEndorser: Record<number, string> = {};
  knowledgeSessionNote: Record<number, string> = {};

  /** Step 3 Certification fields per skill index (draft) */
  certName: Record<number, string> = {};
  certIssuingBody: Record<number, string> = {};
  certDateObtained: Record<number, string> = {};
  certExpiryType: Record<number, string> = {};
  certCredential: Record<number, string> = {};
  certFileReferenceKey: Record<number, string> = {};
  certOriginalFilename: Record<number, string> = {};
  certFileSizeBytes: Record<number, number> = {};
  certFileMimeType: Record<number, string> = {};

  /** Step 3 Training fields per skill index (draft) */
  trainingCourseName: Record<number, string> = {};
  trainingPlatform: Record<number, string> = {};
  trainingCompletionYear: Record<number, string> = {};

  /** Step 4 per project index (draft) */
  projRole: Record<number, string> = {};
  projAlloc: Record<number, number> = {};
  projSummary: Record<number, string> = {};

  /** Step 4: map project to skill domain master (optional per row). */
  submitSkillDomainOptions: { domainId: number; domainName: string }[] = [];
  private submitDomainsLoaded = false;
  projDomainSpecific: boolean[] = [];
  projDomainId: (number | null)[] = [];
  projSubdomainId: (number | null)[] = [];
  projFeatureId: (number | null)[] = [];
  projHasSubdomains: boolean[] = [];
  projSubdomainOptions: { subdomainId: number; subdomainName: string }[][] = [];
  projFeatureOptions: { featureId: number; featureName: string }[][] = [];

  /** Step 5 */
  messageToManager = '';

  doneManager = '';
  doneRefId = '';
  doneRefWhen = '';

  /** When coming from My Submissions → Edit (rejected), we pass submissionId in query param. */
  editSubmissionId: string = '';
  /** skillId -> manager decision/comment (for rejected/changes requested edit UX) */
  private reviewNotesBySkillId: Record<number, { decision: string; comment: string }> = {};
  /** skillName(normalized) -> decision/comment (fallback when skillId mapping isn't ready yet). */
  private reviewNotesBySkillName: Record<string, { decision: string; comment: string }> = {};
  /** Original draft snapshot to detect if employee updated a rejected skill. */
  private draftSnapshotBySkillId: Record<number, {
    rating: number;
    years: string;
    lastUsed: string;
    freq: string;
    what: string;
    usedInProject: boolean;
    github: string;
    endorser: string;
    ks: string;
    subskillNames: string[];
  }> = {};

  constructor(
    private skillMatrixService: SkillMatrixService,
    private loaderService: LoaderService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.loadSubmitWorkspace();
  }

  private loadSubmitWorkspace(): void {
    this.contextLoading = true;
    this.pingError = null;
    this.contextError = null;
    const mode = String(this.route?.snapshot?.queryParamMap?.get('mode') || '').trim().toLowerCase();
    this.isAddSkillsMode = mode === 'add-skills';
    const baseSid = String(this.route?.snapshot?.queryParamMap?.get('baseSubmissionId') || '').trim();
    this.baseApprovedSubmissionId = baseSid || null;
    const editSid = String(this.route?.snapshot?.queryParamMap?.get('submissionId') || '').trim();
    this.approvedBlock = false;
    this.approvedBlockSubmissionId = null;
    this.rejectedBlock = false;
    this.rejectedBlockSubmissionId = null;
    forkJoin({
      ping: this.skillMatrixService.pingSubmitForReview(),
      ctx: this.skillMatrixService.getSubmitForReviewContext(),
      lock: this.skillMatrixService.getSubmitLockStatus()
    })
      .pipe(
        first(),
        finalize(() => {
          this.contextLoading = false;
        })
      )
      .subscribe({
        next: ({ ping, ctx, lock }: { ping: any; ctx: any; lock: any }) => {
          if (ping?.serviceStatus !== 'Success') {
            this.pingError =
              typeof ping?.serviceResponse === 'string'
                ? ping.serviceResponse
                : 'Submit for review is not available right now.';
          }
          try {
            const b = lock?.serviceStatus === 'Success' ? lock.serviceResponse : null;
            if (b?.locked) {
              this.isLocked = true;
              this.lockSubmissionId = String(b.submissionId || '');
              this.lockPendingWith = String(b.pendingWith || '');
              this.lockPendingWithName = String(b.pendingWithName || '');
              return;
            }
            const fin = String(b?.finalStatus || '').trim().toLowerCase();
            const subId = String(b?.submissionId || '').trim();
            // When latest submission is rejected, block opening a fresh Submit-for-review form.
            // Employee must use My Submissions -> Edit (submissionId param) to edit/resubmit.
            if (fin === 'rejected' && !this.isAddSkillsMode && !editSid) {
              this.rejectedBlock = true;
              this.rejectedBlockSubmissionId = subId || null;
              return;
            }
            const hasApproved = !!b?.hasApproved;
            const approvedSid = String(b?.approvedSubmissionId || '').trim();
            if (hasApproved && approvedSid) {
              if (this.isAddSkillsMode) {
                this.baseApprovedSubmissionId = this.baseApprovedSubmissionId || approvedSid;
              } else if (!editSid) {
                // Block only "new submission" flow. Editing an existing rejected/changes_requested submission
                // via ?submissionId=... must still be allowed even if an approved baseline exists.
                this.approvedBlock = true;
                this.approvedBlockSubmissionId = approvedSid;
              } else {
                // Edit rejected submission: show approved baseline as readonly reference.
                this.baseApprovedSubmissionId = approvedSid;
              }
            }
          } catch {
            // ignore lock status errors; do not block form
          }
          if (ctx?.serviceStatus === 'Success' && ctx.serviceResponse) {
            this.applySubmitContext(ctx.serviceResponse as Record<string, unknown>);
            this.loadSubmitAspirationChips();
            if (this.isAddSkillsMode) {
              this.loadApprovedBaselineIfAny();
              // In add-skills mode we start a fresh draft for newly added skills.
              this.submissionId = '';
              this.draftLoaded = false;
            } else {
              if (this.baseApprovedSubmissionId) {
                this.loadApprovedBaselineIfAny();
              }
              this.loadDraftIfAny();
            }
          } else {
            this.contextError =
              typeof ctx?.serviceResponse === 'string'
                ? ctx.serviceResponse
                : 'Could not load your employee profile for this form.';
          }
        },
        error: () => {
          /* Intentionally no banner: local dev often has no backend; form stays usable when empty. */
        }
      });
  }

  private loadApprovedBaselineIfAny(): void {
    const sid = String(this.baseApprovedSubmissionId || '').trim();
    if (!sid) return;
    this.skillMatrixService
      .getApprovedBaseline(sid)
      .pipe(first())
      .subscribe({
        next: (res: any) => {
          if (res?.serviceStatus !== 'Success') return;
          this.approvedBaseline = res.serviceResponse;
          this.approvedSkillIdSet = new Set<number>();
          try {
            const skills = Array.isArray(this.approvedBaseline?.skills) ? this.approvedBaseline.skills : [];
            skills.forEach((s: any) => {
              const id = Number(s?.skillId || 0);
              if (id) this.approvedSkillIdSet.add(id);
            });
          } catch {
            // ignore
          }
          // When not in Add Skills mode, approved baseline is reference-only.
          if (!this.isAddSkillsMode) {
            return;
          }
          // Step 4: Allow editing existing project details by pre-filling the editable UI
          // with approved baseline projects (same merge path as draft editing).
          try {
            const prows = Array.isArray(this.approvedBaseline?.projects) ? this.approvedBaseline.projects : [];
            // In Add Skills: show ONLY the projects that were previously not included (is_included = 0),
            // and allow editing/updating those rows without inserting duplicates.
            const excluded = prows.filter((p: any) => p?.included === false);
            this.draftProjectsSnapshot = excluded.length ? excluded.map((x: any) => ({ ...x })) : null;
            // Build the Step-4 project list from these excluded baseline rows (not from HRMS history).
            this.projectHistory = excluded.map((p: any) => ({
              employeeTeamMapId: p?.assessmentProjectId != null ? +p.assessmentProjectId : null,
              projectId: p?.hrmsProjectId != null ? +p.hrmsProjectId : null,
              projectName: String(p?.projectName || ''),
              teamId: null,
              teamName: null,
              clientName: p?.clientOrType != null ? String(p.clientOrType) : null,
              startDate: p?.startDate != null ? String(p.startDate) : null,
              endDate: p?.endDate != null ? String(p.endDate) : null,
              status: p?.projectStatus != null ? String(p.projectStatus) : null
            }));
            this.manualProjects = [];
            this.rebuildAllProjects();
            this.rebuildProjectOpenState();
          } catch {
            this.draftProjectsSnapshot = null;
          }

          // Step 5: Aspirations are read-only in Add Skills mode; prefill from approved baseline.
          try {
            this.goalRole = String(this.approvedBaseline?.targetRole2yr || this.goalRole || '');
            this.messageToManager = String(this.approvedBaseline?.messageToManager || this.messageToManager || '');
            this.aspPicked.clear();
            const asps = Array.isArray(this.approvedBaseline?.aspirationSkillNames)
              ? (this.approvedBaseline.aspirationSkillNames as any[])
              : [];
            asps
              .map((x: any) => String(x || '').trim())
              .filter((t: string) => !!t)
              .forEach((t: string) => this.aspPicked.add(t));
            // Approved baseline does not store a separate free-text field; keep it empty in add-skills mode.
            this.aspirationOtherSkillsText = '';
          } catch {
            // ignore
          }
          this.filterSkillPoolsForAddMode();
        }
      });
  }

  isApprovedSkillName(skillName: string): boolean {
    const id = this.skillIdByName(skillName);
    return id != null && this.approvedSkillIdSet?.has(id);
  }

  private filterSkillPoolsForAddMode(): void {
    if (!this.isAddSkillsMode || !this.approvedSkillIdSet?.size) return;
    this.requiredPool = (this.requiredPool || []).filter((s) => !this.approvedSkillIdSet.has(Number((s as any)?.skillId || 0)));
    this.optionalPool = (this.optionalPool || []).filter((s) => !this.approvedSkillIdSet.has(Number((s as any)?.skillId || 0)));
    // Also clear any picks that accidentally match an approved skill.
    this.requiredPickIds = (this.requiredPickIds || []).filter((id) => !this.approvedSkillIdSet.has(Number(id)));
    this.optionalPickIds = (this.optionalPickIds || []).filter((id) => !this.approvedSkillIdSet.has(Number(id)));
  }

  approvedSkillNames(): string[] {
    const skills = Array.isArray(this.approvedBaseline?.skills) ? this.approvedBaseline.skills : [];
    return skills.map((s: any) => String(s?.skillName || '')).filter((t: string) => !!t);
  }

  approvedProjects(): any[] {
    return Array.isArray(this.approvedBaseline?.projects) ? this.approvedBaseline.projects : [];
  }

  approvedAspirations(): string[] {
    return Array.isArray(this.approvedBaseline?.aspirationSkillNames) ? this.approvedBaseline.aspirationSkillNames : [];
  }

  // Lock UI is rendered inside template (not a global popup),
  // so employee can still navigate to other tabs like My Submissions.

  private loadDraftIfAny(): void {
    const sid = String(this.route?.snapshot?.queryParamMap?.get('submissionId') || '').trim();
    this.editSubmissionId = sid;
    const req$ = sid ? this.skillMatrixService.loadSubmitDraftById(sid) : this.skillMatrixService.loadSubmitDraft();

    req$
      .pipe(first())
      .subscribe({
        next: (res: any) => {
          if (res?.serviceStatus !== 'Success') {
            return;
          }
          const d = res.serviceResponse;
          if (!d?.submissionId) {
            this.draftProjectsSnapshot = null;
            return;
          }
          this.submissionId = d.submissionId;
          this.draftLoaded = true;

          // Show rejection reasons per skill when employee edits a rejected submission.
          try {
            const reasons: { skillName: string; by: string; comment: string }[] = [];
            this.reviewNotesBySkillId = {};
            this.reviewNotesBySkillName = {};
            this.draftSnapshotBySkillId = {};
            if (Array.isArray(d.skills)) {
              d.skills.forEach((s: any) => {
                const sid2 = Number(s?.skillId || 0);
                const mdec = String(s?.managerDecision || '').trim().toLowerCase();
                const mcmt = String(s?.managerComment || '').trim();
                const hdec = String(s?.hodDecision || '').trim().toLowerCase();
                const hcmt = String(s?.hodComment || '').trim();
                // snapshot original values for change detection (even if there is no manager comment)
                if (sid2) {
                  const subs = Array.isArray(s?.selectedSubskills) ? s.selectedSubskills : [];
                  this.draftSnapshotBySkillId[sid2] = {
                    rating: Number(s?.selfRating || 0),
                    years: String(s?.yearsExperience || ''),
                    lastUsed: String(s?.lastUsed || ''),
                    freq: String(s?.usageFrequency || ''),
                    what: String(s?.whatCanYouDo || ''),
                    usedInProject: !!s?.usedInProject,
                    github: String(s?.githubPortfolioUrl || ''),
                    endorser: String(s?.colleagueEndorser || ''),
                    ks: String(s?.knowledgeSessionNote || ''),
                    subskillNames: subs.map((x: any) => String(x?.subskillName || '')).filter((t: string) => !!t)
                  };
                }
                const skillName = String(s?.skillName || 'Skill');
                const skillKey = skillName.trim().toLowerCase();
                // Prefer HOD rejection if present; else manager decision.
                if (hdec && hcmt && (hdec === 'rejected' || hdec === 'sent_back' || hdec === 'adjusted')) {
                  reasons.push({
                    skillName,
                    by: `HOD: ${this.hodName || '—'}`,
                    comment: hcmt
                  });
                  if (sid2) {
                    this.reviewNotesBySkillId[sid2] = { decision: hdec, comment: hcmt };
                  }
                  if (skillKey) {
                    this.reviewNotesBySkillName[skillKey] = { decision: hdec, comment: hcmt };
                  }
                  return;
                }
                if (mdec && mcmt && (mdec === 'rejected' || mdec === 'sent_back' || mdec === 'adjusted')) {
                  reasons.push({
                    skillName,
                    by: `Manager: ${this.empMgr || '—'}`,
                    comment: mcmt
                  });
                  if (sid2) {
                    this.reviewNotesBySkillId[sid2] = { decision: mdec, comment: mcmt };
                  }
                  if (skillKey) {
                    this.reviewNotesBySkillName[skillKey] = { decision: mdec, comment: mcmt };
                  }
                }
              });
            }
            if (sid && reasons.length) {
              const rows = reasons
                .map((r) => `<tr>
                  <td style="padding:10px 12px;border-bottom:1px solid #e9eef7;font-weight:700;color:#193d8a;vertical-align:top">${this.escapeHtml(r.skillName)}</td>
                  <td style="padding:10px 12px;border-bottom:1px solid #e9eef7;white-space:nowrap;color:#6b7280;font-weight:700;vertical-align:top">${this.escapeHtml(r.by)}</td>
                  <td style="padding:10px 12px;border-bottom:1px solid #e9eef7;vertical-align:top">${this.escapeHtml(r.comment)}</td>
                </tr>`)
                .join('');
              const html = `<div style="text-align:left">
                <div style="margin:0 0 10px;font-size:12px;color:#6b7280">These are the rejection comments per skill. Update and resubmit.</div>
                <div style="border:1px solid #e6ebf5;border-radius:12px;overflow:hidden">
                  <table style="width:100%;border-collapse:collapse;font-size:13px">
                    <thead>
                      <tr style="background:#f7f9ff">
                        <th style="padding:10px 12px;text-align:left;border-bottom:1px solid #e9eef7;font-size:11px;letter-spacing:.4px;text-transform:uppercase;color:#6b7280">Skill</th>
                        <th style="padding:10px 12px;text-align:left;border-bottom:1px solid #e9eef7;font-size:11px;letter-spacing:.4px;text-transform:uppercase;color:#6b7280">Rejected by</th>
                        <th style="padding:10px 12px;text-align:left;border-bottom:1px solid #e9eef7;font-size:11px;letter-spacing:.4px;text-transform:uppercase;color:#6b7280">Reason</th>
                      </tr>
                    </thead>
                    <tbody>${rows}</tbody>
                  </table>
                </div>
              </div>`;
              Swal.fire({
                title: 'Rejection reason(s)',
                html,
                confirmButtonText: 'OK',
                confirmButtonColor: '#193d8a',
                width: 760,
                customClass: { popup: 'smSwalRejection' }
              });
            }
          } catch {
            // ignore
          }
          // skills
          if (Array.isArray(d.skills)) {
            const reqIds: number[] = [];
            const optIds: number[] = [];
            const ratings: Record<number, number> = {};
            const what: Record<number, string> = {};
            const years: Record<number, string> = {};
            const last: Record<number, string> = {};
            const freq: Record<number, string> = {};
            const used: Record<number, boolean> = {};
            const gh: Record<number, string> = {};
            const end: Record<number, string> = {};
            const ks: Record<number, string> = {};
            const certOnById: Record<number, boolean> = {};
            const certNameById: Record<number, string> = {};
            const certIssuingById: Record<number, string> = {};
            const certDobById: Record<number, string> = {};
            const certExpiryTypeById: Record<number, string> = {};
            const certCredById: Record<number, string> = {};
            const certFileKeyById: Record<number, string> = {};
            const certOrigById: Record<number, string> = {};
            const certSizeById: Record<number, number> = {};
            const certMimeById: Record<number, string> = {};
            const trOnById: Record<number, boolean> = {};
            const trCourseById: Record<number, string> = {};
            const trPlatformById: Record<number, string> = {};
            const trYearById: Record<number, string> = {};
            const subs: Record<string, boolean> = {};
            d.skills.forEach((s: any) => {
              const id = +s.skillId;
              if (!id) return;
              if (s.required) reqIds.push(id);
              else optIds.push(id);
              // rating map uses skill-index, so we rebuild after pools are loaded
              // store temp by skillId -> rating
              ratings[id] = +s.selfRating || 0;
              what[id] = s.whatCanYouDo || '';
              years[id] = s.yearsExperience || '';
              last[id] = s.lastUsed || '';
              freq[id] = s.usageFrequency || '';
              used[id] = !!s.usedInProject;
              gh[id] = s.githubPortfolioUrl || '';
              end[id] = s.colleagueEndorser || '';
              ks[id] = s.knowledgeSessionNote || '';
              // certifications (first one only; UI supports 0..1 per skill)
              const c0 = Array.isArray(s.certifications) && s.certifications.length ? s.certifications[0] : null;
              if (c0?.certName || c0?.issuingBody || c0?.dateObtained) {
                certOnById[id] = true;
                certNameById[id] = String(c0?.certName || '');
                certIssuingById[id] = String(c0?.issuingBody || '');
                certDobById[id] = String(c0?.dateObtained || '');
                certExpiryTypeById[id] = String(c0?.expiryType || '');
                // draft may return credentialId or credentialUrl
                certCredById[id] = String(c0?.credentialUrl || c0?.credentialId || '');
                certFileKeyById[id] = String(c0?.fileReferenceKey || '');
                certOrigById[id] = String(c0?.originalFilename || '');
                certSizeById[id] = Number(c0?.fileSizeBytes || 0);
                certMimeById[id] = String(c0?.fileMimeType || '');
              }
              const t0 = Array.isArray(s.trainings) && s.trainings.length ? s.trainings[0] : null;
              if (t0?.courseName || t0?.platformInstitute || t0?.completionYear != null) {
                trOnById[id] = true;
                trCourseById[id] = String(t0?.courseName || '');
                trPlatformById[id] = String(t0?.platformInstitute || '');
                trYearById[id] = t0?.completionYear != null ? String(t0.completionYear) : '';
              }
              if (Array.isArray(s.selectedSubskills)) {
                s.selectedSubskills.forEach((ss: any) => {
                  if (ss?.subskillId) subs[`${id}:${+ss.subskillId}`] = true;
                });
              }
            });
            // Defer applying pickIds until pools are loaded (loadSkillPools() clears/overwrites pools).
            (this as any)._draftReqIds = reqIds;
            (this as any)._draftOptIds = optIds;
            // store temp into maps used later by index mapping
            (this as any)._draftRatingsBySkillId = ratings;
            (this as any)._draftWhatBySkillId = what;
            (this as any)._draftYearsBySkillId = years;
            (this as any)._draftLastUsedBySkillId = last;
            (this as any)._draftUsageFreqBySkillId = freq;
            (this as any)._draftUsedInProjectBySkillId = used;
            (this as any)._draftGithubBySkillId = gh;
            (this as any)._draftEndorserBySkillId = end;
            (this as any)._draftKsBySkillId = ks;
            (this as any)._draftCertOnBySkillId = certOnById;
            (this as any)._draftCertNameBySkillId = certNameById;
            (this as any)._draftCertIssuingBySkillId = certIssuingById;
            (this as any)._draftCertDobBySkillId = certDobById;
            (this as any)._draftCertExpiryTypeBySkillId = certExpiryTypeById;
            (this as any)._draftCertCredBySkillId = certCredById;
            (this as any)._draftCertFileKeyBySkillId = certFileKeyById;
            (this as any)._draftCertOrigBySkillId = certOrigById;
            (this as any)._draftCertSizeBySkillId = certSizeById;
            (this as any)._draftCertMimeBySkillId = certMimeById;
            (this as any)._draftTrOnBySkillId = trOnById;
            (this as any)._draftTrCourseBySkillId = trCourseById;
            (this as any)._draftTrPlatformBySkillId = trPlatformById;
            (this as any)._draftTrYearBySkillId = trYearById;
            (this as any)._draftSubsBySkillId = subs;

            // If pools are already loaded, apply immediately (draft can load after pools due to async timing).
            if (this.requiredPool.length || this.optionalPool.length) {
              this.applyDraftToPools();
            }
          }
          // projects
          if (Array.isArray(d.projects)) {
            this.draftProjectsSnapshot = d.projects.length ? d.projects.map((x: any) => ({ ...x })) : null;
            this.manualProjects = [];
            const manual: ManualProjectDraft[] = [];
            d.projects.forEach((p: any) => {
              if (p?.projectSource === 'self_added') {
                manual.push({
                  tempId: `m${Date.now().toString(36)}${Math.random().toString(36).slice(2, 6)}`,
                  projectName: p.projectName || '',
                  clientName: p.clientOrType || '',
                  duration: p.durationText || '',
                  role: p.employeeRole || '',
                  details: p.contributionSummary || ''
                });
              }
            });
            this.manualProjects = manual;
            this.rebuildAllProjects();
            this.rebuildProjectOpenState();
          } else {
            this.draftProjectsSnapshot = null;
          }
          // aspirations
          this.goalRole = d.targetRole2yr || this.goalRole;
          this.messageToManager = d.messageToManager || '';
          if (Array.isArray(d.aspirationSkillNames)) {
            this.pendingAspirationDraftNames = d.aspirationSkillNames.filter((x: any) => typeof x === 'string');
            this.applyDraftAspirationSkillNames(this.pendingAspirationDraftNames);
            if (this.aspirationChipsApiReady) {
              this.pendingAspirationDraftNames = null;
            }
          } else {
            this.pendingAspirationDraftNames = null;
          }
        },
        error: () => {
          /* ignore */
        }
      });
  }

  private skillIdByName(skillName: string): number | null {
    const nm = String(skillName || '').trim();
    if (!nm) return null;
    const all: any[] = [...this.requiredPicksOrdered, ...this.optionalPicksOrdered, ...this.customSkillPicks];
    const hit = all.find((s) => String(s?.skillName || '').trim() === nm);
    return hit?.skillId ? Number(hit.skillId) : null;
  }

  reviewNoteForSkillName(skillName: string): { decision: string; comment: string } | null {
    const sid = this.skillIdByName(skillName);
    if (!sid) return null;
    const note = this.reviewNotesBySkillId[sid];
    if (note?.comment) {
      return note;
    }
    const key = String(skillName || '').trim().toLowerCase();
    const byName = key ? this.reviewNotesBySkillName[key] : null;
    return byName?.comment ? byName : null;
  }

  wasRejectedSkillUpdated(skillName: string): boolean {
    const sid = this.skillIdByName(skillName);
    if (!sid) return false;
    if (!this.reviewNotesBySkillId[sid]?.comment) return false;
    const snap = this.draftSnapshotBySkillId[sid];
    if (!snap) return false;
    const idx = this.skillIndexById(sid);
    const curRating = Number(this.ratings[idx] || 0);
    const curWhat = String(this.whatCanDo[idx] || '');
    const curYears = String(this.yearsExperience[idx] || '');
    const curLast = String(this.lastUsed[idx] || '');
    const curFreq = String(this.usageFrequency[idx] || '');
    const curUsed = !!this.usedInProject[idx];
    const curGh = String(this.githubPortfolioUrl[idx] || '');
    const curEnd = String(this.colleagueEndorser[idx] || '');
    const curKs = String(this.knowledgeSessionNote[idx] || '');
    const subNames = this.subsFor(skillName) || [];
    const curSubs = subNames
      .map((nm, si) => (this.isSubChecked(idx, si) ? String(nm || '').trim() : ''))
      .filter((t) => !!t)
      .sort((a, b) => a.localeCompare(b));
    const snapSubs = (snap.subskillNames || []).slice().map((t) => String(t || '').trim()).filter(Boolean).sort((a, b) => a.localeCompare(b));
    const sameSubs = curSubs.length === snapSubs.length && curSubs.every((v, i) => v === snapSubs[i]);
    return (
      curRating !== Number(snap.rating || 0) ||
      curWhat !== String(snap.what || '') ||
      curYears !== String(snap.years || '') ||
      curLast !== String(snap.lastUsed || '') ||
      curFreq !== String(snap.freq || '') ||
      curUsed !== !!snap.usedInProject ||
      curGh !== String(snap.github || '') ||
      curEnd !== String(snap.endorser || '') ||
      curKs !== String(snap.ks || '') ||
      !sameSubs
    );
  }

  rejectionLabel(skillName: string): string {
    return this.wasRejectedSkillUpdated(skillName) ? 'Updated' : 'Needs update';
  }

  openReviewNote(skillName: string, ev?: MouseEvent): void {
    if (ev) {
      ev.preventDefault();
      ev.stopPropagation();
    }
    const note = this.reviewNoteForSkillName(skillName);
    if (!note) return;
    const decidedBy = String((this as any).empMgr || '').trim() || '—';
    const decisionStatus = String(note.decision || '').replace(/_/g, ' ').trim();
    Swal.fire({
      title: '',
      html: `<div style="text-align:left">
        <div style="display:grid;grid-template-columns:110px 1fr;gap:6px 10px;align-items:start">
          <div style="font-size:10px;letter-spacing:.35px;text-transform:uppercase;color:#6b7280;font-weight:800">Skill</div>
          <div style="font-weight:800;color:#193d8a;font-size:13px;line-height:1.2;word-break:break-word">${this.escapeHtml(skillName)}</div>

          <div style="font-size:10px;letter-spacing:.35px;text-transform:uppercase;color:#6b7280;font-weight:800">Decision Status</div>
          <div>
            <span style="display:inline-flex;align-items:center;padding:2px 8px;border-radius:999px;
              background:rgba(180,35,24,.08);border:1px solid rgba(180,35,24,.18);color:#b42318;
              font-size:10px;font-weight:800;text-transform:capitalize">
              ${this.escapeHtml(decisionStatus)}
            </span>
          </div>

          <div style="font-size:10px;letter-spacing:.35px;text-transform:uppercase;color:#6b7280;font-weight:800">Decision by</div>
          <div style="font-size:12px;font-weight:700;color:#111827;word-break:break-word">${this.escapeHtml(decidedBy)}</div>
        </div>

        <div style="margin-top:10px;font-size:10px;letter-spacing:.35px;text-transform:uppercase;color:#6b7280;font-weight:800">Decision Comments</div>
        <div style="margin-top:5px;padding:8px 10px;border:1px solid #e6ebf5;border-radius:8px;background:#f9fbff;
          font-size:12px;line-height:1.4;color:#111827;white-space:pre-wrap;word-break:break-word">
          ${this.escapeHtml(note.comment)}
        </div>
      </div>`,
      confirmButtonText: 'OK',
      confirmButtonColor: '#193d8a',
      showCloseButton: false,
      width: 320,
      padding: '10px 12px',
      customClass: { popup: 'smSwalTiny' }
    });
  }

  private escapeHtml(v: string): string {
    return String(v || '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  private applySubmitContext(ctx: Record<string, unknown>): void {
    this.submitContext = ctx;
    this.empName = (ctx['fullName'] as string) ?? '';
    this.empId = (ctx['employmentId'] as string) ?? '';
    this.empMgr = (ctx['reportingManagerName'] as string) ?? '';
    this.hodName = String(ctx['hodName'] ?? '').trim();
    this.hodRevName = this.hodName || this.hodRevName;
    this.doj = this.formatDojIso(ctx['dateOfJoining'] as string | null | undefined);
    this.empRole = (ctx['designation'] as string) ?? '';
    this.experienceInRole = (ctx['experience'] as string) ?? '';
    this.mobileNo = (ctx['mobileNumber'] as string) ?? '';
    this.deptDisplay = (ctx['departmentName'] as string) ?? '';
    this.selectedDept = this.buildSelectedDeptFromContext(ctx);
    this.pickDeptResetSkillState();
    this.loadSkillPools();
  }

  private formatDojIso(iso: string | null | undefined): string {
    if (!iso || typeof iso !== 'string') {
      return '';
    }
    const d = new Date(`${iso}T12:00:00`);
    if (Number.isNaN(d.getTime())) {
      return iso;
    }
    return d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
  }

  private aspirationsForDept(deptName: string): string[] {
    const cfg = SKILL_MATRIX_SUBMIT_DEPARTMENTS.find((d) => d.name === deptName);
    return (
      cfg?.aspirations ?? [
        `Senior ${deptName} track`,
        'Cross-functional leadership',
        'Industry certification'
      ]
    );
  }

  private buildSelectedDeptFromContext(ctx: Record<string, unknown>): SkillMatrixSubmitDeptConfig | null {
    const deptName = (ctx['departmentName'] as string) ?? '';
    if (!deptName) {
      return null;
    }
    return {
      name: deptName,
      badgeClass: 'b-pu',
      requiredSkills: [],
      optionalSkills: [],
      subsBySkill: {},
      aspirations: this.aspirationsForDept(deptName)
    };
  }

  private loadSkillPools(): void {
    if (this.submitContext?.['departmentId'] == null) {
      return;
    }
    forkJoin({
      req: this.skillMatrixService.getSubmitSkillPool('Required', ''),
      opt: this.skillMatrixService.getSubmitSkillPool('Optional', ''),
      cat: this.skillMatrixService.getSubmitSkillCategories('')
    })
      .pipe(first())
      .subscribe({
        next: ({ req, opt, cat }: { req: any; opt: any; cat: any }) => {
          this.requiredPool = this.unwrapSkillPool(req);
          this.optionalPool = this.unwrapSkillPool(opt);
          this.categoryOptions = this.unwrapCategoryList(cat);
          this.filterSkillPoolsForAddMode();
          // Only clear picks when there is no draft loaded.
          const hasDraftIds = Array.isArray((this as any)._draftReqIds) || Array.isArray((this as any)._draftOptIds);
          if (!this.draftLoaded && !hasDraftIds) {
            this.requiredPickIds = [];
            this.optionalPickIds = [];
          } else {
            this.applyDraftToPools();
          }
        },
        error: () => {
          this.requiredPool = [];
          this.optionalPool = [];
          this.categoryOptions = [];
          this.requiredPickIds = [];
          this.optionalPickIds = [];
        }
      });
  }

  private applyDraftToPools(): void {
    const reqIds: number[] = (this as any)._draftReqIds ?? [];
    const optIds: number[] = (this as any)._draftOptIds ?? [];
    const ratingsById: Record<number, number> = (this as any)._draftRatingsBySkillId ?? {};
    const whatById: Record<number, string> = (this as any)._draftWhatBySkillId ?? {};
    const yearsById: Record<number, string> = (this as any)._draftYearsBySkillId ?? {};
    const lastUsedById: Record<number, string> = (this as any)._draftLastUsedBySkillId ?? {};
    const freqById: Record<number, string> = (this as any)._draftUsageFreqBySkillId ?? {};
    const usedInProjectById: Record<number, boolean> = (this as any)._draftUsedInProjectBySkillId ?? {};
    const githubById: Record<number, string> = (this as any)._draftGithubBySkillId ?? {};
    const endorserById: Record<number, string> = (this as any)._draftEndorserBySkillId ?? {};
    const ksById: Record<number, string> = (this as any)._draftKsBySkillId ?? {};
    const certOnById: Record<number, boolean> = (this as any)._draftCertOnBySkillId ?? {};
    const certNameById: Record<number, string> = (this as any)._draftCertNameBySkillId ?? {};
    const certIssuingById: Record<number, string> = (this as any)._draftCertIssuingBySkillId ?? {};
    const certDobById: Record<number, string> = (this as any)._draftCertDobBySkillId ?? {};
    const certExpiryTypeById: Record<number, string> = (this as any)._draftCertExpiryTypeBySkillId ?? {};
    const certCredById: Record<number, string> = (this as any)._draftCertCredBySkillId ?? {};
    const certFileKeyById: Record<number, string> = (this as any)._draftCertFileKeyBySkillId ?? {};
    const certOrigById: Record<number, string> = (this as any)._draftCertOrigBySkillId ?? {};
    const certSizeById: Record<number, number> = (this as any)._draftCertSizeBySkillId ?? {};
    const certMimeById: Record<number, string> = (this as any)._draftCertMimeBySkillId ?? {};
    const trOnById: Record<number, boolean> = (this as any)._draftTrOnBySkillId ?? {};
    const trCourseById: Record<number, string> = (this as any)._draftTrCourseBySkillId ?? {};
    const trPlatformById: Record<number, string> = (this as any)._draftTrPlatformBySkillId ?? {};
    const trYearById: Record<number, string> = (this as any)._draftTrYearBySkillId ?? {};
    const subsByKey: Record<string, boolean> = (this as any)._draftSubsBySkillId ?? {};

    // Only keep ids that exist in the pool (otherwise my-select shows blank selections).
    const reqSet = new Set(this.requiredPool.map((s) => s.skillId));
    const optSet = new Set(this.optionalPool.map((s) => s.skillId));
    this.requiredPickIds = (reqIds ?? []).filter((id) => reqSet.has(id));
    this.optionalPickIds = (optIds ?? []).filter((id) => optSet.has(id));

    // Apply ratings / text / subskill checks by skill index.
    const all = this.allSkillNames;
    for (let i = 0; i < all.length; i++) {
      const sk = this.findPick(all[i]);
      if (!sk?.skillId) continue;
      const sid = sk.skillId;
      const r = ratingsById[sid] ?? 0;
      if (r) {
        this.ratings = { ...this.ratings, [i]: r };
      }
      const w = whatById[sid] ?? '';
      if (w) {
        this.whatCanDo = { ...this.whatCanDo, [i]: w };
      }
      const ye = yearsById[sid] ?? '';
      if (ye) this.yearsExperience = { ...this.yearsExperience, [i]: ye };
      const lu = lastUsedById[sid] ?? '';
      if (lu) this.lastUsed = { ...this.lastUsed, [i]: lu };
      const uf = freqById[sid] ?? '';
      if (uf) this.usageFrequency = { ...this.usageFrequency, [i]: uf };
      if (usedInProjectById[sid] != null) {
        this.usedInProject = { ...this.usedInProject, [i]: !!usedInProjectById[sid] };
      }
      const gh = githubById[sid] ?? '';
      if (gh) this.githubPortfolioUrl = { ...this.githubPortfolioUrl, [i]: gh };
      const ce = endorserById[sid] ?? '';
      if (ce) this.colleagueEndorser = { ...this.colleagueEndorser, [i]: ce };
      const ks = ksById[sid] ?? '';
      if (ks) this.knowledgeSessionNote = { ...this.knowledgeSessionNote, [i]: ks };

      if (certOnById[sid]) {
        this.certOn = { ...this.certOn, [i]: true };
        this.certName = { ...this.certName, [i]: certNameById[sid] ?? '' };
        this.certIssuingBody = { ...this.certIssuingBody, [i]: certIssuingById[sid] ?? '' };
        this.certDateObtained = { ...this.certDateObtained, [i]: certDobById[sid] ?? '' };
        this.certExpiryType = { ...this.certExpiryType, [i]: certExpiryTypeById[sid] ?? '' };
        this.certCredential = { ...this.certCredential, [i]: certCredById[sid] ?? '' };
        const fk = certFileKeyById[sid] ?? '';
        if (fk) {
          this.certFileReferenceKey = { ...this.certFileReferenceKey, [i]: fk };
          this.certOriginalFilename = { ...this.certOriginalFilename, [i]: certOrigById[sid] ?? '' };
          this.certFileSizeBytes = { ...this.certFileSizeBytes, [i]: certSizeById[sid] ?? 0 };
          this.certFileMimeType = { ...this.certFileMimeType, [i]: certMimeById[sid] ?? '' };
          const label = this.certOriginalFilename[i] ? `✓ ${this.certOriginalFilename[i]} uploaded successfully` : '✓ Certificate uploaded successfully';
          this.certUploadLabel = { ...this.certUploadLabel, [i]: label };
        }
      }

      if (trOnById[sid]) {
        this.trainingOn = { ...this.trainingOn, [i]: true };
        this.trainingCourseName = { ...this.trainingCourseName, [i]: trCourseById[sid] ?? '' };
        this.trainingPlatform = { ...this.trainingPlatform, [i]: trPlatformById[sid] ?? '' };
        this.trainingCompletionYear = { ...this.trainingCompletionYear, [i]: trYearById[sid] ?? '' };
      }
      if (sk.subskills?.length) {
        for (let si = 0; si < sk.subskills.length; si++) {
          const subId = sk.subskills[si]?.subskillId;
          if (!subId) continue;
          if (subsByKey[`${sid}:${subId}`]) {
            this.setSubChecked(i, si, true);
          }
        }
      }
    }
  }

  private unwrapSkillPool(res: any): SubmitPickSkill[] {
    if (res?.serviceStatus !== 'Success' || !Array.isArray(res.serviceResponse)) {
      return [];
    }
    return (res.serviceResponse as any[]).map((row) => this.mapApiToPickSkill(row));
  }

  private unwrapCategoryList(res: any): SubmitCategoryRow[] {
    if (res?.serviceStatus !== 'Success' || !Array.isArray(res.serviceResponse)) {
      return [];
    }
    return (res.serviceResponse as any[]).map((c) => ({
      categoryId: Number(c.categoryId),
      categoryName: String(c.categoryName ?? '')
    }));
  }

  private mapApiToPickSkill(row: any): SubmitPickSkill {
    const subs = Array.isArray(row.subskills)
      ? row.subskills.map((u: any) => ({
          subskillId: Number(u.subskillId),
          subskillName: String(u.subskillName ?? '')
        }))
      : [];
    return {
      skillId: Number(row.skillId),
      skillName: String(row.skillName ?? ''),
      skillType: String(row.skillType ?? ''),
      categoryId: row.categoryId != null ? Number(row.categoryId) : undefined,
      categoryName: row.categoryName != null ? String(row.categoryName) : undefined,
      subskills: subs
    };
  }

  get requiredPicksOrdered(): SubmitPickSkill[] {
    return this.orderPicksByIds(this.requiredPickIds, this.requiredPool);
  }

  get optionalPicksOrdered(): SubmitPickSkill[] {
    return this.orderPicksByIds(this.optionalPickIds, this.optionalPool);
  }

  private orderPicksByIds(ids: number[], pool: SubmitPickSkill[]): SubmitPickSkill[] {
    return ids
      .map((id) => pool.find((s) => s.skillId === id))
      .filter((s): s is SubmitPickSkill => !!s);
  }

  /** Clears per-skill state when department/skills are re-bound (same as legacy pickDept, without changing config). */
  private pickDeptResetSkillState(): void {
    this.requiredPool = [];
    this.optionalPool = [];
    this.requiredPickIds = [];
    this.optionalPickIds = [];
    this.customSkillPicks = [];
    this.categoryOptions = [];
    this.selectedCategoryId = null;
    this.customSkillName = '';
    this.showProposeSkillForm = false;
    this.skillProposeError = null;
    this.ratings = {};
    this.expandedSkill.clear();
    this.certOn = {};
    this.certUploadLabel = {};
    this.certUploadError = {};
    this.certName = {};
    this.certIssuingBody = {};
    this.certDateObtained = {};
    this.certExpiryType = {};
    this.certCredential = {};
    this.certFileReferenceKey = {};
    this.certOriginalFilename = {};
    this.certFileSizeBytes = {};
    this.certFileMimeType = {};
    this.trainingCourseName = {};
    this.trainingPlatform = {};
    this.trainingCompletionYear = {};
    this.trainingOn = {};
    this.additionalEvidenceOn = {};
    this.subChecked = {};
    this.aspPicked.clear();
    this.aspirationOtherSkillsText = '';
    this.pendingAspirationDraftNames = null;
    this.ps0Rows = [];
    this.ps1Rows = [];
  }

  get progressWidth(): string {
    return `${PROGRESS_PCT[this.curStep] ?? 0}%`;
  }

  /** Non-empty server message (e.g. missing skills for department). */
  get contextHint(): string | null {
    const m = this.submitContext?.['contextMessage'];
    return typeof m === 'string' && m.trim().length > 0 ? m.trim() : null;
  }

  get allSkillNames(): string[] {
    const seen = new Set<number>();
    const names: string[] = [];
    for (const s of [...this.requiredPicksOrdered, ...this.optionalPicksOrdered, ...this.customSkillPicks]) {
      if (seen.has(s.skillId)) {
        continue;
      }
      seen.add(s.skillId);
      names.push(s.skillName);
    }
    return names;
  }

  get step2Banner(): string {
    if (!this.selectedDept) {
      return '';
    }
    const n = this.requiredPickIds.length;
    return `${this.selectedDept.name} — ${n} required skill(s) selected. Add optional skills if you need them.`;
  }

  get s3ProgText(): string {
    const tot = this.allSkillNames.length;
    const rated = Object.keys(this.ratings).filter((k) => (this.ratings[+k] ?? 0) >= 1).length;
    return `${rated} of ${tot} skills rated`;
  }

  get certCount(): number {
    return Object.keys(this.certOn).filter((k) => this.certOn[+k]).length;
  }

  onProposeSkillFormToggle(on: boolean): void {
    if (!on) {
      this.customSkillName = '';
      this.selectedCategoryId = null;
      this.skillProposeError = null;
    }
  }

  proposeCustomSkill(): void {
    this.skillProposeError = null;
    const name = this.customSkillName.trim();
    if (!name || this.selectedCategoryId == null) {
      this.skillProposeError = 'Enter a skill name and choose a category.';
      return;
    }
    this.skillMatrixService
      .proposeSubmitSkill({ skillName: name, categoryId: this.selectedCategoryId })
      .pipe(first())
      .subscribe({
        next: (res: any) => {
          if (res?.serviceStatus === 'Success' && res.serviceResponse) {
            const requestId = Number(res.serviceResponse?.requestId || 0);
            const requestedSkillName = String(res.serviceResponse?.skillName || name).trim();
            if (requestId > 0) {
              this.pendingCustomSkillRequests = [
                {
                  requestId,
                  skillName: requestedSkillName,
                  status: String(res.serviceResponse?.status || 'pending')
                },
                ...this.pendingCustomSkillRequests.filter((r) => r.requestId !== requestId)
              ];
            }
            this.customSkillName = '';
            this.selectedCategoryId = null;
            this.skillProposeError = null;
            Swal.fire({
              icon: 'success',
              title: 'Sent for HOD approval',
              text: `${requestedSkillName} has been submitted for HOD approval. It will be added to the skill list only after approval and Required/Optional selection.`
            });
          } else {
            this.skillProposeError =
              typeof res?.serviceResponse === 'string' ? res.serviceResponse : 'Could not submit skill request.';
          }
        },
        error: () => {
          this.skillProposeError = 'Could not submit skill request.';
        }
      });
  }

  removeCustomPick(row: SubmitPickSkill): void {
    this.customSkillPicks = this.customSkillPicks.filter((s) => s.skillId !== row.skillId);
  }

  private mergeIntoOptionalPool(row: SubmitPickSkill): void {
    if (this.optionalPool.some((s) => s.skillId === row.skillId)) {
      return;
    }
    this.optionalPool = [...this.optionalPool, row].sort((a, b) => a.skillName.localeCompare(b.skillName));
  }

  private findPick(skillName: string): SubmitPickSkill | undefined {
    return [...this.requiredPicksOrdered, ...this.optionalPicksOrdered, ...this.customSkillPicks].find(
      (s) => s.skillName === skillName
    );
  }

  private validateStepBeforeLeaving(step: number): string | null {
    switch (step) {
      case 2:
        if (this.selectedDept && this.requiredPickIds.length === 0) {
          return 'Select at least one required skill, or ask your administrator to configure Required skills for your department.';
        }
        return null;
      case 3:
        return this.validateSkillRatingsStep();
      case 4:
        return this.validateProjectExperienceStep();
      case 5:
        return this.validateAspirationsStep();
      default:
        return null;
    }
  }

  private validateSkillRatingsStep(): string | null {
    for (let i = 0; i < this.allSkillNames.length; i++) {
      const skillName = this.allSkillNames[i];
      if (!this.ratings[i]) {
        this.expandedSkill.add(i);
        return `Step 3: select proficiency level for ${skillName}.`;
      }
      if (!String(this.yearsExperience[i] || '').trim()) {
        this.expandedSkill.add(i);
        return `Step 3: select years of experience for ${skillName}.`;
      }
      if (!String(this.whatCanDo[i] || '').trim()) {
        this.expandedSkill.add(i);
        return `Step 3: fill "What specifically can you do with this skill?" for ${skillName}.`;
      }
      if (this.isCertOn(i)) {
        if (!String(this.certName[i] || '').trim()) {
          this.expandedSkill.add(i);
          return `Step 3: enter certification name for ${skillName}, or turn off Certification.`;
        }
        if (!String(this.certIssuingBody[i] || '').trim()) {
          this.expandedSkill.add(i);
          return `Step 3: enter issuing body for ${skillName}, or turn off Certification.`;
        }
        if (!String(this.certDateObtained[i] || '').trim()) {
          this.expandedSkill.add(i);
          return `Step 3: enter date obtained for ${skillName}, or turn off Certification.`;
        }
      }
    }
    return null;
  }

  private validateProjectExperienceStep(): string | null {
    const selectedProjects = this.projectOpen
      .map((isOpen, index) => ({ isOpen, index }))
      .filter((row) => row.isOpen);

    for (const { index } of selectedProjects) {
      const project = this.allProjects[index];
      const projectName = String(project?.projectName || `Project ${index + 1}`).trim();
      if (!String(this.projRole[index] || '').trim()) {
        return `Step 4: enter your role for ${projectName}.`;
      }
      if (!String(this.projSummary[index] || '').trim()) {
        return `Step 4: fill "What did you build / contribute?" for ${projectName}.`;
      }
      const rows = this.rowsForProject(index) || [];
      const usedRows = rows.filter((row) => String(row?.skill || '').trim() || String(row?.contrib || '').trim());
      if (usedRows.length === 0) {
        return `Step 4: add at least one skill row for ${projectName}.`;
      }
      for (const row of usedRows) {
        if (!String(row?.skill || '').trim()) {
          return `Step 4: select a skill in "Skills applied" for ${projectName}.`;
        }
        if (!String(row?.contrib || '').trim()) {
          return `Step 4: fill contribution for skill ${row.skill} in ${projectName}.`;
        }
      }
      if (this.projDomainSpecific[index]) {
        if (!this.projDomainId[index]) {
          return `Step 4: select domain for ${projectName}.`;
        }
        if (this.projectSubdomainRowVisible(index) && !this.projSubdomainId[index]) {
          return `Step 4: select sub domain for ${projectName}.`;
        }
        if (this.projectFeatureRowVisible(index) && !this.projFeatureId[index]) {
          return `Step 4: select domain feature for ${projectName}.`;
        }
      }
    }
    return null;
  }

  private validateAspirationsStep(): string | null {
    if (this.isAddSkillsMode) {
      return null;
    }
    if (!String(this.goalRole || '').trim()) {
      return 'Step 5: enter your target role in 2 years.';
    }
    return null;
  }

  go(step: number): void {
    this.stepError = null;
    if (step === 2 && !this.selectedDept) {
      this.stepError = 'Your department could not be loaded from iShine.';
      return;
    }
    if (step >= 3 && step <= 6 && !this.selectedDept) {
      this.stepError = 'Your department could not be loaded from iShine.';
      return;
    }
    /** Require at least one mandatory skill only when leaving Step 2 for Step 3 (not when opening Step 2 from Step 1). */
    if (step === 3 && this.selectedDept && this.requiredPickIds.length === 0) {
      this.stepError =
        'Select at least one required skill, or ask your administrator to configure Required skills for your department.';
      return;
    }
    if (step >= 3 && step <= 6 && this.allSkillNames.length === 0) {
      this.stepError = 'No skills are available to rate. Go back to step 2 or reload the page.';
      return;
    }
    if (step > this.curStep) {
      for (let fromStep = this.curStep; fromStep < step; fromStep++) {
        const validationError = this.validateStepBeforeLeaving(fromStep);
        if (validationError) {
          this.stepError = validationError;
          return;
        }
      }
    }
    if (step === 3 && this.selectedDept) {
      this.expandedSkill.clear();
    }
    if (step === 4) {
      this.ensureProjectRows();
      this.loadProjectHistoryIfNeeded();
      this.loadSubmitSkillDomainsOnce();
      this.syncProjectDomainArrays();
    }
    if (step === 5 && this.selectedDept) {
      /* aspiration pool from dept */
    }
    if (step === 6) {
      this.refreshReviewFields();
    }
    this.curStep = step;
    if (typeof window !== 'undefined') {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  saveDraft(currentStep: number): void {
    if (!this.selectedDept) {
      return;
    }
    // Add Skills: Step 4 should update existing excluded projects only (no re-insert).
    if (this.isAddSkillsMode && currentStep === 4) {
      this.saveExcludedProjectsUpdateOnly();
      return;
    }
    this.draftSaving = true;
    this.draftError = null;
    this.loaderService.requestStarted();
    const payload = this.buildDraftPayload(currentStep);
    this.skillMatrixService
      .saveSubmitDraft(payload)
      .pipe(
        first(),
        finalize(() => {
          this.loaderService.requestEnded();
          this.draftSaving = false;
        })
      )
      .subscribe({
        next: (res: any) => {
          if (res?.serviceStatus === 'Success') {
            this.submissionId = res.serviceResponse;
            const now = new Date();
            this.draftSavedAt = now.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
            void Swal.fire({
              icon: 'success',
              title: 'Data saved successfully',
              confirmButtonColor: '#193d8a'
            });
          } else {
            this.draftError = typeof res?.serviceResponse === 'string' ? res.serviceResponse : 'Could not save draft.';
          }
        },
        error: () => {
          this.draftError = 'Could not save draft.';
        }
      });
  }

  private saveExcludedProjectsUpdateOnly(): void {
    const sid = String(this.baseApprovedSubmissionId || '').trim();
    if (!sid) {
      return;
    }
    this.draftSaving = true;
    this.draftError = null;
    this.loaderService.requestStarted();
    // Build only projects payload; keep existing ids.
    const payload = this.buildDraftPayload(4);
    const projects = Array.isArray(payload?.projects) ? payload.projects : [];
    // Only send rows that correspond to existing DB ids (assessmentProjectId).
    const toUpdate = projects
      .map((p: any, pi: number) => {
        const id = this.allProjects?.[pi]?.employeeTeamMapId;
        const assessmentProjectId = id != null && +id > 0 ? +id : null;
        return assessmentProjectId ? { ...p, assessmentProjectId } : null;
      })
      .filter((x: any) => !!x);

    this.skillMatrixService
      .updateExistingProjects({ submissionId: sid, projects: toUpdate })
      .pipe(
        first(),
        finalize(() => {
          this.loaderService.requestEnded();
          this.draftSaving = false;
        })
      )
      .subscribe({
        next: (res: any) => {
          if (res?.serviceStatus === 'Success') {
            const now = new Date();
            this.draftSavedAt = now.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
            void Swal.fire({ icon: 'success', title: 'Projects updated', confirmButtonColor: '#193d8a' });
          } else {
            this.draftError =
              typeof res?.serviceResponse === 'string' ? res.serviceResponse : 'Could not update projects.';
          }
        },
        error: () => {
          this.draftError = 'Could not update projects.';
        }
      });
  }

  private buildDraftPayload(currentStep: number): any {
    const skills: DraftSkillDTO[] = [];
    const byName = (nm: string) => this.findPick(nm);
    const picks = [...this.requiredPicksOrdered, ...this.optionalPicksOrdered, ...this.customSkillPicks];
    for (const s of picks) {
      const idx = this.skillIndexById(s.skillId);
      const required = this.requiredPickIds.includes(s.skillId);
      const selfRating = (this.ratings[idx] ?? 0) as number;
      const w = this.whatCanDo[idx] ?? '';
      const ye = this.yearsExperience[idx] ?? '';
      const lu = this.lastUsed[idx] ?? '';
      const uf = this.usageFrequency[idx] ?? '';
      const uip = this.usedInProject[idx] ?? false;
      const gh = this.githubPortfolioUrl[idx] ?? '';
      const ce = this.colleagueEndorser[idx] ?? '';
      const ks = this.knowledgeSessionNote[idx] ?? '';

      const certs: DraftSkillCertificationDTO[] = [];
      if (this.isCertOn(idx)) {
        const cn = (this.certName[idx] || '').trim();
        const ib = (this.certIssuingBody[idx] || '').trim();
        const dob = (this.certDateObtained[idx] || '').trim();
        if (cn && ib && dob) {
          const expType = (this.certExpiryType[idx] || '').trim();
          const cred = (this.certCredential[idx] || '').trim();
          const fileKey = (this.certFileReferenceKey[idx] || '').trim();
          certs.push({
            certName: cn,
            issuingBody: ib,
            dateObtained: dob,
            expiryType: expType || null,
            credentialId: cred && !cred.startsWith('http') ? cred : null,
            credentialUrl: cred && cred.startsWith('http') ? cred : null,
            fileReferenceKey: fileKey || null,
            originalFilename: fileKey ? this.certOriginalFilename[idx] || null : null,
            fileSizeBytes: fileKey ? this.certFileSizeBytes[idx] || null : null,
            fileMimeType: fileKey ? this.certFileMimeType[idx] || null : null
          });
        }
      }

      const trainings: DraftSkillTrainingDTO[] = [];
      if (this.isTrainingOn(idx)) {
        const course = (this.trainingCourseName[idx] || '').trim();
        const platform = (this.trainingPlatform[idx] || '').trim();
        const yearRaw = (this.trainingCompletionYear[idx] || '').trim();
        const year = yearRaw ? parseInt(yearRaw, 10) : null;
        if (course) {
          trainings.push({
            courseName: course,
            platformInstitute: platform || null,
            completionYear: Number.isFinite(year as any) ? (year as number) : null
          });
        }
      }
      const selectedSubs = (s.subskills ?? [])
        .filter((ss, si) => this.isSubChecked(idx, si))
        .map((ss) => ({ subskillId: ss.subskillId, subskillName: ss.subskillName }));
      skills.push({
        skillId: s.skillId,
        skillName: s.skillName,
        required,
        selfRating,
        yearsExperience: ye,
        lastUsed: lu,
        usageFrequency: uf,
        whatCanYouDo: w,
        usedInProject: uip,
        githubPortfolioUrl: gh,
        colleagueEndorser: ce,
        knowledgeSessionNote: ks,
        certifications: certs,
        trainings,
        selectedSubskills: selectedSubs
      });
    }

    const projects: DraftProjectDTO[] = [];
    this.allProjects.forEach((p, pi) => {
      const src = p.status === 'Manual' ? 'self_added' : 'hrms';
      const hrmsId = src === 'hrms' ? (p.projectId ?? null) : null;
      const rowSkills = this.rowsForProject(pi) ?? [];
      const applied: DraftProjectSkillDTO[] = rowSkills
        .map((r) => {
          const pk = byName(r.skill);
          return pk
            ? {
                skillId: pk.skillId,
                skillName: pk.skillName,
                levelUsed: parseInt((r.level || 'L0').replace('L', ''), 10) || 0,
                specificContribution: r.contrib || ''
              }
            : null;
        })
        .filter((x): x is DraftProjectSkillDTO => !!x);
      const domOn = !!this.projDomainSpecific[pi];
      const domId = this.projDomainId[pi];
      const subId = this.projSubdomainId[pi];
      const featId = this.projFeatureId[pi];
      projects.push({
        projectSource: src,
        hrmsProjectId: hrmsId,
        projectName: (p.projectName || '').trim(),
        clientOrType: p.clientName || null,
        projectStatus: p.status || null,
        startDate: src === 'hrms' ? (p.startDate || null) : null,
        endDate: src === 'hrms' ? (p.endDate || null) : null,
        durationText: src === 'self_added' ? (p.startDate || null) : null,
        employeeRole: this.projRole[pi] || null,
        allocationPct: this.projAlloc[pi] || null,
        contributionSummary: this.projSummary[pi] || '',
        included: !!this.projectOpen[pi],
        domainSpecific: domOn,
        skillDomainId: domOn && domId != null ? domId : null,
        skillSubdomainId: domOn && subId != null ? subId : null,
        skillDomainFeatureId: domOn && featId != null ? featId : null,
        skillsApplied: applied
      });
    });

    return {
      submissionId: this.submissionId,
      currentStep,
      skills,
      projects,
      targetRole2yr: this.goalRole,
      messageToManager: this.messageToManager,
      aspirationSkillNames: this.mergeAspirationSkillNamesForSave()
    };
  }

  /** Splits draft list: chip pool → `aspPicked`, everything else → free-text field. */
  private applyDraftAspirationSkillNames(raw: any[]): void {
    const pool = new Set(this.aspirationsPool());
    const chip = new Set<string>();
    const extras: string[] = [];
    for (const x of raw) {
      if (typeof x !== 'string') {
        continue;
      }
      const t = x.trim();
      if (!t) {
        continue;
      }
      if (pool.has(t)) {
        chip.add(t);
      } else {
        extras.push(t);
      }
    }
    this.aspPicked = chip;
    this.aspirationOtherSkillsText = extras.join(', ');
  }

  private parseFreeformAspirations(text: string): string[] {
    if (!text || typeof text !== 'string') {
      return [];
    }
    return text
      .split(/[\n,;]+/)
      .map((s) => s.trim())
      .filter((s) => s.length > 0);
  }

  private mergeAspirationSkillNamesForSave(): string[] {
    const out = new Set<string>();
    for (const s of this.aspPicked) {
      const t = (s || '').trim();
      if (t) {
        out.add(t);
      }
    }
    for (const s of this.parseFreeformAspirations(this.aspirationOtherSkillsText)) {
      out.add(s);
    }
    return Array.from(out);
  }

  private skillIndexById(skillId: number): number {
    const all = this.allSkillNames;
    const nm = [...this.requiredPicksOrdered, ...this.optionalPicksOrdered, ...this.customSkillPicks].find((s) => s.skillId === skillId)?.skillName;
    const i = nm ? all.findIndex((x) => x === nm) : -1;
    return i >= 0 ? i : 0;
  }

  private loadProjectHistoryIfNeeded(): void {
    if (this.projectHistoryLoading || this.projectHistory.length) {
      return;
    }
    this.projectHistoryLoading = true;
    this.projectHistoryError = null;
    this.skillMatrixService
      .getSubmitProjectHistory()
      .pipe(
        first(),
        finalize(() => {
          this.projectHistoryLoading = false;
        })
      )
      .subscribe({
        next: (res: any) => {
          if (res?.serviceStatus === 'Success' && Array.isArray(res.serviceResponse)) {
            this.projectHistory = res.serviceResponse as SubmitProjectHistoryRow[];
            this.rebuildAllProjects();
            this.rebuildProjectOpenState();
          } else {
            this.projectHistoryError =
              typeof res?.serviceResponse === 'string' ? res.serviceResponse : 'Could not load projects.';
          }
        },
        error: () => {
          this.projectHistoryError = 'Could not load projects.';
        }
      });
  }

  toggleProject(i: number, on: boolean): void {
    this.projectOpen = this.projectOpen.map((v, idx) => (idx === i ? on : v));
    if (on) {
      const p = this.allProjects[i];
      const key = this.projectKeyForIndex(i, p);
      if (!this.projectSkillRows[key]?.length) {
        this.projectSkillRows[key] = [this.blankProjectRow()];
      }
    }
  }

  rowsForProject(i: number): ProjectRow[] {
    const p = this.allProjects[i];
    const key = this.projectKeyForIndex(i, p);
    return this.projectSkillRows[key] ?? [];
  }

  addProjectSkillRow(i: number): void {
    const p = this.allProjects[i];
    const key = this.projectKeyForIndex(i, p);
    const cur = this.projectSkillRows[key] ?? [];
    this.projectSkillRows = { ...this.projectSkillRows, [key]: [...cur, this.blankProjectRow()] };
  }

  removeProjectSkillRow(i: number, ri: number): void {
    const p = this.allProjects[i];
    const key = this.projectKeyForIndex(i, p);
    const cur = this.projectSkillRows[key] ?? [];
    if (cur.length <= 1) {
      return;
    }
    this.projectSkillRows = { ...this.projectSkillRows, [key]: cur.filter((_, idx) => idx !== ri) };
  }

  addManualProject(): void {
    const name = (this.manualDraft.projectName ?? '').trim();
    if (!name) {
      return;
    }
    const row: ManualProjectDraft = {
      tempId: `m${Date.now().toString(36)}${Math.random().toString(36).slice(2, 6)}`,
      projectName: name,
      clientName: (this.manualDraft.clientName ?? '').trim(),
      duration: (this.manualDraft.duration ?? '').trim(),
      role: (this.manualDraft.role ?? '').trim(),
      details: (this.manualDraft.details ?? '').trim()
    };
    const id = this.manualKeyToId(row.tempId);
    this.manualIdToTempId[id] = row.tempId;
    this.manualProjects = [row, ...this.manualProjects];
    this.manualDraft = this.blankManualDraft();
    this.rebuildAllProjects();
    this.rebuildProjectOpenState();
  }

  removeManualProjectByEmployeeTeamMapId(employeeTeamMapId: number | null): void {
    if (employeeTeamMapId == null) {
      return;
    }
    const tempId = this.manualIdToTempId[employeeTeamMapId];
    if (!tempId) {
      return;
    }
    this.manualProjects = this.manualProjects.filter((p) => p.tempId !== tempId);
    delete this.manualIdToTempId[employeeTeamMapId];
    this.rebuildAllProjects();
    this.rebuildProjectOpenState();
  }

  private rebuildProjectOpenState(): void {
    const total = this.allProjects.length;
    const next: boolean[] = new Array(total).fill(false);
    for (let i = 0; i < total; i++) {
      next[i] = this.projectOpen[i] ?? i < 2; // keep previous, default open first 2
    }
    this.projectOpen = next;
    this.syncProjectDomainArrays();
  }

  private rebuildAllProjects(): void {
    // In Add Skills mode, projects list is driven by approved baseline excluded rows.
    // Do not merge in HRMS history or allow manual projects here.
    if (this.isAddSkillsMode) {
      this.allProjects = [...this.projectHistory];
      this.syncProjectDomainArrays();
      this.mergeDraftProjectsSnapshotIntoUi();
      return;
    }
    const manualAsRows: SubmitProjectHistoryRow[] = this.manualProjects.map((m) => ({
      employeeTeamMapId: this.manualKeyToId(m.tempId),
      projectId: null,
      projectName: m.projectName,
      teamId: null,
      teamName: m.role ? `Role: ${m.role}` : null,
      clientName: m.clientName || null,
      startDate: m.duration || null,
      endDate: null,
      status: 'Manual'
    }));
    this.allProjects = [...this.projectHistory, ...manualAsRows];
    this.syncProjectDomainArrays();
    this.mergeDraftProjectsSnapshotIntoUi();
  }

  private loadSubmitSkillDomainsOnce(): void {
    if (this.submitDomainsLoaded) {
      return;
    }
    this.skillMatrixService.getSubmitSkillDomains().pipe(first()).subscribe({
      next: (res: any) => {
        if (res?.serviceStatus === 'Success' && Array.isArray(res.serviceResponse)) {
          this.submitSkillDomainOptions = (res.serviceResponse as any[]).map((r) => ({
            domainId: +r.domainId,
            domainName: String(r.domainName ?? '')
          }));
          this.submitDomainsLoaded = true;
        }
      },
      error: () => {
        /* optional: leave list empty */
      }
    });
  }

  private syncProjectDomainArrays(): void {
    const n = this.allProjects.length;
    const padBool = (a: boolean[], def: boolean) => {
      const o = a.slice(0, n);
      while (o.length < n) {
        o.push(def);
      }
      return o;
    };
    const padNullNum = (a: (number | null)[]) => {
      const o = a.slice(0, n);
      while (o.length < n) {
        o.push(null);
      }
      return o;
    };
    this.projDomainSpecific = padBool(this.projDomainSpecific, false);
    this.projDomainId = padNullNum(this.projDomainId);
    this.projSubdomainId = padNullNum(this.projSubdomainId);
    this.projFeatureId = padNullNum(this.projFeatureId);
    this.projHasSubdomains = padBool(this.projHasSubdomains, false);
    const subs = this.projSubdomainOptions.slice(0, n);
    while (subs.length < n) {
      subs.push([]);
    }
    this.projSubdomainOptions = subs;
    const feats = this.projFeatureOptions.slice(0, n);
    while (feats.length < n) {
      feats.push([]);
    }
    this.projFeatureOptions = feats;
  }

  setProjDomainSpecific(pi: number, on: boolean): void {
    const next = [...this.projDomainSpecific];
    while (next.length <= pi) {
      next.push(false);
    }
    next[pi] = on;
    this.projDomainSpecific = next;
    if (!on) {
      this.clearProjectDomainStateAt(pi);
    }
  }

  toggleProjDomainSpecific(pi: number): void {
    this.setProjDomainSpecific(pi, !this.projDomainSpecific[pi]);
  }

  private clearProjectDomainStateAt(pi: number): void {
    this.projDomainId = this.projDomainId.map((v, i) => (i === pi ? null : v));
    this.projSubdomainId = this.projSubdomainId.map((v, i) => (i === pi ? null : v));
    this.projFeatureId = this.projFeatureId.map((v, i) => (i === pi ? null : v));
    this.projHasSubdomains = this.projHasSubdomains.map((v, i) => (i === pi ? false : v));
    const so = [...this.projSubdomainOptions];
    if (pi < so.length) {
      so[pi] = [];
    }
    this.projSubdomainOptions = so;
    const fo = [...this.projFeatureOptions];
    if (pi < fo.length) {
      fo[pi] = [];
    }
    this.projFeatureOptions = fo;
  }

  projectSubdomainRowVisible(pi: number): boolean {
    return !!this.projDomainSpecific[pi] && this.projDomainId[pi] != null && !!this.projHasSubdomains[pi];
  }

  projectFeatureRowVisible(pi: number): boolean {
    if (!this.projDomainSpecific[pi] || this.projDomainId[pi] == null) {
      return false;
    }
    if (this.projHasSubdomains[pi]) {
      return this.projSubdomainId[pi] != null;
    }
    return true;
  }

  onProjDomainChange(pi: number, domainId: number | null): void {
    this.projDomainId = this.projDomainId.map((v, i) => (i === pi ? domainId : v));
    this.projSubdomainId = this.projSubdomainId.map((v, i) => (i === pi ? null : v));
    this.projFeatureId = this.projFeatureId.map((v, i) => (i === pi ? null : v));
    const so = [...this.projSubdomainOptions];
    if (pi < so.length) {
      so[pi] = [];
    }
    this.projSubdomainOptions = so;
    const fo = [...this.projFeatureOptions];
    if (pi < fo.length) {
      fo[pi] = [];
    }
    this.projFeatureOptions = fo;
    if (domainId == null) {
      this.projHasSubdomains = this.projHasSubdomains.map((v, i) => (i === pi ? false : v));
      return;
    }
    this.skillMatrixService
      .getSubmitSkillSubdomains(domainId)
      .pipe(first())
      .subscribe({
        next: (res: any) => {
          const raw =
            res?.serviceStatus === 'Success' && Array.isArray(res.serviceResponse) ? res.serviceResponse : [];
          const list = (raw as any[]).map((r) => ({
            subdomainId: +r.subdomainId,
            subdomainName: String(r.subdomainName ?? '')
          }));
          const nextSo = [...this.projSubdomainOptions];
          while (nextSo.length <= pi) {
            nextSo.push([]);
          }
          nextSo[pi] = list;
          this.projSubdomainOptions = nextSo;
          const has = list.length > 0;
          this.projHasSubdomains = this.projHasSubdomains.map((v, i) => (i === pi ? has : v));
          if (!has) {
            this.loadProjFeatures(pi, domainId, null);
          }
        },
        error: () => {
          this.projHasSubdomains = this.projHasSubdomains.map((v, i) => (i === pi ? false : v));
        }
      });
  }

  onProjSubdomainChange(pi: number, subdomainId: number | null): void {
    this.projSubdomainId = this.projSubdomainId.map((v, i) => (i === pi ? subdomainId : v));
    this.projFeatureId = this.projFeatureId.map((v, i) => (i === pi ? null : v));
    const fo = [...this.projFeatureOptions];
    if (pi < fo.length) {
      fo[pi] = [];
    }
    this.projFeatureOptions = fo;
    const dom = this.projDomainId[pi];
    if (dom == null || subdomainId == null) {
      return;
    }
    this.loadProjFeatures(pi, dom, subdomainId);
  }

  private loadProjFeatures(pi: number, domainId: number, subdomainId: number | null): void {
    this.skillMatrixService
      .getSubmitSkillDomainFeatures(domainId, subdomainId)
      .pipe(first())
      .subscribe({
        next: (res: any) => {
          const raw =
            res?.serviceStatus === 'Success' && Array.isArray(res.serviceResponse) ? res.serviceResponse : [];
          const list = (raw as any[]).map((r) => ({
            featureId: +r.featureId,
            featureName: String(r.featureName ?? '')
          }));
          const nextFo = [...this.projFeatureOptions];
          while (nextFo.length <= pi) {
            nextFo.push([]);
          }
          nextFo[pi] = list;
          this.projFeatureOptions = nextFo;
        },
        error: () => {
          const nextFo = [...this.projFeatureOptions];
          if (pi < nextFo.length) {
            nextFo[pi] = [];
          }
          this.projFeatureOptions = nextFo;
        }
      });
  }

  private mergeDraftProjectsSnapshotIntoUi(): void {
    const rows = this.draftProjectsSnapshot;
    if (!rows?.length || !this.allProjects.length) {
      return;
    }
    const consumed = new Set<number>();
    const matchDraftIndex = (ap: SubmitProjectHistoryRow): number => {
      const wantSrc = ap.status === 'Manual' ? 'self_added' : 'hrms';
      const wantHrms = wantSrc === 'hrms' ? ap.projectId : null;
      const wantName = (ap.projectName || '').trim().toLowerCase();
      return rows.findIndex((r, ri) => {
        if (consumed.has(ri)) {
          return false;
        }
        const src = (r.projectSource || '').toLowerCase() === 'self_added' ? 'self_added' : 'hrms';
        if (src !== wantSrc) {
          return false;
        }
        if (wantSrc === 'hrms') {
          // Prefer stable numeric match; fall back to project name when history API doesn't provide projectId.
          if (r.hrmsProjectId != null && wantHrms != null && +r.hrmsProjectId === +wantHrms) {
            return true;
          }
          const rn = (r.projectName || '').trim().toLowerCase();
          return !!rn && !!wantName && rn === wantName;
        }
        return (r.projectName || '').trim().toLowerCase() === wantName;
      });
    };

    this.allProjects.forEach((ap, pi) => {
      const idx = matchDraftIndex(ap);
      if (idx < 0) {
        return;
      }
      consumed.add(idx);
      const row = rows[idx];
      if (row.employeeRole != null) {
        this.projRole = { ...this.projRole, [pi]: String(row.employeeRole) };
      }
      if (row.allocationPct != null) {
        this.projAlloc = { ...this.projAlloc, [pi]: +row.allocationPct };
      }
      if (row.contributionSummary != null) {
        this.projSummary = { ...this.projSummary, [pi]: String(row.contributionSummary) };
      }
      if (typeof row.included === 'boolean') {
        this.projectOpen = this.projectOpen.map((v, i) => (i === pi ? row.included : v));
      }
      if (Array.isArray(row.skillsApplied) && row.skillsApplied.length > 0) {
        const key = this.projectKeyForIndex(pi, ap);
        const mapped: ProjectRow[] = row.skillsApplied.map((a: any) => ({
          skill: String(a.skillName ?? ''),
          level: `L${a.levelUsed != null ? +a.levelUsed : 0}`,
          contrib: String(a.specificContribution ?? '')
        }));
        this.projectSkillRows = { ...this.projectSkillRows, [key]: mapped };
      }
      this.restoreProjectDomainFromDraftRow(pi, row);
    });
  }

  private draftRowDomainSpecific(row: any): boolean {
    const v = row?.domainSpecific;
    return v === true || v === 1 || v === '1';
  }

  private restoreProjectDomainFromDraftRow(pi: number, row: any): void {
    if (!this.draftRowDomainSpecific(row)) {
      this.setProjDomainSpecific(pi, false);
      return;
    }
    const domainId = row.skillDomainId != null ? +row.skillDomainId : null;
    if (domainId == null) {
      this.setProjDomainSpecific(pi, false);
      return;
    }
    this.setProjDomainSpecific(pi, true);
    this.projDomainId = this.projDomainId.map((v, i) => (i === pi ? domainId : v));
    const subDraft = row.skillSubdomainId != null ? +row.skillSubdomainId : null;
    const featDraft = row.skillDomainFeatureId != null ? +row.skillDomainFeatureId : null;
    this.skillMatrixService
      .getSubmitSkillSubdomains(domainId)
      .pipe(first())
      .subscribe({
        next: (res: any) => {
          const raw =
            res?.serviceStatus === 'Success' && Array.isArray(res.serviceResponse) ? res.serviceResponse : [];
          const list = (raw as any[]).map((r) => ({
            subdomainId: +r.subdomainId,
            subdomainName: String(r.subdomainName ?? '')
          }));
          const nextSo = [...this.projSubdomainOptions];
          while (nextSo.length <= pi) {
            nextSo.push([]);
          }
          nextSo[pi] = list;
          this.projSubdomainOptions = nextSo;
          const has = list.length > 0;
          this.projHasSubdomains = this.projHasSubdomains.map((v, i) => (i === pi ? has : v));
          if (!has) {
            this.restoreDraftProjectFeatures(pi, domainId, null, featDraft);
          } else if (subDraft != null && list.some((s) => s.subdomainId === subDraft)) {
            this.projSubdomainId = this.projSubdomainId.map((v, i) => (i === pi ? subDraft : v));
            this.restoreDraftProjectFeatures(pi, domainId, subDraft, featDraft);
          } else {
            this.projSubdomainId = this.projSubdomainId.map((v, i) => (i === pi ? null : v));
          }
        },
        error: () => {
          this.projHasSubdomains = this.projHasSubdomains.map((v, i) => (i === pi ? false : v));
        }
      });
  }

  private restoreDraftProjectFeatures(
    pi: number,
    domainId: number,
    subdomainId: number | null,
    featureDraftId: number | null
  ): void {
    this.skillMatrixService
      .getSubmitSkillDomainFeatures(domainId, subdomainId)
      .pipe(first())
      .subscribe({
        next: (res: any) => {
          const raw =
            res?.serviceStatus === 'Success' && Array.isArray(res.serviceResponse) ? res.serviceResponse : [];
          const list = (raw as any[]).map((r) => ({
            featureId: +r.featureId,
            featureName: String(r.featureName ?? '')
          }));
          const nextFo = [...this.projFeatureOptions];
          while (nextFo.length <= pi) {
            nextFo.push([]);
          }
          nextFo[pi] = list;
          this.projFeatureOptions = nextFo;
          if (featureDraftId != null && list.some((f) => f.featureId === featureDraftId)) {
            this.projFeatureId = this.projFeatureId.map((v, i) => (i === pi ? featureDraftId : v));
          }
        },
        error: () => {
          const nextFo = [...this.projFeatureOptions];
          if (pi < nextFo.length) {
            nextFo[pi] = [];
          }
          this.projFeatureOptions = nextFo;
        }
      });
  }

  private projectKeyForIndex(i: number, p: SubmitProjectHistoryRow | null | undefined): string {
    const id = p?.employeeTeamMapId;
    return id != null ? String(id) : `idx-${i}`;
  }

  trackByProject(_index: number, p: SubmitProjectHistoryRow): number | string {
    return p.employeeTeamMapId != null ? p.employeeTeamMapId : _index;
  }

  private blankManualDraft(): ManualProjectDraft {
    return { tempId: '', projectName: '', clientName: '', duration: '', role: '', details: '' };
  }

  private manualKeyToId(tempId: string): number {
    // stable negative-ish numeric key so it can live in SubmitProjectHistoryRow.employeeTeamMapId
    let h = 0;
    for (let i = 0; i < tempId.length; i++) {
      h = (h * 31 + tempId.charCodeAt(i)) | 0;
    }
    return -Math.abs(h || 1);
  }

  stepCircleClass(n: number): string {
    if (this.curStep === 7 || n < this.curStep) {
      return 'done';
    }
    if (n === this.curStep) {
      return 'act';
    }
    return 'pend';
  }

  circleInner(n: number): string {
    if (this.curStep === 7 || n < this.curStep) {
      return '✓';
    }
    return String(n);
  }

  stepLabelClass(n: number): string {
    if (this.curStep === 7 || n < this.curStep) {
      return 'done';
    }
    if (n === this.curStep) {
      return 'act';
    }
    return '';
  }

  isRequiredSkill(skill: string): boolean {
    return this.requiredPicksOrdered.some((s) => s.skillName === skill);
  }

  subsFor(skill: string): string[] {
    const p = this.findPick(skill);
    if (p?.subskills?.length) {
      return p.subskills.map((u) => u.subskillName);
    }
    return this.selectedDept?.subsBySkill[skill] ?? [];
  }

  subKey(skillIndex: number, subIndex: number): string {
    return `${skillIndex}_${subIndex}`;
  }

  isSubChecked(skillIndex: number, subIndex: number): boolean {
    return !!this.subChecked[this.subKey(skillIndex, subIndex)];
  }

  setSubChecked(skillIndex: number, subIndex: number, checked: boolean): void {
    const k = this.subKey(skillIndex, subIndex);
    this.subChecked = { ...this.subChecked, [k]: checked };
  }

  toggleSkillBlock(i: number): void {
    const next = new Set(this.expandedSkill);
    if (next.has(i)) {
      next.delete(i);
    } else {
      next.add(i);
    }
    this.expandedSkill = next;
  }

  isSkillOpen(i: number): boolean {
    return this.expandedSkill.has(i);
  }

  rate(skillIndex: number, level: number): void {
    this.ratings = { ...this.ratings, [skillIndex]: level };
  }

  isRatingSelected(skillIndex: number, level: number): boolean {
    return this.ratings[skillIndex] === level;
  }

  skillStatusText(i: number): string {
    const v = this.ratings[i];
    if (!v) {
      return '· Not yet rated';
    }
    return `· L${v} — ${RATING_LABELS[v - 1]}`;
  }

  toggleCert(i: number): void {
    this.certOn = { ...this.certOn, [i]: !this.certOn[i] };
  }

  isCertOn(i: number): boolean {
    return !!this.certOn[i];
  }

  toggleTraining(i: number): void {
    this.trainingOn = { ...this.trainingOn, [i]: !this.trainingOn[i] };
  }

  isTrainingOn(i: number): boolean {
    return !!this.trainingOn[i];
  }

  toggleAdditionalEvidence(i: number): void {
    this.additionalEvidenceOn = { ...this.additionalEvidenceOn, [i]: !this.additionalEvidenceOn[i] };
  }

  isAdditionalEvidenceOn(i: number): boolean {
    return !!this.additionalEvidenceOn[i];
  }

  openCertPicker(i: number, inputEl: HTMLInputElement, ev?: MouseEvent): void {
    if (ev) {
      ev.preventDefault();
      ev.stopPropagation();
    }
    this.certUploadError = { ...this.certUploadError, [i]: '' };
    inputEl.click();
  }

  onCertFileSelected(i: number, ev: Event): void {
    const input = ev.target as HTMLInputElement;
    const f = input?.files?.[0];
    input.value = '';
    if (!f) return;

    this.certUploadLabel = { ...this.certUploadLabel, [i]: 'Uploading…' };
    this.certUploadError = { ...this.certUploadError, [i]: '' };
    this.skillMatrixService
      .uploadSubmitCertificate(f)
      .pipe(first())
      .subscribe({
        next: (res: any) => {
          if (res?.serviceStatus !== 'Success') {
            const msg = typeof res?.serviceResponse === 'string' ? res.serviceResponse : 'Could not upload certificate.';
            this.certUploadLabel = { ...this.certUploadLabel, [i]: '' };
            this.certUploadError = { ...this.certUploadError, [i]: msg };
            return;
          }
          const r = res.serviceResponse || {};
          const ref = String(r.referenceKey || '');
          if (!ref) {
            this.certUploadLabel = { ...this.certUploadLabel, [i]: '' };
            this.certUploadError = { ...this.certUploadError, [i]: 'Could not upload certificate.' };
            return;
          }
          this.certFileReferenceKey = { ...this.certFileReferenceKey, [i]: ref };
          this.certOriginalFilename = { ...this.certOriginalFilename, [i]: String(r.originalFilename || f.name || '') };
          this.certFileSizeBytes = { ...this.certFileSizeBytes, [i]: Number(r.fileSizeBytes || f.size || 0) };
          this.certFileMimeType = { ...this.certFileMimeType, [i]: String(r.fileMimeType || f.type || '') };
          const labelName = String(r.originalFilename || f.name || 'certificate');
          this.certUploadLabel = { ...this.certUploadLabel, [i]: `✓ ${labelName} uploaded successfully` };
        },
        error: () => {
          this.certUploadLabel = { ...this.certUploadLabel, [i]: '' };
          this.certUploadError = { ...this.certUploadError, [i]: 'Could not upload certificate.' };
        }
      });
  }

  toggleAsp(s: string): void {
    const next = new Set(this.aspPicked);
    if (next.has(s)) {
      next.delete(s);
    } else {
      next.add(s);
    }
    this.aspPicked = next;
  }

  isAspPicked(s: string): boolean {
    return this.aspPicked.has(s);
  }

  aspirationsPool(): string[] {
    if (this.aspirationChipsApiReady && this.aspirationChipsApiList.length > 0) {
      return this.aspirationChipsApiList;
    }
    return this.selectedDept?.aspirations ?? [];
  }

  private loadSubmitAspirationChips(): void {
    this.aspirationChipsApiReady = false;
    this.aspirationChipsApiList = [];
    this.skillMatrixService
      .getSubmitAspirationChips()
      .pipe(first())
      .subscribe({
        next: (res: any) => {
          const raw = res?.serviceStatus === 'Success' && Array.isArray(res.serviceResponse) ? res.serviceResponse : [];
          const labels = (raw as any[])
            .map((r) => String(r.chipLabel ?? '').trim())
            .filter((s) => s.length > 0);
          this.aspirationChipsApiList = labels;
          this.aspirationChipsApiReady = true;
          if (this.pendingAspirationDraftNames != null && this.pendingAspirationDraftNames.length > 0) {
            this.applyDraftAspirationSkillNames(this.pendingAspirationDraftNames);
          }
          this.pendingAspirationDraftNames = null;
        },
        error: () => {
          this.aspirationChipsApiReady = true;
          this.aspirationChipsApiList = [];
          this.pendingAspirationDraftNames = null;
        }
      });
  }

  setProjectDetail(i: number, checked: boolean): void {
    if (i === 0) {
      this.projectDetail0 = checked;
    } else if (i === 1) {
      this.projectDetail1 = checked;
    } else {
      this.projectDetail2 = checked;
    }
  }

  ensureProjectRows(): void {
    if (this.ps0Rows.length === 0) {
      this.ps0Rows = [this.blankProjectRow()];
    }
    if (this.ps1Rows.length === 0) {
      this.ps1Rows = [this.blankProjectRow()];
    }
  }

  private blankProjectRow(): ProjectRow {
    const skills = this.allSkillNames;
    return {
      skill: skills[0] ?? 'Skill',
      level: 'L3',
      contrib: ''
    };
  }

  addProjectRow(which: 'ps0' | 'ps1'): void {
    const row = this.blankProjectRow();
    if (which === 'ps0') {
      this.ps0Rows = [...this.ps0Rows, row];
    } else {
      this.ps1Rows = [...this.ps1Rows, row];
    }
  }

  removeProjectRow(which: 'ps0' | 'ps1', index: number): void {
    if (which === 'ps0') {
      if (this.ps0Rows.length <= 1) {
        return;
      }
      this.ps0Rows = this.ps0Rows.filter((_, i) => i !== index);
    } else {
      if (this.ps1Rows.length <= 1) {
        return;
      }
      this.ps1Rows = this.ps1Rows.filter((_, i) => i !== index);
    }
  }

  projectSkillOptions(): string[] {
    const s = this.allSkillNames;
    return s.length ? s : ['Skill'];
  }

  revEmployee = '—';
  revDept = '—';
  revRole = '—';
  revMgr = '—';
  revSkills = '—';
  revCerts = '—';
  revGoal = '—';
  mgrRevName = 'your manager';
  hodRevName = 'your HOD';

  private refreshReviewFields(): void {
    const tot = this.allSkillNames.length;
    const req = this.requiredPickIds.length;
    const addl = Math.max(0, tot - req);
    this.revEmployee = `${this.empName} · ${this.empId}`;
    this.revDept = this.selectedDept?.name ?? '—';
    this.revRole = this.empRole.trim() || '—';
    this.revMgr = `${this.empMgr} — will receive approval request`;
    this.revSkills = `${req} required + ${addl} additional = ${tot} total`;
    this.revCerts =
      this.certCount > 0 ? `${this.certCount} certificate(s) indicated` : 'None uploaded';
    this.revGoal = this.goalRole.trim() || '—';
    this.mgrRevName = this.empMgr;
    // Keep HOD name for Step 6 messaging
    if (!this.hodRevName || this.hodRevName === 'your HOD') {
      // already set from context; fallback stays
    }
  }

  submit(): void {
    this.stepError = null;
    for (const step of [2, 3, 4, 5]) {
      const validationError = this.validateStepBeforeLeaving(step);
      if (validationError) {
        this.curStep = step;
        this.stepError = validationError;
        if (typeof window !== 'undefined') {
          window.scrollTo({ top: 0, behavior: 'smooth' });
        }
        return;
      }
    }
    // Always persist latest on-screen changes before final submit.
    this.draftSaving = true;
    this.draftError = null;
    this.loaderService.requestStarted();
    const payload = this.buildDraftPayload(6);
    this.skillMatrixService
      .saveSubmitDraft(payload)
      .pipe(first())
      .subscribe({
        next: (saveRes: any) => {
          if (saveRes?.serviceStatus !== 'Success') {
            this.loaderService.requestEnded();
            this.draftSaving = false;
            this.draftError =
              typeof saveRes?.serviceResponse === 'string' ? saveRes.serviceResponse : 'Could not save draft before submit.';
            return;
          }
          this.submissionId = saveRes.serviceResponse;
          this.skillMatrixService
            .submitSubmitForReview({ submissionId: this.submissionId })
            .pipe(first())
            .subscribe({
              next: (res: any) => {
                this.loaderService.requestEnded();
                this.draftSaving = false;
                if (res?.serviceStatus === 'Success') {
                  this.doneManager = this.empMgr;
                  const now = new Date();
                  const dStr = now.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
                  const tStr = now.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
                  this.doneRefId = this.submissionId || '';
                  this.doneRefWhen = `${dStr}, ${tStr}`;
                  this.go(7);
                } else {
                  this.draftError = typeof res?.serviceResponse === 'string' ? res.serviceResponse : 'Could not submit.';
                }
              },
              error: () => {
                this.loaderService.requestEnded();
                this.draftSaving = false;
                this.draftError = 'Could not submit.';
              }
            });
        },
        error: () => {
          this.loaderService.requestEnded();
          this.draftSaving = false;
          this.draftError = 'Could not save draft before submit.';
        }
      });
  }

  resetDemo(): void {
    this.goalRole = '';
    this.aspirationOtherSkillsText = '';
    this.aspPicked.clear();
    this.pendingAspirationDraftNames = null;
    this.aspirationChipsApiReady = false;
    this.aspirationChipsApiList = [];
    this.ps0Rows = [];
    this.ps1Rows = [];
    this.projectDetail0 = true;
    this.projectDetail1 = true;
    this.projectDetail2 = false;
    this.curStep = 1;
    this.stepError = null;
    this.doneRefId = '';
    this.doneRefWhen = '';
    this.loadSubmitWorkspace();
  }
}
