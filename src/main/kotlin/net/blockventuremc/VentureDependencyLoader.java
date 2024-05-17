package net.blockventuremc;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage") // We keep an eye on that.
public class VentureDependencyLoader implements PluginLoader {

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver maven = new MavenLibraryResolver();
        JavaDotEnv dotenv = new JavaDotEnv();

        new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("venture.dependencies"))))
                .lines()
                .forEach(dependency -> maven.addDependency(new Dependency(new DefaultArtifact(dependency), null)));

        maven.addRepository(new RemoteRepository.Builder("flawcra", "default", "https://nexus.flawcra.cc/repository/maven-mirrors/").build());
        maven.addRepository(new RemoteRepository.Builder("private", "default", "https://maven.pkg.github.com/BlockVentureMC/AudioServer")
                .setAuthentication(
                        new AuthenticationBuilder()
                                .addUsername(dotenv.get("PACKAGE_USER"))
                                .addPassword(dotenv.get("PACKAGE_TOKEN"))
                                .build()
                )
                .build()
        );

        classpathBuilder.addLibrary(maven);
    }

}
