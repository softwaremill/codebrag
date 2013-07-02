In terminal go to folder, where Grunt file is located and 
```
	npm install
```
npm will run, if there is node.js installed on your machine.
To install node.js go to [nodejs.org](http://nodejs.org/).

Next, in order to install grunt-cli, use following command (verified on OS X):
```
	sudo npm install -g grunt-cli
```

Grunt has 3 commands:
```
	grunt watch
```
Watch changes in .styl file and compile into index.css. Run this command, when you start working with .styl file, to see the changes in browser immediately.

```
	grunt build
```
Same as grunt watch, but compile index.css file just once. Run this command, if you need to update index.css just once.

```
	grunt connect
```
Run server. Go to [http://localhost:9090/#/commits?nobackend](http://localhost:9090/#/commits?nobackend) to work with frontend.

The ***?nobackend*** param in the URL is important - don't miss it! You should see screen with some stubbed commits.

Hint: run 'grunt watch' in the other terminal window to see all your updates immediately without compiling index.css by yourself.


If you need to apply any css changes, apply it to codebrag/codebrag-ui-grunt/styl/index.styl file.
Try to keep formatting, as it may cause problems, while compiling.
After applied changes run 'grunt build' to compile index.css.

All changes applied directly to index.css will be lost, after any next change in index.styl.
