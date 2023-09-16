# Contributing

We'd love for you to contribute and to make this project even better than it is today!
Here are the guidelines we'd like you to follow:

- [Question or Problem?](#question)
- [Issues and Bugs](#issue)
- [Feature Requests](#feature)
- [Docs Improvements](#docs)
- [Submission Guidelines](#submit)
- [Coding Rules](#rules)
- [Commit Message Guidelines](#commit)

## <a name="question"></a> Got a Question or Problem?

If you have questions about how to use our plugins, please direct these to the respective repository issues
or [StackOverflow][stackoverflow].

## <a name="issue"></a> Found an Issue?

If you find a bug in the source code or a mistake in the documentation, you can help us by
submitting an issue to our [GitHub Repository][github]. Even better you can submit a Pull Request
with a fix.

**Please see the Submission Guidelines below**.

## <a name="feature"></a> Want a Feature?

You can request a new feature by submitting an issue to our [GitHub Repository][github]. If you
would like to implement a new feature then consider what kind of change it is:

- **Major Changes** that you wish to contribute to the project should be discussed first
  so that we can better coordinate our efforts, prevent duplication of work, and help you to
  craft the change so that it is successfully accepted into the project.
- **Small Changes** can be crafted and submitted to the [GitHub Repository][github] as a Pull Request.

## <a name="docs"></a> Want a Doc Fix?

If you want to help improve the docs, it's a good idea to let others know what you're working on to
minimize duplication of effort. Before starting, check out the open issues.
Comment on an issue to let others know what you're working on, or create a new issue if your work
doesn't fit within the scope of any of the existing doc fix projects.

For large fixes, please build and test the documentation before submitting the Pull Request to be
sure you haven't accidentally introduced any layout or formatting issues. You should also make
sure that your commit message is labeled "docs:" and follows the **Git Commit Guidelines** outlined below.

## <a name="submit"></a> Submission Guidelines

### Submitting an Issue

Before you submit your issue search the archive, maybe your question was already answered.

If your issue appears to be a bug, and hasn't been reported, open a new issue.
Help us to maximize the effort we can spend fixing issues and adding new
features, by not reporting duplicate issues. Providing the following information will
increase the chances of your issue being dealt with quickly:

- **Overview of the Issue** - if an error is being thrown a non-minified stack trace helps
- **Motivation for or Use Case** - explain why this is a bug for you
- **Version(s)** - is it a regression?
- **Browsers and Operating System** - is this a problem with all browsers or only IE8?
- **Reproduce the Error** - provide a live example (using Plunker or
  JSFiddle) or an unambiguous set of steps.
- **Related Issues** - has a similar issue been reported before?
- **Suggest a Fix** - if you can't fix the bug yourself, perhaps you can point to what might be
  causing the problem (line of code or commit)

### Submitting a Pull Request

Before you submit your pull request consider the following guidelines:

- Search the repository for an open or closed Pull Request
  that relates to your submission. You don't want to duplicate effort.
- Make your changes in a new git branch:

  ```shell
  git checkout -b my-fix-branch main
  ```

- Create your patch, **including appropriate test cases**.
- Follow our [Coding Rules](#rules).
- Commit your changes using a descriptive commit message that follows our
  [commit message conventions](#commit-message-format). Adherence to
  the [commit message conventions](#commit-message-format)
  is required because release notes are automatically generated from these messages.

  ```shell
  git commit -a
  ```

  Note: the optional commit `-a` command line option will automatically "add" and "rm" edited files.

- Build your changes locally to ensure all the tests pass:

  ```shell
  gulp test
  ```

- Push your branch to GitHub:

  ```shell
  git push origin my-fix-branch
  ```

- In GitHub, send a pull request to the `main` branch.
- If we suggest changes then:
  - Make the required updates.
  - Commit your changes to your branch (e.g. `my-fix-branch`).
  - Push the changes to your GitHub repository (this will update your Pull Request).

If the PR gets too outdated we may ask you to rebase and force push to update the PR:

```shell
git rebase main -i
git push origin my-fix-branch -f
```

_WARNING. Squashing or reverting commits and forced push thereafter may remove GitHub comments
on code that were previously made by you and others in your commits._

#### After your pull request is merged

After your pull request is merged, you can safely delete your branch and pull the changes
from the main (upstream) repository:

- Delete the remote branch on GitHub either through the GitHub web UI or your local shell as follows:

  ```shell
  git push origin --delete my-fix-branch
  ```

- Check out the main branch:

  ```shell
  git checkout main -f
  ```

- Delete the local branch:

  ```shell
  git branch -D my-fix-branch
  ```

- Update your main with the latest upstream version:

  ```shell
  git pull --ff upstream main
  ```

## <a name="rules"></a> Coding Rules

To ensure consistency throughout the source code, keep these rules in mind as you are working:

- All features or bug fixes **must be tested** by one or more specs.
- All public API methods **must be documented**.
- Code should be check against our **lint rules**: `gulp lint`.

## <a name="commit"></a> Git Commit Guidelines

We have very precise rules over how our git commit messages can be formatted. This leads to **more
readable messages** that are easy to follow when looking through the **project history**. But also,
we use the git commit messages to **generate the change logs**.

The commit message formatting can be added using a typical git workflow
or through the use of a CLI wizard ([Commitizen](https://github.com/commitizen/cz-cli)).
To use the wizard, run `npm run commit` in your terminal after staging your changes in git.

### Commit Message Format

Each commit message consists of a **header** and a **body**. The header has a special
format that includes a **type**, a **scope**, and a **subject**:

```
<type>(<scope>): <subject>
<BLANK LINE>
<body>
<BLANK LINE>
---
<BLANK LINE>
<diff>
```

The **header** is mandatory and the **scope** of the header is optional.

The header line cannot be longer than 50 characters, any other line
cannot be longer than 72 characters! This allows
the message to be easier to read on GitHub as well as in various git tools.

### Revert

If the commit reverts a previous commit, it should begin with `revert:`,
followed by the header of the reverted commit. In the body it should
say: `This reverts commit <hash>.`, where the hash is the SHA of the commit being reverted.

### Type

Must be one of the following:

- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation only changes
- **style**: Changes that do not affect the meaning of the code (white-space, formatting, missing
  semi-colons, etc)
- **refactor**: A code change that neither fixes a bug nor adds a feature
- **perf**: A code change that improves performance
- **test**: Adding missing tests
- **chore**: Changes to the build process or auxiliary tools and libraries such as documentation
  generation

### Scope

The scope could be anything specifying place of the commit change.

### Subject

The subject contains succinct description of the change:

- use the imperative, present tense: "change" not "changed" nor "changes"
- don't capitalize first letter
- no dot (.) at the end

### Body

Just as in the **subject**, use the imperative, present tense: "change" not "changed" nor "changes".
The body should include the motivation for the change and contrast this with previous behavior.

**Breaking Changes** should start with the word `BREAKING CHANGE:` with a space or two newlines.
The rest of the commit message is then used for this.

### Diff

Add the diff of the commit from `git diff HEAD --stat=73`
A git diff from a sub directory is possible with the parameter `--relative`.
Filter can be used via `--diff-filter=[(A|C|D|M|R|T|U|X|B)…​[*]]`.

[github]: https://github.com/CycriLabs/keycloak-configurator
[stackoverflow]: http://stackoverflow.com/
