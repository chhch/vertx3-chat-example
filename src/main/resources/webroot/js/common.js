// Configure loading modules from the chat directory,
// except 'main' ones,
requirejs.config({
    "baseUrl": "../js/chat",
    //"baseUrl": "js/chat",
    "paths": {
        // on a productive environment you would may prefer to use [ cdn address, local path ] instead of only cdn address
        "jquery": "http://code.jquery.com/jquery-1.11.3.min",
        "bootstrap": "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min",
        "knockout": "https://ajax.aspnetcdn.com/ajax/knockout/knockout-3.3.0",
        "sockjs": "https://cdn.jsdelivr.net/sockjs/1.0.3/sockjs.min",
        "vertxbus": "https://cdnjs.cloudflare.com/ajax/libs/vertx/2.0.0/vertxbus.min",
        "dateformat": "http://cdnjs.cloudflare.com/ajax/libs/jquery-dateFormat/1.0/jquery.dateFormat.min",
        "emoji": "https://cdn.jsdelivr.net/emojione/2.0.0/lib/js/emojione.min",
        "common": "../common"
    },
    "shim": {
        /* Set bootstrap dependencies, because bootstrap don't support AM */
        'bootstrap' : ['jquery'],
        'dateformat' : ['jquery']
    }
});

requirejs(["jquery", "bootstrap"]);
