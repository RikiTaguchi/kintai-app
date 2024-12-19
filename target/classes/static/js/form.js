// テンプレートを自動入力する関数
function setTemplate(templateId) {
    const classM = document.getElementById('classM');
    const classK = document.getElementById('classK');
    const classS = document.getElementById('classS');
    const classA = document.getElementById('classA');
    const classB = document.getElementById('classB');
    const classC = document.getElementById('classC');
    const classD = document.getElementById('classD');
    const helpArea = document.getElementById('helpArea');
    const timeStart = document.getElementById('timeStart');
    const timeEnd = document.getElementById('timeEnd');
    const breakTime = document.getElementById('breakTime');
    const carfare = document.getElementById('carfare');
    const officeTimeStart = document.getElementById('officeTimeStart');
    const officeTimeEnd = document.getElementById('officeTimeEnd');
    const otherWork = document.getElementById('otherWork');
    const otherTimeStart = document.getElementById('otherTimeStart');
    const otherTimeEnd = document.getElementById('otherTimeEnd');
    const otherBreakTime = document.getElementById('otherBreakTime');
    
    if (document.getElementById('classM-'+templateId).textContent === 'true') {classM.setAttribute('checked', '');} else {classM.removeAttribute('checked');}
    if (document.getElementById('classK-'+templateId).textContent === 'true') {classK.setAttribute('checked', '');} else {classK.removeAttribute('checked');}
    if (document.getElementById('classS-'+templateId).textContent === 'true') {classS.setAttribute('checked', '');} else {classS.removeAttribute('checked');}
    if (document.getElementById('classA-'+templateId).textContent === 'true') {classA.setAttribute('checked', '');} else {classA.removeAttribute('checked');}
    if (document.getElementById('classB-'+templateId).textContent === 'true') {classB.setAttribute('checked', '');} else {classB.removeAttribute('checked');}
    if (document.getElementById('classC-'+templateId).textContent === 'true') {classC.setAttribute('checked', '');} else {classC.removeAttribute('checked');}
    if (document.getElementById('classD-'+templateId).textContent === 'true') {classD.setAttribute('checked', '');} else {classD.removeAttribute('checked');}
    helpArea.value = document.getElementById('helpArea-'+templateId).textContent;
    timeStart.value = document.getElementById('timeStart-'+templateId).textContent;
    timeEnd.value = document.getElementById('timeEnd-'+templateId).textContent;
    breakTime.value = document.getElementById('breakTime-'+templateId).textContent;
    carfare.value = document.getElementById('carfare-'+templateId).textContent;
    officeTimeStart.value = document.getElementById('officeTimeStart-'+templateId).textContent;
    officeTimeEnd.value = document.getElementById('officeTimeEnd-'+templateId).textContent;
    otherWork.value = document.getElementById('otherWork-'+templateId).textContent;
    otherTimeStart.value = document.getElementById('otherTimeStart-'+templateId).textContent;
    otherTimeEnd.value = document.getElementById('otherTimeEnd-'+templateId).textContent;
    otherBreakTime.value = document.getElementById('otherBreakTime-'+templateId).textContent;
    
    return false;
}
