import "@testing-library/jest-dom/vitest";
import { cleanup } from "@testing-library/react";
import { afterEach, beforeAll, vi } from "vitest";

// React Testing Library の DOM をテストごとに掃除（vitest では globals=true でも
// 自動 cleanup が働かないケースがあるため明示的にフック）
afterEach(() => {
	cleanup();
	vi.restoreAllMocks();
	vi.unstubAllGlobals();
});

// jsdom が提供しないブラウザ API の最低限のポリフィル
beforeAll(() => {
	// crypto.randomUUID（一部環境で未定義）
	if (typeof globalThis.crypto === "undefined" || !("randomUUID" in globalThis.crypto)) {
		const nodeCrypto = require("node:crypto") as typeof import("node:crypto");
		Object.defineProperty(globalThis, "crypto", {
			value: nodeCrypto.webcrypto,
			configurable: true,
		});
	}

	// matchMedia（ダークモード判定などで参照される可能性）
	if (typeof window !== "undefined" && !window.matchMedia) {
		Object.defineProperty(window, "matchMedia", {
			writable: true,
			value: (query: string) => ({
				matches: false,
				media: query,
				onchange: null,
				addEventListener: () => {},
				removeEventListener: () => {},
				addListener: () => {},
				removeListener: () => {},
				dispatchEvent: () => false,
			}),
		});
	}

	// IntersectionObserver（使用箇所がある場合の安全弁）
	if (typeof window !== "undefined" && !("IntersectionObserver" in window)) {
		class MockIO {
			observe() {}
			unobserve() {}
			disconnect() {}
		}
		Object.defineProperty(window, "IntersectionObserver", {
			writable: true,
			value: MockIO,
		});
	}

	// scrollTo（Next.js App Router のレイアウト等で呼ばれることがある）
	if (typeof window !== "undefined" && !window.scrollTo) {
		Object.defineProperty(window, "scrollTo", {
			writable: true,
			value: () => {},
		});
	}
});
