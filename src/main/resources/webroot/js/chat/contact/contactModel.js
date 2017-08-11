define(['jquery', 'knockout'],

  function($, ko) {
    'use strict';

    return function (name, unreadMessageCount) {
        this.name = ko.observable(name);
        this.unreadMessageCount = ko.observable(unreadMessageCount);
    };
  }
);
