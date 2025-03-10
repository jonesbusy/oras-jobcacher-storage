package io.jenkins.plugins.jobcacher.oras;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import jenkins.plugins.itemstorage.GlobalItemStorage;
import jenkins.plugins.itemstorage.ItemStorage;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

public class DataMigrationTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    @LocalData
    public void shouldMigrateData() {
        ItemStorage<?> storage = GlobalItemStorage.get().getStorage();
        assertThat(storage, is(notNullValue()));
        RegistryItemStorage registryItemStorage = (RegistryItemStorage) storage;
        assertThat(registryItemStorage.getNamespace(), is("library"));
        assertThat(registryItemStorage.getRegistryUrl(), is("localhost:5000"));
        assertThat(registryItemStorage.getStorageCredentialId(), is("registry"));
    }
}
