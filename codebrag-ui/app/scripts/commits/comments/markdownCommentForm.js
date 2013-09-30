angular.module('codebrag.commits.comments')

    .directive('markdownCommentForm', function($q, events, authService) {
        var additionalArgsName = 'extraArgs';
        var closeableName = 'closeable';
        return {
            restrict: 'E',
            replace: true,
            scope: true,
            templateUrl: 'views/commentForm.html',
            link: function(scope, element, attrs) {

                scope.togglePreviewMode = function() {
                    scope.previewModeOn = !scope.previewModeOn;
                    if(scope.previewModeOn) {
                        scope.preview = marked(scope.content || 'Nothing to preview');
                    } else {
                        setTimeout(function() {
                            element.find('textarea').focus();
                        }, 0);
                    }
                };
                scope.username = authService.loggedInUser.fullName;

                scope.avatarUrl = authService.loggedInUser.avatarUrl;

                scope._submitComment = function(content) {
                    var submitArguments = [content];
                    if(_.isString(attrs[additionalArgsName])) {
                        _.forEach(attrs[additionalArgsName].split(","), function(el) {
                            submitArguments.push(scope[el]);
                        });
                    }
                    $q.when(scope[attrs.action].apply(this, submitArguments)).then(function() {
                        scope.content = '';
                        scope.previewModeOn = false;
                        scope.closeForm();
                    });
                };

                scope.isCloseable = function() {
                    return !!attrs[closeableName];
                };

                scope.closeForm = function() {
                    if(!!attrs[closeableName]) {
                        scope.$emit(events.closeForm);
                    }
                };

            }
        };
    })

    .directive('focusAndTabLoop', function() {

        var TAB_KEY = 9;

        function focusableElementsCollection() {
            var focusableElements = ['textarea', '[data-action-send]', '[data-action-preview]', '[data-action-cancel]'];
            var index = -1;
            return {
                next: function() {
                    index = (index + 1) % focusableElements.length;
                    return focusableElements[index];
                }
            };
        }

        function findFirstEnabled(baseEl, array) {
            var found = baseEl.find(array.next());
            if(found.length && !found.is(':disabled')) {
                return found;
            }
            return findFirstEnabled(baseEl, array);
        }

        return {
            restrict: 'A',
            link: function(scope, el) {
                var focusables = focusableElementsCollection();
                el.find(findFirstEnabled(el, focusables)).focus();
                el.on('keydown', function(event) {
                    if (event.which === TAB_KEY) {
                        event.preventDefault();
                        var nextEl = findFirstEnabled(el, focusables);
                        nextEl.focus();
                    }
                });
            }
        };
    });