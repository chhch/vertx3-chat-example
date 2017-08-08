define(['jquery', 'knockout'],

  function($, ko) {
    'use strict';

    var Model = function(name, unreadMessageCount) {
      this.name = ko.observable(name);
      this.unreadMessageCount = ko.observable(unreadMessageCount);
    };

    return Model;
  }
);
