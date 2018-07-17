package de.clustscape.perisim;

import de.clustscape.perisim.service.ftp.FTPService;
import de.clustscape.perisim.service.sftp.SFTPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PreDestroy;

@SpringBootApplication
@PropertySource({"classpath:application.properties", "application.properties"})
public class PerisimApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerisimApplication.class);

    @Autowired
    private FTPService ftpService;

    @Autowired
    private SFTPService sftpService;

    public static void main(String[] args) {
        SpringApplication.run(PerisimApplication.class, args);
	}

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("Available services: FTPService, SFTPService");
        startupServices();
        LOGGER.info("Press Ctrl+C to shutdown the application.");
        Thread.currentThread().join();
    }

    private void startupServices() {
        ftpService.startup();
        sftpService.startup();
    }

    @PreDestroy
    private void preDestroy() {
        ftpService.shutdown();
        sftpService.shutdown();
    }
}
