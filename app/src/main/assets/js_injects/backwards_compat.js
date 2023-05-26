(function(){
    var versionWarnings = document.querySelectorAll('.compat-warning');
    Array.prototype.forEach.call(versionWarnings, function(warn, i){
        var version = warn.getAttribute('data-version');
        if (version){
            version = parseInt(version);
            if({{INTERFACE_EXPOSED_NAME}}.checkCompatibility(version)){
                warn.remove();
            }
        }
    });
})();