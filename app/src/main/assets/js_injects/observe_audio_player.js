(function(){
    var playButton = document.getElementById("play-icon");
    playButton.addEventListener("click", function() {
        var audioElement = document.querySelector("audio");
        var audioSource = audioElement.getAttribute("src");
        {{INTERFACE_EXPOSED_NAME}}.onPlayButtonClick(audioSource);
        audioElement.addEventListener("ended", function() {
            {{INTERFACE_EXPOSED_NAME}}.onAudioCompleted(audioSource);
        });
    });
})();