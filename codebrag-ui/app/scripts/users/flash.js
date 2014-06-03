angular.module('codebrag.userMgmt')

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
            clear: function() {
                this.messages.length = 0;
            },
            get: function(type) {
                return this.messages.filter(function(msg) {
                    return msg.type === type;
                });
            }
        };

        return Flash;
    });