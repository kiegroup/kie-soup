package org.appformer.maven.integration;

import junit.framework.TestCase;
import org.appformer.maven.support.AFReleaseId;
import org.appformer.maven.support.AFReleaseIdImpl;
import org.junit.Assert;
import org.mockito.Mockito;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ArtifactResolverTest extends TestCase {

    class MyInJarArtifactResolver extends InJarArtifactResolver {

        MyInJarArtifactResolver(ClassLoader classLoader, AFReleaseId releaseId) {
            super(classLoader, releaseId);
        }

        @Override
        protected Optional<URL> tryInStructuredJar(AFReleaseId releaseId) {
            return super.tryInStructuredJar(releaseId);
        }
    }

    public void testInJarResolverSnapshotLocal() {
        ClassLoader classLoader = getClass().getClassLoader();
        AFReleaseId releaseId = new AFReleaseIdImpl("org.jbpm", "kjar", "1.0.0-SNAPSHOT", "jar");


        ArrayList<URL> urlArrayList = new ArrayList<URL>();
        urlArrayList.add(classLoader.getResource("BOOT-INF/classes/KIE-INF/lib/kjar-1.0.0-SNAPSHOT.pom"));

        InJarArtifactResolver inJarArtifactResolver = new InJarArtifactResolver(classLoader, releaseId, true);
        InJarArtifactResolver spy = Mockito.spy(inJarArtifactResolver);
        doReturn(urlArrayList).when(spy).buildResources(any());

        spy.init();

        verify(spy).getURL(endsWith("BOOT-INF/classes/KIE-INF/lib/kjar-1.0.0-SNAPSHOT.pom"));
    }

    public void testInJarResolverSnapshotExternal() {
        ClassLoader classLoader = getClass().getClassLoader();
        AFReleaseId releaseId = new AFReleaseIdImpl("org.jbpm", "kjar", "1.0.0-SNAPSHOT", "jar");

        InJarArtifactResolver inJarArtifactResolver = new InJarArtifactResolver(classLoader, releaseId, true);
        InJarArtifactResolver spy = Mockito.spy(inJarArtifactResolver);

        ArrayList<URL> urlArrayList = new ArrayList<URL>();
        urlArrayList.add(classLoader.getResource("BOOT-INF/classes/KIE-INF/lib/kjar-1.0.0-20240717-143315-1.pom"));
        doReturn(urlArrayList).when(spy).buildResources(any());

        spy.init();

        verify(spy).getURL(endsWith("BOOT-INF/classes/KIE-INF/lib/kjar-1.0.0-20240717-143315-1.pom"));
    }
}