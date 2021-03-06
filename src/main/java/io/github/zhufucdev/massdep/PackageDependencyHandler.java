package io.github.zhufucdev.massdep;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

public class PackageDependencyHandler extends Closure<Void> {
    private final Project project;

    public PackageDependencyHandler(Project project) {
        super(project);
        this.project = project;
    }

    @Override
    public Void call(Object... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Only one argument for dependency descriptor is allowed.");
        }
        Descriptor descriptor = Descriptor.parse((String) args[0], project);

        RepositoryHandler repositories = project.getRepositories();
        boolean existing =
                repositories.stream().anyMatch((r) ->
                        (r instanceof MavenArtifactRepository)
                                && ((MavenArtifactRepository) r).getUrl() == CredentialRepository.getUri(descriptor, project));
        if (!existing) {
            // add corresponding repository
            repositories.maven(new CredentialRepository(repositories, project, descriptor));
        }
        String configuration = descriptor.getConfiguration().isEmpty()
                ? Plugin.depExtensionMap.get(project).getDefaultConfiguration().getOrElse("implementation")
                : descriptor.getConfiguration();
        project.getDependencies().add(configuration , descriptor.getGradleNotation());
        return null;
    }
}
