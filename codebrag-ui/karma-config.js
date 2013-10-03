'use strict';

module.exports = function(config) {

    var files = [];

    [
        'jquery-1.8.2-min.js',
        'angular.js',
        'angular-resource.js',
        'angular-mocks.js',
        'angular-ui-states.js',
        'lodash.js',
        'markdown/marked.js',
        'angular-ui/validate.js',
        'angular-ui/keypress.js'
    ].forEach(function(file) {
        files.push('app/vendor/' + file);
    });

    files.push('app/scripts/*.js');
    files.push('app/scripts/**/*.js');
    files.push('tmp/scripts/**/*.js');
    files.push('test/**/*.js');

    config.set({
        basePath: '',
        frameworks: ['jasmine'],
        files: files,
        exclude: [],
        port: 7070,
        logLevel: config.LOG_INFO,
        autoWatch: false,

        // Start these browsers, currently available:
        // - Chrome
        // - ChromeCanary
        // - Firefox
        // - Opera
        // - Safari (only Mac)
        // - PhantomJS
        // - IE (only Windows)
        browsers: ['PhantomJS']
    });
};
