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
            link: function (scope, element) {

                scope.noLikes = function () {
                    return _.isEmpty(scope.likesCollection)
                };

                var currentUserName = authService.loggedInUser.fullName;

                var setUserAlreadyLikedStyle = function (isAlreadyLiked) {
                    if (isAlreadyLiked) $('div.like-commit', element).addClass("liked-by-user");
                };

                var removeAuthorWatch = scope.$watch(function () {
                    return scope.author
                }, function (newVal) {
                    if (angular.isUndefined(newVal)) {
                        return;
                    }
                    setUserAlreadyLikedStyle(scope.author == currentUserName);
                    removeAuthorWatch();
                });

                var removeCollectionReferenceWatch = scope.$watch(function () {
                    return scope.likesCollection
                }, function (newVal) {
                    if (angular.isUndefined(newVal)) {
                        return;
                    }
                    scope.$watch(function () {
                            return scope.likesCollection.length;
                        }, function () {
                            if (_.some(scope.likesCollection, function (like) {
                                return like.authorName == currentUserName
                            })) {
                                setUserAlreadyLikedStyle(true);
                            }
                        }
                    );
                    removeCollectionReferenceWatch();
                });
            }
        }

    }
)
;
