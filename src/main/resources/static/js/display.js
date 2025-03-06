// 詳細画面の表示・非表示を切り替えるプログラム
/// 読み込み元
//// detailClass.html
//// detailUser.html
//// info.html
//// infoTemplate.html
//// infoSalary.html

// 講師級フォームダウンロードフォームの表示・非表示を切り替える
const detailForm = Array.from(document.getElementsByClassName('detail-form-button'));
detailForm.forEach(form => {
    form.addEventListener('click', () => {
        let element = document.getElementsByClassName('detail-form-element')[0];
        if (element.style.display === 'none') {
            element.style.display = 'block';
        } else {
            element.style.display = 'none';
        }
    })
})

// 勤務詳細を表示する
const detailOn = Array.from(document.getElementsByClassName('detail-listener-on'));
detailOn.forEach(element => {
    element.addEventListener('click', () => {
        let panel = document.getElementsByClassName(element.className.split(' ')[0].replace('on', '') + 'element')[0];
        if (panel.style.display === 'none') {
            panel.style.display = 'block';
        }
    })
})

// 勤務詳細を非表示にする
const detailOff = Array.from(document.getElementsByClassName('detail-listener-off'));
detailOff.forEach(element => {
    element.addEventListener('click', () => {
        let panel = document.getElementsByClassName(element.className.split(' ')[0].replace('off', '') + 'element')[0];
        if (panel.style.display === 'block') {
            panel.style.display = 'none';
        }
    })
})

// 研修費・時間外内訳の表示・非表示を切り替える
const detailSalary = Array.from(document.getElementsByClassName('detail-salary-button'));
detailSalary.forEach(element => {
    element.addEventListener('click', () => {
        let panel = document.getElementsByClassName('detail-salary-element')[0];
        if (panel.style.display === 'none') {
            panel.style.display = 'block';
        } else {
            panel.style.display = 'none';
        }
    })
})
