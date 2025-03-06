// 所得税のオン・オフを切り替えるプログラム
/// 読み込み元
//// infoSalary.html

// URLの取得
const url = new URL(window.location);

// 要素の取得（所得税）
const taxOn = document.getElementById("tax-on");
const taxOff = document.getElementById("tax-off");
const taxButton = document.getElementById("tax-button");

// 初期化（所得税）
if (url.searchParams.has("tax") && url.searchParams.get("tax") === "on") {
    taxOn.checked = true;
    taxOff.checked = false;
} else {
    taxOn.checked = false;
    taxOff.checked = true;
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
