# CHANGELOG

## v2.3.1 (11.12.2014)
- Fix: support for gitflow-like branches (branches having`/` in their names)

## v2.3.2 (07.08.2015)
- Added functionality allowing to define hooks on different internal events (#10)
- Scentry authentication switched from `cookieStore` to regular server-side session. It will invalidate existing sessions.
- "Remember me" option modified to allow using it on many devices
- H2 Database configuration modified to allow connecting with console while the application is running
- Other minor bugfixes (#19, #46)

## v2.3.3 (15.03.2016)
 - Support for `teams` was added
 - It is possible to mark all commits as reviewed at once
 - Rest Subversion repo before rebasing (#69)
 - Other small things (#48)
