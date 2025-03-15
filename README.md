# 勤怠管理システム

## CSSについて
- htmlファイルのheadタグ内に、以下の指定をする
- ``` <link th:href="@{/css/style.css}" rel="stylesheet"> ```
- style.cssの部分は、任意のファイル名を指定する
- cssファイルは、main/resources/static/css内にある
- 何個でもcssファイル作成可能
  
## JavaScriptについて
- htmlファイルのheadタグ内に、以下の指定をする
- ``` <script th:src="@{/js/test.js}"></script> ```
- test.jsの部分は、任意のファイル名を指定する
- jsファイルは、main/resources/static/js内にある
- 何個でもjsファイル作成可能

## DBについて
### セットアップの流れ（ローカル）
- pgAdmin4の初期設定を行う（エンジニアフリークス第7章を参照）
  - username: postgres
  - password: postgres
- pgAdmin4の「Servers/PostgreSQL/データベース」内に、DBを作成する
  - DB名: kintaisystemdbで登録する
- あとは、初回のサーバー起動（アプリケーション実行）時に自動で必要なテーブルが作成される
- 初回以降は、保存されたレコードがそのまま使える

### テーブル構成
- managers: 管理者基本情報を格納
  - id(UUID)
  - loginId(string)
  - password(string)
  - classArea(string)
- users: 講師基本情報を格納
  - id(UUID)
  - loginId(string)
  - userName(string)
  - password(string)
  - classAreaId(UUID)
  - teacherNo(int)
  - state(bool)
  - retireDate(string)
- works: シフト情報を格納
  - id(UUID)
  - userId(UUID)
  - date(string)
  - dayOfWeek(string)
  - classM(bool)
  - classK(bool)
  - classS(bool)
  - classA(bool)
  - classB(bool)
  - classC(bool)
  - classD(bool)
  - classCount(int)
  - helpArea(string)
  - timeStart(string)
  - timeEnd(string)
  - breakTime(int)
  - officeTimeStart(string)
  - officeTimeEnd(string)
  - officeTime(int)
  - otherWork(string)
  - otherTimeStart(string)
  - otherTimeEnd(string)
  - otherBreakTime(int)
  - otherTime(int)
  - carfare(int)
  - outOfTime(int)
  - overTime(int)
  - nightTime(int)
  - supportSalary(string)
- salaries: 給与情報を管理
  - id(UUID)
  - userId(UUID)
  - dateFrom(string)
  - classSalary(int)
  - officeSalary(int)
  - supportSalary(int)
  - carfare(int)
- workTemplates: テンプレート情報を管理
  - id(UUID)
  - userId(UUID)
  - title(string)
  - classM(bool)
  - classK(bool)
  - classS(bool)
  - classA(bool)
  - classB(bool)
  - classC(bool)
  - classD(bool)
  - helpArea(string)
  - timeStart(string)
  - timeEnd(string)
  - breakTime(int)
  - officeTimeStart(string)
  - officeTimeEnd(string)
  - otherWork(string)
  - otherTimeStart(string)
  - otherTimeEnd(string)
  - otherBreakTime(int)
  - carfare(int)
  - outOfTime(int)
  - overTime(int)
  - nightTime(int)
- incometaxes: 所得税の対応表
  - id(int)
  - min(int)
  - max(int)
  - tax(int)
- locks: 編集権限のステータスを管理
  - id(UUID)
  - classAreaId(UUID)
  - userId(UUID)
  - year(int)
  - month(int)
  - status(bool)

## Appの起動（ローカル）について
### VsCode
- VSCodeで下記プラグインをインストール
  - Java Extension Pack
  - Spring Boot Extension Pack
  - Lombok Annotations Support for VS Code
- cloneしたフォルダ（リポジトリ）に移動する
- Spring Boot Dashboard > APPS > 起動ボタンをクリック
- 起動が完了したら、以下のリンクにアクセスし、ログイン完了後に各画面に遷移できる
- 講師ログイン
  - http://localhost:8080/login
- 社員ログイン
  - http://localhost:8080/loginManager
  
## Appの概要
- 講師: http://localhost:8080/login
- 社員: http://localhost:8080/loginManager
- からどこの画面にも遷移できるようになっている
- 社員アカウント作成 > 社員アカウントログイン > 講師アカウントの登録
- ここまで済めば、
- http://localhost:8080/login
- から講師ログインが可能

## htmlファイルの詳細
### 講師用（スマホで使う想定）
- login: 講師ログイン
- index: ホーム
- info: シフト管理
- addForm: シフト管理 > シフト登録
- editForm: シフト管理 > シフト修正
- infoTemplate: テンプレート
- templateForm: テンプレート > 登録
- editTemplateForm: テンプレート > 修正
- infoSalary: 給与情報
- detailSalary: 給与情報 > 給与推移
- user: アカウント
- userForm: アカウント > アカウント情報修正

### 管理者用（PCで使う想定）
- signUpManager: 管理者サインアップ
- loginManager: 管理者ログイン
- indexManager: ホーム
- detailClass: 講師管理
- signUp: 講師管理 > 新規講師登録
- infoUser: 講師管理 > 基本情報
- editUserForm: 講師管理 > 基本情報 > アカウント情報修正
- retireForm: 講師管理 > 基本情報 > 退職手続
- updateForm: 講師管理 > 基本情報 > 給与更新
- salaryForm: 講師管理 > 基本情報 > 給与修正
- detail: 給与管理
- createForm: 給与管理 > シフト登録
- setForm: 給与管理 > シフト修正
- infoManager: アカウント
- editManagerForm: アカウント > アカウント情報修正
