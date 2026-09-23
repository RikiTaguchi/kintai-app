import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import Button from "@/components/ui/Button";

describe("Button", () => {
  it("children をそのまま描画する", () => {
    render(<Button>保存する</Button>);
    expect(screen.getByRole("button", { name: "保存する" })).toBeInTheDocument();
  });

  it("デフォルトは primary", () => {
    render(<Button>primary</Button>);
    expect(screen.getByRole("button").className).toMatch(/bg-indigo-600/);
  });

  it("各 variant でスタイルが分かれる", () => {
    const { rerender } = render(<Button variant="secondary">x</Button>);
    expect(screen.getByRole("button").className).toMatch(/bg-white/);

    rerender(<Button variant="danger">x</Button>);
    expect(screen.getByRole("button").className).toMatch(/bg-red-600/);

    rerender(<Button variant="ghost">x</Button>);
    expect(screen.getByRole("button").className).toMatch(/text-gray-600/);
  });

  it("各 size でスタイルが分かれる", () => {
    const { rerender } = render(<Button size="sm">x</Button>);
    expect(screen.getByRole("button").className).toMatch(/px-3/);

    rerender(<Button size="lg">x</Button>);
    expect(screen.getByRole("button").className).toMatch(/px-6/);
  });

  it("disabled を渡すとボタンが無効になる", () => {
    render(<Button disabled>x</Button>);
    expect(screen.getByRole("button")).toBeDisabled();
  });

  it("isLoading=true でスピナーが出てボタンも disabled になる", () => {
    render(<Button isLoading>x</Button>);
    const btn = screen.getByRole("button");
    expect(btn).toBeDisabled();
    expect(btn.querySelector(".animate-spin")).not.toBeNull();
  });

  it("onClick を透過する", async () => {
    const user = userEvent.setup();
    const handleClick = vi.fn();
    render(<Button onClick={handleClick}>x</Button>);
    await user.click(screen.getByRole("button"));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it("isLoading 中は onClick が効かない（disabled のため）", async () => {
    const user = userEvent.setup();
    const handleClick = vi.fn();
    render(
      <Button isLoading onClick={handleClick}>
        x
      </Button>
    );
    await user.click(screen.getByRole("button"));
    expect(handleClick).not.toHaveBeenCalled();
  });

  it("className を追加してマージできる", () => {
    render(<Button className="mt-4">x</Button>);
    expect(screen.getByRole("button").className).toMatch(/mt-4/);
  });
});
