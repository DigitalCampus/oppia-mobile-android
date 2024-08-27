(function(){
    const slides = document.querySelectorAll('slide');
    const totalSlides = slides.length;
    const sliderContainer = document.querySelector('slide').parentNode;

    if (totalSlides > 0) {
        let touchStartX = 0;
        let touchEndX = 0;
        let touchStartY = 0;
        let touchEndY = 0;
        let currentSlide;

        sliderContainer.addEventListener('touchstart', (e) => {
            touchStartX = e.changedTouches[0].screenX;
            touchStartY = e.changedTouches[0].screenY;
            currentSlide = Array.from(slides).findIndex(slide => slide.classList.contains('active'));
            {{INTERFACE_EXPOSED_NAME}}.canScroll(true);
        });

        function handleTouchEnd(e) {
            touchEndX = e.changedTouches[0].screenX;
            touchEndY = e.changedTouches[0].screenY;

            deltaX = touchEndX - touchStartX;
            deltaY = touchEndY - touchStartY;

            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                if ((deltaX > 0 && currentSlide === 0) ||
                    (deltaX < 0 && currentSlide === totalSlides - 1)) {
                    {{INTERFACE_EXPOSED_NAME}}.canScroll(false);
                } else {
                    {{INTERFACE_EXPOSED_NAME}}.canScroll(true);
                }
            }
        }

        sliderContainer.addEventListener('touchcancel', handleTouchEnd);
        sliderContainer.addEventListener('touchend', handleTouchEnd);
        sliderContainer.addEventListener('touchmove', handleTouchEnd);
    }

})();

