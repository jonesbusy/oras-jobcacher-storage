package io.jenkins.plugins.jobcacher.oras;

import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.Serializable;
import land.oras.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryClient {

    public static final Logger LOGGER = LoggerFactory.getLogger(RegistryClient.class);

    private final RegistryConfig config;
    private final Registry registry;

    public RegistryClient(
            @NonNull String registryUrl, @NonNull String namespace, @NonNull UsernamePasswordCredentials credentials) {
        this.config = new RegistryConfig(registryUrl, namespace, credentials);
        this.registry = buildRegistry();
    }

    public RegistryClient(@NonNull RegistryConfig config) {
        this(config.getRegistryUrl(), config.getNamespace(), config.getCredentials());
    }

    public RegistryConfig getConfig() {
        return config;
    }

    public Registry getRegistry() {
        return registry;
    }

    public RegistryConfig buildRegistryConfig() {
        return new RegistryConfig(this.config.registryUrl, this.config.namespace, this.config.credentials);
    }

    private Registry buildRegistry() {
        return Registry.Builder.builder()
                .defaults(
                        config.credentials.getUsername(),
                        config.credentials.getPassword().getPlainText())
                .build();
    }

    public static final class RegistryConfig implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String registryUrl;
        private final String namespace;
        private final UsernamePasswordCredentials credentials;

        public RegistryConfig(String registryUrl, String namespace, UsernamePasswordCredentials credentials) {
            this.registryUrl = registryUrl;
            this.namespace = namespace;
            this.credentials = credentials;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getRegistryUrl() {
            return registryUrl;
        }

        public UsernamePasswordCredentials getCredentials() {
            return credentials;
        }
    }
}
