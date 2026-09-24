# 学習塾向け勤怠管理システム

学習塾（教室）を対象とした、管理者（教室長）と講師の双方が利用する勤怠管理 Web アプリケーションです。

---

## 目次

1. [アプリの概要](#アプリの概要)
2. [バックエンドの設計](#バックエンドの設計)
3. [フロントエンドの設計](#フロントエンドの設計)
4. [DB 設計](#db-設計)
5. [ローカル開発環境のセットアップ](#ローカル開発環境のセットアップ)
6. [本番環境へのデプロイ（XServer VPS）](#本番環境へのデプロイxserver-vps)

---

## アプリの概要

### ユーザーロール

| ロール | 説明 |
|--------|------|
| **管理者（Manager）** | 教室長。講師の管理・勤務・給与を横断的に管理する |
| **講師（Tutor）** | 自身の勤務情報の登録・確認と給与明細の閲覧を行う |

### 管理者の主な機能

| 機能 | 説明 |
|------|------|
| 管理者登録・編集・削除 | 教室長アカウントの作成と情報管理 |
| 管理者パスワード変更 | 現在のパスワード確認後に新パスワードを設定 |
| 講師登録・編集・削除 | 所属教室の講師アカウントの作成と情報管理（在籍状態・退職日を含む） |
| 講師パスワードリセット | 管理者が講師の新パスワードを直接設定（現在パスワードの確認不要） |
| 講師の勤務情報 CRUD | 任意の講師の月次勤務情報を一覧表示・登録・編集・削除 |
| 勤務情報の一括コピー | 登録済み勤務情報を他の日付に一括コピー（管理者画面） |
| 講師の給与情報 CRUD | 適用開始日ごとの授業単価・事務単価・交通費を登録・編集・削除 |
| 講師給フォーム DL | 指定月の全講師の勤務・給与データを Excel ファイル（.xlsx）として一括ダウンロード |

### 講師の主な機能

| 機能 | 説明 |
|------|------|
| 勤務情報 CRUD | 月次の自身の勤務情報を登録・編集・削除。授業業務・事務業務・その他業務の3種類を1日単位で記録 |
| 勤務情報の一括コピー | 登録済み勤務情報を他の日付に一括コピー。カレンダー形式で複数日付を選択でき、月をまたいだコピーにも対応 |
| テンプレート CRUD | よく使う勤務パターンをテンプレートとして保存し、勤務登録時に呼び出せる |
| 給与明細の確認 | 月単位で授業・事務・その他各業務の労働時間と賃金の内訳を確認 |
| パスワード変更 | 現在のパスワード確認後に新パスワードを設定 |

### 認証方式

- セッション認証（Spring Security の `HttpSession` ＋ `JSESSIONID` Cookie）
- CSRF 保護（`XSRF-TOKEN` Cookie ／ `X-XSRF-TOKEN` リクエストヘッダー）
- ルートガード：Next.js Middleware（`proxy.ts`）がサーバーサイドで `user_role` Cookie を検査し、未認証アクセスをログイン画面にリダイレクト

---

## バックエンドの設計

### 使用技術

| 技術 | バージョン | 用途 |
|------|-----------|------|
| Java | 21 | メイン言語 |
| Spring Boot | 4.0.7 | アプリケーションフレームワーク |
| Spring Security | （Spring Boot 管理） | 認証・認可・CSRF 保護 |
| MyBatis | 4.0.1 | O/R マッパー（XML ベース） |
| Lombok | （Spring Boot 管理） | ボイラープレート削減（`@Data`, `@Slf4j` 等） |
| Maven | （Wrapper） | ビルド・依存関係管理 |
| PostgreSQL | — | リレーショナルデータベース |

### ディレクトリ構成

```
api/src/main/java/com/example/api/
├── common/
│   └── constant/              # アプリ全体で使う定数クラス
├── controller/                # REST コントローラー
│   ├── handler/               # GlobalExceptionHandler（集約例外ハンドラー）
│   ├── model/                 # リクエスト/レスポンス共通モデル
│   │   ├── template/
│   │   └── work/
│   ├── request/               # リクエスト DTO（バリデーションアノテーション付き）
│   │   ├── manager/
│   │   ├── salary/
│   │   ├── template/
│   │   ├── tutor/
│   │   └── work/
│   └── response/              # レスポンス DTO
├── exception/                 # カスタム例外クラス
├── mapper/                    # MyBatis マッパー
│   ├── entity/                # DB 行をマッピングするエンティティクラス
│   │   ├── template/
│   │   └── work/
│   ├── handler/               # MyBatis 型ハンドラー（UUID 変換など）
│   ├── template/              # テンプレート系マッパーインターフェース
│   └── work/                  # 勤務系マッパーインターフェース
├── security/                  # SecurityConfig, CustomUserDetails, CustomUserDetailsService
├── service/                   # ビジネスロジック
│   ├── dto/                   # サービス層 DTO（Entity ↔ Response の橋渡し）
│   │   ├── payslip/
│   │   ├── schedule/
│   │   ├── template/
│   │   └── work/
│   └── validator/             # 入力バリデーションロジック
└── ApiApplication.java
```

```
api/src/main/resources/
├── mapper/                    # MyBatis XML マッパー
│   ├── AccountMapper.xml
│   ├── ClassroomMapper.xml
│   ├── ManagerMapper.xml
│   ├── SalaryMapper.xml
│   ├── TutorMapper.xml
│   ├── template/
│   │   ├── TemplateMapper.xml
│   │   ├── LessonTemplateDetailMapper.xml
│   │   ├── OfficeTemplateDetailMapper.xml
│   │   └── OtherTemplateDetailMapper.xml
│   └── work/
│       ├── WorkMapper.xml
│       ├── LessonWorkDetailMapper.xml
│       ├── OfficeWorkDetailMapper.xml
│       └── OtherWorkDetailMapper.xml
├── schema.sql                 # DB 初期化 DDL
└── application.properties
```

### アーキテクチャ方針

- **三層 DTO 分離**: `Request/Response`（コントローラー層） → `Dto`（サービス層） → `Entity`（マッパー層）の役割を明確に分ける
- **メソッド命名規則**:
  - Service 層: `find`, `register`, `edit`, `delete`
  - Mapper 層: `select`, `insert`, `update`, `delete`
- **N+1 対策**: 勤務情報・テンプレートの詳細サブテーブルは `LEFT JOIN` ＋ MyBatis `<association>` で一括取得
- **認可制御**: `@PreAuthorize` ＋ `AuthorizationService` でコントローラー内に集約

### 例外クラス一覧

| 例外クラス | HTTP ステータス | 用途 |
|-----------|----------------|------|
| `LoginException` | 401 Unauthorized | ログイン認証失敗（認証情報の不一致） |
| `AuthorizationFailedException` | 403 Forbidden | アクセス権限なし（他ユーザーのリソース操作など） |
| `ResourceNotFoundException` | 404 Not Found | 指定リソースが存在しない |
| `AlreadyExistsException` | 409 Conflict | 一意制約違反（同日の勤務重複など） |
| `BusinessException` | 422 Unprocessable Entity | ビジネスルール違反 |
| `InvalidInputException` | 400 Bad Request | 入力値の論理的不正 |

### エンドポイント一覧

#### 認証 / ログイン（`LoginController`）

| メソッド | パス | 認証要否 | 説明 |
|---------|------|---------|------|
| POST | `/api/managers/login` | 不要 | 管理者ログイン |
| POST | `/api/tutors/login` | 不要 | 講師ログイン |

#### ログアウト（Spring Security）

| メソッド | パス | 説明 |
|---------|------|------|
| POST | `/api/managers/logout` | 管理者ログアウト（`JSESSIONID` 無効化、`user_role` Cookie クリア） |
| POST | `/api/tutors/logout` | 講師ログアウト（同上） |

#### 管理者（`ManagerController`）

| メソッド | パス | 認可 | 説明 |
|---------|------|------|------|
| POST | `/api/managers` | 不要 | 管理者登録 |
| PUT | `/api/managers/{managerId}` | 管理者本人 | 管理者情報編集（ログイン ID・氏名・教室） |
| PUT | `/api/managers/{managerId}/my-password` | 管理者本人 | 管理者パスワード変更（現在パスワード確認あり） |
| DELETE | `/api/managers/{managerId}` | 管理者本人 | 管理者削除 |

#### 講師（`TutorController`）

| メソッド | パス | 認可 | 説明 |
|---------|------|------|------|
| GET | `/api/tutors` | 管理者 | 自教室の講師一覧取得 |
| GET | `/api/tutors/{tutorId}` | 管理者（自教室のみ） | 講師情報取得 |
| POST | `/api/tutors` | 管理者 | 講師登録 |
| PUT | `/api/tutors/{tutorId}` | 管理者（自教室のみ） | 講師情報編集 |
| PUT | `/api/tutors/{tutorId}/password` | 管理者（自教室のみ） | 講師パスワードリセット（現在パスワード確認なし） |
| PUT | `/api/tutors/{tutorId}/my-password` | 講師本人 | 講師パスワード変更（現在パスワード確認あり） |
| DELETE | `/api/tutors/{tutorId}` | 管理者（自教室のみ） | 講師削除 |

#### 勤務情報（`WorkController`）

| メソッド | パス | 認可 | 説明 |
|---------|------|------|------|
| GET | `/api/works/{tutorId}?year=&month=` | 管理者 or 講師本人 | 月次勤務情報一覧取得 |
| GET | `/api/works/{tutorId}/{workId}` | 管理者 or 講師本人 | 勤務情報取得 |
| POST | `/api/works/{tutorId}` | 管理者 or 講師本人 | 勤務情報登録 |
| PUT | `/api/works/{tutorId}/{workId}` | 管理者 or 講師本人 | 勤務情報編集 |
| DELETE | `/api/works/{tutorId}/{workId}` | 管理者 or 講師本人 | 勤務情報削除 |

#### テンプレート（`TemplateController`）

| メソッド | パス | 認可 | 説明 |
|---------|------|------|------|
| GET | `/api/templates/{tutorId}` | 講師本人 | テンプレート一覧取得 |
| GET | `/api/templates/{tutorId}/{templateId}` | 講師本人 | テンプレート取得 |
| POST | `/api/templates/{tutorId}` | 講師本人 | テンプレート登録 |
| PUT | `/api/templates/{tutorId}/{templateId}` | 講師本人 | テンプレート編集 |
| DELETE | `/api/templates/{tutorId}/{templateId}` | 講師本人 | テンプレート削除 |

#### 給与情報（`SalaryController`）

| メソッド | パス | 認可 | 説明 |
|---------|------|------|------|
| GET | `/api/salaries/{tutorId}` | 管理者 or 講師本人 | 給与情報一覧取得 |
| GET | `/api/salaries/{tutorId}/{salaryId}` | 管理者 or 講師本人 | 給与情報取得 |
| POST | `/api/salaries/{tutorId}` | 管理者のみ | 給与情報登録 |
| PUT | `/api/salaries/{tutorId}/{salaryId}` | 管理者のみ | 給与情報編集 |
| DELETE | `/api/salaries/{tutorId}/{salaryId}` | 管理者のみ | 給与情報削除 |

#### 給与明細（`PayslipController`）

| メソッド | パス | 認可 | 説明 |
|---------|------|------|------|
| GET | `/api/payslips/{tutorId}?year=&month=` | 講師本人 | 月次給与明細取得（授業・事務・その他業務の賃金計算結果を返す） |

#### 教室・CSRF（`ClassroomController`, `CsrfController`）

| メソッド | パス | 認証要否 | 説明 |
|---------|------|---------|------|
| GET | `/api/classrooms` | 不要 | 教室一覧取得（ログイン画面等で使用） |
| GET | `/api/csrf` | 不要 | CSRF トークン発行（ページ初期ロード時に呼び出す） |

---

## フロントエンドの設計

### 使用技術

| 技術 | 用途 |
|------|------|
| Next.js（App Router） | SSR / CSR ハイブリッドの React フレームワーク |
| TypeScript | 型安全な開発 |
| Tailwind CSS v4 | ユーティリティファーストの CSS フレームワーク |
| React Context API | 認証状態（`AuthContext`）・トースト通知（`ToastContext`）のグローバル管理 |
| exceljs | XLSX テンプレートの読み込み・データ書き込み |
| JSZip | exceljs 出力後の OOXML (ZIP) を直接修正するポストプロセス |

### ディレクトリ構成

```
frontend/
├── templates/
│   └── excelTemplate.xlsx             # 講師給フォームの XLSX テンプレート（サーバーサイド専用）
└── src/
```

```
frontend/src/
├── app/
│   ├── api/
│   │   └── excel/
│   │       └── payslips/
│   │           └── route.ts           # 講師給フォーム生成 API（Next.js Route Handler）
│   ├── manager/                       # 管理者画面（PCメイン）
│   │   ├── page.tsx                   # ホーム（講師一覧へのリダイレクト）
│   │   ├── login/page.tsx             # ログイン
│   │   ├── register/page.tsx          # 管理者登録
│   │   ├── detail/page.tsx            # 管理者情報確認
│   │   ├── edit/page.tsx              # 管理者情報編集
│   │   ├── tutors/
│   │   │   ├── page.tsx               # 講師一覧
│   │   │   ├── register/page.tsx      # 講師登録
│   │   │   └── [tutorId]/
│   │   │       ├── page.tsx           # 講師情報詳細
│   │   │       └── edit/page.tsx      # 講師情報編集
│   │   ├── works/
│   │   │   ├── page.tsx               # 勤務情報一覧（講師選択）
│   │   │   └── [tutorId]/
│   │   │       ├── register/page.tsx  # 勤務情報登録
│   │   │       └── [workId]/edit/page.tsx  # 勤務情報編集
│   │   └── salaries/
│   │       └── [tutorId]/
│   │           ├── register/page.tsx  # 給与情報登録
│   │           └── [salaryId]/edit/page.tsx  # 給与情報編集
│   └── tutor/                         # 講師画面（スマホメイン）
│       ├── page.tsx                   # ホーム（月次勤務情報へのリダイレクト）
│       ├── login/page.tsx             # ログイン
│       ├── detail/page.tsx            # 講師情報確認
│       ├── edit/page.tsx              # 講師情報編集
│       ├── works/
│       │   ├── page.tsx               # 月次勤務情報一覧
│       │   ├── register/page.tsx      # 勤務情報登録
│       │   └── [workId]/edit/page.tsx # 勤務情報編集
│       ├── templates/
│       │   ├── page.tsx               # テンプレート一覧
│       │   ├── register/page.tsx      # テンプレート登録
│       │   └── [templateId]/edit/page.tsx  # テンプレート編集
│       └── salaries/
│           └── page.tsx               # 給与推移・明細確認
├── components/
│   ├── auth/
│   │   ├── PasswordChangeModal.tsx    # パスワード変更モーダル（現在パスワード確認あり）
│   │   └── PasswordResetModal.tsx     # パスワードリセットモーダル（管理者が講師用に使用）
│   ├── layout/                        # ヘッダー・レイアウトコンポーネント
│   ├── template/
│   │   └── TemplateDetailModal.tsx    # テンプレート詳細モーダル（編集・削除起点）
│   ├── ui/                            # 共通 UI（Button, FormField, LoadingSpinner, EmptyState, Toast 等）
│   └── work/
│       ├── WorkDetailFields.tsx       # 勤務詳細入力フォーム（授業・事務・その他業務）
│       ├── WorkDetailModal.tsx        # 勤務詳細モーダル（編集・削除・コピー起点）
│       └── CopyWorkModal.tsx          # 勤務一括コピー用カレンダー選択モーダル（管理者・講師共用）
├── context/
│   ├── AuthContext.tsx                # 認証状態管理（user, login, logout, isLoading）
│   └── ToastContext.tsx               # トースト通知管理
├── hooks/
│   ├── useMonthNavigation.ts          # 月次ナビゲーション（URL sync 対応）
│   ├── useConfirm.ts                  # 削除確認ダイアログ汎用フック
│   └── ...
├── lib/
│   ├── apiClient.ts                   # Fetch ラッパー（CSRF ヘッダー付与・401 リダイレクト処理）
│   ├── api.ts                         # 各 API 関数（apiClient を使用）
│   └── utils.ts                       # ユーティリティ（getErrorMessage, parsePeriodParams 等）
└── types/
    └── index.ts                       # 共通型定義（PeriodCode, ClassroomResponse 等）
```

### 主要な機能・実装詳細

#### 認証フロー

1. ログイン時に `/api/managers/login` または `/api/tutors/login` を POST
2. Spring Security がセッションを作成し、`JSESSIONID`（httpOnly）と `user_role`（httpOnly）Cookie をセット
3. レスポンスのユーザー情報を `sessionStorage` の `user_info` に保存し、`AuthContext` に反映
4. ページ遷移時は Next.js Middleware（`proxy.ts`）がサーバーサイドで `user_role` Cookie を検査し、未認証・権限不一致の場合はリダイレクト

#### API 通信（`apiClient.ts`）

- 全 API 呼び出しを `apiClient` 関数に集約
- `GET /api/csrf` で取得した `XSRF-TOKEN` Cookie を `X-XSRF-TOKEN` ヘッダーに付与（POST/PUT/DELETE 時）
- 401 レスポンス時は自動的にログイン画面へリダイレクト
- 204 No Content レスポンスには `undefined` を返す（ログアウト時など）

#### 勤務詳細入力（`WorkDetailFields.tsx`）

- 1 日の勤務は「授業業務」「事務業務」「その他業務」の 3 セクションで構成
- 担当コマ（M・K・S・A・B・C・D の 7 コマ）をトグルボタンで選択
- 3 コマ以上選択時に開始・終了時刻と休憩時間の入力欄を表示
- 事務業務・その他業務は任意追加（追加ボタン / 削除ボタン）
- `value/onChange` パターンで状態を親コンポーネントに委譲し再利用性を確保

#### ダークモード対応

- Tailwind CSS v4 の `dark:` バリアントを全フロントエンドファイルに適用
- 切り替えは OS 設定に連動（`@media (prefers-color-scheme: dark)`）— 手動トグルなし
- ライトモードのスタイルは変更せず、ダークモード用スタイルのみ追加

#### 勤務情報の一括コピー（`CopyWorkModal.tsx`）

- 勤務詳細モーダル内の「他の日付にコピー」ボタンから起動するカレンダー選択モーダル
- カレンダーの月をまたいだ複数日付の選択に対応（`selectedDates: Set<string>` は月切り替えをまたいで保持）
- コピー先の日付に既に勤務が登録されている場合はカレンダー上で選択不可（グレーアウト表示）
- 全フィールドをコピー（日次手当はサーバー算出のためリクエストに含まない）
- `Promise.allSettled` で並列登録し、成功・失敗件数をトーストで通知

#### カスタム Hooks

| Hook | 説明 |
|------|------|
| `useMonthNavigation` | 前月・翌月ナビゲーション。URL クエリパラメータへの同期オプション付き |
| `useConfirm<T>` | 削除確認ダイアログの表示制御を汎用化したジェネリック Hook |
| `useToast` | `ToastContext` から `addToast` を取得するショートカット Hook |

#### 講師給フォーム出力（`app/excel/payslips/route.ts`）

- 管理者画面の勤務管理ページから `GET /excel/payslips?year=&month=` を呼び出して XLSX をダウンロードする Next.js Route Handler
 - 本番 nginx が `location /api/ { proxy_pass http://api:8080 }` で `/api/*` を Java へ横流しするため、Route Handler の URL から `/api` プレフィックスを外している
- `frontend/templates/excelTemplate.xlsx` をベーステンプレートとして exceljs でデータを書き込み、JSZip で OOXML（ZIP 構造）を直接修正して出力する
  - exceljs が Form Control（VML チェックボックス）を欠落させるため、テンプレートの `xl/drawings/`, `xl/ctrlProps/` を JSZip で直接コピーして復元している
  - 表示サイズ調整（列幅スケーリング・ゼロ表示抑制・印刷設定など）も JSZip の XML 直接操作で行っている
- ファイル名形式: `講師給フォーム(教室No+教室名)yyyy.m.xlsx`（月のゼロ埋めなし）
- 出力シート: トップシート（月次集計）＋ 講師ごとのシート（Unicode 丸数字 ①〜⑱ を使用、最大 18 名）

#### 操作マニュアルへの導線（`public/manual/manager.html`, `public/manual/tutor.html`）

- 操作マニュアルは `frontend/public/manual/` 配下の静的 HTML（Next.js のルーティング対象外のため通常の `<a>` タグで参照する）
- ログイン前: ログイン画面（`manager/login`, `tutor/login`）に確認リンクを設置
- ログイン後:
  - 管理者: ダッシュボード（`manager/page.tsx`）のカードと `ManagerLayout.tsx` のサイドバー下部（PC・モバイル共通）に設置。いずれも `target="_blank"` で別タブ表示
  - 講師: マイページ（`tutor/detail/page.tsx`）の「パスワードを変更」と「ログアウト」の間に設置。同様に別タブ表示

---

## DB 設計

### 使用データベース

**PostgreSQL**（開発環境は Docker Compose で構築）

- 全テーブルの主キーは `UUID`（`gen_random_uuid()` で自動生成）
- 全テーブルに `created_at` / `updated_at` を持ち、`BEFORE UPDATE` トリガーで `updated_at` を自動更新

### テーブル一覧

#### `accounts`（アカウント）

| カラム | 型 | 制約 | 説明 |
|-------|-----|------|------|
| id | uuid | PK | |
| login_id | varchar(50) | NOT NULL, UNIQUE | ログイン ID |
| password | varchar(255) | NOT NULL | bcrypt ハッシュ化パスワード |
| created_at | timestamp | NOT NULL | |
| updated_at | timestamp | NOT NULL | |

#### `classrooms`（教室）

| カラム | 型 | 制約 | 説明 |
|-------|-----|------|------|
| id | uuid | PK | |
| name | varchar(50) | NOT NULL | 教室名 |
| classroom_number | integer | NOT NULL | 教室番号 |
| created_at | timestamp | NOT NULL | |
| updated_at | timestamp | NOT NULL | |

#### `managers`（管理者）

| カラム | 型 | 制約 | 説明 |
|-------|-----|------|------|
| id | uuid | PK | |
| account_id | uuid | FK → accounts | |
| classroom_id | uuid | FK → classrooms | 所属教室 |
| first_name | varchar(100) | NOT NULL | 名 |
| last_name | varchar(100) | NOT NULL | 姓 |
| created_at | timestamp | NOT NULL | |
| updated_at | timestamp | NOT NULL | |

#### `tutors`（講師）

| カラム | 型 | 制約 | 説明 |
|-------|-----|------|------|
| id | uuid | PK | |
| account_id | uuid | FK → accounts | |
| classroom_id | uuid | FK → classrooms | 所属教室 |
| tutor_number | integer | NULL 可 | 講師番号 |
| first_name | varchar(100) | NOT NULL | 名 |
| last_name | varchar(100) | NOT NULL | 姓 |
| terminated | boolean | NOT NULL, default false | 退職フラグ |
| termination_date | date | NULL 可 | 退職日 |
| created_at | timestamp | NOT NULL | |
| updated_at | timestamp | NOT NULL | |

#### `salaries`（給与情報）

| カラム | 型 | 制約 | 説明 |
|-------|-----|------|------|
| id | uuid | PK | |
| tutor_id | uuid | FK → tutors | |
| effective_date | date | NOT NULL | 適用開始日 |
| lesson_wage | integer | NOT NULL | 授業業務の時給（円） |
| office_wage | integer | NOT NULL | 事務業務の時給（円） |
| transportation_fee | integer | NOT NULL | 交通費（円） |
| created_at | timestamp | NOT NULL | |
| updated_at | timestamp | NOT NULL | |

> UNIQUE 制約: `(tutor_id, effective_date)`

#### `works`（勤務情報）

| カラム | 型 | 制約 | 説明 |
|-------|-----|------|------|
| id | uuid | PK | |
| tutor_id | uuid | FK → tutors | |
| classroom_id | uuid | FK → classrooms | 勤務教室 |
| working_date | date | NOT NULL | 勤務日 |
| transportation_fee | integer | NOT NULL | 交通費（円） |
| created_at | timestamp | NOT NULL | |
| updated_at | timestamp | NOT NULL | |

> UNIQUE 制約: `(tutor_id, working_date)`

#### `lesson_work_details`（授業業務詳細）

| カラム | 型 | 制約 | 説明 |
|-------|-----|------|------|
| id | uuid | PK | |
| work_id | uuid | FK → works（CASCADE） | |
| start_time | time | NULL 可 | 開始時刻（3コマ以上で必須） |
| end_time | time | NULL 可 | 終了時刻（3コマ以上で必須） |
| break_minutes | integer | NULL 可 | 休憩時間（分） |
| period_code_m | boolean | NOT NULL, default false | M コマ |
| period_code_k | boolean | NOT NULL, default false | K コマ |
| period_code_s | boolean | NOT NULL, default false | S コマ |
| period_code_a | boolean | NOT NULL, default false | A コマ |
| period_code_b | boolean | NOT NULL, default false | B コマ |
| period_code_c | boolean | NOT NULL, default false | C コマ |
| period_code_d | boolean | NOT NULL, default false | D コマ |
| created_at | timestamp | NOT NULL | |
| updated_at | timestamp | NOT NULL | |

#### `office_work_details`（事務業務詳細）

| カラム | 型 | 制約 | 説明 |
|-------|-----|------|------|
| id | uuid | PK | |
| work_id | uuid | FK → works（CASCADE） | |
| start_time | time | NULL 可 | 開始時刻 |
| end_time | time | NULL 可 | 終了時刻 |
| created_at | timestamp | NOT NULL | |
| updated_at | timestamp | NOT NULL | |

#### `other_work_details`（その他業務詳細）

| カラム | 型 | 制約 | 説明 |
|-------|-----|------|------|
| id | uuid | PK | |
| work_id | uuid | FK → works（CASCADE） | |
| start_time | time | NULL 可 | 開始時刻 |
| end_time | time | NULL 可 | 終了時刻 |
| break_minutes | integer | NULL 可 | 休憩時間（分） |
| description | varchar(500) | NULL 可 | 業務内容 |
| created_at | timestamp | NOT NULL | |
| updated_at | timestamp | NOT NULL | |

#### `templates`（テンプレート）

`works` と同じ構造でテンプレートとして保存。教室・交通費・業務詳細を定義する。

| カラム | 型 | 制約 | 説明 |
|-------|-----|------|------|
| id | uuid | PK | |
| tutor_id | uuid | FK → tutors（CASCADE） | |
| classroom_id | uuid | FK → classrooms | 勤務教室 |
| title | varchar(100) | NOT NULL | テンプレート名 |
| transportation_fee | integer | NOT NULL | 交通費（円） |
| created_at | timestamp | NOT NULL | |
| updated_at | timestamp | NOT NULL | |

#### `lesson_template_details` / `office_template_details` / `other_template_details`

`works` の詳細テーブルに対応するテンプレート用の詳細テーブル。カラム構造は各 `*_work_details` と同一で、`work_id` の代わりに `template_id`（FK → templates）を持つ。

### テーブル参照関係

```
accounts ──< managers >── classrooms
         └──< tutors   >── classrooms
                │
                ├──< works >── classrooms
                │      │
                │      ├── lesson_work_details    (1:1, LEFT JOIN)
                │      ├── office_work_details    (1:1, LEFT JOIN)
                │      └── other_work_details     (1:1, LEFT JOIN)
                │
                ├──< templates >── classrooms
                │      │
                │      ├── lesson_template_details   (1:1, LEFT JOIN)
                │      ├── office_template_details   (1:1, LEFT JOIN)
                │      └── other_template_details    (1:1, LEFT JOIN)
                │
                └──< salaries
```

- `accounts` と `managers` / `tutors` は `account_id` で 1:1 対応（`ON DELETE CASCADE`）
- `works` と各 `*_work_details` は `work_id` で 1:1 対応（`ON DELETE CASCADE`）
- 授業業務詳細（`lesson_work_details`）は常に存在。事務・その他業務詳細は任意のため `LEFT JOIN` で取得し、行がない場合は空 DTO として扱う
- `salaries` の `(tutor_id, effective_date)` ユニーク制約により、同一講師への同日付の給与設定は不可

---

## ローカル開発環境のセットアップ

### 前提条件

以下のツールを事前にインストールしてください。

#### 1. Git
ソースコードのバージョン管理に使用します。

- [Git 公式サイト](https://git-scm.com/) からインストーラーをダウンロードしてインストール
- インストール確認: `git --version`

#### 2. Java 21（JDK）
バックエンド（Spring Boot）の実行に必要です。**JRE ではなく JDK** をインストールしてください。

- **推奨**: [Eclipse Temurin（OpenJDK）](https://adoptium.net/) の Java 21 LTS
- macOS の場合は Homebrew でもインストール可能:
  ```bash
  brew install --cask temurin@21
  ```
- インストール確認: `java -version`（`openjdk 21` と表示されれば OK）
- `JAVA_HOME` 環境変数が Java 21 の JDK を指していることを確認してください

> Maven は `api/mvnw`（Maven Wrapper）が同梱されているため、別途インストール不要です。

#### 3. Docker Desktop
データベース（PostgreSQL）の実行に使用します。

- [Docker Desktop 公式サイト](https://www.docker.com/products/docker-desktop/) からインストール
- インストール後、Docker Desktop を起動しておく
- インストール確認: `docker --version` および `docker compose version`

#### 4. Node.js（v20.9 以上）
フロントエンド（Next.js）の実行に必要です。Next.js 16 では Node.js 18 のサポートが終了しているため、**v20.9 以上**が必須です。

- **推奨**: [nvm（Node Version Manager）](https://github.com/nvm-sh/nvm) を使ったバージョン管理
  ```bash
  # nvm でインストールする場合
  nvm install 20
  nvm use 20
  ```
- または [Node.js 公式サイト](https://nodejs.org/) から v20 LTS 以上をインストール
- インストール確認: `node --version`（v20.9.0 以上）および `npm --version`

---

### セットアップ手順

#### 1. リポジトリのクローン

```bash
git clone <リポジトリURL>
cd kintai-app
```

#### 2. データベースの起動

Docker Desktop が起動していることを確認してから、以下を実行します。

```bash
docker compose up -d
```

PostgreSQL 15 がコンテナとして起動します。接続情報は以下の通りです。

| 項目 | 値 |
|------|-----|
| ホスト | `localhost` |
| ポート | `5432` |
| データベース名 | `kintai_app_dev` |
| ユーザー名 | `postgres` |
| パスワード | `password` |

起動確認:
```bash
docker compose ps
# State が "Up" になっていれば OK
```

#### 3. バックエンドの起動

```bash
cd api
./mvnw spring-boot:run
```

> Windows の場合は `mvnw.cmd spring-boot:run`

- 初回起動時は Maven が依存ライブラリをダウンロードするため、数分かかります
- 起動後、Spring Boot が `schema.sql`（テーブル定義）と `data.sql`（教室マスタ）を自動実行し、DB を初期化します
- `Started ApiApplication` のログが表示されたら起動完了
- バックエンドは **ポート 8080** で動作します

#### 4. フロントエンドの起動

バックエンドが起動した状態で、別のターミナルで以下を実行します。

```bash
cd frontend
npm install      # 初回のみ（依存パッケージのインストール）
npm run dev
```

- フロントエンドは **ポート 3000** で動作します
- `/api/*` へのリクエストは `next.config.ts` の rewrites 設定によりバックエンド（ポート 8080）へ自動的にプロキシされます

#### 5. 動作確認

ブラウザで [http://localhost:3000](http://localhost:3000) にアクセスします。

**初回利用時のアカウント作成手順:**

1. [http://localhost:3000/manager/register](http://localhost:3000/manager/register) から管理者アカウントを登録します
   - 教室には初期データとして「戸塚（22番）」「東戸塚（23番）」が登録済みです
2. 登録後、ログインページからログインしてください

---

### 起動順序のまとめ

```
1. Docker Desktop 起動
       ↓
2. docker compose up -d（DB）
       ↓
3. ./mvnw spring-boot:run（バックエンド, ポート 8080）
       ↓
4. npm run dev（フロントエンド, ポート 3000）
```

> バックエンドが起動していない状態でフロントエンドを開いても、API 呼び出しがすべて失敗します。必ず上記の順序で起動してください。

---

### DB の初期化（データリセット）

開発中にデータをすべてリセットしたい場合:

```bash
docker compose down -v   # コンテナとボリューム（データ）を削除
docker compose up -d     # 再起動（Spring Boot 起動時に DB が再初期化される）
```

---

## 本番環境へのデプロイ（XServer VPS）

コスト最適化のため、AWS を使用せず XServer の VPS 1 台に全コンポーネント（Nginx / Next.js / Spring Boot / PostgreSQL）を構築する構成を採用する。ドメインも AWS（Route 53）から完全に移管し、AWS への依存をなくす。

### 全体アーキテクチャ

```
[ブラウザ] --HTTPS--> [Nginx] --HTTP--> [Next.js (next start, :3000)]
                                              │ サーバーサイド rewrites（同一ホスト内）
                                              ▼
                                    [Spring Boot (:8080, 127.0.0.1 のみ)]
                                              │
                                              ▼
                                    [PostgreSQL (:5432, localhost のみ)]
```

このアプリはブラウザから API を直接呼ばず、`next.config.ts` の `rewrites()` で Next.js が Spring Boot へサーバーサイド・プロキシする構成になっている。本番でもこの「ブラウザからは常に同一オリジンにしか見えない」構成を維持することで、CORS 設定を一切追加せずに済む。

> `SecurityConfig` で発行される Cookie（`JSESSIONID` / `XSRF-TOKEN` / `user_role`）は `secure(true)` が設定されているため、**HTTPS が正しく機能するまではログイン自体が動作しない**。デプロイ時は SSL 化を後回しにせず、初回の動作確認までに完了させる。

### ドメインの移行（AWS からの完全移管）

現在 AWS（Route 53）で取得・管理しているドメインを、レジストラごと XServer 側（または任意の他レジストラ）へ移管し、AWS の利用を完全に停止する。

1. **移管準備（AWS 側）**
   - ドメインの登録日・前回移管日から 60 日以上経過していることを確認する（ICANN ルールにより直近 60 日以内に登録・移管したドメインは再移管不可）
   - Route 53 コンソール → 「登録済みドメイン」→ 対象ドメインで **移管ロック（Transfer Lock）を解除**
   - 同画面から **認証コード（EPP コード）** の発行をリクエストする（登録者のメールアドレスに送付される）
2. **新レジストラでの移管申請**
   - XServer ドメイン（または任意のレジストラ）でアカウントを作成し、「ドメイン移管」から対象ドメイン名と取得した認証コードを入力して申請
   - AWS 側に送られる移管承認メールで **Approve** する（承認しない場合も一定期間で自動承認されるが、早めに承認した方がスムーズ）
3. **移管の完了待ち**
   - ICANN の異議申立て期間（最大 5 日）を経て移管が完了する。トータルで数日〜1 週間程度
   - 移管中は既存の DNS 設定（AWS 側）がそのまま維持されるため、サービス停止は発生しない
4. **新レジストラ側での DNS 設定**
   - 移管完了後、新レジストラ（XServer 等）の管理画面で A レコード（`@` および `www`）を **契約した VPS の固定グローバル IP** に向ける
   - 反映には DNS の TTL に応じて数分〜数時間かかる（`dig your-domain.com` で反映確認）
5. **AWS 側リソースの完全停止**
   - 移管完了・DNS 反映確認後、Route 53 の Hosted Zone を削除
   - 旧本番環境で稼働していた EC2 / RDS / ALB 等のコンピューティングリソースを削除し、AWS の利用を完全に停止する

### VPS の選定

- **プラン**: PostgreSQL・Spring Boot（JVM）・Next.js（Node）を 1 台に載せるため、メモリ **4GB 以上**のプランを推奨（JVM の起動時メモリ消費を考慮）
- **OS**: Ubuntu 22.04 LTS または 24.04 LTS（XServer VPS のテンプレートから選択可能）

### VPS 初期セットアップ

#### 1. SSH セキュリティ設定

```bash
# 一般ユーザーの作成（root 直接運用は避ける）
adduser deploy
usermod -aG sudo deploy

# 公開鍵認証を設定後、パスワードログイン・root 直接ログインを無効化
# /etc/ssh/sshd_config: PasswordAuthentication no, PermitRootLogin no
sudo systemctl restart sshd

# タイムゾーン設定
sudo timedatectl set-timezone Asia/Tokyo
```

#### 2. ファイアウォール

```bash
sudo ufw allow OpenSSH
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

#### 3. 必要ソフトウェアのインストール

```bash
# Java 21
sudo apt update && sudo apt install -y openjdk-21-jdk

# Node.js 20 LTS（Next.js 16 は Node.js 20.9 以上が必須。v18 は非対応）
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs

# PostgreSQL 15（開発環境の docker-compose.yml と同バージョンに揃える）
sudo apt install -y postgresql-15

# Nginx・Certbot（Let's Encrypt）
sudo apt install -y nginx certbot python3-certbot-nginx

# Git
sudo apt install -y git
```

#### 4. PostgreSQL の初期設定

```bash
sudo -u postgres psql -c "CREATE DATABASE kintai_app_prod;"
sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD '強固なパスワードに変更';"
```

### アプリケーション本番化に必要なコード修正

#### 1. バックエンド：接続情報の環境変数化

`api/src/main/resources/application.properties` の DB 接続情報を環境変数対応にし、`127.0.0.1` のみでリッスンするよう変更する。

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/kintai_app_dev}
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD:password}

server.address=127.0.0.1
server.port=${SERVER_PORT:8080}
```

デフォルト値を維持することで開発環境の動作は変わらず、本番では systemd の Unit ファイルから環境変数で上書きする。`server.address=127.0.0.1` により、バックエンドは外部から直接アクセスできず、Nginx → Next.js 経由でのみ到達可能になる。

#### 2. フロントエンド：バックエンド URL の環境変数化

`next.config.ts` と `app/excel/payslips/route.ts` にハードコードされている `http://localhost:8080` を、共通の環境変数（例: `BACKEND_INTERNAL_URL`）に切り出す。同一 VPS 上で稼働させる限り値自体は `http://127.0.0.1:8080` のまま変わらないが、設定を 1 箇所に外出しすることで将来の構成変更に対応しやすくする。

#### 3. 初期データの確認

`spring.sql.init.mode=always` により、起動ごとに `schema.sql` / `data.sql` が実行される（いずれも `create table if not exists` / `on conflict do nothing` で冪等なため、繰り返し実行しても安全）。ただし `data.sql` には開発用のサンプル教室データが含まれているため、本番投入前に内容を見直す。

### デプロイ手順

#### 1. ソース取得・ビルド

```bash
sudo mkdir -p /opt/kintai-app && sudo chown deploy:deploy /opt/kintai-app
git clone <リポジトリURL> /opt/kintai-app
cd /opt/kintai-app/api && ./mvnw clean package -DskipTests
cd /opt/kintai-app/frontend && npm ci && npm run build
```

#### 2. systemd サービス化（バックエンド）

`/etc/systemd/system/kintai-api.service`

```ini
[Unit]
Description=Kintai API (Spring Boot)
After=network.target postgresql.service

[Service]
User=deploy
WorkingDirectory=/opt/kintai-app/api
Environment=DB_URL=jdbc:postgresql://localhost:5432/kintai_app_prod
Environment=DB_USER=postgres
Environment=DB_PASSWORD=強固なパスワード
ExecStart=/usr/bin/java -jar /opt/kintai-app/api/target/api-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

#### 3. systemd サービス化（フロントエンド）

`/etc/systemd/system/kintai-frontend.service`

```ini
[Unit]
Description=Kintai Frontend (Next.js)
After=network.target kintai-api.service

[Service]
User=deploy
WorkingDirectory=/opt/kintai-app/frontend
Environment=NODE_ENV=production
Environment=BACKEND_INTERNAL_URL=http://127.0.0.1:8080
ExecStart=/usr/bin/npm run start
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now kintai-api kintai-frontend
```

#### 4. Nginx リバースプロキシ設定

`/etc/nginx/sites-available/kintai-app`

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://127.0.0.1:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/kintai-app /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

#### 5. SSL 証明書の取得（Let's Encrypt / Certbot）

DNS の向き先が VPS に反映されたことを確認した上で実行する。

```bash
sudo certbot --nginx -d your-domain.com -d www.your-domain.com
```

Certbot が Nginx 設定に SSL 部分を自動追記し、証明書の自動更新（systemd timer）も設定される。以後 `http://` へのアクセスは自動で `https://` にリダイレクトされる。

### 運用

- **DB バックアップ**: `pg_dump` を cron で定期実行し、VPS 内の別ディスク領域または外部ストレージに保存する
- **ログ確認**: `journalctl -u kintai-api -f` / `journalctl -u kintai-frontend -f`
- **デプロイの自動化（今後の実装メモ）**: GitHub Actions を用いた CI/CD を導入予定。ビルドは VPS 上で実行する方式（GitHub Actions のランナー上ではビルドしない）とし、以下の構成を想定している。
  - トリガー: `main` ブランチへの push
  - フロー: GitHub Actions から SSH（`appleboy/ssh-action` 等）で VPS に接続 → `git pull` → `./mvnw clean package -DskipTests` / `npm ci && npm run build` → `systemctl restart kintai-api kintai-frontend`
  - GitHub Secrets に登録が必要な値: VPS のホスト名（または移管後の本番ドメイン）、SSH 接続用ユーザー名、デプロイ専用の SSH 秘密鍵
  - 未実装のため、`.github/workflows/` にワークフローファイルはまだ存在しない。実装時はこのメモを起点に作業する

### デプロイ手順チェックリスト

1. [ ] ドメインを AWS（Route 53）から新レジストラへ移管し、AWS 側のリソース（Hosted Zone・EC2 等）を削除した
2. [ ] VPS を契約し、Ubuntu LTS で初期セットアップ（SSH 鍵認証・ufw・タイムゾーン）を行った
3. [ ] Java 21 / Node.js 20 以上 / PostgreSQL / Nginx / Certbot をインストールした
4. [ ] `application.properties` の DB 接続情報を環境変数化し、`server.address=127.0.0.1` を設定した
5. [ ] `next.config.ts` / `payslips/route.ts` のバックエンド URL を環境変数化した
6. [ ] `data.sql` の初期データ内容を本番向けに見直した
7. [ ] バックエンド・フロントエンドをビルドし、systemd サービスとして登録・起動した
8. [ ] Nginx のリバースプロキシ設定を行った
9. [ ] DNS の反映を確認後、Certbot で SSL 証明書を取得した
10. [ ] `https://` 経由でログイン・主要機能の動作確認を行った
