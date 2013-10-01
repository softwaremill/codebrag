#Codebrag Installation Guide


## Prerequisites

* Java 1.7 installed. [Download here](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) or install package for your server's OS.
* MongoDB version 2.4.x installed. [Download here](http://www.mongodb.org/downloads) or install package for your server's OS.

## Configuring Codebrag 

Codebrag has one configuration file `codebrag.conf`provided with distribution. Edit the following settings accordingly.

### "mongo" section

Set `servers` and `database` properties to point your MongoDB installation.

### "repository" section

Codebrag can work with VCS repositories in three modes. You need to set one using `type` property:

* `git-https` - git via https (with user/password provided)
* `git-ssh` - git via ssh (with keys)
* `svn` - experimental

##### Git repository via SSH

To use Codebrag with git repository with authentication via ssh (keys) please configure repository section as follows. Please remember to have ssh keys properly configured (leave passphrase empty if there is no passphrase for your keystore).

*** NOTE: *** Test you repository ssh connection and add host to `known-hosts` otherwise Codebrag will not be able to authenticate. You may also want to create ssh configuration in `~/.ssh/config` file.

	repository {
	    type = "git-ssh"
	    git-ssh {
	        name = "..."			// e.g. codebrag"
	        uri ="..." 				// e.g.	git@github.com:sml/codebrag.git
	        branch ="..."  			// e.g. "refs/heads/master"
	        passphrase ="..." 		// or "" if no passphrase required
	    }
	}

##### Git repository via HTTPS

To use Codebrag with git via https please configure repository section as follows (leave username and password empty for public repositories):

	repository {
	    type = "git-https"
	    git-https {
	        name = "..."		
	        uri = "..."				// e.g. https://github.com/sml/codebrag.git
	        branch = "..." 	
	        username = "..."
	        password = "..."
	    }
	}

##### SVN repository (experimental)

You can also use Codebrag with SVN repositories although *** this is an experimental feature *** still in development. To use SVN, please change your repository configuration section to this:

	repository {
	    type = "svn"
	    svn {
	        name = "..." 
	        uri = "..."				// e.g. http://codebrag.com/svn
	        username = "..."
	        password = "..."
	    }
	}

### "email" section

Setup your email server/account so that Codebrag can send emails with invitations and other notifications to users.

    email {
        smtp-host = "smtp.gmail.com"
        smtp-port = "465"
        smtp-username = "user@mydomain.com"
        smtp-password = "myPassword"
        from = "user@mydomain.com"
        encoding = "UTF-8"
    }

### "codebrag" section

##### application-url

`application-url` property should indicate URL that users would access to use Codebrag. It is used to create registration link.

	application-url = "http://codebrag.mydomain.com:8080"
		
##### local-git-storage-path

Codebrag needs to store repository data somewhere on your server. Edit `local-git-storage-path` accordingly. Codebrag will create directory called `repos` under provided location. Remember to set access rights accordingly so that Codebrag can read and write to this location.

##### invitation-expiry-time

`invitation-expiry-time` property indicates how long invitation links are valid. Afte that time no new registration can be issues with this link. This value defaults to 24 hours.

	// number followed by "M" (minutes) "H" (hours) or "D" (days)
	invitation-expiry-time= "24H"   

### "web-server" section

By default Codebrag starts on port 8080. If you want to change that, edit `port` property accordingly.

## Running Codebrag

Assuming your MongoDB is working and java is installed Codebrag can be run with 

	./run.sh
	
Logs will be written to `codebrag.log`

## Logging into codebrag

First user that accesses Codebrag after installation will be asked to register an account. Either email address or name provided have to match corresponding fields in `git log` in order to match commits with given user. Codebrag uses [Gravatar](http://gravatar.com) to display user avatars in application.

Other users can be invited to join Codebrag by choosing "Invite friends" link from menu in upper left corner.

## Contact us if you need assistance

We are really eager to hear your feedback about Codebrag beta. Do drop us a line at
**ask@codebrag.com** if you find an issue or just want to share a thought or ask a question.
