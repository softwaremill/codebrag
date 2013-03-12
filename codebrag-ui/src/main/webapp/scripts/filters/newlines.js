"use strict";

angular.module('smlCodebrag.filters').filter('newlines', function () {
    return function (text) {
        return text.replace(/\n/g, '<br/>');
    };
});
