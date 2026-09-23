import { describe, expect, it } from "vitest";

import { filterTutors } from "@/lib/tutorSearch";
import type { TutorResponse } from "@/types";

function tutor(p: Partial<TutorResponse>): TutorResponse {
  return {
    id: "t-1",
    tutorNumber: null,
    lastName: "山田",
    firstName: "太郎",
    lastNameKana: "ヤマダ",
    firstNameKana: "タロウ",
    birthDate: "1990-01-01",
    postalCode: "123-4567",
    address1: "東京都",
    address2: null,
    address3: null,
    phoneNumber: "090-1234-5678",
    email: "t@example.com",
    hireDate: "2024-04-01",
    terminated: false,
    terminationDate: null,
    loginId: "yamada_t",
    role: "ROLE_TUTOR",
    classroomId: "c-1",
    classroomName: "本部",
    classroomNumber: 1,
    ...p,
  } as TutorResponse;
}

describe("filterTutors", () => {
  const list: TutorResponse[] = [
    tutor({ id: "1", lastName: "山田", firstName: "太郎", loginId: "yamada_t", tutorNumber: 101 }),
    tutor({ id: "2", lastName: "佐藤", firstName: "花子", loginId: "sato_h", tutorNumber: 102 }),
    tutor({ id: "3", lastName: "田中", firstName: "次郎", loginId: "tanaka_j", tutorNumber: null }),
  ];

  it("空文字検索は全件返す", () => {
    expect(filterTutors(list, "")).toHaveLength(3);
  });

  it("姓で検索できる", () => {
    expect(filterTutors(list, "山田")).toHaveLength(1);
    expect(filterTutors(list, "山田")[0].id).toBe("1");
  });

  it("名で検索できる", () => {
    expect(filterTutors(list, "花子")).toHaveLength(1);
    expect(filterTutors(list, "花子")[0].id).toBe("2");
  });

  it("姓+名の連結で検索できる", () => {
    expect(filterTutors(list, "田中次郎")).toHaveLength(1);
    expect(filterTutors(list, "田中次郎")[0].id).toBe("3");
  });

  it("ログイン ID で検索できる（部分一致）", () => {
    expect(filterTutors(list, "sato")).toHaveLength(1);
    expect(filterTutors(list, "sato")[0].id).toBe("2");
  });

  it("講師番号で検索できる（文字列部分一致）", () => {
    expect(filterTutors(list, "101")).toHaveLength(1);
    expect(filterTutors(list, "101")[0].id).toBe("1");
    // "10" でも 101, 102 で 2 件
    expect(filterTutors(list, "10")).toHaveLength(2);
  });

  it("講師番号 null の講師は番号検索でヒットしないが、名前でヒットする", () => {
    expect(filterTutors(list, "次郎")).toHaveLength(1);
    expect(filterTutors(list, "次郎")[0].lastName).toBe("田中");
    expect(filterTutors(list, "次郎")[0].tutorNumber).toBeNull();
  });

  it("該当なし検索は空配列", () => {
    expect(filterTutors(list, "zzz")).toHaveLength(0);
  });
});
