define(['jquery', 'knockout', 'contact/contactModel', 'alert/alertModel'],
    function ($, ko, Contact, Alert) {
        'use strict';

        return function (eventbus, postbox) {
            var self = this;

            self.eventBus = eventbus;

            self.addressReceiveContact = "chat.receive.contact." + $("#username").text();
            self.addressAddContact = "chat.add.contact";
            self.addressGetContactMap = "chat.get.contactList";

            self.isLoading = ko.observable(false);

            self.eventBus.registerHandler(self.addressReceiveContact, function (newContact) {
                self.contacts.push(new Contact(newContact, 0));
            });

            self.eventBus.send(self.addressGetContactMap, null, function (message) {
                var contacts = message.contactList;
                for (var i = 0; i < contacts.length; i++) {
                    self.contacts.push(new Contact(contacts[i].name, contacts[i].unreadMessages));
                }
            });

            self.contacts = ko.observableArray();

            self.newContactName = ko.observable();
            self.addContact = function () {
                if (self.newContactName()) {
                    self.isLoading(true);
                    var message = {"contact": self.newContactName()};
                    self.eventBus.send(self.addressAddContact, message, self.replyHandler);
                }
            };

            self.replyHandler = function (reply, reply_err) {
                if (reply.status === "success") {
                    self.contacts.push(new Contact(self.newContactName(), 0));
                    self.newContactName('');
                } else {
                    postbox.notifySubscribers(new Alert(reply.status, reply.statusMessage), "alertToPublish");
                }
                self.isLoading(false);
            };

            self.selected = ko.observable();
            self.selected.subscribe(function (newValue) {
                postbox.notifySubscribers(newValue, "messageToPublish");
            });

            self.select = function (contact) {
                self.selected(contact);
            };

            postbox.subscribe(function (contactName) {
                for (var i = 0; i < self.contacts().length; i++) {
                    if (self.contacts()[i].name() === contactName) {
                        var inkrementedMessageCount = self.contacts()[i].unreadMessageCount() + 1;
                        self.contacts()[i].unreadMessageCount(inkrementedMessageCount);
                        break;
                    }
                }
            }, this, "incrementUnreadMessageCount");

            postbox.subscribe(function (contactName) {
                for (var i = 0; i < self.contacts().length; i++) {
                    if (self.contacts()[i].name() === contactName) {
                        self.contacts()[i].unreadMessageCount(0);
                        break;
                    }
                }
            }, this, "resetUnreadMessageCount");
        };
    }
);
