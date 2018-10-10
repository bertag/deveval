package net.bertag.deveval

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.isSuccessful
import com.github.kittinunf.result.failure
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.util.*

/**
 * Simple client wrapper for the Github API which supports the operations needed for this project.
 */
class GithubOrgClient constructor(
        private val user: String,
        private val password: String,
        private val org: String) {

    private val baseUrl = "https://api.github.com"
    private val jsonParser = JsonParser()

    /**
     * Retrieves the contents of the given license from Github's license API.
     *
     * @param licenseName the name of the license template
     * @return the license contents
     */
    fun licenseContent(licenseName: String): String {
        val (_, _, response) = Fuel.get("$baseUrl/licenses/$licenseName")
                .responseString()

        val json = jsonParser.parse(response.get()).asJsonObject
        return json.getAsJsonPrimitive("body").asString
    }

    /**
     * Retrieves the list of repository names owned by the organization.
     *
     * @return the list of repository names
     */
    fun listRepos(): List<String> {
        val (_, _, result) = Fuel.get("$baseUrl/orgs/$org/repos")
                .authenticate(user, password)
                .responseString()

        val json = jsonParser.parse(result.get()).asJsonArray
        return json.map { repo -> repo.asJsonObject.getAsJsonPrimitive("name").asString }
    }

    /**
     * Retrieves whether or not the given repository has an attached license.
     *
     * @param repo the name of the repository
     * @return true if the repo has a license, false otherwise
     */
    fun hasLicense(repo: String): Boolean {
        val (_, response, _) = Fuel.get("$baseUrl/repos/$org/$repo/license")
                .authenticate(user, password)
                .response()
        return response.isSuccessful
    }

    /**
     * Retrieves the SHA for the head of the given branch.
     *
     * @param repo the name of the repository
     * @param branch the name of the branch
     * @return the SHA representing the head of the branch
     */
    private fun retrieveHead(repo: String, branch: String): String {
        val (_, _, result) = Fuel.get("$baseUrl/repos/$org/$repo/branches/$branch")
                .authenticate(user, password)
                .responseString()

        val json = jsonParser.parse(result.get()).asJsonObject
        return json.getAsJsonObject("commit").getAsJsonPrimitive("sha").asString
    }

    /**
     * Creates a new branch in the repository.
     *
     * @param repo the name of the repository
     * @param baseBranch the name of the branch from which to base
     * @param newBranch the name of the new branch being created
     */
    fun createBranch(repo: String, baseBranch: String, newBranch: String) {
        val body = JsonObject()
        body.addProperty("ref", "refs/heads/$newBranch")
        body.addProperty("sha", retrieveHead(repo, baseBranch))

        Fuel.post("$baseUrl/repos/$org/$repo/git/refs")
                .authenticate(user, password)
                .header("Content-type" to "application/json")
                .body(body.toString())
                .responseString()
    }

    /**
     * Creates a file in the given repository and branch.
     *
     * @param repo the name of the repository where the file should be created
     * @param branch the branch of the repository where the file should be created (this can be a new branch)
     * @param path the path to where the file should be created relative to the repository root
     * @param content the contents of the file
     * @param message the git commit message to include when creating the file
     * @return the result of the creation attempt
     */
    fun createFile(repo: String, branch: String, path: String, content: String, message: String) {
        val body = JsonObject()
        body.addProperty("message", message)
        body.addProperty("content", Base64.getEncoder().encodeToString(content.toByteArray()))
        body.addProperty("branch", branch)

        Fuel.put("$baseUrl/repos/$org/$repo/contents/$path")
                .authenticate(user, password)
                .header("Content-type" to "application/json")
                .body(body.toString())
                .response()
    }

    /**
     * Creates a pull request to merge the head into the base.
     *
     * @param repo the name of the repository
     * @param head the source branch for the pull request
     * @param base the destination branch for the pull request
     * @param title the title of the pull request
     */
    fun createPullRequest(repo: String, head: String, base: String, title: String) {
        val body = JsonObject()
        body.addProperty("title", title)
        body.addProperty("head", head)
        body.addProperty("base", base)

        Fuel.post("$baseUrl/repos/$org/$repo/pulls")
                .authenticate(user, password)
                .header("Content-type" to "application/json")
                .body(body.toString())
                .response()
    }

    /**
     * Creates a new repository owned by the organization.
     *
     * The resulting repository will contain a brief README file and optionally a license file.  This method is intended
     * to allow for quick scaffolding and testing.
     *
     * @param repo the name of the repository
     * @param licenseName the license type to include, or null if no license should be included
     */
    fun createRepo(repo: String, licenseName: String? = null) {
        val body = JsonObject()
        body.addProperty("name", repo)
        body.addProperty("auto_init", true)
        if (licenseName != null) {
            body.addProperty("license_template", licenseName)
        }

        Fuel.post("$baseUrl/orgs/$org/repos")
                .authenticate(user, password)
                .header("Content-type" to "application/json")
                .body(body.toString())
                .response()
    }

    /**
     * Deletes the given repository.
     *
     * This method is intended to allow for quick scaffolding and testing.  Use with care...
     *
     * @param repo the name of the repository
     */
    fun deleteRepo(repo: String) {
        Fuel.delete("$baseUrl/repos/$org/$repo")
                .authenticate(user, password)
                .response()
    }

}
