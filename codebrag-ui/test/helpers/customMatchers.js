jasmine.Matchers.prototype.toHaveClass = function(cssClass) {
    this.message = function() {
        return "Expected '" + angular.mock.dump(this.actual) + "' to have class '" + cssClass + "'.";
    };
    return this.actual.hasClass(cssClass);
};