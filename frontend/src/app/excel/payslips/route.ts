import { NextRequest, NextResponse } from "next/server";
import path from "path";
import { readFile } from "fs/promises";
import ExcelJS from "exceljs";
import JSZip from "jszip";
import {
  computeRow,
  findStandardFee,
  type RowValues,
} from "@/lib/excelCalc";

// docker-compose ではコンテナ名（api）で解決し、ローカル開発では localhost を使う
const BACKEND = process.env.API_BASE_URL ?? "http://localhost:8080";
const CIRCLED = [
  "①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨",
  "⑩", "⑪", "⑫", "⑬", "⑭", "⑮", "⑯", "⑰", "⑱",
  "⑲", "⑳", "㉑", "㉒", "㉓", "㉔", "㉕", "㉖", "㉗",
  "㉘", "㉙", "㉚",
];
const WEEKDAYS = ["日", "月", "火", "水", "木", "金", "土"];

function setTimeFrac(ws: ExcelJS.Worksheet, row: number, col: number, val: number | null) {
  if (val == null) return;
  const c = ws.getCell(row, col);
  c.value = val;
  c.numFmt = "h:mm";
}

function dateToWeekday(dateStr: string): string {
  const [y, m, d] = dateStr.split("-").map(Number);
  return WEEKDAYS[new Date(y, m - 1, d).getDay()];
}

export async function GET(request: NextRequest) {
  const { searchParams } = new URL(request.url);
  const year = parseInt(searchParams.get("year") ?? "0");
  const month = parseInt(searchParams.get("month") ?? "0");
  if (!year || !month) {
    return NextResponse.json({ error: "year and month are required" }, { status: 400 });
  }

  const cookie = request.headers.get("cookie") ?? "";
  const headers = { cookie };

  const tutorsRes = await fetch(`${BACKEND}/api/tutors`, { headers });
  if (tutorsRes.status === 401) {
    return NextResponse.redirect(new URL("/manager/login", request.url));
  }
  if (!tutorsRes.ok) {
    return NextResponse.json({ error: "Failed to fetch tutors" }, { status: 500 });
  }
  const tutors: any[] = await tutorsRes.json();
  if (!tutors.length) {
    return NextResponse.json({ error: "No tutors found" }, { status: 404 });
  }
  // 講師番号の昇順で出力する（在籍・退職に関係なく同じ並びで処理する）
  // tutorNumber は INTEGER だが、API 応答にない/数値変換できない場合は未設定として末尾に回す
  const tutorNumberOrder = (t: any) => {
    if (t?.tutorNumber == null || t.tutorNumber === "") return Number.MAX_SAFE_INTEGER;
    const n = Number(t.tutorNumber);
    return Number.isNaN(n) ? Number.MAX_SAFE_INTEGER : n;
  };
  const sortedTutors = [...tutors].sort(
    (a, b) => tutorNumberOrder(a) - tutorNumberOrder(b)
  );

  const tutorData = await Promise.all(
    sortedTutors.map(async (tutor: any) => {
      const [worksRes, salariesRes] = await Promise.all([
        fetch(`${BACKEND}/api/works/${tutor.id}?year=${year}&month=${month}`, { headers }),
        fetch(`${BACKEND}/api/salaries/${tutor.id}`, { headers }),
      ]);
      const works = worksRes.ok ? await worksRes.json() : [];
      const salaries = salariesRes.ok ? await salariesRes.json() : [];
      return { tutor, works, salaries };
    })
  );

  const templatePath = path.join(process.cwd(), "templates", "excelTemplate.xlsx");
  const wb = new ExcelJS.Workbook();
  await wb.xlsx.readFile(templatePath);

  const classroomName: string = tutors[0]?.classroomName ?? "";
  const prevMonth = month === 1 ? 12 : month - 1;
  const topWs = wb.getWorksheet("トップ ");

  if (topWs) {
    topWs.getCell(1, 7).value = year;           // G1: 年
    topWs.getCell(1, 9).value = month;          // I1: 月
    topWs.getCell(1, 11).value = classroomName; // K1: 教室名
  }

  tutorData.slice(0, 30).forEach(({ tutor, works, salaries }, idx) => {
    const sheetName = `${CIRCLED[idx]}${tutor.lastName}`;
    const ws = wb.getWorksheet(`sheet${idx + 1}`);
    if (!ws) return;
    ws.name = sheetName;

    const stdFee = findStandardFee(salaries, year, month);
    const sorted = [...works].sort((a: any, b: any) =>
      a.workingDate.localeCompare(b.workingDate)
    );

    // 退職済み講師のタブ色（白、背景1、黒+基本色25% = #BFBFBF）
    if (tutor.terminationDate) {
      ws.properties.tabColor = { argb: "FFBFBFBF" };
    }

    // ─── Row 1: ヘッダー情報 ───────────────────────────────────────
    ws.getCell(1, 5).value = classroomName;                           // E1: 教室名
    ws.getCell(1, 17).value = `${tutor.lastName} ${tutor.firstName}`; // Q1: 氏名
    ws.getCell(1, 26).value = year;                                   // Z1: 年
    ws.getCell(1, 30).value = month;                                  // AD1: 月
    ws.getCell(1, 34).value = prevMonth;                              // AH1: 前月
    ws.getCell(1, 41).value = month;                                  // AO1: 当月

    // ─── Row 4 集計値の積算 ────────────────────────────────────────
    let totalPeriods = 0, totalDays = 0;
    let totalOfficeFrac = 0, totalDailyAllowance = 0, totalTransportFee = 0;
    let totalTrainingFrac = 0, totalOvertimeFrac = 0;
    let totalExcessFrac = 0, totalNightFrac = 0;

    const rowCache: (RowValues | null)[] = [];

    for (let i = 0; i < 36; i++) {
      const w = sorted[i];
      if (!w) { rowCache.push(null); continue; }

      const rv = computeRow(w);
      rowCache.push(rv);

      totalPeriods += rv.I;
      totalDays++;
      if (rv.X != null) totalOfficeFrac += rv.X;
      totalDailyAllowance += w.dailyAllowance ?? 0;
      totalTransportFee += w.transportationFee ?? 0;
      if (rv.AN != null) totalTrainingFrac += rv.AN;
      if (rv.AP != null) totalOvertimeFrac += rv.AP;
      if (rv.AR != null) totalExcessFrac += rv.AR;
      if (rv.AT != null) totalNightFrac += rv.AT;
    }

    // ─── Row 4: 集計行（(分)列は整数分、それ以外はそのまま） ──────────
    ws.getCell(4, 2).value = totalPeriods;                         // B4: コマ数
    ws.getCell(4, 5).value = totalDays;                            // E4: 日数
    ws.getCell(4, 8).value = Math.round(totalOfficeFrac * 1440);   // H4: 事務時間（分）
    ws.getCell(4, 12).value = totalDailyAllowance;                 // L4: 日次手当合計
    ws.getCell(4, 16).value = totalTransportFee;                   // P4: 交通費合計
    ws.getCell(4, 20).value = Math.round(totalTrainingFrac * 1440);// T4: 研修時間（分）
    ws.getCell(4, 24).value = Math.round(totalOvertimeFrac * 1440);// X4: 時間外（分）
    ws.getCell(4, 28).value = Math.round(totalExcessFrac * 1440);  // AB4: 超過（分）
    ws.getCell(4, 32).value = Math.round(totalNightFrac * 1440);   // AF4: 深夜（分）
    ws.getCell(4, 40).value = stdFee;                              // AN4: 標準交通費
    ws.getCell(4, 43).value = 0;                                   // AQ4: 交通費②

    // ─── トップシート ──────────────────────────────────────────────
    if (topWs) {
      const r = 34 + idx;
      topWs.getCell(r, 1).value = idx + 1;         // A: No
      topWs.getCell(r, 2).value = tutor.tutorNumber ?? null; // B: 講師No
      topWs.getCell(r, 3).value = `${tutor.lastName} ${tutor.firstName}`; // C: 氏名
      if (tutor.terminationDate) {
        const termCell = topWs.getCell(r, 4);
        const [ty, tm] = (tutor.terminationDate as string).split("-").map(Number);
        termCell.value = `${ty}年${tm}月`;
      }
      topWs.getCell(r, 5).value = totalPeriods;                          // E: コマ数
      topWs.getCell(r, 6).value = totalDays;                             // F: 日数
      topWs.getCell(r, 7).value = Math.round(totalOfficeFrac * 1440);    // G: 事務(分)
      topWs.getCell(r, 8).value = totalDailyAllowance;                   // H: 日次手当
      topWs.getCell(r, 9).value = totalTransportFee;                     // I: 交通費
      topWs.getCell(r, 10).value = Math.round(totalTrainingFrac * 1440); // J: 研修(分)
      topWs.getCell(r, 11).value = Math.round(totalOvertimeFrac * 1440); // K: 時間外(分)
      topWs.getCell(r, 12).value = Math.round(totalExcessFrac * 1440);   // L: 超過(分)
      topWs.getCell(r, 13).value = Math.round(totalNightFrac * 1440);    // M: 深夜(分)

      // 退職済み講師の行をグレー背景でハイライト（B〜D列のみ）
      if (tutor.terminationDate) {
        for (let col = 2; col <= 4; col++) {
          const cell = topWs.getCell(r, col);
          // exceljs はスタイルを共有するため、深いコピーで共有参照を切る
          const ownStyle = JSON.parse(JSON.stringify(cell.style ?? {}));
          ownStyle.fill = { type: "pattern", pattern: "solid", fgColor: { argb: "FFBFBFBF" } };
          cell.style = ownStyle;
        }
      }
    }

    // ─── データ行 8-43 ─────────────────────────────────────────────
    for (let i = 0; i < 36; i++) {
      const row = 8 + i;
      const w = sorted[i];
      const rv = rowCache[i];

      if (!w || !rv) {
        // テンプレートの数式セルを空値で上書き
        ws.getCell(row, 51).value = null; // AY
        ws.getCell(row, 52).value = null; // AZ
        ws.getCell(row, 53).value = null; // BA
        ws.getCell(row, 54).value = null; // BB
        ws.getCell(row, 55).value = null; // BC
        continue;
      }

      // B: 日付（"m月d日" 形式の文字列として挿入）
      const [, m, d] = w.workingDate.split("-").map(Number);
      ws.getCell(row, 2).value = `${m}月${d}日`;

      // E: 曜日
      ws.getCell(row, 5).value = dateToWeekday(w.workingDate);

      // F: コマ記号
      if (rv.periodCodes) ws.getCell(row, 6).value = rv.periodCodes;

      // I: コマ数
      if (rv.I > 0) ws.getCell(row, 9).value = rv.I;

      // K: ヘルプ教室（自教室と異なる場合のみ）
      if (w.classroomId !== tutor.classroomId && w.classroomName) {
        ws.getCell(row, 11).value = w.classroomName;
      }

      // N, P, R: 授業 開始/終了/休憩
      setTimeFrac(ws, row, 14, rv.N);
      setTimeFrac(ws, row, 16, rv.P);
      setTimeFrac(ws, row, 18, rv.R);

      // T, V: 事務 開始/終了
      setTimeFrac(ws, row, 20, rv.T);
      setTimeFrac(ws, row, 22, rv.V);

      // X: 事務時間 (V-T)
      setTimeFrac(ws, row, 24, rv.X);

      // Z: 日次手当
      if ((w.dailyAllowance ?? 0) > 0) ws.getCell(row, 26).value = w.dailyAllowance;

      // AB: 交通費
      if ((w.transportationFee ?? 0) > 0) ws.getCell(row, 28).value = w.transportationFee;

      // AD: 研修内容
      if (w.otherWorkDetail?.description) ws.getCell(row, 30).value = w.otherWorkDetail.description;

      // AH, AJ, AL: 研修 開始/終了/休憩
      setTimeFrac(ws, row, 34, rv.AH);
      setTimeFrac(ws, row, 36, rv.AJ);
      setTimeFrac(ws, row, 38, rv.AL);

      // AN: 研修時間 (AJ-AH-AL)
      setTimeFrac(ws, row, 40, rv.AN);

      // AP: 時間外
      setTimeFrac(ws, row, 42, rv.AP);

      // AR: 超過時間外
      setTimeFrac(ws, row, 44, rv.AR);

      // AT: 深夜合計
      setTimeFrac(ws, row, 46, rv.AT);

      // AY: 総勤務時間（テンプレート数式を値で上書き）
      const ayCell = ws.getCell(row, 51);
      ayCell.value = rv.AY;
      if (rv.AY != null) ayCell.numFmt = "h:mm";

      // AZ: 授業深夜（テンプレート数式を値で上書き）
      const azCell = ws.getCell(row, 52);
      azCell.value = rv.AZ > 1e-9 ? rv.AZ : null;
      if (rv.AZ > 1e-9) azCell.numFmt = "h:mm";

      // BA: 事務深夜（テンプレート数式を値で上書き）
      const baCell = ws.getCell(row, 53);
      baCell.value = rv.BA > 1e-9 ? rv.BA : null;
      if (rv.BA > 1e-9) baCell.numFmt = "h:mm";

      // BB: 研修深夜（テンプレート数式を値で上書き）
      const bbCell = ws.getCell(row, 54);
      bbCell.value = rv.BB > 1e-9 ? rv.BB : null;
      if (rv.BB > 1e-9) bbCell.numFmt = "h:mm";

      // BC: 深夜計（テンプレート数式を値で上書き）
      const bcCell = ws.getCell(row, 55);
      bcCell.value = rv.BC > 1e-9 ? rv.BC : null;
      if (rv.BC > 1e-9) bcCell.numFmt = "h:mm";
    }
  });

  // 講師数 < 30 の場合、余分なシートを「①新採用」形式に改名してヘッダーだけ挿入
  // ①②③… の Unicode 丸数字（CIRCLED と同一値）
  const usedSheetCount = Math.min(tutorData.length, 30);
  for (let i = usedSheetCount; i < 30; i++) {
    const emptyWs = wb.getWorksheet(`sheet${i + 1}`);
    if (!emptyWs) continue;
    emptyWs.name = `${CIRCLED[i]}新採用${i - usedSheetCount + 1}`;
    emptyWs.getCell(1, 5).value = classroomName;  // E1: 教室名
    emptyWs.getCell(1, 26).value = year;           // Z1: 年
    emptyWs.getCell(1, 30).value = month;          // AD1: 月
    emptyWs.getCell(1, 34).value = prevMonth;      // AH1: 前月
    emptyWs.getCell(1, 41).value = month;          // AO1: 当月
  }

  const rawBuffer = await wb.xlsx.writeBuffer();

  // exceljs が sheetView 属性・ページブレークを落とすため、JSZip で XML を直接修正する
  const zip = await JSZip.loadAsync(rawBuffer as ArrayBuffer);

  // workbook.xml + rels から「トップ」シートの実ファイルパスを動的に特定する
  // （exceljs は内部 id 順でファイルを番号付けするため sheet1.xml とは限らない）
  const wbXml = (await zip.files["xl/workbook.xml"]?.async("string")) ?? "";
  const relsXml = (await zip.files["xl/_rels/workbook.xml.rels"]?.async("string")) ?? "";
  const topRId = wbXml.match(/<sheet\b[^>]*name="トップ[^"]*"[^>]*r:id="([^"]*)"/)?.[1] ?? "";
  const topTarget = relsXml.match(new RegExp(`Id="${topRId}"[^>]*Target="([^"]*)"`  ))?.[1] ?? "";
  const topSheetFile = topTarget ? `xl/${topTarget}` : "";

  for (const filename of Object.keys(zip.files)) {
    if (!filename.match(/^xl\/worksheets\/sheet\d+\.xml$/)) continue;
    let xml = await zip.files[filename].async("string");
    const isTopSheet = topSheetFile !== "" && filename === topSheetFile;

    // sheetView の zoomScale を 100 に固定（開いた際に常に 100% 表示されるよう保証）
    xml = xml.replace(/\s+zoomScale="[^"]*"/, "");
    xml = xml.replace(/<sheetView\b/, '<sheetView zoomScale="100"');

    // view="pageBreakPreview" がなければ追加
    if (!xml.includes('view="pageBreakPreview"')) {
      xml = xml.replace(/<sheetView\b/, '<sheetView view="pageBreakPreview"');
    }
    // zoomScaleNormal がなければ追加
    if (!xml.includes("zoomScaleNormal=")) {
      xml = xml.replace(/<sheetView\b/, '<sheetView zoomScaleNormal="100"');
    }
    // zoomScaleSheetLayoutView がなければ追加
    if (!xml.includes("zoomScaleSheetLayoutView=")) {
      xml = xml.replace(/<sheetView\b/, '<sheetView zoomScaleSheetLayoutView="100"');
    }
    // showZeros="0" がなければ追加（ゼロ値を非表示。トップシートは除外）
    if (!xml.includes("showZeros=") && !isTopSheet) {
      xml = xml.replace(/<sheetView\b/, '<sheetView showZeros="0"');
    }

    // exceljs が pageSetup に追加する余分な属性を削除
    xml = xml.replace(/<pageSetup\b([^/]*)\/>/g, (_, attrs) => {
      const cleaned = attrs
        .replace(/\s+fitToWidth="[^"]*"/, "")
        .replace(/\s+fitToHeight="[^"]*"/, "")
        .replace(/\s+firstPageNumber="[^"]*"/, "")
        .replace(/\s+useFirstPageNumber="[^"]*"/, "")
        .replace(/\s+copies="[^"]*"/, "")
        .replace(/\s+horizontalDpi="[^"]*"/, "")
        .replace(isTopSheet ? /\s+scale="100"/ : /(?:)/, "");
      return `<pageSetup${cleaned}/>`;
    });

    // sheetFormatPr をテンプレート準拠に正規化（baseColWidth 追加、exceljs 追加属性を除去）
    xml = xml.replace(/<sheetFormatPr\b[^/]*\/>/g, (match) => {
      const drh = match.match(/\bdefaultRowHeight="([^"]*)"/)?.[1];
      const dcw = match.match(/\bdefaultColWidth="([^"]*)"/)?.[1];
      let attrs = ' baseColWidth="10"';
      if (dcw) attrs += ` defaultColWidth="${dcw}"`;
      if (drh) attrs += ` defaultRowHeight="${drh}"`;
      return `<sheetFormatPr${attrs}/>`;
    });

    // 列幅を均一拡大（トップシート: 1.15倍、講師シート: 1.2倍）
    const colWidthScale = isTopSheet ? 1.15 : 1.2;
    xml = xml.replace(
      /(<col\b[^>]*\bwidth=")([^"]*)("[^>]*\/>)/g,
      (match, pre, width, post) => {
        if (match.includes('hidden="1"')) return match;
        return `${pre}${(parseFloat(width) * colWidthScale).toFixed(8)}${post}`;
      }
    );

    // トップシートの N 列以降（col 14+）の明示的定義を削除
    // また、行の高さを 0.9 倍に縮小
    if (isTopSheet) {
      xml = xml.replace(/<col\b[^>]*\bmin="14"[^>]*\/>/g, "");
      xml = xml.replace(/\bht="([^"]*)"/g, (_, ht) => {
        return `ht="${(parseFloat(ht) * 0.9).toFixed(8)}"`;
      });
    }

    // 列ブレークを注入（講師シートのみ / col 42 / man=1）
    // トップシートはテンプレートに列ブレークが存在しないため注入しない
    if (!isTopSheet && !xml.includes("<colBreaks")) {
      xml = xml.replace(
        "</worksheet>",
        `<colBreaks count="1" manualBreakCount="1"><brk id="42" max="42" man="1"/></colBreaks></worksheet>`
      );
    }

    zip.file(filename, xml);
  }

  // ===== Fix A: チェックボックス復元 =====
  // exceljs は Form Control（VML チェックボックス）を一切削除するため、
  // テンプレートから drawing/VML/ctrlProps ファイルをそのままコピーして復元する
  {
    const tmplZip = await JSZip.loadAsync(await readFile(templatePath));

    // テンプレートのトップシートファイルを動的に特定
    const tmplWbXml = (await tmplZip.files["xl/workbook.xml"]?.async("string")) ?? "";
    const tmplRelsXml = (await tmplZip.files["xl/_rels/workbook.xml.rels"]?.async("string")) ?? "";
    const tmplTopRId = tmplWbXml.match(/<sheet\b[^>]*name="トップ[^"]*"[^>]*r:id="([^"]*)"/)?.[1] ?? "";
    const tmplTopTarget = tmplRelsXml.match(new RegExp(`Id="${tmplTopRId}"[^>]*Target="([^"]*)"`))?.[1] ?? "";
    const tmplTopSheetFile = `xl/${tmplTopTarget}`;

    // 1. drawing1.xml / vmlDrawing1.vml / ctrlProp1-14.xml をコピー
    const filesToCopy = [
      "xl/drawings/drawing1.xml",
      "xl/drawings/vmlDrawing1.vml",
      ...Array.from({ length: 14 }, (_, i) => `xl/ctrlProps/ctrlProp${i + 1}.xml`),
    ];
    for (const f of filesToCopy) {
      if (tmplZip.files[f]) {
        zip.file(f, await tmplZip.files[f].async("uint8array"));
      }
    }

    // 2. 出力トップシート用の .rels ファイルを生成
    // テンプレートの rels から printerSettings を除いて rId 衝突を回避
    const tmplTopRelsPath = `xl/worksheets/_rels/${tmplTopTarget.split("/").pop()}.rels`;
    const tmplTopRels = (await tmplZip.files[tmplTopRelsPath]?.async("string")) ?? "";
    const outTopRels = tmplTopRels.replace(
      /<Relationship[^/]*printerSettings[^/]*\/>/g,
      ""
    );
    if (topSheetFile && outTopRels) {
      const outTopBasename = topSheetFile.replace("xl/worksheets/", "");
      zip.file(`xl/worksheets/_rels/${outTopBasename}.rels`, outTopRels);
    }

    // 3. トップシート XML に drawing / legacyDrawing / controls を注入
    // テンプレートから <drawing ...> 以降（</worksheet> 直前まで）を抽出
    const tmplTopXml = (await tmplZip.files[tmplTopSheetFile]?.async("string")) ?? "";
    const ctrlStart = tmplTopXml.indexOf("<drawing");
    const controlsSection =
      ctrlStart > 0 ? tmplTopXml.slice(ctrlStart, tmplTopXml.lastIndexOf("</worksheet>")) : "";

    if (controlsSection && topSheetFile) {
      let topXml = await zip.files[topSheetFile].async("string");
      // xdr: / x14: namespace が未宣言の場合は worksheet 要素に追加
      if (!topXml.includes("xmlns:xdr=")) {
        topXml = topXml.replace(
          "<worksheet ",
          '<worksheet xmlns:xdr="http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing" '
        );
      }
      if (!topXml.includes("xmlns:x14=")) {
        topXml = topXml.replace(
          "<worksheet ",
          '<worksheet xmlns:x14="http://schemas.microsoft.com/office/spreadsheetml/2009/9/main" '
        );
      }
      topXml = topXml.replace("</worksheet>", `${controlsSection}</worksheet>`);
      zip.file(topSheetFile, topXml);
    }

    // 4. [Content_Types].xml に drawing1.xml と ctrlProp1-14.xml のエントリを追加
    let ctXml = (await zip.files["[Content_Types].xml"]?.async("string")) ?? "";
    if (!ctXml.includes("drawing1.xml")) {
      ctXml = ctXml.replace(
        "</Types>",
        '<Override PartName="/xl/drawings/drawing1.xml" ContentType="application/vnd.openxmlformats-officedocument.drawing+xml"/></Types>'
      );
    }
    for (let i = 1; i <= 14; i++) {
      if (!ctXml.includes(`ctrlProp${i}.xml`)) {
        ctXml = ctXml.replace(
          "</Types>",
          `<Override PartName="/xl/ctrlProps/ctrlProp${i}.xml" ContentType="application/vnd.ms-excel.controlproperties+xml"/></Types>`
        );
      }
    }
    zip.file("[Content_Types].xml", ctXml);
  }

  // ===== Fix B: トップシートの Print_Area をテンプレート値に修正 =====
  // exceljs が localSheetId="0"（トップシート）に誤った Print_Area を追加するため
  // 一旦削除した上で、テンプレートと同じ $A$1:$M$51 を正しく挿入する
  {
    let wbXmlFixed = wbXml.replace(
      /<definedName name="_xlnm\.Print_Area" localSheetId="0">[^<]*<\/definedName>\n?/g,
      ""
    );
    const topPrintArea = `<definedName name="_xlnm.Print_Area" localSheetId="0">'トップ '!$A$1:$M$63</definedName>`;
    wbXmlFixed = wbXmlFixed.replace("</definedNames>", `${topPrintArea}</definedNames>`);
    zip.file("xl/workbook.xml", wbXmlFixed);
  }

  const buffer = await zip.generateAsync({ type: "arraybuffer" });

  const classroomNumber: number = tutors[0]?.classroomNumber ?? 0;
  const filename = `講師給フォーム(${classroomNumber}${classroomName})${year}.${month}.xlsx`;

  return new Response(buffer, {
    headers: {
      "Content-Type": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      "Content-Disposition": `attachment; filename*=UTF-8''${encodeURIComponent(filename)}`,
    },
  });
}
