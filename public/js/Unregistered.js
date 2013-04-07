var App = function(){
    this.registerEvents();
}

App.prototype.registerEvents = function(){
    // this.registerRegister();
    this.registerLogin();
}

// App.prototype.registerRegister = function(){
//     $('#register').onButtonTap(function(){
//         var username = $('#regUsername').val();
//         var password = $('#regPassword').val();

        
//         this.ajaxFormJSON(  
//                 {
//                     username: username,
//                     password: password
//                 },
//                 '/register',
//                 function success(data){
//                     alert(JSON.stringify(data));
//                 },
//                 function error(xhr, status, err){
//                     alert(JSON.stringify(err));
//                 });
//     }.bind(this));
// }

App.prototype.ajaxFormJSON = function(json, url, onSuccess, onError){
    var data = new FormData();
    for (var key in json){
        data.append(key, json[key]);
    }

        $.ajax({
            url: url,
            data: data,
            cache: false,
            contentType: false,
            processData: false,
            type: 'POST',
            success: onSuccess,
            error: onError});
}

App.prototype.registerLogin = function(){
    console.log($('#login'));
    $('#login').click(function(){
        var username = $('#loginUsername').val();
        var password = $('#loginPassword').val();

        var data = new FormData();
        data.append('username', username);
        data.append('password', password);

        this.ajaxFormJSON(  
                {
                    username: username,
                    password: password
                },
                '/login',
                function success(data){
                    if (data === 'success'){
                        window.location = '/';
                    }
                    else {
                        alert(JSON.stringify(data));
                    }
                },
                function error(xhr, status, err){
                    alert(JSON.stringify(err));
                });
    }.bind(this));
}
