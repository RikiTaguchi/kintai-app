# 学習塾向け勤怠管理システム - プロジェクトガイド

このファイルは、本プロジェクトにおけるAIアシスタント（Claude）がコード生成・修正・レビューを行うためのコンテキストおよびコーディング規約です。各種操作を行う際は、必ず以下のルールに従ってください。

> **重要**: アプリの詳細設計（APIエンドポイント一覧・DB テーブル定義・参照関係など）は **`README.md`** に記載されています。実装・修正作業の前に必ず参照してください。

## 1. アプリケーション概要
- **対象**: 学習塾向けの勤怠管理システム
- **ユーザーロール**:
  - **管理者（教室長）**: 講師情報のCRUD、講師の勤務情報のCRUD、講師の給与情報のCRUD
  - **講師**: 勤務情報のCRUD、勤務テンプレートのCRUD、給与明細の確認

## 2. 技術スタックとディレクトリ構成
- **バックエンド (`/api` 配下)**: Java 21, Spring Boot 4.0.7, Spring Security, MyBatis 4.0.1, Lombok, Maven
- **フロントエンド (`/frontend` 配下)**: Next.js (App Router), TypeScript, Tailwind CSS v4
- **データベース**: PostgreSQL（開発環境は `docker-compose.yml` で構築）
- **認証機構**:
  - セッション認証（Spring Security の `HttpSession` + `JSESSIONID` Cookie）
  - `user_role` Cookie（httpOnly）をフロントエンドの Middleware（`proxy.ts`）がサーバーサイドで読み取り、未認証・権限不一致の場合はリダイレクト
  - ユーザー情報は `sessionStorage` の `user_info` に保存し、`AuthContext` で管理
  - CSRF 保護: `XSRF-TOKEN` Cookie を `apiClient.ts` が読み取り、`X-XSRF-TOKEN` ヘッダーとして全ミューテーションリクエストに付与

## 3. コーディング規約：バックエンド (Java/Spring Boot)
- **命名規則**:
  - 変数: キャメルケース (`camelCase`)
  - クラス: パスカルケース (`PascalCase`)
  - 定数: 大文字スネークケース (`UPPER_SNAKE_CASE`)
- **メソッドの命名 (重要)**:
  - `@Data` 等で自動生成されるgetterと区別するため、それ以外の自作メソッド名に「`get`」は**使用しない**こと。
  - CRUD操作のメソッド名:
    - Service層: `find`, `register`, `edit`, `delete`
    - Mapper層: `select`, `insert`, `update`, `delete`
- **アーキテクチャ・設計**:
  - ビジネスロジックの責務分離を重視し、コードの重複を避ける。
  - **三層分離**: `Request/Response`, `Dto`, `Entity` の役割を明確に分ける。
  - データベース接続時、N+1問題が発生しないようクエリやフェッチ戦略に注意する。
- **例外クラスの命名と HTTP ステータス対応**:
  - `LoginException` → 401 Unauthorized（ログイン認証失敗）
  - `AuthorizationFailedException` → 403 Forbidden（アクセス権限なし）
  - `ResourceNotFoundException` → 404 Not Found（リソース不在）
  - `AlreadyExistsException` → 409 Conflict（一意制約違反）
  - `BusinessException` → 422 Unprocessable Entity（ビジネスルール違反）
  - `InvalidInputException` → 400 Bad Request（入力値の論理的不正）

## 4. コーディング規約：フロントエンド (Next.js)
- **アーキテクチャ・設計**:
  - **App Router** を採用する。
  - インターフェース設計については、以下の内容に準拠すること。ただし、必要に応じて変更しても良い。（その場合は変更前に確認を取ること）

```
frontend/src/app/
├── manager/
│  ├── page.tsx # ホーム画面
│  ├── login/page.tsx # ログイン画面
│  ├── register/page.tsx # 管理者登録画面
│  ├── detail/page.tsx # 管理者情報画面
│  ├── edit/page.tsx # 管理者編集画面
│  ├── tutors/
│  │  ├── page.tsx # 講師一覧画面
│  │  ├── register/page.tsx # 講師登録画面
│  │  └── [tutorId]/
│  │    ├── page.tsx # 講師情報画面
│  │    ├── edit/page.tsx # 講師情報編集画面
│  ├── works/
│  │  ├── page.tsx # 勤務情報一覧画面（講師選択）
│  │  └── [tutorId]/
│  │    ├── page.tsx # 勤務情報一覧画面（月次）
│  │    ├── register/page.tsx # 勤務情報登録画面
│  │    └── [workId]/edit/page.tsx # 勤務情報編集画面
│  └── salaries/
│    └── [tutorId]/
│      ├── register/page.tsx # 給与情報登録画面
│      └── [salaryId]/
│        └── edit/page.tsx # 給与情報編集画面
└── tutor/
    ├── page.tsx # ホーム画面
    ├── login/page.tsx # ログイン画面
    ├── detail/page.tsx # 講師情報画面
    ├── edit/page.tsx # 講師情報編集画面
    ├── works/
    │ ├── page.tsx # 月次勤務情報画面
    │ ├── register/page.tsx # 勤務情報登録画面
    │ └── [workId]/
    │   └── edit/page.tsx # 勤務情報編集画面
    ├── templates/
    │ ├── page.tsx # テンプレート一覧画面
    │ ├── register/page.tsx # テンプレート登録画面
    │ └── [templateId]/
    │   └── edit/page.tsx # テンプレート編集画面
    └── salaries/
      └── page.tsx # 給与推移画面 
```

- **命名規則**:
  - 変数・関数: キャメルケース (`camelCase`)
  - コンポーネント: パスカルケース (`PascalCase`)
  - 定数: 大文字スネークケース (`UPPER_SNAKE_CASE`)
- **コンポーネント・状態管理**:
  - カスタムHooksを使用して、ロジックとUIの責務を分離する。
  - Context API等を活用し、親要素から子要素への連続したPropsの受け渡し（バケツリレー）を防ぐ。
  - コンポーネントは細かく分割し、再利用性を高める。
- **通信・認証**:
  - Cookieにセッション情報をキャッシュし、ログイン状態を管理する。
  - API通信処理は1つのファイルにまとめる。各関数は必ず `src/lib/apiClient.ts` で定義した関数を利用して実装すること。
- **UI/UX・スタイリング**:
  - Tailwind CSSを用いたシンプルなデザインとする。
  - UX向上のため、ローディング画面や必要なアニメーションの実装は必須。
  - レスポンシブ対応:
    - 管理者画面: PCメイン想定（レスポンシブ対応も実装）
    - 講師画面: スマホメイン想定（レスポンシブ対応は必須）
  - **ダークモード**:
    - Tailwind CSS v4 の `dark:` バリアントを使用する。
    - OS設定に連動（`@media (prefers-color-scheme: dark)`）— 手動トグルは実装しない。
    - ライトモードのスタイルを変更せず、`dark:` バリアントを追加する形で対応する。

## 5. 用語・命名辞書
プロジェクト内で使用するドメイン用語の英単語は、以下で統一すること。
- **管理者（教室長）**: `Manager`
- **講師**: `Tutor`
- **勤務情報**: `Work`
- **テンプレート情報**: `Template`
- **給与情報**: `Salary`
- **給与明細**: `Payslip`

## 6. よく使うコマンド
- **DB起動（バックグラウンド）**: `docker-compose up -d`
- **DB停止＆ボリューム削除（データ初期化）**: `docker-compose down -v`
- **バックエンド起動**: `cd api && ./mvnw spring-boot:run` （※環境によっては `mvn spring-boot:run`）
- **フロントエンド起動**: `cd frontend && npm run dev`

## 7. 講師給フォーム出力の実装メモ

### ファイル構成

- **API Route**: `frontend/src/app/api/excel/payslips/route.ts`（Next.js Route Handler）
- **テンプレート**: `frontend/templates/excelTemplate.xlsx`（`public/` 配下には置かず URL 直アクセスを防ぐ）
- テンプレートの読み込みは `path.join(process.cwd(), "templates", "excelTemplate.xlsx")` で行う（`process.cwd()` は Next.js 実行時 `frontend/` を指す）

### exceljs の制約と JSZip によるポストプロセス

exceljs は OOXML を一部変換・欠落させるため、`await workbook.xlsx.writeBuffer()` の出力を JSZip で開いてから XML を直接修正する処理が必要。

| 問題 | 対処 |
|------|------|
| Form Control（VML チェックボックス）を一切削除する | テンプレート ZIP から `xl/drawings/drawing*.xml`・`xl/drawings/vmlDrawing*.vml`・`xl/ctrlProps/ctrlProp*.xml` をそのままコピーして復元 |
| `zoomScale` を自動付与する | `zoomScale="100"` に固定して上書き |
| `sheetFormatPr` に `customHeight="1"` 等の余分属性を追加する | `baseColWidth="10"` + テンプレート由来の `defaultColWidth` / `defaultRowHeight` のみに正規化 |
| `fitToWidth`/`fitToHeight` を `pageSetup` に追加する | 正規表現で除去 |
| トップシートの `Print_Area` を削除または誤生成する | `workbook.xml` の `definedNames` を削除してからテンプレート値 `'トップ '!$A$1:$M$51` を再挿入 |
| 全シートに `rowBreaks` を注入する | 注入コードを削除（テンプレートは `rowBreaks` を持たない） |
| 全シートに `colBreaks` を注入する（列・最大値も誤り） | 講師シートのみに絞り、`id="42" max="42" man="1"` を注入（トップシートは注入しない） |

### シートファイル番号の動的解決

exceljs はシートファイル名（`xl/worksheets/sheet1.xml` 等）を `sheetId` の値で決定するため、ワークブック表示順とファイル番号が一致しない場合がある。トップシートのファイルパスは以下の手順で動的に解決する:

1. `xl/workbook.xml` から `<sheet name="トップ ">` の `r:id` を取得
2. `xl/_rels/workbook.xml.rels` でその rId に対応するファイルパスを取得
3. 取得したパス（例: `xl/worksheets/sheet3.xml`）で各処理の `isTopSheet` フラグを判定

### 列幅・行高さのスケーリング値（調整済み）

| 対象 | 列幅 | 行高さ |
|------|------|--------|
| トップシート | ×1.15 | ×0.9 |
| 講師シート | ×1.20 | 変更なし |

- 非表示列（`hidden="1"`）はスケーリング対象外
- トップシートの N 列（`min="14"` の `<col>` 要素）は出力から削除する

## 7.5 PWA（ホーム画面追加 / スタンドアロン）の実装メモ

### ファイル構成

- **共通ビルダー**: `frontend/src/lib/pwaManifest.ts`（`buildManifest("manager" | "tutor")` がロール別Manifestを生成）
- **manifest 配信**: `frontend/src/app/{manager,tutor}/manifest.webmanifest/route.ts`（Route Handler。Next.js の manifest.ts 規約はルート直下のみのため）
- **manifest 紐付け**: `frontend/src/app/{manager,tutor}/layout.tsx` の `metadata.manifest`
- **認証除外**: `frontend/src/proxy.ts` の matcher が `manifest.webmanifest` を否定先読みで除外（Android Chrome が未認証で manifest を fetch するため）

### ロール別 manifest の設計

- **id**: `/manager/` と `/tutor/` で別値を指定。**同一オリジンに複数 PWA を共存させるには id の差異が必須**（Chrome/Edge は id をキーにインストール済みアプリを識別する）
- **start_url**: 各ロールのホーム（`/manager/`・`/tutor/`）。ログイン後そのまま機能へ入れるようにする。未ログインなら middleware が各ロールの `/login` へリダイレクトする
- **scope**: ロールのルートパスと同一（`/manager/`・`/tutor/`）
- **display**: `standalone`。iOS Safari では standalone のみが PWA表示モードとして有効
- **display_override**: `["standalone"]`。standalone が無理な場合に browser へのフォールバックを抑制

### manifest キャッシュ方針

Route Handler が `Cache-Control: no-store, no-cache, must-revalidate` を返す。
これは iOS Safari / 一部ブラウザが過去に取得した manifest を長期間保持し、別ロールからA2HSした際に古いmanifestを参照してしまう事故を防ぐための保険。

### 旧 iOS 向け apple メタ

`metadata.appleWebApp.capable: true` は `<meta name="mobile-web-app-capable">` のみを出力するため、
旧 iOS Safari が参照する `<meta name="apple-mobile-web-app-capable">` は `metadata.other` で明示的に出力する
（`frontend/src/app/{manager,tutor}/layout.tsx` の `other["apple-mobile-web-app-capable"]` 参照）。

### トラブルシューティング

**「/tutor/login からホームに追加したのに /manager/login が起動する」** 場合の確認ポイント:

1. 各ログインページの `<link rel="manifest">` が正しいロールのmanifestを指しているかを確認（view-source で確認可能）
2. `/{role}/manifest.webmanifest` を直接開き、`start_url` と `id` がロール固有であるかを確認
3. iOS 側で既存のホーム画面アイコン・Web App を削除してから再試行（Safari が過去のmanifestを記憶している可能性）
4. ホーム画面アイコン名でロールを識別できる（`short_name` が「勤怠管理(管理者)」／「勤怠管理(講師)」）

## 8. 勤務情報の一括コピー機能の実装メモ

### ファイル構成

- **コンポーネント**: `frontend/src/components/work/CopyWorkModal.tsx`
- **起点**: `WorkDetailModal` 内「他の日付にコピー」ボタン（`onCopy` prop）
- **呼び出し元**: `frontend/src/app/tutor/works/page.tsx`（講師）、`frontend/src/app/manager/works/[tutorId]/page.tsx`（管理者）

### 実装の要点

- コピー対象は全フィールド（日次手当はサーバー算出のため `WorkRegisterRequest` に含まれない）
- 既存の `POST /api/works/{tutorId}` エンドポイントを再利用（新規エンドポイント不要）
- `Promise.allSettled` で選択日付分を並列登録し、成功・失敗件数をトーストで通知
- カレンダーの月切り替えをまたいだ複数日付の選択に対応
  - `selectedDates: Set<string>` は月切り替えをまたいで保持する
  - `bookedDates: Set<string>` は表示月ごとに `getWorks()` で再取得する
- 既に勤務が登録されている日付は選択不可（`disabled` + グレーアウト）
- `tutorId` prop（省略可）を持つ。渡された場合はそれを使用し、省略時は `user.id`（講師本人）を使用するため管理者・講師両画面で再利用可能

## 9. パスワード変更・リセット機能の実装メモ

### エンドポイント

| エンドポイント | 認可 | 処理 |
|--------------|------|------|
| `PUT /api/managers/{managerId}/my-password` | 管理者本人 | 管理者が自分のパスワードを変更（現在パスワード確認あり） |
| `PUT /api/tutors/{tutorId}/password` | 管理者（自教室のみ） | 管理者が講師のパスワードをリセット（現在パスワード確認なし） |
| `PUT /api/tutors/{tutorId}/my-password` | 講師本人 | 講師が自分のパスワードを変更（現在パスワード確認あり） |

### バックエンド構成

- **`AccountService`**: パスワード操作を3メソッドに分離
  - `editLoginId(accountId, loginId)` — ログイン ID のみ更新
  - `changePassword(accountId, currentRaw, newRaw)` — bcrypt 照合後に更新。不一致時は `BusinessException("現在のパスワードが正しくありません")`
  - `resetPassword(accountId, newRaw)` — 現在パスワード確認なしで更新
- **リクエストクラス**:
  - `ManagerPasswordChangeRequest` — `currentPassword`, `newPassword`
  - `TutorPasswordChangeRequest` — `currentPassword`, `newPassword`
  - `TutorPasswordResetRequest` — `newPassword`
- **管理者情報編集**（`ManagerEditRequest`）からパスワードフィールドを削除済み。パスワード変更は専用エンドポイントのみで行う

### フロントエンド構成

- **`PasswordChangeModal`** (`components/auth/PasswordChangeModal.tsx`): 現在パスワード・新パスワード・確認パスワードの3フィールド。クライアント側で新パスワード一致確認後、`onSubmit(currentPassword, newPassword)` を呼び出す
- **`PasswordResetModal`** (`components/auth/PasswordResetModal.tsx`): 新パスワード・確認パスワードの2フィールド。管理者が講師名を確認しながら設定する
- **呼び出し元**:
  - 管理者情報画面（`manager/detail/page.tsx`）→ `PasswordChangeModal`
  - 講師情報画面（管理者）（`manager/tutors/[tutorId]/page.tsx`）→ `PasswordResetModal`
  - 講師情報画面（講師）（`tutor/detail/page.tsx`）→ `PasswordChangeModal`

## 10. 操作マニュアルへの導線の実装メモ

### ファイル構成

- マニュアル本体: `frontend/public/manual/manager.html`, `frontend/public/manual/tutor.html`（静的 HTML。Next.js のルーティング対象外のため、`Link` ではなく通常の `<a href="/manual/....html">` で参照する）
- ログイン前: `manager/login/page.tsx`, `tutor/login/page.tsx` に確認リンクを設置（同一タブで開く）

### ログイン後の導線

| 画面 | 配置場所 | 挙動 |
|------|---------|------|
| 管理者 | `ManagerLayout.tsx` のサイドバー下部（PC・モバイル共通）。並び順は「管理者情報 → 使い方を確認 → ログアウト」（ログアウトを最下部に固定） | `target="_blank" rel="noopener noreferrer"` で別タブ表示 |
| 管理者 | `manager/page.tsx`（ダッシュボード）に「管理者情報」「使い方を確認」カードを追加。既存の「講師管理」「勤務管理」と合わせ、4カードすべてのアイコン配色を indigo に統一 | 同上 |
| 講師 | `tutor/detail/page.tsx`（マイページ）の「パスワードを変更」と「ログアウト」の間 | 同上 |

### 実装上の注意

- `Button` コンポーネントは `<button>` 専用で `href` を受け付けないため、リンクとして配置する場合は `secondary` バリアントと同等のクラスを直接指定した `<a>` タグで代替する
- 講師画面（`TutorLayout.tsx`）は下部固定タブ（勤務／テンプレート／給与／マイページの4項目のみ）とヘッダーのみの構成でハンバーガーメニュー等のドロワーが存在しないため、新規タブの追加やヘッダーへの要素追加は行わず、既存のアカウント関連操作（パスワード変更・ログアウト）と同じマイページ内に配置する
