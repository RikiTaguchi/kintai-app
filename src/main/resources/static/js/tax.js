// 所得税のオン・オフを切り替えるプログラム
/// 読み込み元
//// infoSalary.html
//// detailUser.html

// URLの取得
const url = new URL(window.location);

// 要素の取得（所得税）
const taxOn = document.getElementById("tax-on");
const taxOff = document.getElementById("tax-off");
const taxButton = document.getElementById("tax-button");

// 要素の取得（給与詳細）
const tableDetail = document.getElementsByClassName("detail-table");
const buttonDetail = document.getElementsByClassName("detail-button");
const nextButton = document.getElementById("next-button");
const backButton = document.getElementById("back-button");
let styleDisplay;

// 初期化（所得税）
if (url.searchParams.has("tax") && url.searchParams.get("tax") === "on") {
    taxOn.checked = true;
    taxOff.checked = false;
} else {
    taxOn.checked = false;
    taxOff.checked = true;
}

// 初期化（給与詳細）
if (url.searchParams.has("detail") && url.searchParams.get("detail") === "on") {
    styleDisplay = "table";
} else {
    styleDisplay = "none";
}
for (let i = 0; i < tableDetail.length; i++) {
    tableDetail[i].style.display = styleDisplay;
}

// 切替（所得税）
taxOn.addEventListener("change", () => {
    let taxURL = new URL(taxButton.href);
    taxURL.searchParams.set("tax", "on");
    taxButton.href = taxURL.toString();
    taxButton.click();
});
taxOff.addEventListener("change", () => {
    let taxURL = new URL(taxButton.href);
    taxURL.searchParams.set("tax", "off");
    taxButton.href = taxURL.toString();
    taxButton.click();
});

// 切替（給与詳細）
for (let i = 0; i < buttonDetail.length; i++) {
    buttonDetail[i].addEventListener("click", () => {
        let taxButtonURL = new URL(taxButton.href);
        let nextButtonURL = new URL(nextButton.href);
        let backButtonURL = new URL(backButton.href);
        if (url.searchParams.get("detail") === "on") {
            taxButtonURL.searchParams.set("detail", "off");
            nextButtonURL.searchParams.set("detail", "off");
            backButtonURL.searchParams.set("detail", "off");
        } else {
            taxButtonURL.searchParams.set("detail", "on");
            nextButtonURL.searchParams.set("detail", "on");
            backButtonURL.searchParams.set("detail", "on");
        }
        taxButton.href = taxButtonURL.toString();
        nextButton.href = nextButtonURL.toString();
        backButton.href = backButtonURL.toString();
        for (let i = 0; i < tableDetail.length; i++) {
            if (url.searchParams.get("detail") === "on") {
                tableDetail[i].style.display = "none";
            } else {
                tableDetail[i].style.display = "table";
            }
        }
        if (url.searchParams.get("detail") === "on") {
            url.searchParams.set("detail", "off");
        } else {
            url.searchParams.set("detail", "on");
        }
        history.replaceState(null, "", url);
    })
}
