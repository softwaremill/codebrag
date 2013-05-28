In terminal go to folder, where Grunt file is located and 
```
	npm install
```
npm will run, if there is node.js installed on your machine.
To install node.js go to [nodejs.org](http://nodejs.org/).

Grunt has 3 commands:
```
	grunt watch
```
Watch changes in .styl files and compile into index.css. Run this command, when you start working with .styl file, to see the changes in browser immediately.

```
	grunt build
```
Same as grunt watch, but compile index.css file once. Run this command, if you need to update index.css just once.

```
	grunt connect
```
Run server. Go to http://localhost:8080/#/commits?nobackend to work with frontend.
Hint: run 'grunt watch' in the other terminal window to see all your updates immediately without compiling index.css by yourself. 
