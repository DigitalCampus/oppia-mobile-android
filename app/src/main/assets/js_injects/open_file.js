(function(){
    var imgs = document.querySelectorAll('img');
    Array.prototype.forEach.call(imgs, function(img, i){
        if (img.parentNode.nodeName.toLowerCase()!=='a'){
            img.addEventListener('click', function(){
                {{INTERFACE_EXPOSED_NAME}}.openFile(img.getAttribute('src'));
            });
        }
    });
})();