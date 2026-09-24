/**
 * ログイン資格情報の localStorage 保存ユーティリティ。
 *
 * セキュリティ注意:
 * パスワードを平文で localStorage に保存するため、「ログイン情報を保存する」を
 * ON にした場合のみ保存される。共有PCでは OFF でログインする運用を推奨。
 * （本番運用では remember-me トークン方式等への移行を検討すること）
 */

type CredentialRole = "manager" | "tutor";

interface SavedCredentials {
  loginId: string;
  password: string;
}

const STORAGE_KEY_PREFIX = "saved_credentials_";

function storageKey(role: CredentialRole): string {
  return `${STORAGE_KEY_PREFIX}${role}`;
}

/** 資格情報を localStorage に保存する */
export function saveCredentials(role: CredentialRole, loginId: string, password: string): void {
  if (typeof window === "undefined") return;
  const data: SavedCredentials = { loginId, password };
  localStorage.setItem(storageKey(role), JSON.stringify(data));
}

/** 保存済みの資格情報を取得する（未保存/壊れている場合は null） */
export function loadCredentials(role: CredentialRole): SavedCredentials | null {
  if (typeof window === "undefined") return null;
  const raw = localStorage.getItem(storageKey(role));
  if (!raw) return null;
  try {
    const parsed = JSON.parse(raw) as Partial<SavedCredentials>;
    if (typeof parsed.loginId === "string" && typeof parsed.password === "string") {
      return { loginId: parsed.loginId, password: parsed.password };
    }
    return null;
  } catch {
    return null;
  }
}

/** 保存済みの資格情報を削除する */
export function clearCredentials(role: CredentialRole): void {
  if (typeof window === "undefined") return;
  localStorage.removeItem(storageKey(role));
}
