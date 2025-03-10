package io.jenkins.plugins.jobcacher.oras;

import hudson.FilePath;
import hudson.model.Job;
import java.io.IOException;
import jenkins.plugins.itemstorage.ObjectPath;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;

public class RegistryItemPath extends ObjectPath {

    private final RegistryClient registry;
    private final String fullName;
    private final String path;

    public RegistryItemPath(final RegistryClient registry, final String fullName, final String path) {
        this.registry = registry;
        this.fullName = fullName;
        this.path = path;
    }

    public RegistryClient getRegistry() {
        return registry;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPath() {
        return path;
    }

    @Override
    public RegistryItemPath child(String childPath) throws IOException, InterruptedException {
        return new RegistryItemPath(registry, fullName, String.format("%s/%s", path, childPath));
    }

    @Override
    public void copyTo(FilePath target) throws IOException, InterruptedException {
        // Not implemented
    }

    @Override
    public void copyFrom(FilePath source) throws IOException, InterruptedException {
        // Not implemented
    }

    @Override
    public boolean exists() throws IOException, InterruptedException {
        // Not implemented
        return true;
    }

    @Override
    public void deleteRecursive() throws IOException, InterruptedException {
        // Not implemented
    }

    @Override
    public HttpResponse browse(StaplerRequest2 request, StaplerResponse2 response, Job<?, ?> job, String name)
            throws IOException {
        return null;
    }
}
