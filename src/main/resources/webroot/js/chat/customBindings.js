define(['knockout'], function (ko) {
    'use strict';

    // a custom binding to handle the enter key (could go in a separate library)
    ko.bindingHandlers.enterKey = {
        init: function (element, valueAccessor, allBindingsAccessor, data, bindingContext) {
            var ENTER_KEY = 13;
            var wrappedHandler;
            var newValueAccessor;

            // wrap the handler with a check for the enter key
            wrappedHandler = function (data, event) {
                if (event.keyCode === ENTER_KEY) {
                    valueAccessor().call(this, data, event);
                }
            };

            // create a valueAccessor with the options that we would want to pass to the event binding
            newValueAccessor = function () {
                return {
                    keyup: wrappedHandler
                };
            };

            // call the real event binding's init function
            ko.bindingHandlers.event.init(element, newValueAccessor, allBindingsAccessor, data, bindingContext);
        }
    };

    // https://groups.google.com/forum/#!topic/knockoutjs/-2w-KGH2uOI
    ko.bindingHandlers.loading = {
        init: function () {
        },
        update: function (element, valueAccessor) {
            var $button, isLoading;

            $button = $(element);
            isLoading = ko.unwrap(valueAccessor());

            if (isLoading) {
                $button.button('loading');
            } else {
                $button.button('reset');
            }
        }
    };

    // http://stackoverflow.com/questions/20740212/knockoutjs-scrollintoviewtrigger
    ko.bindingHandlers.scrollTo = {
        update: function (element, valueAccessor, allBindings, viewModel, bindingContext) {
            var _value = valueAccessor();
            var _valueUnwrapped = ko.unwrap(_value);
            if (_valueUnwrapped) {
                var parent = $(element).parent()[0];
                var actualPosition = parent.scrollHeight - parent.clientHeight - $(element).outerHeight(true);
                if ((parent.scrollTop === actualPosition) || bindingContext.$root.force())
                    element.scrollIntoView();
            }
        }
    };

});
