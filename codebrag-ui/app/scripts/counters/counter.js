angular.module('codebrag.counters').factory('Counter', function() {

    var Counter = function(initialCount) {

        var currentCount = initialCount || 0,
            incomingCount = initialCount || 0;

        this.setIncomingTo = function(count) {
            incomingCount = count;
        };

        this.replace = function() {
            currentCount = incomingCount;
        };

        this.decrease = function() {
            (incomingCount > 0) &&(incomingCount -= 1);
            this.replace();
        };

        this.currentCount = function() {
            return currentCount;
        };

        this.updateAvailable = function() {
            return currentCount !== incomingCount;
        };

        this.reInitialize = function(initialCount) {
            currentCount = initialCount;
            incomingCount = initialCount;
        };

    };

    return Counter;

});