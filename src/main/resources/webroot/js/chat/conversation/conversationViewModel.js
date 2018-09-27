define(['jquery', 'knockout', 'conversation/messageModel', 'alert/alertModel', 'emoji'],
    function ($, ko, Message, Alert) {
        'use strict';

        return function (eventbus, postbox) {
            var self = this;

            self.eventBus = eventbus;

            self.addressGetConversation = "chat.get.conversation";
            self.addressSendReadNotification = "chat.send.read.notification";
            self.addressReceiveReadNotification = "chat.receive.read.notification." + $("#username").text();
            self.addressSendMessage = "chat.send.message";
            self.addressReceiveMessage = "chat.receive.message." + $("#username").text();

            self.isLoading = ko.observable(false);

            self.conversation = ko.observableArray(); // TODO: Rename messages

            var timer = setInterval(function () {
                self.conversation().forEach(function (message) {
                    message.updatePrettyDate();
                });
            }, 60 * 1000);

            self.scrolledItem = ko.observable();
            self.force = ko.observable(false); // TODO: Remove?

            self.selectedContact = ko.observable();
            postbox.subscribe(function (newValue) {
                self.selectedContact(newValue.name());
                self.force(true);
                var conversationAlreadyLoaded = self.conversation().some(function (element) {
                    return (element.sender() === self.selectedContact()) || (element.receiver() === self.selectedContact());
                });
                var messageJson = {"contact": newValue.name(), "onlyUnread": conversationAlreadyLoaded};
                self.eventBus.send(self.addressGetConversation, messageJson, self.getConversationReplyHandler);
            }, this, "messageToPublish");

            self.getConversationReplyHandler = function (reply, reply_err) {
                if (reply.status === "success") {
                    self.addConversation(reply.conversation);
                    postbox.notifySubscribers(self.selectedContact(), "resetUnreadMessageCount");
                } else {
                    self.selectedContact = ko.observable();
                    postbox.notifySubscribers(new Alert(reply.status, reply.statusMessage), "alertToPublish");
                }
                self.force(false);
            };

            self.addConversation = function (conversation) {
                var message = null;
                conversation.forEach(function (entry) {
                    message = new Message(entry.sender, entry.receiver, entry.message, entry.read, entry.date);
                    self.conversation.push(message);
                });
                if (message === null) {
                    message = self.conversation().reduce(function (previousValue, currentValue) {
                        return (currentValue.sender() === self.selectedContact()) || (currentValue.receiver() === self.selectedContact()) ? currentValue : previousValue;
                    });
                }
                self.scrolledItem(message); // TODO: Remove?
            };

            self.newMessage = ko.observable();
            self.sendMessage = function () {
                self.isLoading(true);
                var messageJson = {"message": self.newMessage(), "receiver": self.selectedContact()};
                self.eventBus.send(self.addressSendMessage, messageJson, self.sendMessageReplyHandler);
            };

            self.sendMessageReplyHandler = function (reply, reply_err) {
                if (reply.status === "success") {
                    var message = new Message(reply.sender, self.selectedContact(), reply.message, reply.read, reply.date);
                    self.conversation.push(message);
                    self.scrolledItem(message); // TODO: Remove?
                    self.newMessage('');
                } else {
                    postbox.notifySubscribers(new Alert(reply.status, reply.statusMessage), "alertToPublish");
                }
                self.isLoading(false);
            };

            self.handleReceivedMessage = function (messageJson) {
                var sender = messageJson.sender;
                if (self.selectedContact() !== sender) {
                    postbox.notifySubscribers(sender, "incrementUnreadMessageCount");
                } else {
                    var message = new Message(messageJson.sender, self.selectedContact(), messageJson.message, messageJson.read, messageJson.date);
                    self.conversation.push(message);
                    self.scrolledItem(message); // TODO: Remove?
                    self.eventBus.send(self.addressSendReadNotification, {"sender": sender});
                }
            };

            self.handleReceivedReadNotification = function (messageJson) {
                var receiver = messageJson.receiver;
                var count = messageJson.count === -1 ? self.conversation().length : messageJson.count;  // by count -1, set all as read
                for (var i = self.conversation().length - 1; i >= self.conversation().length - count; i--) {
                    if (self.conversation()[i].receiver() === receiver) {
                        self.conversation()[i].read(true);
                    }
                }
            };

            self.eventBus.registerHandler(self.addressReceiveMessage, self.handleReceivedMessage);
            self.eventBus.registerHandler(self.addressReceiveReadNotification, self.handleReceivedReadNotification);

            self.myPostProcessingLogic = function (elements) {
                emojione.ascii = true;
                $(elements).children('p').each(function () {
                    var input = $(this).text();
                    console.log('input: ' + input);
                    var output = emojione.toImage(input);
                    console.log('output: ' + output);
                    $(this).html(output); // FIXME: Not secure!
                });
            }
        };
    }
);
