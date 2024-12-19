# 勤怠管理システム

## CSSについて
- htmlファイルのheadタグ内に、以下の指定をする。
- ``` <link th:href="@{/css/style.css}" rel="stylesheet"> ```
- style.cssの部分は、任意のファイル名を指定する
- cssファイルは、main/resources/static/css内にある
- 何個でもcssファイル作成可能
  
## JavaScriptについて
- htmlファイルのheadタグ内に、以下の指定をする。
- ``` <script th:src="@{/js/test.js}"></script> ```
- test.jsの部分は、任意のファイル名を指定する
- jsファイルは、main/resources/static/js内にある
- 何個でもjsファイル作成可能

## DBについて
### セットアップの流れ
- pgAdmin4の初期設定を行う（エンジニアフリークス第7章を参照）
  - username: postgres
  - password: postgres
- pgAdmin4の「Servers/PostgreSQL/データベース」内に、DBを作成する
  - DB名: kintai_systemで登録する（アンダーバーなのでハイフンにしないよう注意！）
- あとは、初回のサーバー起動（アプリケーション実行）時に自動で必要なテーブルが作成される
- 初回以降は、保存されたレコードがそのまま使える

## Appの起動（サーバーの起動）について
### Eclipse
- Eclipse内で、KintaiSystemフォルダにカーソルを合わせる
- 右クリック
- 「実行 > SpringBootアプリケーション」でサーバー起動
- コンソールにログが出力されるので、エラーが起きてないか確認
- 起動が完了したら、以下のリンクにアクセスし、ログイン完了後に各画面に遷移できる
- 講師ログイン
  - http://localhost:8080/login
- 社員ログイン
  - http://localhost:8080/loginManager
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
- からどこの画面にも遷移できるようになっています！
- 社員アカウント作成 > 社員アカウントログイン > 講師アカウントの登録
- ここまで済めば、
- http://localhost:8080/login
- から講師ログインが可能

## htmlファイルの詳細
### 講師用（スマホで使う想定）
##### ベース機能
- login: 講師ログイン
- index: ホーム画面
- detail: 給与明細
##### シフト関連
- addForm: シフト登録
- detailWork: シフト詳細
- editForm: シフト修正
##### テンプレート関連
- infoTemplate: テンプレート一覧
- templateForm: テンプレート登録
- detailTemplate: テンプレート詳細
- editTemplateForm: テンプレート修正
##### その他
- user: 基本情報詳細
- userForm: 基本情報修正
- detailSalary: 昇給情報
### 社員用（PCで使う想定）
##### ベース機能
- loginManager: 社員ログイン
- signUpManager: 社員アカウント登録
- signUp: 講師アカウント登録
- indexManager: ホーム画面
- infoManager: 社員アカウント情報詳細
- editManagerForm: 社員アカウント情報修正
##### 講師関連
- createForm: シフト登録
- detailUser: 給与詳細、シフト詳細
- setForm: シフト修正
- infoUser: 基本情報
- infoSalary: 昇給情報一覧
- updateForm: 昇給登録
- salaryForm: 昇給修正
