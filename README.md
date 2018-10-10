# Developer Evaluation

This Kotlin program uses the Github API to examine the repositories in a GitHub organization and checks if they have a license.  If the license is missing, the program will then open a pull request to add a license.

## Installation and Usage

- Clone this repository by running `git clone https://github.com/bertag/deveval.git`.
- Compile the program by running `./gradlew build` from the project root directory. *The resulting compiled JAR file will be located at `build/libs/deveval-1.0.0.jar` (relative to the project root directory).*
- To execute the program on an already populated organization, run the following command: `java -jar build/libs/deveval-1.0.0.jar USERNAME PASSWORD ORGANIZATION [LICENSE_TYPE] [PREPOPULATE]`
  - `USERNAME`: The username of your (individual) Github account.
  - `PASSWORD`: The password of your (individual) Github account.
  - `ORGANIZATION`: The identifier of the organization to be checked.
  - `LICENSE_TYPE` (Optional): The type of license to be added as needed; the list of possible licenses is available at https://help.github.com/articles/licensing-a-repository/.  If not set, `apache-2.0` is used by default.
  - `PREPOPULATE` (Optional): If `false` (which is the default value), then this argument does nothing.  However, if `true`, 3 new repositories will be created in the organization: `repo1`, `repo2`, and `repo3`.  `repo1` and `repo3` have no license initially, but `repo2` does.  This argument is offered to make testing and demonstration of this program super easy.
  
### Example

```
git clone https://github.com/bertag/deveval.git
./gradlew build
java -jar build/libs/deveval-1.0.0.jar myuser myPa$$word myorganization lgpl true
```

## Notes and Assumptions

I chose to write this program in Kotlin (using Fuel and GSON to handle HTTP requests and JSON parsing respectively).  This language was listed as one of the preferred options, and it is a language that I have been wanting to learn for a while, so I saw this as a good opportunity to get my feet wet.  Overall my experience with Kotlin was fun and educational and I generally enjoyed working in this new language.  Naturally, however, there are still some aspects to the language that I am not yet comfortable with.  For example, my program features little to any input validation or error handling at the present time simply because I'm not quite sure how to implement it properly yet.  I also used this program as a chance to dabble in Gradle.  I am much more comfortable in Maven, but it seemed that Gradle is more preferred for Kotlin development; so like Kotlin itself, I decided to try it out!

One of the interesting tradeoffs involved in working in this new language and environment is that I didn't spend any time developing automated tests.  That is probably the next step if I were to continue with this project.  My hunch is that -- given the close relationship between Kotlin and Java -- that tests could be written using the venerable JUnit framework, which I am very familiar with.  In the meantime however, an inspection of my code will show that in addition to the functionality described earlier that allows you to prepopulate some test/demo repos in the organization, I also implemented the ability to delete those test repos.  This would be an essential operation in any automated test, but I did not expose it as a feature in the delivered runtime.

Another design choice which I think I would correct in a future build is my choice of authentication pattern.  The GitHub API supports a number of different options, including both BASIC and OAUTH2.  While I have used both in the past, my experience with OAUTH2 is limited, so I chose to implement this program using the simpler BASIC authentication pattern.  It works and it works fine...but the resulting requirement to provide a password at runtime left me a bit...squeamish.  With more time, I think it would be worthwhile to convert the program to OAUTH2, and now that I am feeling more comfortable with Kotlin and the Fuel HTTP library, I feel like I could do this much better now than at the start.

Other assumptions which I made when coding this evaluation include:

- Assuming that the organization already exists (since there is no public API to support this in the cloud version of Github) and that the user is a member with commit rights to all the repositories in the organization.
- Assuming that each repository has had at least 1 commit already.  The behavior of git gets a little wonky when there are no commits/heads, which would require some extra checking to prevent errors.  But given the nature of the assignment, it seems a safe assumption that we are interested in populated repositories.
- Assuming that we are only interested in checking the `master` branch for license files (and subsequently creating pull requests to merge directly to the `master`).  This assumption allowed me to use Github's built-in License API, dramatically simplifying the amount and complexity of web requests needed to complete the assignment.
- Assuming that the requests complete successfully.  As I mentioned earlier, I am still figuring out how Kotlin (and Fuel specifically) handle error responses, so in the present build, the program just quietly assumes that all is well in Zion.  This would obviously get corrected in future commits, since network IO failures must always be planned for and handled gracefully -- one of the interesting challenges of working with Microservices as Kelly Flanagan has mandated.
- Assuming that the `apache-2.0` license is a sane default.  I decided to go with this license as the default simply because an inspection of BYU-OIT's public GitHub repositories showed that it is the preferred license for your organization.
- Assuming that this program will be run only periodically as a command-line program.  It strikes me that this could be implemented as an observer that regularly checks (either via polling or as an event hook) repositories and corrects them on an ongoing basis.  In fact, I was a little surprised to see that OIT has already built such a bot, which was responsible for adding licenses to several of the repositories I found on your site!

## Conclusion

Thank you for the opportunity to prepare this demonstration of my technical ability and willingness to learn new technologies.  Please let me know if you have any questions related to my submission.  I look forward to hearing from you!
