package io.jenkins.plugins.jobcacher.oras;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import jenkins.model.Jenkins;
import jenkins.plugins.itemstorage.ItemStorage;
import jenkins.plugins.itemstorage.ItemStorageDescriptor;
import land.oras.ContainerRef;
import land.oras.Registry;
import land.oras.exception.OrasException;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extension
public class RegistryItemStorage extends ItemStorage<RegistryItemPath> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Logger LOGGER = LoggerFactory.getLogger(RegistryItemStorage.class);

    private String storageCredentialId;
    private String registryUrl;
    private String namespace;

    @DataBoundConstructor
    public RegistryItemStorage() {}

    @DataBoundSetter
    public void setStorageCredentialId(String storageCredentialId) {
        this.storageCredentialId = storageCredentialId;
    }

    @DataBoundSetter
    public void setRegistryUrl(String registryUrl) {
        this.registryUrl = registryUrl;
    }

    @DataBoundSetter
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getStorageCredentialId() {
        return storageCredentialId;
    }

    public String getRegistryUrl() {
        return registryUrl;
    }

    public String getNamespace() {
        return namespace;
    }

    @Override
    public RegistryItemPath getObjectPath(Item item, String path) {
        return new RegistryItemPath(createClient(), String.format("%s/%s", namespace, item.getFullName()), path);
    }

    @Override
    public RegistryItemPath getObjectPathForBranch(Item item, String path, String branch) {
        String branchPath = new File(item.getFullName()).getParent() + "/" + branch;
        return new RegistryItemPath(createClient(), String.format("%s/%s", namespace, branchPath), path);
    }

    private RegistryClient createClient() {
        return new RegistryClient(registryUrl, namespace, Utils.getCredentials(storageCredentialId));
    }

    public static RegistryItemStorage get() {
        return ExtensionList.lookupSingleton(RegistryItemStorage.class);
    }

    @Extension
    public static final class DescriptorImpl extends ItemStorageDescriptor<RegistryItemPath> {

        @Override
        public String getDisplayName() {
            return "ORAS";
        }

        @Override
        public boolean configure(StaplerRequest2 req, JSONObject json) throws FormException {
            save();
            return super.configure(req, json);
        }

        @SuppressWarnings("lgtm[jenkins/csrf]")
        public ListBoxModel doFillStorageCredentialIdItems(@AncestorInPath Item item) {
            StandardListBoxModel result = new StandardListBoxModel();
            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return result.includeCurrentValue(get().getStorageCredentialId());
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ) && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return result.includeCurrentValue(get().getStorageCredentialId());
                }
            }
            return result.includeEmptyValue()
                    .includeMatchingAs(
                            ACL.SYSTEM2,
                            item,
                            StandardUsernameCredentials.class,
                            Collections.emptyList(),
                            CredentialsMatchers.instanceOf(StandardUsernameCredentials.class))
                    .includeCurrentValue(get().getStorageCredentialId());
        }

        @SuppressWarnings("lgtm[jenkins/csrf]")
        public FormValidation doCheckServerUrl(@QueryParameter String registryUrl) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            FormValidation ret = FormValidation.ok();
            if (StringUtils.isBlank(registryUrl)) {
                ret = FormValidation.error("Server url cannot be blank");
            } else {
                try {
                    ContainerRef.parse("%s/library/test:latest".formatted(registryUrl));
                } catch (OrasException e) {
                    ret = FormValidation.error("Registry url doesn't seem valid.");
                }
            }
            return ret;
        }

        @RequirePOST
        public FormValidation doValidateArtifactoryConfig(
                @QueryParameter("registryUrl") final String registryUrl,
                @QueryParameter("storageCredentialId") final String storageCredentialId,
                @QueryParameter("namespace") final String namespace) {

            Jenkins.get().checkPermission(Jenkins.ADMINISTER);

            if (StringUtils.isBlank(registryUrl)
                    || StringUtils.isBlank(storageCredentialId)
                    || StringUtils.isBlank(namespace)) {
                return FormValidation.error("Fields required");
            }

            try {
                Path tmpFile = Files.createTempFile("tmp-", "jenkins-artifactory-plugin-test");
                RegistryClient client =
                        new RegistryClient(registryUrl, namespace, Utils.getCredentials(storageCredentialId));

                Registry registry = client.getRegistry();
                ContainerRef ref = ContainerRef.parse("%s/%stest:latest".formatted(registryUrl, namespace));

                registry.pushBlob(ref, Files.readAllBytes(tmpFile));
                registry.deleteBlob(ref);

                LOGGER.debug("Registry configuration validated");

            } catch (Exception e) {
                LOGGER.error("Unable to connect to Registry. Please check the server url and credentials", e);
                return FormValidation.error(
                        "Unable to connect to Registry. Please check the server url and credentials : "
                                + e.getMessage());
            }

            return FormValidation.ok("Success");
        }
    }
}
