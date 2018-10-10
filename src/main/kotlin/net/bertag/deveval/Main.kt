package net.bertag.deveval

import kotlin.system.exitProcess

/*
- Assume the Apache license (since this is what appears to be used most commonly)
- Assume that we are only interested in the master branch.
- Assume user is a member of the organization with the appropriate permissions to create branches, create commits, and create pull requests.
- Assume that repository has at least 1 commit in the "master" branch.

- Using the Java library provided by Github wrapping its API: https://github.com/eclipse/egit-github/tree/master/org.eclipse.egit.github.core
 */

const val DEFAULT_LICENSE_NAME = "apache-2.0"
const val MASTER_BRANCH = "master"
const val FEATURE_BRANCH = "add-missing-license"
const val LICENSE_PATH = "LICENSE"

/**
 * Runs the program.
 */
fun main(args: Array<String>) {
    if (args.size < 3) {
        System.err.println("At least 3 arguments are expected (username, password, organization).")
        System.err.println(
                "Usage: java -jar deveval.jar USERNAME PASSWORD ORGANIZATION [LICENSE_TYPE] [POPULATE_TEST_REPOS]")
        exitProcess(1)
    }

    val username = args[0]
    val password = args[1]
    val organization = args[2]
    val licenseName = if (args.size > 3) args[3] else DEFAULT_LICENSE_NAME
    val populateOrg = if (args.size > 4) args[4].toBoolean() else false
    val client = GithubOrgClient(username, password, organization)

    println("Checking organization \"$organization\" as user \"$username\" for licenses.")
    println("Repositories with no license will have a pull request created to add a(n) \"$licenseName\" license.")

    val testRepos = arrayOf(
            Pair("repo1", null),
            Pair("repo2", licenseName),
            Pair("repo3", null)
    )

    // Uncomment the following 7 lines to cleanup from a previous test.  Ideally, this would be available as a
    // command-line option.
    /*
    testRepos.forEach{repo ->
        val (repoName, _) = repo
        print("Deleting repository \"$repoName\"...")
        client.deleteRepo(repoName)
        println("done.")
    }
    exitProcess(2)
    */


    // Populate the organization with some repositories if requested by the user. Repositories `repo1` and `repo3` will
    // not have a license, but `repo2` will.
    if (populateOrg) {
        testRepos.forEach{repo ->
            val (repoName, license) = repo
            print("Creating new repository \"$repoName\" with $license license...")
            client.createRepo(repoName, license)
            println("done.")
        }
    }

    val license = client.licenseContent(licenseName)
    val message = "Added $licenseName license file."
    val repos = client.listRepos()
    repos.forEach{repo ->
        if (!client.hasLicense(repo)) {
            print("Creating pull request for \"$repo\"...")
            client.createBranch(repo, MASTER_BRANCH, FEATURE_BRANCH)
            client.createFile(repo, FEATURE_BRANCH, LICENSE_PATH, license, message)
            client.createPullRequest(repo, FEATURE_BRANCH, MASTER_BRANCH, message)
            println("done.")
        }
    }
}