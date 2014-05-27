angular.module('codebrag.auth').factory('User', function() {
    'use strict';

    var User = function (userData) {
        angular.extend(this, userData);
    };

    User.prototype = {
        loggedInAs: function (data) {
            angular.copy(data, this);
            this.authenticated = true;
        },
        isAdmin: function () {
            return this.admin === true;
        },
        isAuthenticated: function () {
            return this.authenticated === true;
        },
        isGuest: function () {
            return !this.isAuthenticated();
        }
    };

    User.guest = function () {
        return new User({authenticated: false});
    };

    return User;

});