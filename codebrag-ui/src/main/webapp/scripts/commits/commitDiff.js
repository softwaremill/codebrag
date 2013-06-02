angular.module('codebrag.commits')

    .directive('commitDiff', function($compile) {
        return {
            restrict: 'E',
            replace: true,
            compile: function(tEl, tAttrs, transclude) {
                var diffTemplate = Handlebars.compile(tEl.html());
                tEl.html('');
                return function(scope, el, attrs) {
                    var dataAttrName = attrs.data;
                    var removeWatcher = scope.$watch(dataAttrName, function(newVal) {
                        var dataRoot = {};
                        if(angular.isUndefined(newVal)) {
                            return;
                        }
                        dataRoot[dataAttrName] = scope.$eval(dataAttrName);
                        el.html(diffTemplate(dataRoot));
                        $compile(el.find(attrs.compile))(scope);
                        removeWatcher();
                    });
                }
            }
        }
    })

    .directive('inlineCommentable', function($compile, events) {

        var inlineCommentFormTemplate = $('#inlineCommentForm').html(); //$templateCache.get('inlineCommentForm');

        var fileDiffRootSelector = 'table';
        var fileDiffLineSelector = 'tbody';
        var clickableSelector = '[data-commentable]';
        var inlineCommentFormRootSelector = 'tr.comment-form';

        var fileNameDataAttr = 'file-name';
        var lineNumberDataAttr = 'line-number';

        function InlineCommentForm(rowClicked, fileDiffRoot) {

            var fileDiffLine = rowClicked.parents(fileDiffLineSelector);

            this.insert = function(afterFormInsertCallback) {
                fileDiffLine.append(inlineCommentFormTemplate);
                var insertedElement = fileDiffLine.find(inlineCommentFormRootSelector);
                afterFormInsertCallback(insertedElement);
            };

            this.destroy = function(afterFormDestroyCallback) {
                fileDiffLine.find(inlineCommentFormRootSelector).remove();
                afterFormDestroyCallback();
            };

            this.commentParams = function() {
                return {
                    fileName: fileDiffRoot.data(fileNameDataAttr),
                    lineNumber: fileDiffLine.data(lineNumberDataAttr)
                }
            };

            this.isAlreadyPresent = function() {
                return fileDiffLine.find(inlineCommentFormRootSelector).length > 0;
            }

        }

        function linkFn(scope, el, attrs) {
            var fileDiffRoot = el.parent(fileDiffRootSelector);
            fileDiffRoot.on('click', clickableSelector, function(event) {
                var commentFormScope = scope.$new();
                var elementClicked = $(event.currentTarget);
                var commentForm = new InlineCommentForm(elementClicked, fileDiffRoot);
                if(commentForm.isAlreadyPresent()) {
                    return;
                }
                commentForm.insert(function bindScope(createdElement) {
                    commentFormScope.commentParams = commentForm.commentParams();
                    $compile(createdElement)(commentFormScope);
                    commentFormScope.$apply();
                });
                commentFormScope.$on(events.closeForm, function() {
                    commentForm.destroy(function destroyScope() {
                        commentFormScope.$destroy();
                    });
                })
            });
        }

        return {
            restrict: 'A',
            link: linkFn
        }
    })


    .directive('liveComments', function($compile) {

        var commentTemplate = $('#inlineComment').html();    // templateCache ???

        var fileDiffRootSelector = 'table';
        var lineCommentsListSelector = '.inline-comments-container';

        var lineNumberDataAttr = 'line-number';

        function FileInlineComments(baseElement, fileInlineComments) {

            this.insert = function(afterDOMInsertCallback) {
                _.forEach(fileInlineComments, function(lineComments, lineNumber) {
                    var diffLine = _getCorrespondingLineDOMElement(lineNumber);
                    if(_lineHasNoCommentsYet(diffLine)) {
                        diffLine.append(commentTemplate);
                        afterDOMInsertCallback(diffLine, lineComments);
                    }
                });
            };

            function _lineHasNoCommentsYet(line) {
                return line.find(lineCommentsListSelector).length === 0;
            }

            function _getCorrespondingLineDOMElement(lineNumber) {
                var lineNumberSelector = ['[data-', lineNumberDataAttr, '="', lineNumber + '"]'].join(""); // create [data-line-number="xx"] selector
                return baseElement.find(lineNumberSelector);
            }

        }

        function linkFn(scope, el, attrs) {

            var fileDiffRoot = el.parent(fileDiffRootSelector);

            scope.$watch(attrs.liveComments, function(fileComments) {
                if(angular.isUndefined(fileComments)) {
                    return;
                }
                var comments = new FileInlineComments(fileDiffRoot, fileComments);
                comments.insert(function(line, lineComments) {
                    var newScope = scope.$new();
                    newScope.lineComments = lineComments;
                    $compile(line)(newScope);
                });
            }, true);
        }

        return {
            restrict: 'A',
            link: linkFn
        }

    });





