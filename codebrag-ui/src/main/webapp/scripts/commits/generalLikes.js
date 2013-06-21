angular.module('codebrag.commits')

    .directive('generalLikes', function (authService) {

        return {
            restrict: 'E',
            scope: {
                author: '=',
                likesCollection: '=',
                submitLike: '&'
            },
            templateUrl: 'generalLikes',
            link: function (scope) {

                scope.noLikes = function () {
                    return _.isEmpty(scope.likesCollection)
                };
                var currentUserName = authService.loggedInUser.fullName;

                var removeAuthorWatch = scope.$watch(function() { return scope.author }, function(newVal) {
                    if (angular.isUndefined(newVal)) {
                        return;
                    }
                    scope.userAllowedToLike = scope.author !== currentUserName;
                    removeAuthorWatch();
                });

                scope.$watch(function () {
                        return scope.likesCollection
                    }, function (newVal) {
                        if (angular.isUndefined(newVal)) {
                            return;
                        }
                        if (scope.userAllowedToLike) {

                            if (_.some(scope.likesCollection, function (like) {
                                return like.authorName == currentUserName
                            })) {
                                scope.userAllowedToLike = false;
                            }
                        }
                    }
                )
                ;
            }
        }

    }
)
;
