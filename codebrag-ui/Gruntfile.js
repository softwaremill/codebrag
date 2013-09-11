'use strict';

module.exports = function (grunt) {

    var proxyRequests = require('grunt-connect-proxy/lib/utils').proxyRequest;
    var liveReload = require('connect-livereload')({port: 9988});

    grunt.initConfig({

        watch: {
            styles: {
                files: ['app/styles/**/*.{styl,css}'],
                tasks: ['stylus:compile']
            },
            templates: {
                files: ['app/views/**/*.html'],
                tasks: ['html2js']
            },
            livereload: {
                options: {
                    livereload: {
                        port: 9988
                    }
                },
                files: [
                    'app/scripts/**/*.js',
                    'tmp/styles/**/*.css',
                    'tmp/scripts/**/*.js',
                    'app/*.html'
                ]
            }
        },

        connect: {
            proxies: [{context: '/rest/', host: 'localhost', port: 8080}],
            options: {
                port: 9090,
                hostname: 'localhost'
            },

            livereload: {
                options: {
                    open: true,
                    middleware: function (connect) {
                        return [
                            proxyRequests,
                            liveReload,
                            connect.static('tmp'),
                            connect.static('app'),
                        ];
                    }
                }
            },
            dist: {
                options: {
                    open: true,
                    middleware: function (connect) {
                        return [
                            proxyRequests,
                            connect.static('dist/webapp')
                        ];
                    },
                    keepalive: true
                }
            }

        },

        stylus: {
            compile: {
                options: {
                    paths: ['app/styles'],
                    urlfunc: 'url',
                    'include css': true,
                    compress: false
                },
                files: {
                    'tmp/styles/index.css': 'app/styles/index.styl'
                }
            }
        },

        html2js: {
            app: {
                options: {
                    base: 'app'
                },
                src: ['app/views/**/*.html'],
                dest: 'tmp/scripts/templates.js',
                module: 'codebrag.templates'
            }
        },


        karma: {
            autotest: {
                configFile: 'karma-config.js',
                reporters: ['dots'],
                singleRun: false,
                autoWatch: true
            },
            test: {
                configFile: 'karma-config.js',
                reporters: ['progress'],
                singleRun: true
            }
        },

        useminPrepare: {
            html: 'app/index.html',
            options: {
                dest: 'dist/webapp'
            }
        },

        uglify: {
            options: {
                compress: false,
                mangle: false
            }
        },

        usemin: {
            html: ['dist/webapp/index.html'],
            options: {
                dirs: ['dist/webapp']
            }
        },

        copy: {
            index: {
                expand: true,
                src: 'app/*.html',
                dest: 'dist/webapp',
                flatten: true
            },
            assets: {
                expand: true,
                cwd: 'app/assets',
                src: ['images/**/*', 'fonts/**/*'],
                dest: 'dist/webapp/assets'
            }
        },

        clean: {
            dist: ['dist', 'tmp'],
            tmp: 'tmp'
        }

    });

    require('matchdep').filterDev('grunt-*').forEach(function (dep) {
        grunt.loadNpmTasks(dep);
    });


    grunt.registerTask('server', function(target) {
        if(target === 'dist') {
            return grunt.task.run(['build', 'configureProxies', 'connect:dist']);
        }

        grunt.task.run([
            'clean:tmp',
            'stylus:compile',
            'html2js',
            'configureProxies',
            'connect:livereload',
            'watch'
        ]);
    });

    grunt.registerTask('build', [
        'clean:dist',
        'test',
        'copy:assets',
        'copy:index',
        'useminPrepare',
        'concat',
        'usemin'
    ]);

    grunt.registerTask('test', [
        'clean:tmp',
        'stylus:compile',
        'html2js',
        'karma:test'
    ]);

    grunt.registerTask('autotest', [
        'clean:tmp',
        'stylus:compile',
        'html2js',
        'karma:autotest'
    ]);

};