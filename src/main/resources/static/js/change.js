// 対象ユーザーを切り替えるプログラム
/// 読み込み元
//// detail.html

// 要素の取得
const changeSelect = Array.from(document.getElementsByClassName('change-select'))[0];
changeSelect.addEventListener('change', function(event) {
    let option = event.target.options[event.target.selectedIndex];
    let button = Array.from(document.getElementsByClassName('button-'+option.className.replace('option-', '')))[0];
    button.click();
});
