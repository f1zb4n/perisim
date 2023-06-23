package de.clustscape.perisim.service.sftp;

import de.clustscape.perisim.service.PerisimService;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.StaticPublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.Collections;

/**
 * SFTP service which starts an embedded SFTP (SSH) server.
 *
 * @author Marco Werner
 */
@Component
public class SFTPService implements PerisimService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SFTPService.class);

    private SshServer sftpServer;
    private boolean isRunning;

    @Value("${sftpservice.enabled}")
    private Boolean enabled = Boolean.FALSE;

    @Value("${sftpservice.port}")
    private Integer port = 22;

    @Value("${sftpservice.username}")
    private String username = "user";

    @Value("${sftpservice.password}")
    private String password = "pass";

    @Value("${sftpservice.homedir}")
    private String homedir = "/home/sftproot";

    @Value("${sftpservice.authentication.publickey.enabled}")
    private Boolean publicKeyAuthenticationEnabled = Boolean.FALSE;

    /**
     * {@inheritDoc}
     */
    @Override
    public void startup() {
        if (enabled) {
            LOGGER.info("Starting SFTPService...");
            sftpServer = SshServer.setUpDefaultServer();

            // configure the SFTP server
            sftpServer.setPort(port);
            sftpServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
            sftpServer.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));

            // configure authentication method(s)
            if (publicKeyAuthenticationEnabled) {
                sftpServer.setPublickeyAuthenticator(new StaticPublickeyAuthenticator(true) {
                    @Override
                    protected void handleAcceptance(String username, PublicKey key, ServerSession session) {
                        super.handleAcceptance(username, key, session);
                    }
                });
            }

            sftpServer.setPasswordAuthenticator((user, pass, session) -> username.equals(user) && password.equals(pass));

            VirtualFileSystemFactory virtualFileSystemFactory = new VirtualFileSystemFactory();
            Path userHomeDir = new File(homedir).toPath();
            virtualFileSystemFactory.setUserHomeDir(username, userHomeDir);
            sftpServer.setFileSystemFactory(virtualFileSystemFactory);

            String authMethod = publicKeyAuthenticationEnabled ? "PublicKey, Password" : "Password";
            LOGGER.info("Port: {}, User: {}, HomeDir: {}, AuthMethods: {}", port, username, userHomeDir.toAbsolutePath(), authMethod);

            // start the server
            try {
                sftpServer.start();
                isRunning = true;
                LOGGER.info("---> SFTPService is up and running.");
            } catch (IOException e) {
                LOGGER.error("SFTPService failed", e);
            }
        } else {
            LOGGER.info("SFTPService is disabled.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        if (isRunning) {
            LOGGER.info("SFTPService is shutting down...");
            if (sftpServer != null) {
                try {
                    sftpServer.stop();
                    isRunning = false;
                } catch (IOException e) {
                    LOGGER.warn("Stopping SFTP server failed. Reason: " + e.getMessage());
                }
            }
            LOGGER.info("SFTPService is down.");
        }
    }

    /**
     * Returns a flog indicating if the service is running or not.
     *
     * @return true if the service is running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }
}
