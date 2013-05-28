module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),

    stylus: {
      compile: {
        options: {
          paths: ['styl'], // folder, where files to be imported are located
          urlfunc: 'url', // use embedurl('test.png') in our code to trigger Data URI embedding
          'include css': true,
          compress: false
        },
        files: {
          '../codebrag-ui/src/main/webapp/v2/index.css': 'styl/index.styl' // 1:1 compile
        }
      }
    },

    watch: {
      src: {
        files: ['styl/*.styl'],
        tasks: ['build']
      }
    },

    connect: {
      server: {
        options: {
          port: 8080,
          base: '../codebrag-ui/src/main/webapp/',
          keepalive: true
        }
      }
    }

  });

  grunt.loadNpmTasks('grunt-contrib-stylus');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-contrib-connect');

  grunt.registerTask('build', ['stylus:compile']);

};