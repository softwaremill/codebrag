###Codebrag Early Adopters version - installation guide


### Prerequisites

* Java 1.7 installed. [Download here](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) or install package for your server's OS.
* MongoDB version 2.4.x installed. [Download here](http://www.mongodb.org/downloads) or install package for your server's OS.

### Configuring Codebrag 

Codebrag has one configuration file `codebrag.conf`provided with distribution. Edit the following settings accordingly.

##### mongo

Set `servers` and `database` properties to point your MongoDB installation.

##### repository

Codebrag can work with git repositories in three modes. You need to set one using `type` property):

* `git-https` - plain git via https (with user/password provided)
* `git-ssh` - plain git via ssh (with keys)
* `github` - using github (authentication via Github) - optional and described later

To use Codebrag with plain git via https (user/password) please configure repository section as follows (leave username and password empty for public repositories):

	repository {
	    
	    type = "git-https"

	    git-https {
	        name = "REPOSITORY_NAME"
	        uri = "REPOSITORY_URI"
	        username = "USER_NAME"
	        password = "PASSWORD"
	    }
	    
	    ...
	}

For example: 

	repository {
	    
	    type = "git-https"

	    github {
	        name = "codebrag"
	        uri = "https://github.com/softwaremill/codebrag.git"
	        username = "secretuser"
	        password = "secretpassword"
	    }
	    
	    ...
	}

If you prefer using Codebrag with plain git via ssh (keys) please configure repository section as follows. Also remember to have ssh keys properly configured (leave passphrase empty if there is no passphrase for your keystore). 

*** NOTE: *** Test you repository ssh connection and add host to `known-hosts` otherwise Codebrag will not be able to authenticate. 

	repository {
	    
	    type = "git-ssh"

	    git-ssh {
	        name = "REPOSITORY_NAME"
	        uri = "REPOSITORY_URI"
	        passphrase = "SSH_KEY_PASSPHRASE"
	    }
	    
	    ...
	}

For example: 

	repository {
	    
	    type = "git-ssh"

	    github {
	        name = "codebrag"
	        uri = "git@github.com:softwaremill/codebrag.git"
	        passphrase = "secretpassphrase"
	    }
	    
	    ...
	}
	
##### codebrag

Codebrag needs to store repository data somewhere on your server. Edit `local-git-storage-path` accordingly. Codebrag will create directory called `repos` under provided location. Remember to set access rights accordingly so that Codebrag can read and write to this location.

##### web-server

By default Codebrag starts on port 8080. If you want to change that, edit `port` property accordingly.



### Running Codebrag

Assuming your MongoDB is working and java is installed Codebrag can be run with 

	./run.sh
	
Logs will be written to `codebrag.log`

### Logging into codebrag

As our user management is very basic you can register your account in Codebragby going to `http://<YOUR_CODEBRAG>/#/register` and submitting the form. Remember that your email must match the one in git log so that Codebrag can recognize your commits. After doing that you can login to Codebrag at `http://<YOUR_CODEBRAG>`. If you are using Github integration described below you can use Github Sign In.

### Troubleshooting

If you encounter a problem with repository synchronization (e.g. message like "No value for key branch.master.merge found in configuration") please remove `repos` directory located at `local-git-storage-path` you configured and restart Codebrag.

In case of any questions feel free to contact us at `ask@codebrag.com`.
If you have issues with installing/using codebrag please contact Michał Ostruszka via email: `michal.ostruszka@softwaremill.com` or via Skype `michal.ostruszka`.  


### Github integration (optional)

If you want to use Codebrag integration with Github there are two sections of configuration file to edit:

#### github 
You need to create Github application for your Codebrag installation on `http://github.com`. As a callback please provide URL in form `http://<YOUR_CODEBRAG>/rest/github/auth_callback` (public IP is required for it to work). Edit `github` section of configuration file and fill `client-id` and `client-secret` with values from created application.


#### repository 
Please configure repository section as follows:

	repository {
	    
	    type = "github"

	    github {
	        owner = "REPOSITORY_OWNER_USER"
	        name = "REPOSITORY_NAME"
	        sync-user-login = "SYNC_USER_NAME"
	    }
	    
	    ...
	}

For example: 

	repository {
	    
	    type = "github"

	    github {
	        owner = "softwaremill"
	        name = "codebrag"
	        sync-user-login = "codebrag"
	    }
	    
	    ...
	}
	
The `sync-user-login` property should contain name of Github user that is allowed to access repository configured.

*** NOTE: ***You need to login to Codebrag as this user first so that Codebrag can collect tokens required to authenticate this user in order to synchronize with repository. After logging in as this user hit `http://<YOUR_CODEBRAG>/refresh` to reload user's credentials. 





