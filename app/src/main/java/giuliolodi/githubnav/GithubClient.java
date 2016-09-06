package giuliolodi.githubnav;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface GithubClient {

    @GET("/repos/{owner}/{repo}/readme")
    Call<Readme> readme(
            @Path("owner") String owner,
            @Path("repo") String repo
    );

    class Readme {
        String name;
        String encoding;
        int size;
        String content;
    }

}
