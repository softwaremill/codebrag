angular.module('codebrag.common.services')

/**
 * Allows creating local "flash messages"-like objects.
 * Instead of throwing bare "error", "success" etc strings into scope,
 * create new Flash() and call $scope.flash.add('error', 'Your msg');
 */
    .factory('Flash', function() {
        var Flash = function() {
            this.messages = [];
        };

        Flash.prototype = {
            add: function(type, message) {
                var msg = { type: type, message: message };
                this.messages.push(msg);
            },
            // takes either map {field: ['err1', 'err2']} or array ['err1', 'err2']
            addAll: function(type, messagesMap) {
                var messages = (Array.isArray(messagesMap)) ? messagesMap : Flash.flattenFieldMessagesMap(messagesMap);
                var add = _.bind(this.add, this, type); // bound to 'this' and partially applied
                messages.forEach(add);
            },
            clear: function() {
                this.messages.length = 0;
            },
            get: function(type) {
                return this.messages.filter(function(msg) {
                    return msg.type === type;
                });
            }
        };

        Flash.flattenFieldMessagesMap = function(fieldMessagesMap) {
            var nestedErrorsList = Object.keys(fieldMessagesMap).map(function(key) {
                return fieldMessagesMap[key];
            });
            return _.flatten(nestedErrorsList);
        };

        return Flash;
    });