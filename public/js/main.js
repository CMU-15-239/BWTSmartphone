$(document).ready(function(){
    //load all current words for assignment
    var words= ['dog', 'cat', 'awesome'];
    for (var i=0; i<words.length; i++) {
        var html = '';
            html += '<tr><td><input type="checkbox"></td><td><input readonly type="text" value="'+ words[i]+ 
                    '"></td><td><input type="text" value="'+'placeholder' +'"></td><td><input type="text" value="'+ 'placeholder'+'"></td></tr>';
        $('#wordTable tr').first().after(html);
    }
});

