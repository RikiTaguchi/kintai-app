import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import DataRow from "@/components/ui/DataRow";
import EmptyState from "@/components/ui/EmptyState";
import FormField from "@/components/ui/FormField";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import MonthNavigator from "@/components/ui/MonthNavigator";
import userEvent from "@testing-library/user-event";
import { vi } from "vitest";

describe("DataRow", () => {
  it("label と value を描画する", () => {
    render(<DataRow label="名前" value="山田太郎" />);
    expect(screen.getByText("名前")).toBeInTheDocument();
    expect(screen.getByText("山田太郎")).toBeInTheDocument();
  });

  it("valueClass でカスタムスタイルを上書きできる", () => {
    render(<DataRow label="備考" value="x" valueClass="text-red-500" />);
    expect(screen.getByText("x").className).toMatch(/text-red-500/);
  });
});

describe("EmptyState", () => {
  it("message のみで描画できる", () => {
    render(<EmptyState message="データがありません" />);
    expect(screen.getByText("データがありません")).toBeInTheDocument();
  });

  it("subMessage を渡すと表示される", () => {
    render(<EmptyState message="空" subMessage="新規登録してください" />);
    expect(screen.getByText("新規登録してください")).toBeInTheDocument();
  });

  it("subMessage 省略時は余分な p 要素を出さない", () => {
    render(<EmptyState message="空" />);
    const paragraphs = document.querySelectorAll("p");
    expect(paragraphs.length).toBe(1);
  });
});

describe("FormField", () => {
  it("required=true で * を表示する", () => {
    render(
      <FormField label="名前" required>
        <input />
      </FormField>
    );
    expect(screen.getByText("*")).toBeInTheDocument();
  });

  it("required 省略時は * が出ない", () => {
    render(
      <FormField label="名前">
        <input />
      </FormField>
    );
    expect(screen.queryByText("*")).toBeNull();
  });

  it("error メッセージを表示する", () => {
    render(
      <FormField label="名前" error="必須項目です">
        <input />
      </FormField>
    );
    expect(screen.getByText("必須項目です")).toBeInTheDocument();
  });

  it("hint を表示する", () => {
    render(
      <FormField label="名前" hint="8文字以内">
        <input />
      </FormField>
    );
    expect(screen.getByText("8文字以内")).toBeInTheDocument();
  });

  it("children を小要素として描画する", () => {
    render(
      <FormField label="名前">
        <input placeholder="名前を入力" />
      </FormField>
    );
    expect(screen.getByPlaceholderText("名前を入力")).toBeInTheDocument();
  });
});

describe("LoadingSpinner", () => {
  it("デフォルトは md サイズ", () => {
    const { container } = render(<LoadingSpinner />);
    expect(container.querySelector(".w-8")).not.toBeNull();
  });

  it("size によってクラスが変わる", () => {
    const { container: sm } = render(<LoadingSpinner size="sm" />);
    expect(sm.querySelector(".w-5")).not.toBeNull();

    const { container: lg } = render(<LoadingSpinner size="lg" />);
    expect(lg.querySelector(".w-12")).not.toBeNull();
  });
});

describe("MonthNavigator", () => {
  it("year/month と下矢印 ボタンが描画される", () => {
    render(
      <MonthNavigator year={2025} month={9} onPrev={() => {}} onNext={() => {}} />
    );
    expect(screen.getByText("2025年9月")).toBeInTheDocument();
  });

  it("suffix を末尾に追記できる", () => {
    render(
      <MonthNavigator year={2025} month={9} onPrev={() => {}} onNext={() => {}} suffix="（後半）" />
    );
    expect(screen.getByText("2025年9月（後半）")).toBeInTheDocument();
  });

  it("前月・次月ボタンが実コールする", async () => {
    const user = userEvent.setup();
    const onPrev = vi.fn();
    const onNext = vi.fn();
    render(<MonthNavigator year={2025} month={9} onPrev={onPrev} onNext={onNext} />);
    const buttons = screen.getAllByRole("button");
    await user.click(buttons[0]);
    expect(onPrev).toHaveBeenCalledTimes(1);
    await user.click(buttons[1]);
    expect(onNext).toHaveBeenCalledTimes(1);
  });
});
