// Load the main app module to start the app
requirejs(['common'], function () {
    requirejs(
        ['jquery', 'knockout', 'vertxbus', 'conversation/conversationViewModel', 'contact/contactViewModel', 'alert/alertViewModel', 'customBindings'],

        function () {
            var $ = requirejs('jquery'),
                ko = requirejs('knockout'),
                ConversationViewModel = requirejs('conversation/conversationViewModel'),
                ContactViewModel = requirejs('contact/contactViewModel'),
                AlertViewModel = requirejs('alert/alertViewModel');

            var postbox = new ko.subscribable();

            $(document).ready(function () {
                var eb = new vertx.EventBus("/eventbus/");


                eb.onopen = function () {
                    console.log('eventbus open');
                    ko.applyBindings(new ContactViewModel(eb, postbox), $('#contactList').get(0));
                    ko.applyBindings(new ConversationViewModel(eb, postbox), $('#conversation').get(0));
                    ko.applyBindings(new AlertViewModel(postbox), $('#alert').get(0));
                };
                eb.onclose = function () {
                    console.log("eventbus close");
                    window.location.href = "/logout";
                };

                $('logout').click(function (e) {
                    eb.close();
                });


            });
        });
});
