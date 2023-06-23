package de.clustscape.perisim.service.ftp;

import de.clustscape.perisim.service.PerisimService;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.ClearTextPasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Paths;

/**
 * FTP service which starts an embedded FTP server.
 *
 * @author Marco Werner
 */
@Component
public class FTPService implements PerisimService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FTPService.class);

    private FtpServer server;
    private boolean isRunning = false;

    @Value("${ftpservice.enabled}")
    private Boolean enabled = Boolean.FALSE;

    @Value("${ftpservice.port}")
    private Integer port = 21;

    @Value("${ftpservice.users.file}")
    private String usersFile;

    @Value("${ftpservice.ssl.enabled}")
    private Boolean sslEnabled = Boolean.FALSE;

    @Value("${ftpservice.ssl.keystore.file}")
    private String keystoreFile;

    @Value("${ftpservice.ssl.keystore.password}")
    private String keystorePassword;

    /**
     * {@inheritDoc}
     */
    @Override
    public void startup() {
        if (enabled) {
            LOGGER.info("Starting FTPService...");
            FtpServerFactory serverFactory = new FtpServerFactory();
            ListenerFactory factory = new ListenerFactory();
            factory.setPort(port);

            // define SSL configuration
            if (sslEnabled) {
                SslConfigurationFactory ssl = new SslConfigurationFactory();
                ssl.setKeystoreFile(Paths.get(keystoreFile).toFile());
                ssl.setKeystorePassword(keystorePassword);
                factory.setSslConfiguration(ssl.createSslConfiguration());
                factory.setImplicitSsl(true);
            }

            // replace the default listener
            serverFactory.addListener("default", factory.createListener());
            PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
            userManagerFactory.setFile(Paths.get(usersFile).toFile());
            userManagerFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor());
            serverFactory.setUserManager(userManagerFactory.createUserManager());

            LOGGER.info("Port: {}, SSL enabled: {}", port, sslEnabled);

            // start the server
            server = serverFactory.createServer();
            try {
                server.start();
                isRunning = true;
                LOGGER.info("---> FTPService up and running.");
            } catch (FtpException e) {
                LOGGER.error("FTPService failed", e);
            }
        } else {
            LOGGER.info("FTPService is disabled.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        if (isRunning) {
            LOGGER.info("FTPService is shutting down...");
            if (server != null) {
                server.stop();
                isRunning = false;
            }
            LOGGER.info("FTPService is down.");
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
