$(document).ready(function() {
  var eventBus = new vertx.EventBus('/eventbus/');

  eventBus.onopen = function() {
    $('i.fa-lock').addClass('fa-unlock');
    $('i.fa-lock').removeClass('fa-lock');
  };
  eventBus.onclose = function() {
    $('i.fa-unlock').addClass('fa-lock');
    $('i.fa-unlock').removeClass('fa-unlock');
  };

  $('#eventBusAddress').change(function() {
      $('#eventBusAdressAppend').prop("disabled", true);
      setSendJsonAsPrimaryButton();

      switch($(this).val()) {
          case 'chat.add.contact':
                $('#json').val('{ "contact" : "<name>" }');
                break;
          case 'chat.receive.contact':
                $('#json').val('<name>');
                $('#eventBusAdressAppend').prop("disabled", false);
                setRegisterHandlerAsPrimaryButton();
                break;
          case 'chat.get.contactList':
                $('#json').val('null');
                setNonePrimaryButton();
                break;
          case 'chat.send.message':
                $('#json').val('{ "message" : "<message>", "receiver" : "<name>" }');
                break;
          case 'chat.receive.message':
                $('#json').val('{ "date" : <2011-12-03T10:15:30+01:00>, "sender" : "<name>", "receiver" : "<name>", "message" : "<messsage>", "read" : <true/false> }');
                $('#eventBusAdressAppend').prop("disabled", false);
                setRegisterHandlerAsPrimaryButton();
                break;
          case 'chat.send.read.notification':
                $('#json').val('{ "sender" : "<name>" }');
                break;
          case 'chat.receive.read.notification':
                $('#json').val('{ "receiver" : "<name>", "count" : 1 }');
                $('#eventBusAdressAppend').prop("disabled", false);
                setRegisterHandlerAsPrimaryButton();
                break;
          case 'chat.get.conversation':
                $('#json').val('{ "contact" : "<name>" }');
                break;
          default:
                $('#json').val('');
      }
  });

    $('#sendJson').click(function() {
      var eventBusAddresse = $('#eventBusAddress').val();
      if ($('#eventBusAdressAppend').prop("disabled") === false && $('#eventBusAdressAppend').val().trim().length > 0) {
        eventBusAddresse += "." + $("#eventBusAdressAppend").val();
    }

    var json = $.parseJSON($('#json').val());
    print("Send to '" + eventBusAddresse + "':\n" + JSON.stringify(json, null, 2));
    eventBus.send(eventBusAddresse, json, replyHandler);
    });

    $('#registerHandler').click(function() {
        var eventBusAddresse = $('#eventBusAddress').val();
        if ($('#eventBusAdressAppend').prop("disabled") === false && $('#eventBusAdressAppend').val().trim().length > 0) {
          eventBusAddresse += "." + $("#eventBusAdressAppend").val();
      }


      print("Register to '" + eventBusAddresse + "'.");
      eventBus.registerHandler(eventBusAddresse, function(message) {
        print("Received from '" + eventBusAddresse + "':\n" + JSON.stringify(message, null, 2));
      });
    });
});

var replyHandler = function(reply, reply_err) {
  print("Replied:\n" + JSON.stringify(reply, null, 2));
//  print("Replied:\n" + JSON.stringify(reply_err, null, 2));
};

var print = function(message) {
  if ($('#output').val().length === 0) {
    $('#output').val(message + "\n");
  } else {
    $('#output').val($('#output').val() + "---\n" + message + "\n");
  }
};

var setSendJsonAsPrimaryButton = function() {
  $('#sendJson').addClass('btn-primary');
  $('#sendJson').removeClass('btn-default');
  $('#registerHandler').addClass('btn-default');
  $('#registerHandler').removeClass('btn-primary');
};

var setRegisterHandlerAsPrimaryButton = function() {
  $('#sendJson').addClass('btn-default');
  $('#sendJson').removeClass('btn-primary');
  $('#registerHandler').addClass('btn-primary');
  $('#registerHandler').removeClass('btn-default');
};

var setNonePrimaryButton = function() {
  $('#sendJson').addClass('btn-default');
  $('#sendJson').removeClass('btn-primary');
  $('#registerHandler').addClass('btn-default');
  $('#registerHandler').removeClass('btn-primary');
};

