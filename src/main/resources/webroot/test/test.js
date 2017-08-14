$(document).ready(function () {
    var eventBus = new vertx.EventBus('/eventbus/');

    var $lock = $('#lock');
    var $output = $('#output');
    var $eventBusAddress = $('#eventBusAddress');
    var eventBusAddressValue = $eventBusAddress.val();
    var $eventBusAddressAppend = $('#eventBusAddressAppend');
    var $json = $('#json');
    var $sendJson = $('#sendJson');
    var $registerHandler = $('#registerHandler');

    eventBus.onopen = function () {
        $lock.addClass('fa-unlock');
        $lock.removeClass('fa-lock');
    };
    eventBus.onclose = function () {
        $lock.addClass('fa-lock');
        $lock.removeClass('fa-unlock');
    };

    $eventBusAddress.change(function () {
        switch ($(this).val()) {
            case 'chat.add.contact':
                $json.val('{ "contact" : "<name>" }');
                $eventBusAddressAppend.prop("disabled", true);
                setSendJsonAsPrimaryButton();
                break;
            case 'chat.receive.contact':
                $json.val('<name>');
                $eventBusAddressAppend.prop("disabled", false);
                setRegisterHandlerAsPrimaryButton();
                break;
            case 'chat.get.contactList':
                $json.val('null');
                $eventBusAddressAppend.prop("disabled", true);
                setNonePrimaryButton();
                break;
            case 'chat.send.message':
                $json.val('{ "message" : "<message>", "receiver" : "<name>" }');
                $eventBusAddressAppend.prop("disabled", true);
                setSendJsonAsPrimaryButton();
                break;
            case 'chat.receive.message':
                $json.val('{ "date" : <2011-12-03T10:15:30+01:00>, "sender" : "<name>", "receiver" : "<name>", "message" : "<messsage>", "read" : <true/false> }');
                $eventBusAddressAppend.prop("disabled", false);
                setRegisterHandlerAsPrimaryButton();
                break;
            case 'chat.send.read.notification':
                $json.val('{ "sender" : "<name>" }');
                $eventBusAddressAppend.prop("disabled", true);
                setSendJsonAsPrimaryButton();
                break;
            case 'chat.receive.read.notification':
                $json.val('{ "receiver" : "<name>", "count" : 1 }');
                $eventBusAddressAppend.prop("disabled", false);
                setRegisterHandlerAsPrimaryButton();
                break;
            case 'chat.get.conversation':
                $json.val('{ "contact" : "<name>", "onlyUnread": false }');
                $eventBusAddressAppend.prop("disabled", true);
                setSendJsonAsPrimaryButton();
                break;
            default:
                $json.val('');
        }
    });

    $sendJson.click(function () {
        if ($eventBusAddressAppend.prop("disabled") === false && $eventBusAddressAppend.val().trim().length > 0) {
            eventBusAddressValue += "." + $eventBusAddressAppend.val();
        }

        var json = $.parseJSON($('#json').val());
        print("Send to '" + eventBusAddressValue + "':\n" + JSON.stringify(json, null, 2));
        eventBus.send(eventBusAddressValue, json, replyHandler);
    });

    $registerHandler.click(function () {
        if ($eventBusAddressAppend.prop("disabled") === false && $eventBusAddressAppend.val().trim().length > 0) {
            eventBusAddressValue += "." + $eventBusAddressAppend.val();
        }

        print("Register to '" + eventBusAddressValue + "'.");
        eventBus.registerHandler(eventBusAddressValue, function (message) {
            print("Received from '" + eventBusAddressValue + "':\n" + JSON.stringify(message, null, 2));
        });
    });

    var replyHandler = function (reply, reply_err) {
        print("Replied:\n" + JSON.stringify(reply, null, 2));
        print("Replied:\n" + JSON.stringify(reply_err, null, 2));
    };

    var print = function (message) {
        if ($output.val().length === 0) {
            $output.val(message + "\n");
        } else {
            $output.val($output.val() + "---\n" + message + "\n");
        }
    };

    var setSendJsonAsPrimaryButton = function () {
        $sendJson.addClass('btn-primary');
        $sendJson.removeClass('btn-default');
        $registerHandler.addClass('btn-default');
        $registerHandler.removeClass('btn-primary');
    };

    var setRegisterHandlerAsPrimaryButton = function () {
        $sendJson.addClass('btn-default');
        $sendJson.removeClass('btn-primary');
        $registerHandler.addClass('btn-primary');
        $registerHandler.removeClass('btn-default');
    };

    var setNonePrimaryButton = function () {
        $sendJson.addClass('btn-default');
        $sendJson.removeClass('btn-primary');
        $registerHandler.addClass('btn-default');
        $registerHandler.removeClass('btn-primary');
    };
});
