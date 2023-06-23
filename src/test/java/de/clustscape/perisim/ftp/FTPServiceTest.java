package de.clustscape.perisim.ftp;

import de.clustscape.perisim.service.ftp.FTPService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests the FTPService.
 *
 * @author Marco Werner
 */
@ExtendWith(MockitoExtension.class)
public class FTPServiceTest {

    @InjectMocks
    private FTPService ftpService;

    @Test
    public void testStartupDisabled() {
        // given

        // when
        ftpService.startup();

        // then
        assertThat(ftpService.isRunning(), is(false));
    }

    @Test
    public void testStartupEnabled() {
        // given
        ReflectionTestUtils.setField(ftpService, "enabled", true);
        ReflectionTestUtils.setField(ftpService, "port", new Random().nextInt(64512) + 1024);
        ReflectionTestUtils.setField(ftpService, "usersFile", "src/test/resources/ftpservice/ftpservice_users.properties");

        // when
        ftpService.startup();

        // then
        assertThat(ftpService.isRunning(), is(true));
        ftpService.shutdown();
    }

    @Test
    public void testStartupSsl() {
        // given
        ReflectionTestUtils.setField(ftpService, "enabled", true);
        ReflectionTestUtils.setField(ftpService, "port", new Random().nextInt(64512) + 1024);
        ReflectionTestUtils.setField(ftpService, "usersFile", "src/test/resources/ftpservice/ftpservice_users.properties");
        ReflectionTestUtils.setField(ftpService, "sslEnabled", true);
        ReflectionTestUtils.setField(ftpService, "keystoreFile", "src/test/resources/ftpservice/ftpservice.jks");
        ReflectionTestUtils.setField(ftpService, "keystorePassword", "secret");

        // when
        ftpService.startup();

        // then
        assertThat(ftpService.isRunning(), is(true));
        ftpService.shutdown();
    }

    @Test
    public void testShutdown() {
        // given
        ReflectionTestUtils.setField(ftpService, "enabled", true);
        ReflectionTestUtils.setField(ftpService, "port", new Random().nextInt(64512) + 1024);
        ReflectionTestUtils.setField(ftpService, "usersFile", "src/test/resources/ftpservice/ftpservice_users.properties");
        ftpService.startup();
        assertThat(ftpService.isRunning(), is(true));

        // when
        ftpService.shutdown();

        // then
        assertThat(ftpService.isRunning(), is(false));
    }

    @Test
    public void testShutdownDisabled() {
        // given
        ReflectionTestUtils.setField(ftpService, "enabled", false);
        ReflectionTestUtils.setField(ftpService, "usersFile", "src/test/resources/ftpservice/ftpservice_users.properties");
        ftpService.startup();
        assertThat(ftpService.isRunning(), is(false));

        // when
        ftpService.shutdown();

        // then
        assertThat(ftpService.isRunning(), is(false));
    }
}
