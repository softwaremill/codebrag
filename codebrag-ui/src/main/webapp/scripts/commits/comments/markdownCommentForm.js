angular.module('codebrag.commits.comments')

    .directive('markdownCommentForm', function($q, events, authService) {
        var mdConverter = Markdown.getSanitizingConverter();
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
                        scope.preview = mdConverter.makeHtml(scope.content || 'Nothing to preview');
                    } else {
                        setTimeout(function() {
                            element.find('textarea').focus()
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
        }
    })

    .directive('focusAndTabLoop', function() {

        var TAB_KEY = 9;

        return {
            restrict: 'A',
            link: function(scope, el, attrs) {

                var focusableElements = ['textarea', '[data-action-send]', '[data-action-preview]', '[data-action-cancel]'];
                focusableElements.next = function() {
                    var returnVal;
                    this.current = this.current || 0;
                    returnVal = this[this.current];
                    if(this.current == this.length - 1) {
                        this.current = 0;
                    } else {
                        this.current++;
                    }
                    return returnVal;
                };

                function findFirstEnabled() {
                    var found = el.find(focusableElements.next());
                    if(found.length && !found.is(':disabled')) {
                        return found;
                    }
                    return findFirstEnabled();
                }

                el.find(findFirstEnabled()).focus();
                el.on('keydown', function(event) {
                    if (event.which == TAB_KEY) {
                        event.preventDefault();
                        var nextEl = findFirstEnabled();
                        nextEl.focus();
                    }
                });
            }
        }
    });