(function(){
    $(".audio-player-container, #audio-player-container").each(function(){
        container = $(this);
        var audioElem = container.find("audio")[0];
        container.find(".play-icon,#play-icon").on('click', function(){
            var audioSource = audioElem.getAttribute("src");
            {{INTERFACE_EXPOSED_NAME}}.onPlayButtonClick(audioSource);
        });
        audioElem.addEventListener("ended", function() {
            var audioSource = audioElem.getAttribute("src");
            {{INTERFACE_EXPOSED_NAME}}.onAudioCompleted(audioSource);
        });
    });
})();