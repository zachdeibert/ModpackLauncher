package com.gitlab.zachdeibert.modpacklauncher.install;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import com.gitlab.zachdeibert.modpacklauncher.StreamUtils;

public class Maven {
    private final List<String> repos;
    
    public void addRepo(final String repo) {
        repos.add(repo);
    }
    
    public void addRepos(final Collection<String> repos) {
        this.repos.addAll(repos);
    }
    
    public void addRepos(final String... repos) {
        addRepos(Arrays.asList(repos));
    }
    
    public InputStream download(final String artifact) throws FileNotFoundException {
        final String parts[] = artifact.split(":");
        final String fileName = String.format("%s/%s/%s/%2$s-%3$s%s.jar", parts[0].replace('.', '/'), parts[1], parts[2], parts.length < 4 ? "" : "-".concat(parts[3]));
        for ( final String repo : repos ) {
            final String url = String.format("%s%s%s", repo, repo.endsWith("/") ? "" : "/", fileName);
            try {
                return StreamUtils.download(url);
            } catch ( final IOException e ) {}
        }
        throw new FileNotFoundException(artifact);
    }
    
    public Maven() {
        repos = new ArrayList<String>();
    }
}
