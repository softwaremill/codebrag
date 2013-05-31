angular.module('codebrag.commits')

    .directive('commitDiff', function($compile) {
        return {
            restrict: 'E',
            replace: true,
            compile: function(tEl, tAttrs, transclude) {
                var template = tEl.html();
                tEl.html('');
                return function(scope, el, attrs) {
                    var attr = attrs.data;
                    var compileSelector = attrs.compile;
                    var dataRoot = {};
                    var watcherRemove = scope.$watch(attr, function(newVal) {
                        if(angular.isUndefined(newVal)) {
                            return;
                        }
                        dataRoot[attr] = scope.$eval(attr);
                        var tpl = Handlebars.compile(template);
                        var output = tpl(dataRoot);
                        el.html(output);
                        $compile(el.find(compileSelector))(scope);
                        watcherRemove();
                    });
                }
            }
        }
    })

    .directive('inlineCommentable', function($compile, events) {

        var formTemplate = $('#inlineCommentForm').html(); //$templateCache.get('inlineCommentForm');

        function RowDOMElements(rowClicked) {

            var tbody = rowClicked.parents('tbody');
            var table = rowClicked.parents('table');

            this.insertCommentForm = function(template) {
                tbody.append(template);
            };

            this.removeCommentForm = function() {
                this.getCommentFormRowInserted().remove();
            };

            this.getCommentFormRowInserted = function() {
                return tbody.find('tr.comment-form')
            };

            this.getFileAndLine = function() {
                return {
                    fileName: table.data('file-name'),
                    lineNumber: tbody.data('line-number')
                }
            };

        }

        function insertFormWithNewScope(domElements, baseScope) {
            domElements.insertCommentForm(formTemplate);
            var formScope = baseScope.$new();
            formScope.commentData = domElements.getFileAndLine();
            $compile(domElements.getCommentFormRowInserted())(formScope);
            formScope.$apply();
            return formScope;
        }

        function linkFn(scope, el, attrs) {
            var table = el.parent('table');
            table.on('click', '[data-commentable="true"]', function(event) {
                var rowClicked = $(event.currentTarget);
                var domElements = new RowDOMElements(rowClicked);
                var formScope = insertFormWithNewScope(domElements, scope);
                formScope.$on(events.closeForm, function() {
                    domElements.removeCommentForm();
                    formScope.$destroy();
                })
            });
        }

        return {
            restrict: 'A',
            link: linkFn
        }
    });




