// 所得税の情報を管理
const taxOn = document.getElementById('tax-on');
const taxOff = document.getElementById('tax-off');
const taxButton = document.getElementById('tax-button');
const params = new URLSearchParams(window.location.search);

if (params.has('tax') && params.get('tax') === 'on') {
    taxOn.checked = true;
    taxOff.checked = false;
} else {
    taxOn.checked = false;
    taxOff.checked = true;
}

taxOn.addEventListener('change', () => {
    let url = new URL(taxButton.href);
    url.searchParams.set('tax', 'on');
    taxButton.href = url.toString();
    taxButton.click();
});

taxOff.addEventListener('change', () => {
    let url = new URL(taxButton.href);
    url.searchParams.set('tax', 'off');
    taxButton.href = url.toString();
    taxButton.click();
});
