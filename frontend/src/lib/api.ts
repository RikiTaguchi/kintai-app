import { apiClient } from "./apiClient";
import type {
  ClassroomResponse,
  ManagerResponse,
  ManagerLoginRequest,
  ManagerRegisterRequest,
  ManagerEditRequest,
  PasswordChangeRequest,
  PasswordResetRequest,
  TutorResponse,
  TutorLoginRequest,
  TutorRegisterRequest,
  TutorEditRequest,
  WorkResponse,
  WorkRegisterRequest,
  WorkEditRequest,
  SalaryResponse,
  SalaryRegisterRequest,
  SalaryEditRequest,
  TemplateResponse,
  TemplateRegisterRequest,
  TemplateEditRequest,
  PayslipResponse,
  UUID,
} from "@/types";

// Auth
export const loginManager = (data: ManagerLoginRequest) =>
  apiClient<ManagerResponse>("/api/managers/login", {
    method: "POST",
    body: JSON.stringify(data),
  });

export const loginTutor = (data: TutorLoginRequest) =>
  apiClient<TutorResponse>("/api/tutors/login", {
    method: "POST",
    body: JSON.stringify(data),
  });

// Classrooms
let _classroomsCache: ClassroomResponse[] | null = null;

export const getClassrooms = () => {
  if (_classroomsCache) return Promise.resolve(_classroomsCache);
  return apiClient<ClassroomResponse[]>("/api/classrooms").then((list) => {
    _classroomsCache = list;
    return list;
  });
};

// Managers
export const registerManager = (data: ManagerRegisterRequest) =>
  apiClient<ManagerResponse>("/api/managers", {
    method: "POST",
    body: JSON.stringify(data),
  });

export const editManager = (managerId: UUID, data: ManagerEditRequest) =>
  apiClient<ManagerResponse>(`/api/managers/${managerId}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });

export const changeManagerPassword = (managerId: UUID, data: PasswordChangeRequest) =>
  apiClient<void>(`/api/managers/${managerId}/my-password`, {
    method: "PUT",
    body: JSON.stringify(data),
  });

export const deleteManager = (managerId: UUID) =>
  apiClient<void>(`/api/managers/${managerId}`, { method: "DELETE" });

// Tutors
export const getTutors = () => apiClient<TutorResponse[]>("/api/tutors");

export const getTutor = (tutorId: UUID) =>
  apiClient<TutorResponse>(`/api/tutors/${tutorId}`);

export const registerTutor = (data: TutorRegisterRequest) =>
  apiClient<TutorResponse>("/api/tutors", {
    method: "POST",
    body: JSON.stringify(data),
  });

export const editTutor = (tutorId: UUID, data: TutorEditRequest) =>
  apiClient<TutorResponse>(`/api/tutors/${tutorId}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });

export const resetTutorPassword = (tutorId: UUID, data: PasswordResetRequest) =>
  apiClient<void>(`/api/tutors/${tutorId}/password`, {
    method: "PUT",
    body: JSON.stringify(data),
  });

export const changeTutorPassword = (tutorId: UUID, data: PasswordChangeRequest) =>
  apiClient<void>(`/api/tutors/${tutorId}/my-password`, {
    method: "PUT",
    body: JSON.stringify(data),
  });

export const deleteTutor = (tutorId: UUID) =>
  apiClient<void>(`/api/tutors/${tutorId}`, { method: "DELETE" });

// Works
export const getWorks = (tutorId: UUID, year: number, month: number) =>
  apiClient<WorkResponse[]>(
    `/api/works/${tutorId}?year=${year}&month=${month}`
  );

export const getWork = (tutorId: UUID, workId: UUID) =>
  apiClient<WorkResponse>(`/api/works/${tutorId}/${workId}`);

export const registerWork = (tutorId: UUID, data: WorkRegisterRequest) =>
  apiClient<WorkResponse>(`/api/works/${tutorId}`, {
    method: "POST",
    body: JSON.stringify(data),
  });

export const editWork = (
  tutorId: UUID,
  workId: UUID,
  data: WorkEditRequest
) =>
  apiClient<WorkResponse>(`/api/works/${tutorId}/${workId}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });

export const deleteWork = (tutorId: UUID, workId: UUID) =>
  apiClient<void>(`/api/works/${tutorId}/${workId}`, { method: "DELETE" });

// Salaries
export const getSalaries = (tutorId: UUID) =>
  apiClient<SalaryResponse[]>(`/api/salaries/${tutorId}`);

export const getSalary = (tutorId: UUID, salaryId: UUID) =>
  apiClient<SalaryResponse>(`/api/salaries/${tutorId}/${salaryId}`);

export const registerSalary = (tutorId: UUID, data: SalaryRegisterRequest) =>
  apiClient<SalaryResponse>(`/api/salaries/${tutorId}`, {
    method: "POST",
    body: JSON.stringify(data),
  });

export const editSalary = (
  tutorId: UUID,
  salaryId: UUID,
  data: SalaryEditRequest
) =>
  apiClient<SalaryResponse>(`/api/salaries/${tutorId}/${salaryId}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });

export const deleteSalary = (tutorId: UUID, salaryId: UUID) =>
  apiClient<void>(`/api/salaries/${tutorId}/${salaryId}`, {
    method: "DELETE",
  });

// Templates
export const getTemplates = (tutorId: UUID) =>
  apiClient<TemplateResponse[]>(`/api/templates/${tutorId}`);

export const getTemplate = (tutorId: UUID, templateId: UUID) =>
  apiClient<TemplateResponse>(`/api/templates/${tutorId}/${templateId}`);

export const registerTemplate = (
  tutorId: UUID,
  data: TemplateRegisterRequest
) =>
  apiClient<TemplateResponse>(`/api/templates/${tutorId}`, {
    method: "POST",
    body: JSON.stringify(data),
  });

export const editTemplate = (
  tutorId: UUID,
  templateId: UUID,
  data: TemplateEditRequest
) =>
  apiClient<TemplateResponse>(`/api/templates/${tutorId}/${templateId}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });

export const deleteTemplate = (tutorId: UUID, templateId: UUID) =>
  apiClient<void>(`/api/templates/${tutorId}/${templateId}`, {
    method: "DELETE",
  });

// Payslips
export const getPayslip = (tutorId: UUID, year: number, month: number) =>
  apiClient<PayslipResponse>(
    `/api/payslips/${tutorId}?year=${year}&month=${month}`
  );
