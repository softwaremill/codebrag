# Resetting data

In order to reset demo data to some initial state, simply run

```
    ./reset.sh
```


# Codebrag demo data

Demo users
---

Demo users are defined in `demo_users.json` file and can be imported to Mongo using the following command.

```
mongoimport --db codebrag --collection users --file codebrag_demo_users.json
```

Password for all users is `codebrag`.
You can change their usernames, logins and avatars but don't touch passwords and salts - they are encrypted and kept in sync.
