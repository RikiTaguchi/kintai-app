-- ============================================================
-- テストデータ（表示確認用・拡充版 v2）
--
-- ログイン情報（全アカウント共通パスワード: password）
--   管理者: manager01 （本郷台教室）
--   講師  : tutor01 山田 太郎（在籍・データ多数）
--           tutor02 佐藤 花子（在籍・9月勤務なし）
--           tutor03 鈴木 一郎（退職）
--           tutor04 高橋 健（在籍・テンプレ3件）
--           tutor05 伊藤 美咲（在籍・朝勤務中心）
--           tutor06 渡辺 翔（在籍・ヘルプ勤務多め）
-- ============================================================

begin;

-- ------------------------------------------------------------
-- 既存のテストデータを削除（再実行可能にするため login_id で特定）
-- ------------------------------------------------------------
delete from accounts
where login_id in (
    'manager01',
    'tutor01', 'tutor02', 'tutor03', 'tutor04', 'tutor05', 'tutor06'
);

-- ------------------------------------------------------------
-- アカウント（既存と同一の BCrypt ハッシュを再利用 → password）
-- ------------------------------------------------------------
insert into accounts (id, login_id, password) values
    ('aaaaaaaa-0000-4000-8000-000000000001', 'manager01', '$2y$10$p2EIkbpprkBVm2oFIOFkmu7wMd6ejmfSSw8EJcxcuTIzLgLa.AWZ6'),
    ('aaaaaaaa-0000-4000-8000-000000000011', 'tutor01',   '$2y$10$p2EIkbpprkBVm2oFIOFkmu7wMd6ejmfSSw8EJcxcuTIzLgLa.AWZ6'),
    ('aaaaaaaa-0000-4000-8000-000000000012', 'tutor02',   '$2y$10$p2EIkbpprkBVm2oFIOFkmu7wMd6ejmfSSw8EJcxcuTIzLgLa.AWZ6'),
    ('aaaaaaaa-0000-4000-8000-000000000013', 'tutor03',   '$2y$10$p2EIkbpprkBVm2oFIOFkmu7wMd6ejmfSSw8EJcxcuTIzLgLa.AWZ6'),
    ('aaaaaaaa-0000-4000-8000-000000000014', 'tutor04',   '$2y$10$p2EIkbpprkBVm2oFIOFkmu7wMd6ejmfSSw8EJcxcuTIzLgLa.AWZ6'),
    ('aaaaaaaa-0000-4000-8000-000000000015', 'tutor05',   '$2y$10$p2EIkbpprkBVm2oFIOFkmu7wMd6ejmfSSw8EJcxcuTIzLgLa.AWZ6'),
    ('aaaaaaaa-0000-4000-8000-000000000016', 'tutor06',   '$2y$10$p2EIkbpprkBVm2oFIOFkmu7wMd6ejmfSSw8EJcxcuTIzLgLa.AWZ6');

-- ------------------------------------------------------------
-- 管理者（本郷台教室）
-- ------------------------------------------------------------
insert into managers (id, account_id, classroom_id, first_name, last_name) values
    ('bbbbbbbb-0000-4000-8000-000000000001',
     'aaaaaaaa-0000-4000-8000-000000000001',
     '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19',
     '管理者', 'テスト');

-- ------------------------------------------------------------
-- 講師（いずれも本郷台教室）
-- ------------------------------------------------------------
insert into tutors (id, account_id, classroom_id, tutor_number, first_name, last_name, terminated, termination_date) values
    ('cccccccc-0000-4000-8000-000000000001', 'aaaaaaaa-0000-4000-8000-000000000011',
     '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', 101, '太郎', '山田', false, null),
    ('cccccccc-0000-4000-8000-000000000002', 'aaaaaaaa-0000-4000-8000-000000000012',
     '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', 102, '花子', '佐藤', false, null),
    ('cccccccc-0000-4000-8000-000000000003', 'aaaaaaaa-0000-4000-8000-000000000013',
     '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', 103, '一郎', '鈴木', true, '2026-10-25'),
    ('cccccccc-0000-4000-8000-000000000004', 'aaaaaaaa-0000-4000-8000-000000000014',
     '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', 104, '健',   '高橋', false, null),
    ('cccccccc-0000-4000-8000-000000000005', 'aaaaaaaa-0000-4000-8000-000000000015',
     '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', 105, '美咲', '伊藤', false, null),
    ('cccccccc-0000-4000-8000-000000000006', 'aaaaaaaa-0000-4000-8000-000000000016',
     '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', 106, '翔',   '渡辺', false, null);

-- ------------------------------------------------------------
-- 給与情報
--   山田・佐藤・高橋・伊藤 ... 2026-04-01 適用 → 日次手当 1コマ=205円 / 2コマ以上=410円
--   鈴木・渡辺           ... 2024-07-01 適用 → 旧ルール（コマ数に関わらず満額410円）
-- ------------------------------------------------------------
insert into salaries (id, tutor_id, effective_date, lesson_wage, office_wage, transportation_fee) values
    ('dddddddd-0000-4000-8000-000000000001', 'cccccccc-0000-4000-8000-000000000001', '2026-04-01', 1700, 1100, 400),
    ('dddddddd-0000-4000-8000-000000000002', 'cccccccc-0000-4000-8000-000000000002', '2026-04-01', 1650, 1080, 500),
    ('dddddddd-0000-4000-8000-000000000003', 'cccccccc-0000-4000-8000-000000000006', '2024-07-01', 1800, 1150, 600),
    ('dddddddd-0000-4000-8000-000000000004', 'cccccccc-0000-4000-8000-000000000004', '2026-04-01', 1750, 1120, 400),
    ('dddddddd-0000-4000-8000-000000000005', 'cccccccc-0000-4000-8000-000000000005', '2026-04-01', 1600, 1050, 300),
    ('dddddddd-0000-4000-8000-000000000006', 'cccccccc-0000-4000-8000-000000000003', '2024-07-01', 1800, 1150, 600);

-- ------------------------------------------------------------
-- 勤務情報（works 本体）
-- 9月期 = 2026-08-26 〜 2026-09-25 / 10月期 = 2026-09-26 〜 2026-10-25
-- ------------------------------------------------------------
insert into works (id, tutor_id, classroom_id, working_date, transportation_fee) values
    -- ===== 山田 太郎 / 9月期 =====
    ('eeeeeeee-0000-4000-8000-000000000001', 'cccccccc-0000-4000-8000-000000000001', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-01', 400),
    ('eeeeeeee-0000-4000-8000-000000000002', 'cccccccc-0000-4000-8000-000000000001', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-02', 400),
    ('eeeeeeee-0000-4000-8000-000000000003', 'cccccccc-0000-4000-8000-000000000001', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-03', 400),
    ('eeeeeeee-0000-4000-8000-000000000004', 'cccccccc-0000-4000-8000-000000000001', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-04', 400),
    ('eeeeeeee-0000-4000-8000-000000000005', 'cccccccc-0000-4000-8000-000000000001', '1f3a2b8e-4c5d-4e6f-9a0b-1c2d3e4f5a01', '2026-09-05', 600),
    ('eeeeeeee-0000-4000-8000-000000000006', 'cccccccc-0000-4000-8000-000000000001', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-08', 400),
    ('eeeeeeee-0000-4000-8000-000000000007', 'cccccccc-0000-4000-8000-000000000001', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-09', 400),
    ('eeeeeeee-0000-4000-8000-000000000008', 'cccccccc-0000-4000-8000-000000000001', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-10', 400),
    ('eeeeeeee-0000-4000-8000-000000000009', 'cccccccc-0000-4000-8000-000000000001', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-11', 400),
    ('eeeeeeee-0000-4000-8000-00000000000a', 'cccccccc-0000-4000-8000-000000000001', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-12', 0),
    ('eeeeeeee-0000-4000-8000-00000000000b', 'cccccccc-0000-4000-8000-000000000001', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-15', 400),
    ('eeeeeeee-0000-4000-8000-00000000000c', 'cccccccc-0000-4000-8000-000000000001', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-25', 400),
    ('eeeeeeee-0000-4000-8000-00000000000d', 'cccccccc-0000-4000-8000-000000000001', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-26', 400),
    ('eeeeeeee-0000-4000-8000-00000000000e', 'cccccccc-0000-4000-8000-000000000001', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-08-26', 400),
    -- 佐藤 花子
    ('eeeeeeee-0000-4000-8000-00000000000f', 'cccccccc-0000-4000-8000-000000000002', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-10-05', 500),
    -- 鈴木 一郎
    ('eeeeeeee-0000-4000-8000-000000000010', 'cccccccc-0000-4000-8000-000000000003', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-20', 600),
    -- ===== 高橋 健 / 9月期 =====
    ('eeeeeeee-0000-4000-8000-000000000011', 'cccccccc-0000-4000-8000-000000000004', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-01', 400),
    ('eeeeeeee-0000-4000-8000-000000000012', 'cccccccc-0000-4000-8000-000000000004', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-03', 400),
    ('eeeeeeee-0000-4000-8000-000000000013', 'cccccccc-0000-4000-8000-000000000004', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-04', 400),
    -- ===== 伊藤 美咲 / 9月期（朝中心） =====
    ('eeeeeeee-0000-4000-8000-000000000014', 'cccccccc-0000-4000-8000-000000000005', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-06', 300),
    ('eeeeeeee-0000-4000-8000-000000000015', 'cccccccc-0000-4000-8000-000000000005', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-07', 300),
    ('eeeeeeee-0000-4000-8000-000000000016', 'cccccccc-0000-4000-8000-000000000005', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-13', 300),
    ('eeeeeeee-0000-4000-8000-000000000017', 'cccccccc-0000-4000-8000-000000000005', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-14', 300),
    -- ===== 渡辺 翔 / 9月期（ヘルプ＋深夜混合） =====
    ('eeeeeeee-0000-4000-8000-000000000018', 'cccccccc-0000-4000-8000-000000000006', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-02', 600),
    ('eeeeeeee-0000-4000-8000-000000000019', 'cccccccc-0000-4000-8000-000000000006', '1f3a2b8e-4c5d-4e6f-9a0b-1c2d3e4f5a01', '2026-09-06', 600),
    ('eeeeeeee-0000-4000-8000-00000000001a', 'cccccccc-0000-4000-8000-000000000006', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-16', 600),
    ('eeeeeeee-0000-4000-8000-00000000001b', 'cccccccc-0000-4000-8000-000000000006', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2026-09-22', 600);

-- ------------------------------------------------------------
-- 授業明細
-- ------------------------------------------------------------
insert into lesson_work_details
    (work_id, start_time, end_time, break_minutes,
     period_code_m, period_code_k, period_code_s,
     period_code_a, period_code_b, period_code_c, period_code_d) values
    ('eeeeeeee-0000-4000-8000-000000000001', '18:10', '21:20', 0,  false,false,false, false,false, true, true),
    ('eeeeeeee-0000-4000-8000-000000000002', '18:10', '19:40', 0,  false,false,false, false,false, true, false),
    ('eeeeeeee-0000-4000-8000-000000000003', null, null, null,     false,false,false, false,false,false,false),
    ('eeeeeeee-0000-4000-8000-000000000004', null, null, null,     false,false,false, false,false,false,false),
    ('eeeeeeee-0000-4000-8000-000000000005', '14:50', '19:40', 15, false,false,false, true,true, true, false),
    ('eeeeeeee-0000-4000-8000-000000000006', '18:10', '22:30', 10, false,false,false, false,false, true, true),
    ('eeeeeeee-0000-4000-8000-000000000007', '13:10', '21:20', 60, false,false,true,  true,true, true, true),
    ('eeeeeeee-0000-4000-8000-000000000008', '14:50', '17:30', 0,  false,false,false, true,false, false,false),
    ('eeeeeeee-0000-4000-8000-000000000009', '16:30', '21:20', 0,  false,false,false, false,true, true, true),
    ('eeeeeeee-0000-4000-8000-00000000000a', '18:10', '19:40', 0,  false,false,false, false,false, true, false),
    ('eeeeeeee-0000-4000-8000-00000000000b', '09:10', '12:20', 10, true, true, false, false,false, false,false),
    ('eeeeeeee-0000-4000-8000-00000000000c', '19:50', '21:20', 0,  false,false,false, false,false,false,true),
    ('eeeeeeee-0000-4000-8000-00000000000d', '18:10', '21:20', 0,  false,false,false, false,false, true, true),
    ('eeeeeeee-0000-4000-8000-00000000000e', '16:30', '19:40', 0,  false,false,false, false,true, true, false),
    ('eeeeeeee-0000-4000-8000-00000000000f', '14:50', '18:00', 0,  false,false,false, true,true, false,false),
    ('eeeeeeee-0000-4000-8000-000000000010', '16:30', '21:20', 0,  false,false,false, false,true, true, true),
    -- 高橋 健
    ('eeeeeeee-0000-4000-8000-000000000011', '14:50', '16:20', 0, false,false,false, true,false, false,false),
    ('eeeeeeee-0000-4000-8000-000000000012', '16:30', '18:00', 0, false,false,false, false,true, false,false),
    ('eeeeeeee-0000-4000-8000-000000000013', '18:10', '21:20', 0, false,false,false, false,false, true, true),
    -- 伊藤 美咲
    ('eeeeeeee-0000-4000-8000-000000000014', '09:10', '12:20', 0, true, true, false, false,false, false,false),
    ('eeeeeeee-0000-4000-8000-000000000015', '09:10', '12:20', 0, true, true, false, false,false, false,false),
    ('eeeeeeee-0000-4000-8000-000000000016', '09:10', '14:40', 30, true, true, true, false,false, false,false),
    ('eeeeeeee-0000-4000-8000-000000000017', '10:50', '14:40', 0, false,true, true, false,false, false,false),
    -- 渡辺 翔
    ('eeeeeeee-0000-4000-8000-000000000018', '18:10', '22:00', 0,  false,false,false, false,false, true, true),
    ('eeeeeeee-0000-4000-8000-000000000019', '14:50', '19:40', 15, false,false,false, true,true, true, false),
    ('eeeeeeee-0000-4000-8000-00000000001a', '18:10', '22:30', 0,  false,false,false, false,false, true, true),
    ('eeeeeeee-0000-4000-8000-00000000001b', '18:10', '21:20', 0,  false,false,false, false,false, true, true);

-- ------------------------------------------------------------
-- 事務明細
-- ------------------------------------------------------------
insert into office_work_details (work_id, start_time, end_time) values
    ('eeeeeeee-0000-4000-8000-000000000001', '21:30', '22:00'),
    ('eeeeeeee-0000-4000-8000-000000000002', null, null),
    ('eeeeeeee-0000-4000-8000-000000000003', '14:00', '17:00'),
    ('eeeeeeee-0000-4000-8000-000000000004', null, null),
    ('eeeeeeee-0000-4000-8000-000000000005', '13:00', '14:30'),
    ('eeeeeeee-0000-4000-8000-000000000006', null, null),
    ('eeeeeeee-0000-4000-8000-000000000007', '09:00', '13:00'),
    ('eeeeeeee-0000-4000-8000-000000000008', null, null),
    ('eeeeeeee-0000-4000-8000-000000000009', '15:00', '16:15'),
    ('eeeeeeee-0000-4000-8000-00000000000a', null, null),
    ('eeeeeeee-0000-4000-8000-00000000000b', null, null),
    ('eeeeeeee-0000-4000-8000-00000000000c', null, null),
    ('eeeeeeee-0000-4000-8000-00000000000d', null, null),
    ('eeeeeeee-0000-4000-8000-00000000000e', null, null),
    ('eeeeeeee-0000-4000-8000-00000000000f', null, null),
    ('eeeeeeee-0000-4000-8000-000000000010', '15:30', '16:30'),
    -- 高橋
    ('eeeeeeee-0000-4000-8000-000000000011', null, null),
    ('eeeeeeee-0000-4000-8000-000000000012', null, null),
    ('eeeeeeee-0000-4000-8000-000000000013', null, null),
    -- 伊藤
    ('eeeeeeee-0000-4000-8000-000000000014', null, null),
    ('eeeeeeee-0000-4000-8000-000000000015', null, null),
    ('eeeeeeee-0000-4000-8000-000000000016', null, null),
    ('eeeeeeee-0000-4000-8000-000000000017', null, null),
    -- 渡辺
    ('eeeeeeee-0000-4000-8000-000000000018', null, null),
    ('eeeeeeee-0000-4000-8000-000000000019', null, null),
    ('eeeeeeee-0000-4000-8000-00000000001a', null, null),
    ('eeeeeeee-0000-4000-8000-00000000001b', null, null);

-- ------------------------------------------------------------
-- 研修・その他明細
-- ------------------------------------------------------------
insert into other_work_details (work_id, start_time, end_time, break_minutes, description) values
    ('eeeeeeee-0000-4000-8000-000000000001', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-000000000002', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-000000000003', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-000000000004', '10:00', '15:00', 60, '新人講師研修（数学指導法）'),
    ('eeeeeeee-0000-4000-8000-000000000005', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-000000000006', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-000000000007', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-000000000008', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-000000000009', '13:00', '14:30', 0, '教室ミーティング'),
    ('eeeeeeee-0000-4000-8000-00000000000a', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-00000000000b', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-00000000000c', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-00000000000d', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-00000000000e', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-00000000000f', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-000000000010', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-000000000011', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-000000000012', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-000000000013', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-000000000014', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-000000000015', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-000000000016', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-000000000017', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-000000000018', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-000000000019', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-00000000001a', null, null, null, null),
    ('eeeeeeee-0000-4000-8000-00000000001b', null, null, null, null);

-- ------------------------------------------------------------
-- テンプレート（高橋 健 に3つ登録）
-- ① 標準夕方（A,Bコマ） ② 2コマ終電（C,D） ③ 事務のみ（授業コマ0）
-- ------------------------------------------------------------
insert into templates (id, tutor_id, classroom_id, title, transportation_fee) values
    ('ffffffff-0000-4000-8000-000000000001', 'cccccccc-0000-4000-8000-000000000004', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '標準夕方（A,B）',       400),
    ('ffffffff-0000-4000-8000-000000000002', 'cccccccc-0000-4000-8000-000000000004', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '2コマ終電（C,D）',    400),
    ('ffffffff-0000-4000-8000-000000000003', 'cccccccc-0000-4000-8000-000000000004', '5b7c9d1e-3f4a-4b6c-9d5e-7f8a9b0c1d19', '事務のみ',           400);

-- テンプレート授業明細
insert into lesson_template_details
    (template_id, start_time, end_time, break_minutes,
     period_code_m, period_code_k, period_code_s,
     period_code_a, period_code_b, period_code_c, period_code_d) values
    ('ffffffff-0000-4000-8000-000000000001', '14:50', '18:00', 0, false,false,false, true,true, false,false),
    ('ffffffff-0000-4000-8000-000000000002', '18:10', '21:20', 0, false,false,false, false,false, true, true),
    ('ffffffff-0000-4000-8000-000000000003', null, null, null,    false,false,false, false,false,false,false);

-- テンプレート事務明細（①②はなし、③は14:00-17:00）
insert into office_template_details (template_id, start_time, end_time) values
    ('ffffffff-0000-4000-8000-000000000001', null, null),
    ('ffffffff-0000-4000-8000-000000000002', null, null),
    ('ffffffff-0000-4000-8000-000000000003', '14:00', '17:00');

-- テンプレートその他明細（全てなし）
insert into other_template_details (template_id, start_time, end_time, break_minutes, description) values
    ('ffffffff-0000-4000-8000-000000000001', null, null, null, null),
    ('ffffffff-0000-4000-8000-000000000002', null, null, null, null),
    ('ffffffff-0000-4000-8000-000000000003', null, null, null, null);

commit;
