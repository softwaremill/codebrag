#Codebrag Early Adopters version
## Installation Guide


### Prerequisites

* Java 1.7 installed. [Download here](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) or install package for your server's OS.
* MongoDB version 2.4.x installed. [Download here](http://www.mongodb.org/downloads) or install package for your server's OS.

### Configuring Codebrag 

Codebrag has one configuration file `codebrag.conf`provided with distribution. Edit the following settings accordingly.

#### mongo

Set `servers` and `database` properties to point your MongoDB installation.

#### repository

Codebrag can work with git repositories in three modes. You need to set one using `type` property:

* `git-https` - plain git via https (with user/password provided)
* `git-ssh` - plain git via ssh (with keys)
* `svn` - experimental

##### git-https

To use Codebrag with plain git via https (user/password) please configure repository section as follows (leave username and password empty for public repositories):

	repository {
	    
	    type = "git-https"

	    git-https {
	        name = "REPOSITORY_NAME"
	        uri = "REPOSITORY_URI"
	        branch = "BRANCH" (in format "refs/heads/NAME")
	        username = "USER_NAME"
	        password = "PASSWORD"
	    }
	    
	    ...
	}

For example: 

	repository {
	    
	    type = "git-https"

	    git-https {
	        name = "codebrag"
	        uri = "https://github.com/softwaremill/codebrag.git"
	        branch = "refs/heads/master"
	        username = "secretuser"
	        password = "secretpassword"
	    }
	    
	    ...
	}


##### git-ssh

If you prefer using Codebrag with plain git via ssh (keys) please configure repository section as follows. Also remember to have ssh keys properly configured (leave passphrase empty if there is no passphrase for your keystore). 

*** NOTE: *** Test you repository ssh connection and add host to `known-hosts` otherwise Codebrag will not be able to authenticate. 

	repository {
	    
	    type = "git-ssh"

	    git-ssh {
	        name = "REPOSITORY_NAME"
	        uri = "REPOSITORY_URI"
	        branch = "BRANCH" (in format "refs/heads/NAME")
	        passphrase = "SSH_KEY_PASSPHRASE"
	    }
	    
	    ...
	}

For example: 

	repository {
	    
	    type = "git-ssh"

	    git-ssh {
	        name = "codebrag"
	        uri = "git@github.com:softwaremill/codebrag.git"
	        branch = "refs/heads/master"
	        passphrase = "secretpassphrase"
	    }
	    
	    ...
	}

##### SVN (experimental)

You can also use Codebrag with SVN repositories although *** this is experimental feature *** still in development. To do that, please change your repository configuration section to this:

	repository {

	    type = "svn"

	    svn {
	        name = "REPOSITORY_NAME"
	        uri = "REPOSITORY_URI"
	        username = "USERNAME"
	        password = "PASSWORD"
	    }

	    ...
	}

    svn {
        name = "yourrepo"
        uri = "http://yourrepo.com/svn"
        username = "secretuser"
        password = "secretpassword"
    }

##### Email server

It's **important** to setup your email server/account. Otherwise Codebrag will not be able to send e.g. invitations.

**e.g.**

    email {
        smtp-host = "smtp.gmail.com"
        smtp-port = "465"
        smtp-username = "user@mydomain.com"
        smtp-password = "myPassword"
        from = "user@mydomain.com"
        encoding = "UTF-8"
    }

##### Application Url

In config please specify `codebrag.applicationUrl` property. It's used e.g. to generate registration link.

**e.g.**

    codebrag {
        (…)
        applicationUrl = "http://codebrag.mydomain.com:8080"
        (…)
    }

##### repository data

Codebrag needs to store repository data somewhere on your server. Edit `local-git-storage-path` accordingly. Codebrag will create directory called `repos` under provided location. Remember to set access rights accordingly so that Codebrag can read and write to this location.

##### web-server

By default Codebrag starts on port 8080. If you want to change that, edit `port` property accordingly.



### Running Codebrag

Assuming your MongoDB is working and java is installed Codebrag can be run with 

	./run.sh
	
Logs will be written to `codebrag.log`

### Logging into codebrag

As our user management is very basic you can register your account in Codebrag by going to `http://localhost:8080/#/register` and submitting the form. Remember that your email must match the one in git log so that Codebrag can recognize your commits. After doing that you can login to Codebrag at `http://localhost:8080`. If you are using Github integration described below you can use Github Sign In.

### Troubleshooting

If you encounter a problem with repository synchronization (e.g. message like "No value for key branch.master.merge found in configuration") please remove `repos` directory located at `local-git-storage-path` you configured and restart Codebrag.

In case of any questions feel free to contact us at `ask@codebrag.com`.
If you have issues with installing/using codebrag please contact Michał Ostruszka via email: `michal.ostruszka@softwaremill.com` or via Skype `michal.ostruszka`.  



