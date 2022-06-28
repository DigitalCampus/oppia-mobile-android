(function(){
    $('[name=reveal]').each(function(){
        var revealSection = $(this);
        if (revealSection.has('button').length > 0){
            revealSection.find('button').on('click', function(){
                inputValue = revealSection.find('input').eq(0).val();
                if (inputValue != null && inputValue != ''){
                    {{INTERFACE_EXPOSED_NAME}}.registerInlineInput(inputValue);
                }
            });
        }
    });
})();