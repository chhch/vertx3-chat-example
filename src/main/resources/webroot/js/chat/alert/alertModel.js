define(['jquery', 'knockout'],

    function ($, ko) {
        'use strict';

        var Model = function (type, message) {
            this.type = ko.observable(type);
            this.message = ko.observable(message);
            this.cssClass = ko.pureComputed(function () {
                return "alert-" + this.type();
            }, this);
            this.cssIcon = ko.pureComputed(function () {
                var iconMap = {
                    'success': 'fa-smile-o',
                    'info': 'fa-info',
                    'warning': 'fa-exclamation',
                    'danger': 'fa-exclamation-triangle'
                };
                return iconMap[this.type()];
            }, this);
        };

        return Model;
    }
);
