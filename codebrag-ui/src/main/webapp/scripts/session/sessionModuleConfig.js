angular.module("codebrag.session")

    .config(function ($routeProvider) {
        $routeProvider.
            when("/", {controller: 'SessionCtrl', templateUrl: "views/main.html"}).
            when("/login", {controller: 'SessionCtrl', templateUrl: "views/login.html"}).
            when("/profile", {controller: "ProfileCtrl", templateUrl: "views/secured/profile.html"});
    });
