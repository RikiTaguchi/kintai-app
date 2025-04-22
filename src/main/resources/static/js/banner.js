const banner = document.querySelector('.banner');
const optionsBanner = {
    duration: 3000,
    easing: 'ease',
    fill: 'forwards',
};
const displayBanner = {
    translate: ['0 0', '0 120%', '0 120%', '0 120%', '0 120%', '0 120%', '0 0'],
    opacity: [0, 1, 1, 1, 1, 1, 0],
};

if (banner) {
    banner.style.display = 'flex';
    banner.animate(displayBanner, optionsBanner);
    setTimeout(() => {
        banner.style.display = 'none';
    }, '3000');
    const url = new URL(window.location.href);
    url.searchParams.delete('banner');
    window.history.replaceState({}, '', url);
}
