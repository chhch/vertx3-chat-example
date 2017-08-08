define(['jquery', 'knockout', 'alert/alertModel'],
  function($, ko, Alert) {
    'use strict';

    var ViewModel = function(postbox) {
      var self = this;

      self.alerts = ko.observableArray();

      postbox.subscribe(function(newAlert) {
        self.alerts.push(newAlert);
      }, this, "alertToPublish");

    };

    return ViewModel;
  }
);
