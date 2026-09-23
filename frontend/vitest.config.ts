import react from "@vitejs/plugin-react";
import path from "path";
import { defineConfig } from "vitest/config";

export default defineConfig({
	plugins: [react()],
	resolve: {
		alias: {
			"@": path.resolve(__dirname, "./src"),
		},
	},
	test: {
		environment: "jsdom",
		setupFiles: ["./src/test/setup.ts"],
		globals: true,
		css: false,
		coverage: {
			provider: "v8",
			reporter: ["text", "html", "lcov"],
			include: [
				"src/lib/**/*.ts",
				"src/lib/**/*.tsx",
				"src/context/**/*.tsx",
				"src/hooks/**/*.ts",
				"src/components/**/*.tsx",
				"src/proxy.ts",
			],
			exclude: ["src/**/*.d.ts", "src/test/**", "src/types/**"],
		},
	},
});
