// Common
export type UUID = string;
export type LocalDate = string; // "YYYY-MM-DD"
export type LocalTime = string; // "HH:mm:ss"

// Classroom
export interface ClassroomResponse {
  id: UUID;
  name: string;
  classroomNumber: number;
}

// Manager
export interface ManagerResponse {
  id: UUID;
  loginId: string;
  classroomId: UUID;
  classroomName: string;
  classroomNumber: number;
  firstName: string;
  lastName: string;
}

export interface ManagerLoginRequest {
  loginId: string;
  password: string;
}

export interface ManagerRegisterRequest {
  loginId: string;
  password: string;
  classroomId: UUID;
  firstName: string;
  lastName: string;
}

export interface ManagerEditRequest {
  id: UUID;
  loginId: string;
  classroomId: UUID;
  firstName: string;
  lastName: string;
}

export interface PasswordChangeRequest {
  currentPassword: string;
  newPassword: string;
}

export interface PasswordResetRequest {
  newPassword: string;
}

// Tutor
export interface TutorResponse {
  id: UUID;
  loginId: string;
  classroomId: UUID;
  classroomName: string;
  classroomNumber: number;
  firstName: string;
  lastName: string;
  tutorNumber: number | null;
  terminated: boolean;
  terminationDate: LocalDate | null;
}

export interface TutorLoginRequest {
  loginId: string;
  password: string;
}

export interface TutorRegisterRequest {
  loginId: string;
  password: string;
  classroomId: UUID;
  tutorNumber: number | null;
  firstName: string;
  lastName: string;
}

export interface TutorEditRequest {
  id: UUID;
  tutorNumber: number | null;
  firstName: string;
  lastName: string;
  terminated: boolean;
  terminationDate: LocalDate | null;
}

// Work
export type PeriodCode = "M" | "K" | "S" | "A" | "B" | "C" | "D";

export const PERIOD_CODES: { code: PeriodCode; label: string }[] = [
  { code: "M", label: "09:10〜10:40" },
  { code: "K", label: "10:50〜12:20" },
  { code: "S", label: "13:10〜14:40" },
  { code: "A", label: "14:50〜16:20" },
  { code: "B", label: "16:30〜18:00" },
  { code: "C", label: "18:10〜19:40" },
  { code: "D", label: "19:50〜21:20" },
];

export interface LessonWorkDetail {
  startTime: LocalTime | null;
  endTime: LocalTime | null;
  breakMinutes: number | null;
  periodCodes: PeriodCode[];
}

export interface OfficeWorkDetail {
  startTime: LocalTime | null;
  endTime: LocalTime | null;
}

export interface OtherWorkDetail {
  startTime: LocalTime | null;
  endTime: LocalTime | null;
  breakMinutes: number | null;
  description: string | null;
}

export interface WorkResponse {
  id: UUID;
  tutorId: UUID;
  classroomId: UUID;
  classroomName: string;
  classroomNumber: number;
  workingDate: LocalDate;
  transportationFee: number;
  dailyAllowance: number;
  lessonWorkDetail: LessonWorkDetail;
  officeWorkDetail: OfficeWorkDetail;
  otherWorkDetail: OtherWorkDetail;
}

export interface WorkRegisterRequest {
  tutorId: UUID;
  classroomId: UUID;
  workingDate: LocalDate;
  transportationFee: number;
  lessonWorkDetail: LessonWorkDetail;
  officeWorkDetail: OfficeWorkDetail;
  otherWorkDetail: OtherWorkDetail;
}

export interface WorkEditRequest {
  id: UUID;
  tutorId: UUID;
  classroomId: UUID;
  workingDate: LocalDate;
  transportationFee: number;
  lessonWorkDetail: LessonWorkDetail;
  officeWorkDetail: OfficeWorkDetail;
  otherWorkDetail: OtherWorkDetail;
}

// Salary
export interface SalaryResponse {
  id: UUID;
  tutorId: UUID;
  effectiveDate: LocalDate;
  lessonWage: number;
  officeWage: number;
  transportationFee: number;
}

export interface SalaryRegisterRequest {
  tutorId: UUID;
  effectiveDate: LocalDate;
  lessonWage: number;
  officeWage: number;
  transportationFee: number;
}

export interface SalaryEditRequest {
  id: UUID;
  tutorId: UUID;
  effectiveDate: LocalDate;
  lessonWage: number;
  officeWage: number;
  transportationFee: number;
}

// Template
export interface LessonTemplateDetail {
  startTime: LocalTime | null;
  endTime: LocalTime | null;
  breakMinutes: number | null;
  periodCodes: PeriodCode[];
}

export interface OfficeTemplateDetail {
  startTime: LocalTime | null;
  endTime: LocalTime | null;
}

export interface OtherTemplateDetail {
  startTime: LocalTime | null;
  endTime: LocalTime | null;
  breakMinutes: number | null;
  description: string | null;
}

export interface TemplateResponse {
  id: UUID;
  tutorId: UUID;
  title: string;
  classroomId: UUID;
  classroomName: string;
  classroomNumber: number;
  transportationFee: number;
  lessonTemplateDetail: LessonTemplateDetail;
  officeTemplateDetail: OfficeTemplateDetail;
  otherTemplateDetail: OtherTemplateDetail;
}

export interface TemplateRegisterRequest {
  tutorId: UUID;
  title: string;
  classroomId: UUID;
  transportationFee: number;
  lessonTemplateDetail: LessonTemplateDetail;
  officeTemplateDetail: OfficeTemplateDetail;
  otherTemplateDetail: OtherTemplateDetail;
}

export interface TemplateEditRequest {
  id: UUID;
  tutorId: UUID;
  title: string;
  classroomId: UUID;
  transportationFee: number;
  lessonTemplateDetail: LessonTemplateDetail;
  officeTemplateDetail: OfficeTemplateDetail;
  otherTemplateDetail: OtherTemplateDetail;
}

// Payslip
export interface PayslipItem {
  amount: number;
  minutes: number;
}

export interface PayslipResponse {
  tutorId: UUID;
  lessonPay: number;
  periodCount: number;
  dailyAllowance: number;
  officeWorkPay: number;
  trainingAndStudyRoom: PayslipItem;
  outsideHoursWork: PayslipItem;
  overtimePremium: PayslipItem;
  nightShiftPremium: PayslipItem;
  otherPay: number;
  transportationFee: number;
}
