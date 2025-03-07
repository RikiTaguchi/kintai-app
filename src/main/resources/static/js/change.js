// 対象ユーザーを切り替えるプログラム
// チェックボックスの値を処理するプログラム
/// 読み込み元
//// detail.html

// ユーザー切替
const changeSelect = Array.from(document.getElementsByClassName('change-select'))[0];
changeSelect.addEventListener('change', function(event) {
    let option = event.target.options[event.target.selectedIndex];
    let button = Array.from(document.getElementsByClassName('button-'+option.className.replace('option-', '')))[0];
    button.click();
});

// チェックボックス
const checkBox = document.getElementById('lock');
checkBox.addEventListener('change', function() {
    this.previousElementSibling.value = this.checked ? "true" : "false";
});

// form送信時にロックステータス画像を変更する
const downloadForm = document.getElementById('downloadForm');
const lockOff = document.getElementById('lockOff');
const lockOn = document.getElementById('lockOn');
const formPanel = Array.from(document.getElementsByClassName('detail-form-element'))[0];
downloadForm.addEventListener('submit', function(event) {
    const formData = new FormData(this);
    const params = new URLSearchParams(formData).toString();
    const lockStatus = params.split('&')[2].split('=')[1];
    if (lockStatus === 'true' && lockOn === null && lockOff !== null) {
        lockOff.src = '/img/lock.svg';
    }
    formPanel.style.display = 'none';
});
