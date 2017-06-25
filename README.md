# GitNav

Right now I'm in the middle of a renovation. I'm basically remaking 
the whole app from scratch because of poor decisions made at 
the beginning of development.
If you want to see the old version (1.0) check the releases.
Interesting stuff I'm gonna use and implement:

 - OAuth
 - Dark mode/theme
 - MVP architecture ([basic structure here][mvp])
 - RxKotlin/RxAndroid
 - Kotlin
 - Dagger2

Basic GitHub Android Client. [Google play link!][play]

<div align="center">
	<img src="https://raw.githubusercontent.com/GLodi/GitNav/master/gfx/web_hi_res_512.png" width="128">
</div>

## Screenshots

![](https://raw.githubusercontent.com/GLodi/GitNav/master/gfx/gitnavgif.gif)


![](https://raw.githubusercontent.com/GLodi/GitNav/master/gfx/Screenshot1.png)


![](https://raw.githubusercontent.com/GLodi/GitNav/master/gfx/Screenshot2.png)

Currently implemented:

User:
 - View basic information
 - View repositories
 - View followers and following
 - Follow/unfollow user
 
Repos:
 - List own repos (with filters)
 - List starred repos (with filters)
 - Star/unstar repo
 - View basic information
 - View stargazers, contributors, issues
 - View files
 - View commits
 
Search Function:
 - Search repos, users and code

Gist:
 - List own gists and starred gists
 - View gist content
 
Trending:
 - View daily, weekly and monthly trending repos
 
 
## List of libraries implemented:

 - [Picasso][picasso]
 - [CircleImageView][circle]
 - [EasyFonts][easy]
 - [OkHttp][okhttp]
 - [Gson][gson]
 - [PrettyTime][pretty]
 - [Butterknife][butter]
 - [MagicIndicator][magic]
 - [MarkdownView-Android][markdown]
 - [RxAndroid][rxandroid]
 - [Egit-GitHub][egit]
 - [material-about-library][material]
 - [highlightjs-android][highlight]
 - [Toasty][toasty]
 - [GitHubContributionsView][gcv]

[picasso]: http://square.github.io/picasso/
[circle]: https://github.com/hdodenhof/CircleImageView
[easy]: https://github.com/vsvankhede/EasyFonts
[okhttp]: http://square.github.io/okhttp/
[gson]: https://github.com/google/gson
[pretty]: http://www.ocpsoft.org/prettytime/
[butter]: http://jakewharton.github.io/butterknife/
[magic]: https://github.com/hackware1993/MagicIndicator
[markdown]: https://github.com/mukeshsolanki/MarkdownView-Android
[rxandroid]: https://github.com/ReactiveX/RxAndroid
[egit]: https://github.com/eclipse/egit-github
[material]: https://github.com/daniel-stoneuk/material-about-library
[highlight]: https://github.com/PDDStudio/highlightjs-android
[toasty]: https://github.com/GrenderG/Toasty
[play]: https://play.google.com/store/apps/details?id=giuliolodi.gitnav
[mvp]: https://github.com/MindorksOpenSource/android-mvp-architecture
[gcv]: https://github.com/javierugarte/GithubContributionsView

