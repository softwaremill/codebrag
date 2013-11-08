#Codebrag Installation Guide


## Prerequisites

* Java 1.7 installed. [Download here](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) or install package for your server's OS.
* MongoDB version 2.4.x installed. [Download here](http://www.mongodb.org/downloads) or install package for your server's OS.

*** NOTE: *** If you want to use **Codebrag** with **SVN** you need to install **Git**. [Download here](http://git-scm.com/) or install package for your server's OS

## Configuring Codebrag 

Codebrag has one configuration file `codebrag.conf`provided with distribution. Edit the following settings accordingly.

### "mongo" section

Set `servers` and `database` properties to point your MongoDB installation.

### "repository" section

Codebrag can work with VCS repositories in three modes. You need to set one using `type` property:

* `git-https` - git via https (with user/password provided)
* `git-ssh` - git via ssh (with keys)
* `svn` - experimental

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

##### Git repository via SSH

To use Codebrag with git repository with authentication via ssh (keys) please configure repository section as follows (leave passphrase empty if there is no passphrase for your keystore):

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

<br><br>

### "email" section

Setup your email server/account so that Codebrag can send emails with invitations and other notifications to users.

    email {
        smtp-host = "your.smtp.server.com"
        smtp-port = "465"
        smtp-username = "user@mydomain.com"
        smtp-password = "myPassword"
        from = "user@mydomain.com"
        encoding = "UTF-8"
    }
    
Please make sure java has all required security certificates in case of using SSL/TLS connection.

### "codebrag" section

##### application-url

`application-url` property should indicate URL that users would access to use Codebrag. It is used to create registration link.

	application-url = "http://codebrag.mydomain.com:8080"
		
##### local-git-storage-path

Codebrag needs to store repository data somewhere on your server. Edit `local-git-storage-path` accordingly. Codebrag will create directory called `repos` under provided location. Remember to set access rights accordingly so that Codebrag can read and write to this location.

##### invitation-expiry-time

`invitation-expiry-time` property indicates how long invitation links are valid. After that time no new registration can be issued with this link. This value defaults to 24 hours.

    //this setting uses Scala's Duration syntax, i.e. 15 minutes, 24 hours, 2 days
	invitation-expiry-time= "24 hours"

<br><br>

##### user-email-notifications

    user-email-notifications {
        enabled = true
        check-interval = "15 minutes"
        user-offline-after = "5 minutes"
        daily-digest-hour = 9
    }

###### enabled

Indicates whether email notifications should be enabled. If notifications are enabled each user can switch them off in his profile in application

###### check-interval

Defines how often the system will check for notifications to send

###### user-offline-after

Defines how long after closing tab with Codebrag user will be considered offline.

###### daily-digest-hour

Defines when (full hour of the day) daily digest emails will be sent

### "web-server" section

By default Codebrag starts on port 8080. If you want to change that, edit `port` property accordingly.

## Running Codebrag

*** NOTE: *** If you already have previous Codebrag installation you need to run database migration scripts before running new version. Go to `mongo_migration` directory in distribution package and follow the instructions from `README` file there.

Assuming your MongoDB is working and java is installed Codebrag can be run with 

On **Unix/OS X**

	#for default config file codebrag.conf
	./run.sh 
	#Or
	./run.sh custom.conf

<br>

On **Windows**

	run.bat
	
Logs will be written to `codebrag.log`. To stop Codebrag on **Unix/OS X** run script

	#for default config file codebrag.conf
	./stop.sh
	#Or
	./stop.sh custom.conf

## First time login

First user that accesses Codebrag after installation will be asked to register an account. Either email address or name provided have to match corresponding fields in `git log` in order to match commits with given user. Codebrag uses [Gravatar](http://gravatar.com) to display user avatars in application.

Other users can be invited to join Codebrag by choosing "Invite friends" link from menu in upper left corner.

## Questions? Help? Contact us

Do drop us a line at
[**ask@codebrag.com**](mailto:ask@codebrag.com) if you find an issue or just want to share a thought or ask a question. If you're looking for Frequently Asked Questions please visit the website!

You may also want to join [Codebrag Users group](https://groups.google.com/forum/#!forum/codebrag-users).

We hope you and your team will enjoy performing code review with Codebrag!
