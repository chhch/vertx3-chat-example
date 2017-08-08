define(['jquery', 'knockout', 'dateformat'],

    function ($, ko) {
        'use strict';

        var Model = function (sender, receiver, message, read, date) {
            this.sender = ko.observable(sender);             //escapeHtml(sender)
            this.receiver = ko.observable(receiver);
            this.message = ko.observable(message);
            this.read = ko.observable(read);
            this.date = new Date(date.$date);
            this.datePretty = ko.observable($.format.prettyDate(this.date));

            this.updatePrettyDate = function () {
                this.datePretty($.format.prettyDate(this.date));
            };
        };


        return Model;
    }
);
