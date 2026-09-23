import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

// api.ts はモジュール内部で `let _classroomsCache` を保持するため、
// テスト間でモジュールをリセットしてキャッシュを初期化する必要がある。
// vi.mock は静的（ホイスティング）に apiClient をモックする。

vi.mock("@/lib/apiClient", () => ({
  apiClient: vi.fn(),
}));

import * as api from "@/lib/api";
import { apiClient } from "@/lib/apiClient";

const mockedApiClient = vi.mocked(apiClient);

function jsonCall(path: string, init?: RequestInit): [string, RequestInit | undefined] {
  return [path, init];
}

function responseOf<T>(value: T): T {
  return value;
}

beforeEach(() => {
  mockedApiClient.mockReset();
});

// キャッシュの初期化は各テストケース内でモジュールをリセットして行う
afterEach(async () => {
  // vitest は vi.resetModules を呼ぶと次の import が新しいモジュールを読み込む。
  // getClassrooms のテストでは個別にリセットを呼んでいるため、ここでは何もしない。
});

describe("api.ts の各エンドポイント", () => {
  const MID = "manager-1";
  const TID = "tutor-1";
  const WID = "work-1";
  const SID = "salary-1";
  const TPID = "template-1";

  it("loginManager は POST /api/managers/login に JSON body で投げる", async () => {
    const body = { loginId: "lm", password: "pw" };
    mockedApiClient.mockResolvedValue(responseOf({ id: "m1" }));
    await api.loginManager(body);
    expect(mockedApiClient).toHaveBeenCalledWith(
      "/api/managers/login",
      expect.objectContaining({ method: "POST" })
    );
    const init = mockedApiClient.mock.calls[0]?.[1];
    expect(JSON.parse(String(init?.body))).toEqual(body);
  });

  it("loginTutor は POST /api/tutors/login", async () => {
    mockedApiClient.mockResolvedValue(responseOf({}));
    await api.loginTutor({ loginId: "t", password: "p" });
    expect(mockedApiClient).toHaveBeenCalledWith(
      "/api/tutors/login",
      expect.objectContaining({ method: "POST" })
    );
  });

  describe("getClassrooms", () => {
    it("GET /api/classrooms を呼ぶ", async () => {
      // 初回呼び出しでキャッシュがリセットされている必要があるため、
      // モジュールを再読み込みして新規キャッシュ状態で確認する。
      vi.resetModules();
      vi.doMock("@/lib/apiClient", () => ({ apiClient: vi.fn() }));
      const freshApi = await import("@/lib/api");
      const { apiClient: freshClient } = await import("@/lib/apiClient");
      const freshMock = vi.mocked(freshClient);
      freshMock.mockResolvedValue([{ id: "c1", name: "戸塚" }]);

      const rooms = await freshApi.getClassrooms();
      expect(freshMock).toHaveBeenCalledWith("/api/classrooms");
      expect(rooms).toEqual([{ id: "c1", name: "戸塚" }]);
    });

    it("2回目以同じ promise / 値を返し、API 呼び出しは1回（キャッシュ）", async () => {
      vi.resetModules();
      vi.doMock("@/lib/apiClient", () => ({ apiClient: vi.fn() }));
      const freshApi = await import("@/lib/api");
      const { apiClient: freshClient } = await import("@/lib/apiClient");
      const freshMock = vi.mocked(freshClient);
      freshMock.mockResolvedValue([{ id: "c1" }]);

      const [r1, r2, r3] = [
        await freshApi.getClassrooms(),
        await freshApi.getClassrooms(),
        await freshApi.getClassrooms(),
      ];

      expect(freshMock).toHaveBeenCalledTimes(1);
      expect(r1).toEqual(r2);
      expect(r2).toEqual(r3);
    });

    it("先の呼び出しと後の呼び出しで「違う値を返す API」でも 2回目は同じ値が返る", async () => {
      vi.resetModules();
      vi.doMock("@/lib/apiClient", () => ({ apiClient: vi.fn() }));
      const freshApi = await import("@/lib/api");
      const { apiClient: freshClient } = await import("@/lib/apiClient");
      const freshMock = vi.mocked(freshClient);
      freshMock
        .mockResolvedValueOnce([{ id: "first" }])
        .mockResolvedValueOnce([{ id: "second" }]);

      const first = await freshApi.getClassrooms();
      const second = await freshApi.getClassrooms();

      expect(first).toEqual([{ id: "first" }]);
      expect(second).toEqual([{ id: "first" }]);
      expect(freshMock).toHaveBeenCalledTimes(1);
    });
  });

  it("registerManager は POST /api/managers", async () => {
    mockedApiClient.mockResolvedValue(responseOf({}));
    await api.registerManager({ loginId: "m" } as never);
    expect(mockedApiClient).toHaveBeenCalledWith(
      "/api/managers",
      expect.objectContaining({ method: "POST" })
    );
  });

  it("editManager は PUT /api/managers/{id}", async () => {
    mockedApiClient.mockResolvedValue(responseOf({}));
    await api.editManager(MID, {} as never);
    expect(mockedApiClient).toHaveBeenCalledWith(
      `/api/managers/${MID}`,
      expect.objectContaining({ method: "PUT" })
    );
  });

  it("changeManagerPassword は PUT /api/managers/{id}/my-password", async () => {
    mockedApiClient.mockResolvedValue(responseOf(void 0));
    await api.changeManagerPassword(MID, { currentPassword: "c", newPassword: "n" });
    expect(mockedApiClient).toHaveBeenCalledWith(
      `/api/managers/${MID}/my-password`,
      expect.objectContaining({ method: "PUT" })
    );
  });

  it("deleteManager は DELETE /api/managers/{id}", async () => {
    mockedApiClient.mockResolvedValue(responseOf(void 0));
    await api.deleteManager(MID);
    expect(mockedApiClient).toHaveBeenCalledWith(
      `/api/managers/${MID}`,
      expect.objectContaining({ method: "DELETE" })
    );
  });

  it("getTutors は GET /api/tutors", async () => {
    mockedApiClient.mockResolvedValue(responseOf([]));
    await api.getTutors();
    expect(mockedApiClient).toHaveBeenCalledWith("/api/tutors");
  });

  it("getTutor は GET /api/tutors/{id}", async () => {
    mockedApiClient.mockResolvedValue(responseOf({}));
    await api.getTutor(TID);
    expect(mockedApiClient).toHaveBeenCalledWith(`/api/tutors/${TID}`);
  });

  it("registerTutor は POST /api/tutors", async () => {
    mockedApiClient.mockResolvedValue(responseOf({}));
    await api.registerTutor({ loginId: "t" } as never);
    expect(mockedApiClient).toHaveBeenCalledWith(
      "/api/tutors",
      expect.objectContaining({ method: "POST" })
    );
  });

  it("editTutor は PUT /api/tutors/{id}", async () => {
    mockedApiClient.mockResolvedValue(responseOf({}));
    await api.editTutor(TID, {} as never);
    expect(mockedApiClient).toHaveBeenCalledWith(
      `/api/tutors/${TID}`,
      expect.objectContaining({ method: "PUT" })
    );
  });

  it("resetTutorPassword は PUT /api/tutors/{id}/password", async () => {
    mockedApiClient.mockResolvedValue(responseOf(void 0));
    await api.resetTutorPassword(TID, { newPassword: "new" });
    expect(mockedApiClient).toHaveBeenCalledWith(
      `/api/tutors/${TID}/password`,
      expect.objectContaining({ method: "PUT" })
    );
  });

  it("changeTutorPassword は PUT /api/tutors/{id}/my-password", async () => {
    mockedApiClient.mockResolvedValue(responseOf(void 0));
    await api.changeTutorPassword(TID, { currentPassword: "c", newPassword: "n" });
    expect(mockedApiClient).toHaveBeenCalledWith(
      `/api/tutors/${TID}/my-password`,
      expect.objectContaining({ method: "PUT" })
    );
  });

  it("deleteTutor は DELETE /api/tutors/{id}", async () => {
    mockedApiClient.mockResolvedValue(responseOf(void 0));
    await api.deleteTutor(TID);
    expect(mockedApiClient).toHaveBeenCalledWith(
      `/api/tutors/${TID}`,
      expect.objectContaining({ method: "DELETE" })
    );
  });

  it("getWorks は GET /api/works/{tutorId}?year=&month=", async () => {
    mockedApiClient.mockResolvedValue(responseOf([]));
    await api.getWorks(TID, 2025, 9);
    expect(mockedApiClient).toHaveBeenCalledWith(
      `/api/works/${TID}?year=2025&month=9`
    );
  });

  it("getWork は GET /api/works/{tutorId}/{workId}", async () => {
    mockedApiClient.mockResolvedValue(responseOf({}));
    await api.getWork(TID, WID);
    expect(mockedApiClient).toHaveBeenCalledWith(`/api/works/${TID}/${WID}`);
  });

  it("registerWork は POST /api/works/{tutorId}", async () => {
    const body = { workingDate: "2025-09-10" } as never;
    mockedApiClient.mockResolvedValue(responseOf({}));
    await api.registerWork(TID, body);
    expect(mockedApiClient).toHaveBeenCalledWith(
      `/api/works/${TID}`,
      expect.objectContaining({ method: "POST" })
    );
    expect(JSON.parse(String(mockedApiClient.mock.calls[0]?.[1]?.body))).toEqual(body);
  });

  it("editWork は PUT /api/works/{tutorId}/{workId}", async () => {
    mockedApiClient.mockResolvedValue(responseOf({}));
    await api.editWork(TID, WID, {} as never);
    expect(mockedApiClient).toHaveBeenCalledWith(
      `/api/works/${TID}/${WID}`,
      expect.objectContaining({ method: "PUT" })
    );
  });

  it("deleteWork は DELETE /api/works/{tutorId}/{workId}", async () => {
    mockedApiClient.mockResolvedValue(responseOf(void 0));
    await api.deleteWork(TID, WID);
    expect(mockedApiClient).toHaveBeenCalledWith(
      `/api/works/${TID}/${WID}`,
      expect.objectContaining({ method: "DELETE" })
    );
  });

  it("getSalaries は GET /api/salaries/{tutorId}", async () => {
    mockedApiClient.mockResolvedValue(responseOf([]));
    await api.getSalaries(TID);
    expect(mockedApiClient).toHaveBeenCalledWith(`/api/salaries/${TID}`);
  });

  it("getSalary は GET /api/salaries/{tutorId}/{salaryId}", async () => {
    mockedApiClient.mockResolvedValue(responseOf({}));
    await api.getSalary(TID, SID);
    expect(mockedApiClient).toHaveBeenCalledWith(`/api/salaries/${TID}/${SID}`);
  });

  it("registerSalary は POST /api/salaries/{tutorId}", async () => {
    mockedApiClient.mockResolvedValue(responseOf({}));
    await api.registerSalary(TID, { effectiveDate: "2025-04-01" } as never);
    expect(mockedApiClient).toHaveBeenCalledWith(
      `/api/salaries/${TID}`,
      expect.objectContaining({ method: "POST" })
    );
  });

  it("editSalary は PUT /api/salaries/{tutorId}/{salaryId}", async () => {
    mockedApiClient.mockResolvedValue(responseOf({}));
    await api.editSalary(TID, SID, {} as never);
    expect(mockedApiClient).toHaveBeenCalledWith(
      `/api/salaries/${TID}/${SID}`,
      expect.objectContaining({ method: "PUT" })
    );
  });

  it("deleteSalary は DELETE /api/salaries/{tutorId}/{salaryId}", async () => {
    mockedApiClient.mockResolvedValue(responseOf(void 0));
    await api.deleteSalary(TID, SID);
    expect(mockedApiClient).toHaveBeenCalledWith(
      `/api/salaries/${TID}/${SID}`,
      expect.objectContaining({ method: "DELETE" })
    );
  });

  it("getTemplates は GET /api/templates/{tutorId}", async () => {
    mockedApiClient.mockResolvedValue(responseOf([]));
    await api.getTemplates(TID);
    expect(mockedApiClient).toHaveBeenCalledWith(`/api/templates/${TID}`);
  });

  it("getTemplate は GET /api/templates/{tutorId}/{templateId}", async () => {
    mockedApiClient.mockResolvedValue(responseOf({}));
    await api.getTemplate(TID, TPID);
    expect(mockedApiClient).toHaveBeenCalledWith(
      `/api/templates/${TID}/${TPID}`
    );
  });

  it("registerTemplate は POST /api/templates/{tutorId}", async () => {
    mockedApiClient.mockResolvedValue(responseOf({}));
    await api.registerTemplate(TID, { title: "t" } as never);
    expect(mockedApiClient).toHaveBeenCalledWith(
      `/api/templates/${TID}`,
      expect.objectContaining({ method: "POST" })
    );
  });

  it("editTemplate は PUT /api/templates/{tutorId}/{templateId}", async () => {
    mockedApiClient.mockResolvedValue(responseOf({}));
    await api.editTemplate(TID, TPID, {} as never);
    expect(mockedApiClient).toHaveBeenCalledWith(
      `/api/templates/${TID}/${TPID}`,
      expect.objectContaining({ method: "PUT" })
    );
  });

  it("deleteTemplate は DELETE /api/templates/{tutorId}/{templateId}", async () => {
    mockedApiClient.mockResolvedValue(responseOf(void 0));
    await api.deleteTemplate(TID, TPID);
    expect(mockedApiClient).toHaveBeenCalledWith(
      `/api/templates/${TID}/${TPID}`,
      expect.objectContaining({ method: "DELETE" })
    );
  });

  it("getPayslip は GET /api/payslips/{tutorId}?year=&month=", async () => {
    mockedApiClient.mockResolvedValue(responseOf({}));
    await api.getPayslip(TID, 2025, 9);
    expect(mockedApiClient).toHaveBeenCalledWith(
      `/api/payslips/${TID}?year=2025&month=9`
    );
  });
});
