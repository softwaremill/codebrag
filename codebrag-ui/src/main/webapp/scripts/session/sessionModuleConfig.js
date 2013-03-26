angular.module("codebrag.session")

    .config(function ($routeProvider) {
        $routeProvider.
            when("/", {controller: 'SessionCtrl', templateUrl: "views/main.html"}).
            when("/login", {controller: 'SessionCtrl', templateUrl: "views/login.html"}).
            when("/profile", {controller: "ProfileCtrl", templateUrl: "views/secured/profile.html"});
    })

    .config(function ($httpProvider) {
        var interceptor = ['$q', '$location', 'flashService', '$injector', function ($q, $location, flashService, $injector) {

            function success(response) {
                return response;
            }

            function error(response) {
                if (response.status === 401) { // user is not logged in
                    var authService = $injector.get("authService")
                    if (authService.isAuthenticated()) {
                        authService.logout(); // Http session expired / logged out - logout on Angular layer
                        flashService.set('Your session timed out. Please login again.');
                        $location.path("/login");
                    }
                } else if (response.status === 403) {
                    console.log(response.data);
                    // do nothing, user is trying to modify data without privileges
                } else if (response.status === 404) {
                    $location.path("/error404");
                } else if (response.status === 500) {
                    $location.path("/error500");
                } else {
                    $location.path("/error");
                }
                return $q.reject(response);
            }

            return function (promise) {
                return promise.then(success, error);
            };

        }];

        $httpProvider.responseInterceptors.push(interceptor);

    });